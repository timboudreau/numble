/*
 * The MIT License
 *
 * Copyright 2015 tim.
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

import com.mastfrog.parameters.TestValidationWithOptional.TemplateValidator;
import com.mastfrog.parameters.validators.StringValidator;
import org.junit.Test;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

/**
 *
 * @author Tim Boudreau
 */
@Params(useRequestBody = true, value = {
    @Param(value = "title", required = false, example = "The Foo Newsletter"),
    @Param(value = "header", required = false, example = "The news from Foo, in your inbox every so often"),
    @Param(value = "template", required = false, validators = TemplateValidator.class),
    @Param(value = "plainTextTemplate", required = false, validators = TemplateValidator.class),
    @Param(value = "owner", required = false, constraints = StringValidators.EMAIL_ADDRESS),
    @Param(value = "password", required = false, constraints = StringValidators.REQUIRE_NON_EMPTY_STRING),
    @Param(value = "enabled", required = false, type = Types.BOOLEAN),
    @Param(value = "testMode", required = false, type = Types.BOOLEAN)
}, generateToJSON = true)
public class TestValidationWithOptional {
    
    @Test
    public void test() {
        
    }

    static class TemplateValidator extends StringValidator {

        @Override
        protected void doValidate(Problems problems, String compName, String model) {
            //do nothing
        }

    }
}
