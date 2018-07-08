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

package com.mastfrog.parameters;

import java.util.regex.Pattern;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ValidatorUtils;

/**
 *
 * @author Tim Boudreau
 */
public class TypeValidation {

    private TypeValidation() {

    }

    public static Validator<String> validator(Types type) {
        switch (type) {
            case DOUBLE:
                return NUMBER_VALIDATOR;
            case NON_NEGATIVE_LONG:
            case NON_NEGATIVE_INTEGER:
                return NON_NEGATIVE_INTEGER_VALIDATOR;
            case LONG:
                return LONG_VALIDATOR;
            case INTEGER:
                return INTEGER_VALIDATOR;
            case BOOLEAN:
                return BOOLEAN_VALIDATOR;
            case NON_EMPTY_STRING:
                return NON_EMPTY_VALIDATOR;
            case STRING:
                return NO_OP;
            default:
                throw new AssertionError(type);
        }
    }
    private static final Validator<String> NUMBER_VALIDATOR = new NumberValidator();

    static abstract class AbstractValidator implements Validator<String> {

        @Override
        public void validate(Problems problems, String key, String value) {
            String problem = validate(key, value);
            if (problem != null) {
                problems.append(problem);
            }
        }

        public abstract String validate(String key, String value);

        @Override
        public Class<String> modelType() {
            return String.class;
        }

    }

    private static final class NumberValidator extends AbstractValidator {

        private static final Pattern PATTERN = Pattern.compile("^[\\d-][\\d\\.]{0,36}$");

        @Override
        public String validate(String key, String value) {
            if (value == null) {
                return null;
            }
            if (PATTERN.matcher(value).matches()) {
                return null;
            }
            return key + " is not a number: '" + value + "'";
        }
    }

    private static final Validator<String> INTEGER_VALIDATOR = new IntegerValidator();

    private static final class IntegerValidator extends AbstractValidator {

        private static final Pattern PATTERN = Pattern.compile("^[\\d-]\\d{0,36}$");

        @Override
        public String validate(String key, String value) {
            if (value == null) {
                return null;
            }
            if (PATTERN.matcher(value).matches()) {
                return null;
            }
            try {
                long val = Long.parseLong(value);
                if (val > Integer.MAX_VALUE) {
                    return "Value is greater than Integer.MAX_VALUE: " + val;
                } else if (val < Integer.MIN_VALUE) {
                    return "Value is less than Integer.MIN_VALUE: " + val;
                }
            } catch (NumberFormatException e) {
                return "Not a valid number: " + value;
            }
            return key + " is not an integer: '" + value + "'";
        }
    }

    private static final Validator<String> LONG_VALIDATOR = new LongValidator();

    private static final class LongValidator extends AbstractValidator {

        private static final Pattern PATTERN = Pattern.compile("^[\\d-]\\d{0,36}$");

        @Override
        public String validate(String key, String value) {
            if (value == null) {
                return null;
            }
            if (PATTERN.matcher(value).matches()) {
                return null;
            }
            try {
                long val = Long.parseLong(value);
            } catch (NumberFormatException e) {
                return "Not a valid 64-bit integer: " + value;
            }
            return key + " is not an integer: '" + value + "'";
        }
    }

    private static final Validator<String> NON_NEGATIVE_INTEGER_VALIDATOR
            = ValidatorUtils.merge(INTEGER_VALIDATOR, new NonNegativeIntegerValidator());

    private static final class NonNegativeIntegerValidator extends AbstractValidator {

        private static final Pattern PATTERN = Pattern.compile("^\\d{1,36}$");

        @Override
        public String validate(String key, String value) {
            if (value == null) {
                return null;
            }
            if (PATTERN.matcher(value).matches()) {
                return null;
            }
            return key + " is not a non-negative integer: '" + value + "'";
        }
    }

    private static final Validator<String> BOOLEAN_VALIDATOR = new BooleanValidator();

    private static final class BooleanValidator extends AbstractValidator {

        @Override
        public String validate(String key, String value) {
            if (value == null) {
                return null;
            }
            if ("TRUE".equalsIgnoreCase(value) || "FALSE".equalsIgnoreCase(value)) {
                return null;
            }
            return key + " is not a boolean value: '" + value + "'";
        }

    }

    private static final Validator<String> NON_EMPTY_VALIDATOR = new NonEmptyValidator();

    private static final class NonEmptyValidator extends AbstractValidator {

        @Override
        public String validate(String key, String value) {
            if (value == null) {
                return null;
            }
            if (value.trim().isEmpty()) {
                return key + " may not be empty or all whitespace";
            }
            return null;
        }

    }

    private static final Validator<String> NO_OP = new NoOpParamValidator();

    private static final class NoOpParamValidator extends AbstractValidator {

        @Override
        public String validate(String key, String value) {
            return null;
        }
    }

}
