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

import com.mastfrog.parameters.Param;
import com.mastfrog.parameters.Params;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.openide.util.lookup.ServiceProvider;

/**
 * Annotation processor which generates typesafe classes for parameters
 *
 * @author Tim Boudreau
 */
@SupportedAnnotationTypes("com.mastfrog.parameters.Params")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@ServiceProvider(service = javax.annotation.processing.Processor.class)
public final class Processor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("com.mastfrog.parameters.Params");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment re) {
        Set<? extends Element> all = re.getElementsAnnotatedWith(Params.class);
        List<GeneratedParamsClass> interfaces = new LinkedList<>();
        outer:
        for (Element e : all) {
            TypeElement te = (TypeElement) e;
            PackageElement pkg = findPackage(e);
            if (pkg == null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Params may not be used in the default package", e);
                continue;
            }
            if (!isPageSubtype(te)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Params must be used on a subclass of org.apache.wicket.Page", e);
                continue;
            }
            String className = te.getQualifiedName().toString();
            Params params = te.getAnnotation(Params.class);

            Map<String, List<String>> validators = validatorsForParam(e);
            GeneratedParamsClass inf = new GeneratedParamsClass(className, te, pkg, params, validators);

            if (!params.useRequestBody()) {
                checkConstructor(te, inf);
            }
            interfaces.add(inf);
            Set<String> names = new HashSet<>();
            for (Param param : params.value()) {
//                if (param.required() && !param.defaultValue().isEmpty()) {
//                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Don't set required to "
//                            + "true if you are providing a default value - required makes it an error not "
//                            + "to have a value, and if there is a default value, that error is an impossibility "
//                            + "because it will always have a value.", e);
//                    continue outer;
//                }
                if (param.value().trim().isEmpty()) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Empty parameter name", e);
                    continue outer;
                }
                if (!isJavaIdentifier(param.value())) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Not a valid Java identifier: " + param.value(), e);
                    continue outer;
                }
                if (!names.add(param.value())) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Duplicate parameter name '" + param.value() + "'", e);
                    continue outer;
                }
                for (char c : ";,./*!@&^/\\<>?'\"[]{}-=+)(".toCharArray()) {
                    if (param.value().contains("" + c)) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Param name may not contain the character '" + c + "'", e);
                    }
                }
                inf.add(param);
            }
        }
        Filer filer = processingEnv.getFiler();
        StringBuilder listBuilder = new StringBuilder();
        for (GeneratedParamsClass inf : interfaces) {
            try {
                String pth = inf.packageAsPath() + '/' + inf.className;
                pth = pth.replace('/', '.');
                JavaFileObject obj = filer.createSourceFile(pth, inf.el);
                try (OutputStream out = obj.openOutputStream()) {
                    out.write(inf.toString().getBytes("UTF-8"));
                }
                listBuilder.append(inf.className).append('\n');
            } catch (Exception ex) {
                ex.printStackTrace();
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error processing annotation: " + ex.getMessage(), inf.el);
                Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (re.processingOver()) {
            try {
                FileObject list = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/paramanos/bind.list", all.toArray(new Element[0]));
                try (OutputStream out = list.openOutputStream()) {
                    out.write(listBuilder.toString().getBytes("UTF-8"));
                }
            } catch (FilerException ex) {
                Logger.getLogger(Processor.class.getName()).log(Level.INFO, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return true;
    }

    private AnnotationMirror findMirror(Element el) {
        for (AnnotationMirror mir : el.getAnnotationMirrors()) {
            TypeMirror type = mir.getAnnotationType().asElement().asType();
            if (Params.class.getName().equals(type.toString())) {
                return mir;
            }
        }
        return null;
    }

    private Map<String, List<String>> validatorsForParam(Element el) {
        AnnotationMirror mirror = findMirror(el);
        Map<String, List<String>> result = new HashMap<>();
        List<AnnotationMirror> params = findParamAnnotations(mirror, new LinkedList<AnnotationMirror>());
        for (AnnotationMirror m : params) {
            List<String> names = findValidatorClassNames(m);
            if (!names.isEmpty()) {
                String name = findParamName(m);
                if (name != null) {
                    result.put(name, names);
                }
            }
        }
        return result;
    }

    private String findParamName(AnnotationMirror mir) {
        String result = null;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : mir.getElementValues().entrySet()) {
            if (e.getKey().getSimpleName().contentEquals("value")) {
                if (e.getValue().getValue() instanceof String) {
                    result = (String) e.getValue().getValue();
                    break;
                }
            }
        }
        return result;
    }

    private List<AnnotationMirror> findParamAnnotations(AnnotationMirror mir, List<AnnotationMirror> result) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : mir.getElementValues().entrySet()) {
            if ("value()".equals(e.getKey().toString())) {
                if (e.getValue().getValue() instanceof List) {
                    List<?> l = (List<?>) e.getValue().getValue();
                    for (Object o : l) {
                        if (o instanceof AnnotationMirror) {
                            AnnotationMirror param = (AnnotationMirror) o;
                            result.add(param);
                            findValidatorClassNames(param);
                        }
                    }
                }
            }
        }
        return result;
    }

    private List<String> findValidatorClassNames(AnnotationMirror mir) {
        List<String> result = new LinkedList<String>();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e : mir.getElementValues().entrySet()) {
            if ("validators()".equals(e.getKey().toString())) {
                if (e.getValue().getValue() instanceof List) {
                    List<?> l = (List<?>) e.getValue().getValue();
                    for (Object o : l) {
                        String s = o.toString();
                        if (s.endsWith(".class")) {
                            s = s.substring(0, s.length() - ".class".length());
                        }
                        result.add(s);
                    }
                } else {
                    result.add(e.getValue().getValue().toString());
                }
            }
        }
        return result;
    }

    static boolean isJavaIdentifier(String id) {
        if (id == null) {
            return false;
        }
        return SourceVersion.isIdentifier(id) && !SourceVersion.isKeyword(id);
    }

    private final PackageElement findPackage(Element e) {
        for (Element curr = e; e != null;) {
            if (curr instanceof PackageElement) {
                return (PackageElement) curr;
            } else {
                curr = curr.getEnclosingElement();
            }
        }
        return null;
    }

    private boolean isPageSubtype(TypeElement e) {
        if (true) {
            return true;
        }
        Types types = processingEnv.getTypeUtils();
        Elements elements = processingEnv.getElementUtils();
        TypeElement pageType = elements.getTypeElement("org.apache.wicket.Page");
        if (pageType == null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "org.apache.wicket.Page not on classpath, cannot process annotation");
            return false;
        }
        return types.isSubtype(e.asType(), pageType.asType());
    }

    private void checkConstructor(TypeElement el, GeneratedParamsClass inf) {
        Elements elements = processingEnv.getElementUtils();
        TypeElement pageParamsType = elements.getTypeElement("org.apache.wicket.request.mapper.parameter.PageParameters");
        TypeElement customParamsType = elements.getTypeElement(inf.qualifiedName());
        boolean found = false;
        boolean foundArgument = false;
        ExecutableElement con = null;
        outer:
        for (Element sub : el.getEnclosedElements()) {
            switch (sub.getKind()) {
                case CONSTRUCTOR:
                    for (AnnotationMirror mir : sub.getAnnotationMirrors()) {
                        DeclaredType type = mir.getAnnotationType();
                        switch (type.toString()) {
                            case "javax.inject.Inject":
                            case "com.google.inject.Inject":
                                ExecutableElement constructor = (ExecutableElement) sub;
                                con = constructor;
                                for (VariableElement va : constructor.getParameters()) {
                                    TypeMirror varType = va.asType();
                                    if (pageParamsType != null && varType.toString().equals(pageParamsType.toString())) {
                                        foundArgument = true;
                                        break;
                                    }
                                    if (customParamsType != null && varType.toString().equals(customParamsType.toString())) {
                                        foundArgument = true;
                                        break;
                                    } else if (customParamsType == null && inf.qualifiedName().equals(varType.toString())) { //first compilation - type not generated yet
                                        foundArgument = true;
                                        break;
                                    }
                                }
                                found = true;
                                break outer;
                            default:
                            //do nothing
                        }
                    }
            }
        }
        if (!found) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Usually a constructor "
                    + "annotated with @Inject that takes a " + inf.className + " is desired", el);
        } else if (found && !foundArgument) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Usually a constructor taking "
                    + "an argument of " + inf.className + " is desired", con);
        }
    }

    private final class GeneratedParamsClass {

        private final List<GeneratedParameter> methods = new LinkedList<>();
        private final String packageName;
        private final String className;
        private final String srcClassName;
        private final TypeElement el;
        private final boolean jsonConstructor;
        private final boolean anySetter;
        private final boolean validate;
        private final boolean generateToMap = true;
        private final Params params;
        private final Map<String, List<String>> validators;

        GeneratedParamsClass(String className, TypeElement el, PackageElement pkg, Params params, Map<String, List<String>> validators) {
            this.params = params;
            this.el = el;
            srcClassName = className;
            this.packageName = pkg.getQualifiedName().toString();
            this.className = className.substring(className.lastIndexOf('.') + 1) + "Params";
            this.jsonConstructor = params.jsonConstructor();
            this.anySetter = params.allowUnlistedParameters();
            this.validate = params.generateValidationCode();
            this.validators = validators;
        }

        public String qualifiedName() {
            return packageName + "." + className;
        }

        public String packageAsPath() {
            return packageName.replace('.', '/');
        }

        public void add(Param param) {
            methods.add(new GeneratedParameter(param));
        }

        private boolean needOptional() {
            for (GeneratedParameter m : methods) {
                if (!m.isRequired()) {
                    return true;
                }
            }
            return false;
        }

        private Set<String> stringValidators() {
            Set<String> result = new HashSet<>();
            for (Param param : params.value()) {
                for (StringValidators v : param.constraints()) {
                    result.add(v.name());
                }
            }
            return result;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(200);
            try {
                sb.append("package ").append(packageName).append(";\n\n");
                List<String> imports = new LinkedList<>();
                List<String> interfaces = new LinkedList<>();
                interfaces.add("Serializable");
                if (anySetter && jsonConstructor) {
                    imports.add("com.fasterxml.jackson.annotation.JsonAnySetter");
                }
                if (jsonConstructor) {
                    imports.add("com.fasterxml.jackson.annotation.JsonCreator");
                    imports.add("com.fasterxml.jackson.annotation.JsonProperty");
                }
                if (params.generateToJSON()) {
                    imports.add("com.fasterxml.jackson.databind.ObjectMapper");
                    imports.add("com.fasterxml.jackson.core.JsonProcessingException");
                }
                if (validate) {
                    imports.add("org.netbeans.validation.api.Validator");
                    imports.add("org.netbeans.validation.api.Problems");
                    imports.add("com.google.inject.Injector");
                    imports.add("com.mastfrog.parameters.gen.Validatable");
                    interfaces.add("Validatable");
                    for (String validator : stringValidators()) {
                        imports.add("static org.netbeans.validation.api.builtin.stringvalidation.StringValidators." + validator);
                    }
                }
                imports.add("com.mastfrog.parameters.KeysValues");
                imports.add("com.mastfrog.parameters.gen.Origin");
                imports.add("java.io.Serializable");
                if (anySetter || generateToMap) {
                    imports.add("java.util.Map");
                    imports.add("java.util.HashMap");
                }
                imports.add("java.util.Objects");
                if (needOptional()) {
                    imports.add("java.util.Optional");
                }
                imports.add("javax.inject.Inject");
                Collections.sort(imports);
                for (String s : imports) {
                    sb.append("import " + s + ";\n");
                }
                sb.append("/** \n    Generated from &#064;Param annotations on ").append(srcClassName).append("\n*/\n");
                Collections.sort(methods);
                sb.append("@Origin(").append(srcClassName).append(".class)\n");
                if (jsonConstructor) {
                    sb.append("public ");
                }
                sb.append("final class ").append(className).append(" ");
                if (!interfaces.isEmpty()) {
                    sb.append("implements ");
                    for (Iterator<String> it = interfaces.iterator(); it.hasNext();) {
                        String iface = it.next();
                        sb.append(iface);
                        if (it.hasNext()) {
                            sb.append(", ");
                        }
                    }
                }
                sb.append(" {\n");
                for (GeneratedParameter m : methods) {
                    indent(m.varDeclaration(), sb, 1);
                }
                if (anySetter) {
                    indent("private final Map<String,String> __metadata = new HashMap<>();", sb, 1);
                }
                sb.append('\n');
                indent("@Inject", sb, 1);
                indent("public " + className + " (KeysValues params) {", sb, 1);
                for (GeneratedParameter m : methods) {
                    indent("this." + m.fieldName() + " = " + m.loadClause(), sb, 2);
                }
                if (anySetter) {
                    indent("for (Map.Entry<String,String> __e : params) {", sb, 2);
                    indent("switch (__e.getKey()) {", sb, 3);
                    for (GeneratedParameter m : methods) {
                        indent("case \"" + m.param.value() + "\" :", sb, 4);
                    }
                    indent("break;", sb, 5);
                    indent("default :", sb, 4);
                    indent("__any (__e.getKey(), __e.getValue());", sb, 5);
                    indent("}", sb, 3);
                    indent("}", sb, 2);
                }
                indent("}\n", sb, 1);

                if (jsonConstructor) {
                    indent("@JsonCreator", sb, 1);
                    indent("public " + className + "(", sb, 1);
                    for (Iterator<GeneratedParameter> it = methods.iterator(); it.hasNext();) {
                        GeneratedParameter m = it.next();
                        StringBuilder b = new StringBuilder();
                        b.append("@JsonProperty(");
                        if (m.isRequired()) {
                            b.append("value=\"").append(m.param.value()).append("\"");
                        } else {
                            b.append("value=\"").append(m.param.value()).append("\"")
                                    .append(", required=false");
                        }
                        b.append(") ");
                        try {
                            b.append(m.param.type().typeName(m.isRequired())).append(" ");
                        } catch (EnumConstantNotPresentException e) {
                            b.append("INVALID_ANNOTATION").append(" ");
                        }
                        b.append(m.fieldName());
                        if (it.hasNext()) {
                            b.append(",");
                        } else {
                            b.append(") {");
                        }
                        indent(b.toString(), sb, 2);
                    }
                    for (GeneratedParameter m : methods) {
                        String defVal = null;
                        if (!"".equals(m.param.defaultValue())) {
                            try {
                                if (m.param.type().isString()) {
                                    defVal = '"' + m.param.defaultValue().replaceAll("\"", "\\\"") + '"';
                                } else {
                                    defVal = m.param.defaultValue();
                                    switch (m.param.type()) {
                                        case LONG:
                                        case NON_NEGATIVE_LONG:
                                            if (!defVal.endsWith("L") && !defVal.endsWith("l")) {
                                                defVal += "L";
                                            }
                                    }
                                }
                            } catch (EnumConstantNotPresentException e) {
                                defVal = "null; // " + e.getMessage();
                            }
                        }
                        if (defVal != null) {
                            indent("this." + m.fieldName() + " = " + m.fieldName() + " == null ? " + defVal + " : " + m.fieldName() + ";", sb, 2);
                        } else if (!m.param.required()) {
                            indent("this." + m.fieldName() + " = Optional.ofNullable(" + m.fieldName() + ");", sb, 2);
                        } else {
                            indent("this." + m.fieldName() + " = " + m.fieldName() + ";", sb, 2);
                        }
                    }
                    indent("}\n", sb, 1);
                }

                if (anySetter) {
                    sb.append("\n");
                    if (jsonConstructor) {
                        indent("@JsonAnySetter", sb, 1);
                    }
                    indent("public void __any(String key, String value){", sb, 1);
                    indent("__metadata.put(key, value);", sb, 2);
                    indent("}", sb, 1);
                    sb.append("\n");
                    indent("public Optional<String> get(String key) {", sb, 1);
                    indent("return Optional.ofNullable(__metadata.get(key));", sb, 2);
                    indent("}", sb, 1);
                    sb.append("\n");
                }

                for (GeneratedParameter m : methods) {
                    indent(m.toString(), sb, 1);
                }
                indent("@Override", sb, 1);
                indent("public String toString() {", sb, 1);
                indent("return ", sb, 2);
                sb.append("           ");
                for (int i = 0; i < methods.size(); i++) {
                    GeneratedParameter m = methods.get(i);
                    sb.append("\" ").append(m.param.value()).append(" = ").append('"').append(" + ").append(m.fieldName());
                    if (i != methods.size() - 1) {
                        sb.append("\n            + ");
                    }
                }
                sb.append(";\n");
                indent("}\n", sb, 1);

                indent("@Override", sb, 1);
                indent("public boolean equals (Object o) {", sb, 1);
                indent("if (o == this) {", sb, 2);
                indent("return true;", sb, 3);
                indent("} else if (o == null) {", sb, 2);
                indent("return false;", sb, 3);
                indent("}\n", sb, 2);
                indent("if (o instanceof " + className + ") {", sb, 2);
                indent(className + " other = (" + className + ") o;", sb, 3);
                indent("return ", sb, 3);
                for (int i = 0; i < methods.size(); i++) {
                    GeneratedParameter method = methods.get(i);
                    String delim = i < methods.size() - 1 ? " &&" : ";";
                    if (method.isPrimitive()) {
                        indent("this." + method.fieldName() + " == other." + method.fieldName() + delim, sb, 4);
                    } else {
                        indent("Objects.equals(this." + method.fieldName() + ", other." + method.fieldName() + ") " + delim, sb, 4);
                    }
                }
                indent("}", sb, 2);
                indent("return false;", sb, 2);
                indent("}\n", sb, 1);

                indent("@Override", sb, 1);
                indent("public int hashCode() {", sb, 1);
                indent("return Objects.hash(", sb, 2);
                for (int i = 0; i < methods.size(); i++) {
                    GeneratedParameter method = methods.get(i);
                    String delim = i < methods.size() - 1 ? "," : ");";
                    indent(method.fieldName() + delim, sb, 3);
                }
                indent("}", sb, 1);

                if (validate) {
                    sb.append("\n");
                    indent("@Override", sb, 1);
                    indent("public Problems validate (Injector inj, Problems problems) {", sb, 1);
                    for (GeneratedParameter p : methods) {
                        List<String> validatorTypes = validators.get(p.param.value());

                        if (p.param.constraints().length == 0 && (validatorTypes == null || validatorTypes.isEmpty())) {
                            continue;
                        }
                        final boolean optional = !p.isRequired();
                        if (p.param.constraints().length > 0) {
                            int ind = optional ? 2 : 3;
                            if (!optional) {
                                indent("if (" + p.fieldName() + ".isPresent()) {", sb, 2);
                            }
                            for (StringValidators v : p.param.constraints()) {
                                indent(v.name() + ".validate(problems, \"" + p.param.value() + "\", " + p.fieldName() + ");", sb, ind);
                            }
                            if (!optional) {
                                indent("}", sb, 2);
                            }
                        }
                        int ix = 0;
                        if (validatorTypes != null && !validatorTypes.isEmpty()) {
                            for (String type : validatorTypes) {
                                String varName = p.fieldName() + "Validator" + ++ix;
                                if (!optional) {
                                    indent("Validator<String> " + varName + " = inj.getInstance(" + type + ".class);", sb, 2);
                                    indent(varName + ".validate (problems, " + "\"" + p.param.value() + "\", " + p.fieldName() + (p.param.type().isString() ? "" : " + \"\"") + ");", sb, 2);
                                } else {
                                    indent("if (" + p.fieldName() + ".isPresent()) {", sb, 2);
                                    indent("Validator<String>  " + varName + " = inj.getInstance(" + type + ".class);", sb, 3);
                                    indent(varName + ".validate (problems, " + "\"" + p.param.value() + "\", " + p.fieldName() + ".get() " + (p.param.type().isString() ? "" : " + \"\"") + ");", sb, 3);
                                    indent("}", sb, 2);
                                }
                            }
                        }
                    }
                    indent("return problems;", sb, 2);
                    indent("}", sb, 1);
                }
                if (generateToMap) {
                    sb.append("\n");
                    indent("public Map<String,Object> toMap() {", sb, 1);
                    indent("Map<String,Object> result = new HashMap<>();", sb, 2);
                    for (GeneratedParameter p : methods) {
                        boolean optional = p.varDeclaration().contains("Optional");
                        if (!optional) {
                            indent("result.put(\"" + p.param.value() + "\", " + p.fieldName() + ");", sb, 2);
                        } else {
                            indent("if (" + p.fieldName() + ".isPresent()) {", sb, 2);
                            indent("result.put(\"" + p.param.value() + "\", " + p.fieldName() + ".get());", sb, 3);
                            indent("}", sb, 2);
                        }
                    }
                    if (anySetter) {
                        indent("result.putAll(__metadata);", sb, 2);
                    }
                    indent("return result;", sb, 2);
                    indent("}", sb, 1);
                }
                if (params.generateToJSON()) {
                    sb.append("\n");
                    indent("public String toJSON() throws JsonProcessingException {", sb, 1);
                    indent("return new ObjectMapper().writeValueAsString(toMap());", sb, 2);
                    indent("}", sb, 1);
                }

                sb.append("}\n");
            } catch (Exception e) {
                sb.append(e);
            }

            return sb.toString();
        }

        private void indent(String s, StringBuilder sb, int count) {
            char[] space = new char[count * 4];
            Arrays.fill(space, ' ');
            sb.append(new String(space)).append(s).append('\n');
        }

        final class GeneratedParameter implements Comparable<GeneratedParameter> {

            private final Param param;

            public GeneratedParameter(Param param) {
                this.param = param;
            }

            boolean isPrimitive() {
                try {
                    if (param.defaultValue() != null || param.required()) {
                        return param.type().isNumber();
                    }
                } catch (AnnotationTypeMismatchException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Default value "
                            + " is not a string", el);
                }
                return false;
            }

            boolean isRequired() {
                if (!param.defaultValue().isEmpty()) {
                    return false;
                }
                return param.required();
            }

            String fieldName() {
                return '_' + param.value();
            }

            String loadClause() {
                try {
                    String nameQuoted = '"' + param.value() + '"';
                    StringBuilder sb = new StringBuilder();
                    String defVal = param.defaultValue().isEmpty() ? null : param.defaultValue();
                    if (defVal != null) {
                        defVal = defVal.trim();
                        switch (param.type()) {
                            case LONG:
                                if (!defVal.endsWith("L")) {
                                    defVal += "L";
                                }
                                break;
                            case STRING:
                            case NON_EMPTY_STRING:
                                defVal = '"' + defVal.replaceAll("\"", "\\\"") + '"';
                                break;
                        }
                        Problems problems = new Problems();
                        param.type().validator().validate(problems, param.value(), defVal);
                        if (problems.hasFatal()) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Bad default value for "
                                    + param.value() + ":" + problems.getLeadProblem(), el);
                        }
                    }
                    if (param.required() && defVal == null) {
                        switch (param.type()) {
                            case STRING:
                            case NON_EMPTY_STRING:
                                sb.append("params.get(").append(nameQuoted).append(")");
                                break;
                            default:
                                sb.append(param.type().conversionMethod()).append("(").append("params.get(").append(nameQuoted).append("))");
                        }
//                        sb.append("params.get(").append(nameQuoted).append(").").append(param.type().conversionMethod());
                    } else if (defVal != null) {
                        switch (param.type()) {
                            case STRING:
                            case NON_EMPTY_STRING:
                                sb.append("params.get(").append(nameQuoted).append(") == null ? " + defVal
                                        + " : params.get(").append(nameQuoted).append(")");
                                break;
                            default:
                                sb.append("params.get(").append(nameQuoted).append(") == null ? ").append(defVal).append(" : ")
                                        .append(param.type().conversionMethod()).append("(").append("params.get(").append(nameQuoted).append("))");
                        }
//                        sb.append("params.get(").append(nameQuoted).append(").").append(param.type().conversionMethod());
                    } else {
                        switch (param.type()) {
                            case STRING:
                            case NON_EMPTY_STRING:
                                sb.append("Optional.ofNullable(params.get(").append(nameQuoted).append(") == null ? " + defVal
                                        + " : params.get(").append(nameQuoted).append("))");
                                break;
                            default:
                                sb.append("Optional.ofNullable(params.get(").append(nameQuoted).append(") == null ? ").append(defVal).append(" : ")
                                        .append(param.type().conversionMethod()).append("(").append("params.get(").append(nameQuoted).append("))");
                        }
                    }
                    sb.append(";");
                    return sb.toString();
                } catch (Exception ex) {
                    return "null;\n if (true) throw new UnsupportedOperatiionException(\"" + ex.getMessage().replace('"', '\'') + "\")";
                }
            }

            String varDeclaration() {
                try {
                    return "private final " + param.type().typeName(param.required() || !param.defaultValue().isEmpty(), true) + ' '
                            + fieldName() + ";";
                } catch (Exception e) {
                    return "// invalid annotation data: " + e.getMessage();
                }
            }

            public String returnType() {
                try {
                    if (!param.defaultValue().isEmpty()) {
                        return param.type().typeName(true);
                    }
                    return param.type().typeName(isRequired(), true);
                } catch (Exception e) {
                    return "Object /* invalid annotation data: " + e.getMessage() + "  */";
                }
            }

            @Override
            public int compareTo(GeneratedParameter t) {
                return param.value().compareTo(t.param.value());
            }

            @Override
            public String toString() {
                return "public " + returnType() + " get" + capitalize(param.value())
                        + "() {\n        return _" + param.value() + ";\n    }\n";
            }

            @SuppressWarnings("null")
            private String capitalize(String s) {
                if (s.isEmpty() || s == null) {
                    return "";
                }
                char[] c = s.toCharArray();
                c[0] = Character.toUpperCase(c[0]);
                return new String(c);
            }
        }
    }
}
