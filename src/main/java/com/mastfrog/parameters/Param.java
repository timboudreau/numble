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

import static com.mastfrog.parameters.Types.STRING;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

/**
 * Annotation that indicates one parameter that may be passed to a page;
 * specified inside a &#064;Params.
 *
 * @author Tim Boudreau
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Param {

    /**
     * The string name of the command; must be a valid Java identifier.
     *
     * @return The name
     */
    String value();

    /**
     * The type of the parameter
     *
     * @return The type
     */
    Types type() default STRING;

    /**
     * An array of validators which should be instantiated to test incoming
     * values.
     *
     * @return The validators
     */
    Class<? extends Validator<String>>[] validators() default {};

    /**
     * Another array of validators - built in ones from <a href="https://kenai.com/projects/simplevalidation>SimpleValidation</a>
     * @return An array of enum constants that implement Validator
     */
    StringValidators[] constraints() default {};

    /**
     * If true, it is considered an error condition for this parameter to be
     * missing
     *
     * @return
     */
    boolean required() default true;

    /**
     * An optional default value; note that this affects the return type of
     * generated classes - a required parameter with no default value will
     * return Optional&lt;SomeType&gt;. With a default value it will return
     * SomeType.
     *
     * @return The default value
     */
    String defaultValue() default "";

    /**
     * For documentation and test generation purposes, an example of a correctly
     * formatted value.
     *
     * @return
     */
    String example() default "";
}
