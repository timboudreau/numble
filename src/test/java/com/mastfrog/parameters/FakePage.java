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

import com.google.inject.Inject;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

/**
 *
 * @author Tim Boudreau
 */
@Params(value = {
    @Param(value = "optionalSomething", type = Types.NON_EMPTY_STRING, required = false),
    @Param(value = "requiredInt", type = Types.INTEGER),
    @Param(value = "requiredBool", type = Types.BOOLEAN, defaultValue = "false"),
    @Param(value = "requiredNonNeg", type = Types.NON_NEGATIVE_INTEGER),
    @Param(value = "requiredNumber", type = Types.DOUBLE, defaultValue = "23"),
    @Param(value = "nothing", defaultValue = "Go away", required = false, constraints = {StringValidators.MAY_NOT_END_WITH_PERIOD, StringValidators.MAY_NOT_START_WITH_DIGIT}),
    @Param(value = "defaultInt", type = Types.INTEGER, defaultValue = "5"),
    @Param(value = "jthing", type = Types.STRING, required = false, validators = {LongerThanTwo.class, StartsWithJValidator.class})}
        ,allowUnlistedParameters = true
        ,generateToJSON = true
        ,generateValidationCode = true
)
class FakePage {

    final com.mastfrog.parameters.FakePageParams params;

    @Inject
    FakePage(com.mastfrog.parameters.FakePageParams params) {
        this.params = params;
    }

}
