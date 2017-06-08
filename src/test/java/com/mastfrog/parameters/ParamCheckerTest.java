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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastfrog.parameters.ParamCheckerTest.M;
import com.google.inject.AbstractModule;
import com.mastfrog.giulius.Dependencies;
import com.mastfrog.parameters.validation.ParamChecker;
import com.mastfrog.parameters.validation.ParamsWhitelist;
import com.mastfrog.giulius.tests.GuiceRunner;
import com.mastfrog.giulius.tests.TestWith;
import com.mastfrog.util.Checks;
import com.mastfrog.util.collections.MapBuilder;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.netbeans.validation.api.Problems;

/**
 *
 * @author Tim Boudreau
 */
@RunWith(GuiceRunner.class)
@TestWith(M.class)
public class ParamCheckerTest {

    @Test
    public void test(ParamChecker checker, Adap pp, com.mastfrog.parameters.FakePageParams params, Dependencies deps) throws JsonProcessingException, IOException {
        assertNotNull(params);
        assertTrue(params.getRequiredBool());
        assertEquals(-23, params.getRequiredInt());
        assertTrue(params.getJthing().isPresent());
        assertEquals("java", params.getJthing().get());
        assertEquals(7.52306D, params.getRequiredNumber(), 0.0001D);
        assertEquals(42, params.getRequiredNonNeg());

        Problems problems = new Problems();
        params.validate(deps.getInjector(), problems);
        problems.throwIfFatalPresent();
        
        problems = new Problems();
        checker.check(FakePage.class, pp, problems);
        assertFalse(problems + "", problems.hasFatal());

        pp.remove("optionalSomething");
        checker.check(FakePage.class, pp, problems);
        assertTrue(problems + "", !problems.hasFatal());

        pp.set("requiredInt", "3.725");
        checker.check(FakePage.class, pp, problems);
        assertFalse(problems + "", !problems.hasFatal());
        System.out.println("PROBLS " + problems);
        problems = new Problems();

        pp.set("requiredInt", "37");
        pp.set("requiredBool", "hello");
        checker.check(FakePage.class, pp, problems);
        System.out.println("PROBLS " + problems);
        assertFalse(problems + "", !problems.hasFatal());
        pp.set("requiredBool", "false");
        problems = new Problems();

        pp.set("requiredNonNeg", "-2307");
        checker.check(FakePage.class, pp, problems);
        System.out.println("PROBLS " + problems);
        assertFalse(problems + "", !problems.hasFatal());
        problems = new Problems();

        pp.set("requiredNonNeg", "abcd");
        checker.check(FakePage.class, pp, problems);
        System.out.println("PROBLS " + problems);
        assertFalse(problems + "", !problems.hasFatal());
        problems = new Problems();

        pp.set("requiredNonNeg", "6666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666");
        checker.check(FakePage.class, pp, problems);
        System.out.println("PROBLS " + problems);
        assertFalse(problems + "", !problems.hasFatal());
        problems = new Problems();

        pp.set("requiredNonNeg", "32");
        checker.check(FakePage.class, pp, problems);
        System.out.println("PROBLS " + problems);
        assertTrue(problems + "", !problems.hasFatal());

        pp.set("fuzzbar", "a32");
        checker.check(FakePage.class, pp, problems);
        System.out.println("PROBLS " + problems);
        assertFalse(problems + "", !problems.hasFatal());
        pp.remove("fuzzbar");

        pp.set("jthing", "gork");
        checker.check(FakePage.class, pp, problems);
        assertFalse(problems + "", !problems.hasFatal());
        problems = new Problems();

        pp.set("jthing", "jork");
        checker.check(FakePage.class, pp, problems);
        assertTrue(problems + "", !problems.hasFatal());

//        assertNotNull(p);
//        assertTrue(p.getJthing().isPresent());
//        assertEquals("java", p.getJthing().get());
//        assertEquals(-23, p.getRequiredInt());
        
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(params.toMap());
        System.out.println("JSON: " + json);
        com.mastfrog.parameters.FakePageParams reconstituted = mapper.readValue(json, com.mastfrog.parameters.FakePageParams.class);
        assertEquals(params, reconstituted);
        
    }

    static class M extends AbstractModule {

        @Override
        @SuppressWarnings("unchecked")
        protected void configure() {
            bind(ParamsWhitelist.class).toInstance(new ParamsWhitelist("cachebuster"));
            Map pp = new MapBuilder()
                    .put("optionalSomething", "something")
                    .put("requiredInt", "-23")
                    .put("requiredBool", "true")
                    .put("requiredNonNeg", "42")
                    .put("jthing", "java")
                    .put("requiredNumber", "7.52306")
                    .build();

            Adap adap = new Adap((Map<String,String>)pp);
            bind(KeysValues.class).toInstance(adap);
            bind(Adap.class).toInstance(adap);
        }
    }
    
    public static final class Adap implements KeysValues {

        final Map<String, String> map;

        public Adap(Map<String, String> map) {
            Checks.notNull("map", map);
            this.map = map;
        }

        @Override
        public String get(String key) {
            Checks.notNull("key", key);
            return map.get(key);
        }
        
        public void set(String key, String val) {
            map.put(key, val);
        }

        @Override
        public Set<String> keySet() {
            return map.keySet();
        }

        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            return map.entrySet().iterator();
        }

        public String toString() {
            return map.toString();
        }
        
        public void remove(String key) {
            map.remove(key);
        }
    }
}
