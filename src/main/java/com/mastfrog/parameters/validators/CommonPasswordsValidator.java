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

package com.mastfrog.parameters.validators;

import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;

/**
 *
 * @author Tim Boudreau
 */
public class CommonPasswordsValidator extends AbstractValidator<String> {

    CommonPasswordsValidator() {
        super(String.class);
    }

    @Override
    public void validate(Problems problems, String compName, String model) {
        // list taken from http://gizmodo.com/the-25-most-popular-passwords-of-2014-were-all-doomed-1680596951
        switch (model.toLowerCase()) {
            case "password":
            case "1234":
            case "12345":
            case "123456":
            case "1234567":
            case "12345678":
            case "querty":
            case "baseball":
            case "dragon":
            case "football":
            case "monkey":
            case "letmein":
            case "mustang":
            case "111111":
            case "access":
            case "shadow":
            case "michael":
            case "superman":
            case "696969":
            case "123123":
            case "batman":
            case "trustno1":
                problems.append("That password is too commonly used");
            default:
        //do nothing
        }
    }

}
