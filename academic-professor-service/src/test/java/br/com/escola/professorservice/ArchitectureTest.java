package br.com.escola.professorservice;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.importer.ClassFileImporter;

class ArchitectureTest {

    @Test
    void interfacesNaoDevemDependerDeInfraestruturaWebclient() {
        var classes = new ClassFileImporter().importPackages("br.com.escola.professorservice");

        noClasses()
                .that().resideInAPackage("..interfaces..")
                .should().dependOnClassesThat().resideInAPackage("..infra.webclient..")
                .check(classes);
    }
}

