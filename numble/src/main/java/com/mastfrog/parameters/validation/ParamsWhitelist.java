/*
 * The MIT License
 *
 * Copyright 2014 Tim Boudreau.
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
package com.mastfrog.parameters.validation;

import com.google.inject.ImplementedBy;
import com.mastfrog.parameters.validation.ParamsWhitelist.EmptyWhitelist;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A white-list of parameter names that are always allowed, even on pages that
 * restrict the parameters to a known set, for use with cache-buster strategies
 * and that sort of thing.
 * <p/>
 * Only used if you have pages annotated with
 * &#064;RestrictToMentionedParameters.
 * <p/>
 * This object should be made available in the Guice injector passed to
 * ParamsChecker's constructor.
 *
 * @author Tim Boudreau
 */
@ImplementedBy(EmptyWhitelist.class)
public class ParamsWhitelist {

    private final Set<String> names = new HashSet<>();

    /**
     * Create a whitelist
     *
     * @param names The parameter names to allow
     */
    public ParamsWhitelist(String... names) {
        this.names.addAll(Arrays.asList(names));
    }

    Set<String> names() {
        return names;
    }

    static final class EmptyWhitelist extends ParamsWhitelist {

        // Default implementation if not injected

        EmptyWhitelist() {
            super(new String[0]);
        }
    }
}
