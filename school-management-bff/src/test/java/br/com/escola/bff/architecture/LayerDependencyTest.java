package br.com.escola.bff.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

class LayerDependencyTest {

    private final JavaClasses classes = new ClassFileImporter().importPackages("br.com.escola.bff");

    @Test
    void applicationNaoDeveDependerDeCamadasExternas() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage("..infra..", "..interfaces..")
                .check(classes);
    }

    @Test
    void interfacesNaoDeveDependerDeInfraestrutura() {
        noClasses().that().resideInAPackage("..interfaces..")
                .should().dependOnClassesThat().resideInAPackage("..infra..")
                .check(classes);
    }

    @Test
    void dominioNaoDeveDependerDasDemaisCamadasNemDeFrameworks() {
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..application..", "..infra..", "..interfaces..",
                        "org.springframework..", "jakarta.persistence..")
                .allowEmptyShould(true)
                .check(classes);
    }
}
