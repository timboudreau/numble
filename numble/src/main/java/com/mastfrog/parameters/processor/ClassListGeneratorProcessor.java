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

import com.mastfrog.annotation.AnnotationUtils;
import com.mastfrog.acteur.annotations.Origin;
import static com.mastfrog.parameters.processor.ClassListGeneratorProcessor.ORIGIN_ANNOTATION;
import com.mastfrog.annotation.registries.AnnotationIndexFactory;
import com.mastfrog.annotation.registries.IndexGeneratingProcessor;
import com.mastfrog.annotation.registries.Line;
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

/**
 *
 * @author Tim Boudreau
 */
@SupportedAnnotationTypes(ORIGIN_ANNOTATION)
@SupportedSourceVersion(SourceVersion.RELEASE_16)
@ServiceProvider(javax.annotation.processing.Processor.class)
public class ClassListGeneratorProcessor extends IndexGeneratingProcessor<Line> {

    private int ix = 0;
    static final String ORIGIN_ANNOTATION = "com.mastfrog.acteur.annotations.Origin";

    public ClassListGeneratorProcessor() {
        super(AnnotationIndexFactory.lines());
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ORIGIN_ANNOTATION);
    }

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment re, AnnotationUtils utils) {
        Set<Element> els = utils.findAnnotatedElements(re, ORIGIN_ANNOTATION);
        for (Element e : els) {
            AnnotationMirror mirror = utils.findAnnotationMirror(e, ORIGIN_ANNOTATION);
            if (mirror != null) {
                TypeElement te = (TypeElement) e;
                String name = te.getQualifiedName().toString();
                super.addIndexElement(Origin.META_INF_PATH, new Line(ix++, new Element[]{e}, name));
            }
        }
        return true;
    }
}
