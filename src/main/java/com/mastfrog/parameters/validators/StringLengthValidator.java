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

import org.netbeans.validation.api.Problems;

/**
 *
 * @author Tim Boudreau
 */
public class StringLengthValidator extends StringValidator {

    private final int min;
    private final int max;

    public StringLengthValidator(int min, int max) {
        if (max <= min && max != -1) {
            throw new IllegalArgumentException("Max less than or equal to min: " + min + "," + max);
        }
        this.min = min;
        this.max = max;
    }

    @Override
    protected void doValidate(Problems problems, String compName, String model) {
        if (min != -1 && model.length() < min) {
            problems.append(compName + " must be at least " + min + " characters long");
        }
        if (max != -1 && model.length() > 20) {
            problems.append(compName + " must be no more than " + max + " characters long");
        }
    }

}
