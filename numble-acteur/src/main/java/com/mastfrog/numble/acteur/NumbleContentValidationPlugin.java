/*
 * The MIT License
 *
 * Copyright 2018 Tim Boudreau.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.mastfrog.numble.acteur;

import com.google.common.net.MediaType;
import com.mastfrog.acteur.ContentConverter;
import com.mastfrog.parameters.KeysValues;
import com.mastfrog.parameters.gen.Origin;
import com.mastfrog.parameters.validation.ParamChecker;
import com.mastfrog.util.codec.Codec;
import com.mastfrog.util.streams.Streams;
import com.mastfrog.util.collections.CollectionUtils;
import com.mastfrog.util.collections.StringObjectMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.netbeans.validation.api.Problems;

/**
 *
 * @author Tim Boudreau
 */
final class NumbleContentValidationPlugin extends ContentConverter.ContentValidationPlugin {

    private final ParamChecker checker;

    @Inject
    NumbleContentValidationPlugin(ContentConverter converter, ParamChecker checker) throws ClassNotFoundException, IOException {
        super(converter, loadTypes());
        this.checker = checker;
    }

    static Set<Class<?>> loadTypes() throws IOException, ClassNotFoundException {
        Set<Class<?>> types = new HashSet<>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> numbleLists = cl.getResources(Origin.META_INF_PATH);
        if (numbleLists != null) { //graal
            for (URL url : CollectionUtils.toIterable(numbleLists)) {
                try (final InputStream in = url.openStream()) {
                    String[] lines = Streams.readString(in, "UTF-8").split("\n");
                    for (String line : lines) {
                        // CRLF issues if build was done on Windows
                        // gives us class names ending in a \r
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        // If A and B depend on C, and D depends on both, D can
                        // wind up with duplicates - harmless but has overhead, so
                        // nip that in the bud here
                        try {
                            types.add(cl.loadClass(line));
                        } catch (Throwable t) {
                            // Graal
                            t.printStackTrace();
                            types.add(Class.forName(line));
                        }
                    }
                }
            }
        }
//        System.out.println("VALIDATION PLUGIN ON " + Strings.join(',', types, Class::getName));
        return types;
    }

    @Override
    protected <T> void validate(ByteBuf buf, MediaType mimeType, Class<T> type, Codec codec) throws Exception {
        Origin origin = type.getAnnotation(Origin.class);
        if (origin != null) {
            StringObjectMap map;
            int originalPosition = buf.readerIndex();
            try (final InputStream in = new ByteBufInputStream(buf)) {
                map = codec.readValue(in, StringObjectMap.class);
                validate(origin, map).throwIfFatalPresent();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                throw ioe;
            } finally {
                buf.readerIndex(originalPosition);
            }
        }
    }

    private Problems validate(Origin origin, Map<String, ?> map) {
        Problems problems = new Problems();
        checker.check(origin.value(), KeysValues.ofMap(map), problems);
        return problems;
    }

    @Override
    protected <T> void validate(Class<T> type, Map<String, ?> map) {
        Origin or = type.getAnnotation(Origin.class);
        if (or != null) {
            validate(or, map);
        }
    }

}
