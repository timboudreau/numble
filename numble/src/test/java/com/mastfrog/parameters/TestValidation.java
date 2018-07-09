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

import com.mastfrog.parameters.TestValidation.LengthBetweenThreeAndTwenty;
import com.mastfrog.parameters.TestValidation.RequireGoodPassword;
import com.mastfrog.parameters.TestValidation.UserMustNotAlreadyExistValidator;
import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;
import static org.netbeans.validation.api.builtin.stringvalidation.StringValidators.NO_WHITESPACE;
import static org.netbeans.validation.api.builtin.stringvalidation.StringValidators.URL_MUST_BE_VALID;

/**
 *
 * @author Tim Boudreau
 */
@Params(generateValidationCode = true, allowUnlistedParameters = true, value={
    @Param(value = "username", constraints = {NO_WHITESPACE}, validators = {UserMustNotAlreadyExistValidator.class, LengthBetweenThreeAndTwenty.class}),
    @Param(value = "password", constraints = {NO_WHITESPACE}, validators = {RequireGoodPassword.class, LengthBetweenThreeAndTwenty.class}),
    @Param(value="name"),
    @Param(value="bio"),
    @Param(value="pictureUrl", constraints = {URL_MUST_BE_VALID})
})
public class TestValidation {

    
    static class UserMustNotAlreadyExistValidator extends AbstractValidator<String> {

        UserMustNotAlreadyExistValidator() {
            super(String.class);
        }
        
        @Override
        public void validate(Problems problems, String compName, String model) {
            // do nothing
        }
    }
    
    static class RequireGoodPassword extends UserMustNotAlreadyExistValidator {}
    static class LengthBetweenThreeAndTwenty extends UserMustNotAlreadyExistValidator{}
}
