// Generated by com.dv.sourcetreetool.impl.App
open module com.mastfrog.numble {
    exports com.mastfrog.parameters;
    exports com.mastfrog.parameters.gen;
    exports com.mastfrog.parameters.processor;
    exports com.mastfrog.parameters.validation;
    exports com.mastfrog.parameters.validators;

    // Sibling com.mastfrog/acteur-annotations-3.0.0-dev
    requires com.mastfrog.acteur.annotations;

    // Sibling com.mastfrog/annotation-processors-3.0.0-dev
    requires com.mastfrog.annotation.processors;

    // Sibling com.mastfrog/annotation-tools-3.0.0-dev

    // Transitive detected by source scan
    requires com.mastfrog.annotation.tools;

    // Inferred from source scan
    requires com.mastfrog.collections;

    // Sibling com.mastfrog/giulius-3.0.0-dev:compile
    requires com.mastfrog.giulius;
    requires com.mastfrog.giulius.tests;
    
    requires com.fasterxml.jackson.databind;

    // Inferred from source scan
    requires com.mastfrog.preconditions;
    requires java.compiler;
    requires java.logging;

    // derived from javax.inject/javax.inject-1:compile in javax/inject/javax.inject/1/javax.inject-1.pom
    requires javax.inject;

    // Inferred from test-source-scan
    requires junit;

    // derived from com.mastfrog/simplevalidation-0.0.0-? in com/mastfrog/simplevalidation/1.14/simplevalidation-1.14.pom
    requires simplevalidation;
    provides javax.annotation.processing.Processor with
       com.mastfrog.parameters.processor.ClassListGeneratorProcessor,
       com.mastfrog.parameters.processor.NumbleProcessor;

}
