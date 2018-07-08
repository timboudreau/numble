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

import com.google.inject.Injector;
import com.mastfrog.parameters.KeysValues;
import com.mastfrog.parameters.Param;
import com.mastfrog.parameters.Params;
import com.mastfrog.parameters.TypeValidation;
import com.mastfrog.parameters.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

/**
 * Validates the parameters that will passed to a page, to ensure they conform
 * to constraints specified in its PageParameters.
 *
 * @author Tim Boudreau
 */
public class ParamChecker {

    private final Injector injector;
    private final ParamsWhitelist whitelist;

    /**
     * Create a new ParamChecker (you should ask for it to be injected).
     *
     * @param injector The Injector which will be used
     * to instantiate validators
     */
    @Inject
    public ParamChecker(Injector injector) {
        this.injector = injector;
        whitelist = injector.getInstance(ParamsWhitelist.class);
    }

    /**
     * Check a Page instance
     *
     * @param declaringObject The object
     * @param params The parameters
     * @param problems A list of problems to populate if something is wrong
     */
    public void check(Object declaringObject, KeysValues params, Problems problems) {
        check(declaringObject.getClass(), params, problems);
    }

    /**
     * Check the parameters that would be passed to a page of the passed type
     *
     * @param declaringType Object type
     * @param params The parameters
     * @param problems A list of problems to populate if something is wrong
     */
    public void check(Class<?> declaringType, KeysValues params, Problems problems) {
        Params parameters = declaringType.getAnnotation(Params.class);
        if (parameters == null) {
            return;
        }
        Map<String, ValidatorSet> setForKey = new HashMap<>();
        Set<String> constrainedKeys = new HashSet<>();
        for (Param p : parameters.value()) {
            boolean isRequired = p.required() && "".equals(p.defaultValue());
            if (!isRequired) {
                continue;
            }
            constrainedKeys.add(p.value());
            ValidatorSet ps = setForKey.get(p.value());
            if (ps == null) {
                ps = new ValidatorSet().add(PRESENT_VALIDATOR);
                setForKey.put(p.value(), ps);
            }
            if (p.type() != Types.STRING) {
                ps.add(TypeValidation.validator(p.type()));
            }
            for (StringValidators v : p.constraints()) {
                ps.add(v);
            }
            for (Class<? extends Validator<String>> validatorType : p.validators()) {
                Validator<String> v = injector.getInstance(validatorType);
                ps.add(v);
            }
        }
        for (Param p : parameters.value()) {
            boolean isRequired = p.required() && "".equals(p.defaultValue());
            if (isRequired) {
                continue;
            }
            constrainedKeys.add(p.value());
            if (params.keySet().contains(p.value())) {
                ValidatorSet ps = setForKey.get(p.value());
                if (ps == null) {
                    ps = new ValidatorSet();
                    setForKey.put(p.value(), ps);
                }
                if (p.type() != Types.STRING) {
                    ps.add(TypeValidation.validator(p.type()));
                }
                for (StringValidators v : p.constraints()) {
                    ps.add(v);
                }
                for (Class<? extends Validator<String>> validatorType : p.validators()) {
                    Validator<String> v = injector.getInstance(validatorType);
                    ps.add(v);
                }
            }
        }
        for (Map.Entry<String, ValidatorSet> e : setForKey.entrySet()) {
            String key = e.getKey();
            String val = params.get(key);
            ValidatorSet s = e.getValue();
            s.check(key, val, problems, params);
        }
        if (!parameters.allowUnlistedParameters()) {
            Set<String> presentParams = new HashSet<>(params.keySet());
            presentParams.removeAll(constrainedKeys);
            presentParams.removeAll(whitelist.names());
            if (!presentParams.isEmpty()) {
                problems.append("Parameters contains unknown keys: " + presentParams);
            }
        }
    }

    private static final Validator<String> PRESENT_VALIDATOR = new PresentValidator();

    static class PresentValidator implements Validator<String> {

        @Override
        public void validate(Problems problems, String key, String model) {
            if (model == null) {
                problems.append("Missing " + key);
            }
        }

        @Override
        public Class<String> modelType() {
            return String.class;
        }
    }

    private static class ValidatorSet {

        List<Validator<String>> validators = new LinkedList<>();

        public ValidatorSet add(Validator<String> validator) {
            validators.add(validator);
            return this;
        }

        public void check(String key, String value, Problems problems, KeysValues in) {
            for (Validator<String> v : validators) {
                v.validate(problems, key, value);
            }
        }
    }
}
