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

import org.netbeans.validation.api.Validator;

/**
 * Types used for parameters - numbers, strings and booleans, with
 * specializations for non-empty and non-negative cases.
 *
 * @author Tim Boudreau
 */
public enum Types {
    /**
     * A number, represented as double or Optional&lt;Double&gt;
     */
    DOUBLE,
    /**
     * A integer, represented as int or Optional&lt;Integer&gt;
     */
    INTEGER,
    /**
     * A long, represented as long or Optional&lt;Long&gt;
     */
    LONG,
    /**
     * An integer, represented as int or Optional&lt;Integer&gt;
     */
    NON_NEGATIVE_INTEGER,
    /**
     * A long, represented as long or Optional&lt;Long&gt;
     */
    NON_NEGATIVE_LONG,
    /**
     * A boolean, represented as boolean or Optional&lt;Boolean&gt;
     */
    BOOLEAN,
    /**
     * A string, represented as String or Optional&lt;String&gt;
     */
    NON_EMPTY_STRING,
    /**
     * A string, represented as String or Optional&lt;String&gt;
     */
    STRING;

    public Validator<String> validator() {
        return TypeValidation.validator(this);
    }

    public String typeName(boolean required) {
        return typeName(required, false);
    }

    public String typeName(boolean required, boolean useOptional) {
        switch (this) {
            case DOUBLE:
                return required ? "double" : useOptional ? "Optional<Double>" : "Double";
            case NON_NEGATIVE_LONG:
            case LONG:
                return required ? "long" : useOptional ? "Optional<Long>" : "Long";
            case NON_NEGATIVE_INTEGER:
            case INTEGER:
                return required ? "int" : useOptional ? "Optional<Integer>" : "Integer";
            case BOOLEAN:
                return required ? "boolean" : useOptional ? "Optional<Boolean>" : "Boolean";
            case NON_EMPTY_STRING:
            case STRING:
                return required ? "String" : useOptional ? "Optional<String>" : "String";
            default:
                throw new AssertionError(this);
        }
    }

    public String conversionMethod() {
        switch (this) {
            case INTEGER:
            case NON_NEGATIVE_INTEGER:
                return "Integer.parseInt";
            case BOOLEAN:
                return "Boolean.parseBoolean";
            case LONG:
            case NON_NEGATIVE_LONG:
                return "Long.parseLong";
            case DOUBLE:
                return "Double.parseDouble";
            default:
                return "to" + typeName(false);
        }
    }

    public boolean isNumber() {
        return this == DOUBLE || this == LONG || this == INTEGER || this == NON_NEGATIVE_INTEGER
                || this == NON_NEGATIVE_LONG;
    }

    public boolean isString() {
        return this == STRING || this == NON_EMPTY_STRING;
    }

}
