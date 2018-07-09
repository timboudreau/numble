/*
 * The MIT License
 *
 * Copyright 2015 Tim Boudreau.
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

import com.mastfrog.parameters.WithOptionalParams.NonNegative;
import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;

/**
 *
 * @author Tim Boudreau
 */
@Params({
    @Param(value = "weight", type = Types.NON_NEGATIVE_INTEGER, required = false), //    @Param(value = "pushups", type = Types.NON_NEGATIVE_INTEGER, validators = {NonNegative.class}, required = false),
//    @Param(value = "situps", type = Types.NON_NEGATIVE_INTEGER, validators = {NonNegative.class}, required = false),
//    @Param(value = "milesrun", type = Types.DOUBLE, validators = {NonNegative.class}, required = false),
})
public class WithOptionalParams {

    static class NonNegative extends AbstractValidator<String> {

        NonNegative() {
            super(String.class);
        }

        @Override
        public void validate(Problems problems, String compName, String model) {
        }
    }
}
