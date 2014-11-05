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

import com.mastfrog.parameters.validation.ParamChecker;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Parameters which may be present on a page, and specify the methods of the
 * generated parameters class. When a Page subclass XPage is annotated with this
 * annotation, a class XPageParams is generated with getters for each parameter.
 * Parameters will be validated according to the rules specified for each Param.
 *
 * @see ParamChecker
 * @see Param
 *
 * @author Tim Boudreau
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Params {

    /**
     * Parameters which should be available in the request for the page
     *
     * @return An array of parameters
     */
    Param[] value() default {};

    /**
     * Hint that this class will be deserialized from JSON, not from a map
     */
    boolean useRequestBody() default false;

    /**
     * If true, create a constructor that uses Jackson for deserialization
     * from JSON, annotating parameters with &#064;JsonParameter, etc.
     * @return 
     */
    boolean jsonConstructor() default true;

    /**
     * If true, the generated class will have a &#064JsonAnySetter annotated
     * method and a <code>get(String)</code> method for fetching parameters that
     * were not anticipated but are present.
     */
    boolean allowUnlistedParameters() default false;
    
    /**
     * If true, the generated class will implement 
     * <a href="gen/Validatable.html">Validatable</a> and have a 
     * <code>validate()</code> method
     * @return 
     */
    boolean generateValidationCode() default true;
    
    /**
     * If true, the generated class will have a <code>toJSON()</code> method
     * @return 
     */
    boolean generateToJSON() default false;
}
