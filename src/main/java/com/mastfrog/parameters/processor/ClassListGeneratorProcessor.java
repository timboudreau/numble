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
package com.mastfrog.parameters.processor;

import com.mastfrog.parameters.gen.Origin;
import com.mastfrog.util.service.AnnotationIndexFactory;
import com.mastfrog.util.service.IndexGeneratingProcessor;
import com.mastfrog.util.service.Line;
import com.mastfrog.util.service.ServiceProvider;
import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 *
 * @author Tim Boudreau
 */
@SupportedAnnotationTypes("com.mastfrog.parameters.gen.Origin")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@ServiceProvider(javax.annotation.processing.Processor.class)
public class ClassListGeneratorProcessor extends IndexGeneratingProcessor<Line> {

    private int ix = 0;

    public ClassListGeneratorProcessor() {
        super(AnnotationIndexFactory.lines());
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("com.mastfrog.parameters.gen.Origin");
    }

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment re) {
        for (Element e : re.getElementsAnnotatedWith(Origin.class)) {
            AnnotationMirror mirror = findMirror(e);
            if (mirror != null) {
                TypeElement te = (TypeElement) e;
                String name = te.getQualifiedName().toString();
                super.addIndexElement(Origin.META_INF_PATH, new Line(ix++, new Element[]{e}, name));
            }
        }
        return true;
    }

    private AnnotationMirror findMirror(Element el) {
        for (AnnotationMirror mir : el.getAnnotationMirrors()) {
            TypeMirror type = mir.getAnnotationType().asElement().asType();
            if (Origin.class.getName().equals(type.toString())) {
                return mir;
            }
        }
        return null;
    }
}
