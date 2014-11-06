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
package com.mastfrog.parameters;

import com.google.common.base.Preconditions;
import com.mastfrog.util.collections.CollectionUtils;
import com.mastfrog.util.collections.Converter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Abstraction for parameters so this can be used with multiple frameworks.
 *
 * @author Tim Boudreau
 */
public interface KeysValues extends Iterable<Map.Entry<String, String>> {

    String get(String key);

    Set<String> keySet();

    public static final class MapAdapter implements KeysValues {

        private final Map<String, ? extends Object> map;

        public MapAdapter(Map<String, ? extends Object> map) {
            Preconditions.checkNotNull(map);
            this.map = map;
        }

        @Override
        public String get(String key) {
            Preconditions.checkNotNull(key);
            Object result = map.get(key);
            if (result instanceof String) {
                return (String) result;
            } else if (result != null) {
                return result + "";
            } else {
                return null;
            }
        }

        @Override
        public Set<String> keySet() {
            return map.keySet();
        }

        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            Converter<Map.Entry<String,String>, Map.Entry<String, ? extends Object>> c = new Conv();
            Iterator<Map.Entry<String, ? extends Object>> it = (Iterator<Map.Entry<String, ? extends Object>>) map.entrySet().iterator();
            return CollectionUtils.convertedIterator(c, it);
        }

        @Override
        public String toString() {
            return map.toString();
        }
        
        private static class Conv implements Converter<Map.Entry<String,String>, Map.Entry<String, ? extends Object>> {

            @Override
            public Map.Entry<String, ? extends Object> unconvert(Map.Entry<String, String> r) {
                return (Map.Entry) r;
            }

            @Override
            public Map.Entry<String, String> convert(Map.Entry<String, ? extends Object> r) {
                return new StringEntry(r);
            }

            private static class StringEntry implements Map.Entry<String,String> {
                private final Map.Entry<String, ? extends Object> delegate;

                public StringEntry(Map.Entry<String, ? extends Object> delegate) {
                    this.delegate = delegate;
                }

                @Override
                public String getKey() {
                    return delegate.getKey();
                }

                @Override
                public String getValue() {
                    Object result = delegate.getValue();
                    return result instanceof String ? (String) result : result + "";
                }

                @Override
                public String setValue(String v) {
                    throw new UnsupportedOperationException("Not supported.");
                }
                
            }
            
        }
    }
}
