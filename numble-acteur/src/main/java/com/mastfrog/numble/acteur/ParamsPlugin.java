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

package com.mastfrog.numble.acteur;

import com.mastfrog.acteur.Application;
import com.mastfrog.acteur.HelpGenerator;
import com.mastfrog.acteur.preconditions.Description;
import com.mastfrog.parameters.Param;
import com.mastfrog.parameters.Params;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

/**
 *
 * @author Tim Boudreau
 */
final class ParamsPlugin extends HelpGenerator.AnnotationDescriptionPlugin<Params> {

    @Inject
    ParamsPlugin(HelpGenerator gen) {
        super(Params.class, gen);
    }

    @Override
    protected void write(Application application, Map<String, Object> into, Params p) {
        for (Param par : p.value()) {
            String name = par.value();
            Map<String, Object> desc = new LinkedHashMap<>();
            desc.put("type", par.type().toString());
            if (!par.defaultValue().isEmpty()) {
                desc.put("Default value", par.defaultValue());
            }
            if (!par.example().isEmpty()) {
                desc.put("Example", par.example());
            }
            desc.put("required", par.required());
            List<String> constraints = new LinkedList<>();
            for (StringValidators validator : par.constraints()) {
                constraints.add(deConstantNameify(validator.name()));
            }
            for (Class<? extends Validator<String>> c : par.validators()) {
                Description des = c.getAnnotation(Description.class);
                if (des == null) {
                    constraints.add(c.getSimpleName());
                } else {
                    constraints.add(des.value());
                }
            }
            if (!constraints.isEmpty()) {
                desc.put("constraints", constraints);
            }
            into.put(name, desc);
        }
    }

}
