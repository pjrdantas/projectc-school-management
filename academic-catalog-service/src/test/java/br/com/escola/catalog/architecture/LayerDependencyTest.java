package br.com.escola.catalog.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

class LayerDependencyTest {

    private final JavaClasses classes = new ClassFileImporter().importPackages("br.com.escola.catalog");

    @Test
    void dominioNaoDeveDependerDeFrameworksOuCamadasExternas() {
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..application..", "..infra..", "..interfaces..",
                        "org.springframework..", "jakarta.persistence..")
                .check(classes);
    }

    @Test
    void applicationNaoDeveDependerDeInfraOuInterfaces() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage("..infra..", "..interfaces..")
                .check(classes);
    }
}

