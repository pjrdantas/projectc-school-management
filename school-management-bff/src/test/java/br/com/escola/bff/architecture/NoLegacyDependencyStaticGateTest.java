package br.com.escola.bff.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class NoLegacyDependencyStaticGateTest {

    private static final List<String> FORBIDDEN_REFERENCES = List.of(
            "school-management-service",
            "clients.monolith",
            "MonolithClient",
            "LegacyFallback",
            "source-url:",
            "source-username:",
            "source-password:",
            "backfill:",
            "gestao_escolar");

    private static final List<Path> PRODUCTION_ROOTS = List.of(
            Path.of("src/main"),
            Path.of("../identity-access-service/src/main"),
            Path.of("../institutional-tenant-service/src/main"),
            Path.of("../academic-catalog-service/src/main"),
            Path.of("../academic-professor-service/src/main"),
            Path.of("../people-service/src/main"),
            Path.of("../responsibles-service/src/main"),
            Path.of("../enrollment-document-service/src/main"),
            Path.of("../pedagogical-service/src/main"),
            Path.of("../planning-ai-service/src/main"),
            Path.of("../dashboard-query-service/src/main"),
            Path.of("../platform"));

    @Test
    void naoDeveConterDependenciaProdutivaOuFallbackParaOMonolito() throws IOException {
        List<String> violations = PRODUCTION_ROOTS.stream()
                .map(Path::toAbsolutePath)
                .flatMap(root -> productionFiles(root).stream())
                .flatMap(path -> forbiddenOccurrences(path).stream())
                .toList();

        assertThat(violations).as("A plataforma ativa nao pode reintroduzir dependencia, origem ou fallback do monolito")
                .isEmpty();
    }

    private List<Path> productionFiles(Path root) {
        try (var files = Files.walk(root)) {
            return files.filter(Files::isRegularFile)
                    .filter(this::isProductionConfigurationOrSource)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel inspecionar " + root, exception);
        }
    }

    private boolean isProductionConfigurationOrSource(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith(".java")
                || fileName.endsWith(".yml")
                || fileName.endsWith(".yaml")
                || fileName.endsWith(".properties");
    }

    private List<String> forbiddenOccurrences(Path file) {
        try {
            String content = Files.readString(file);
            return FORBIDDEN_REFERENCES.stream()
                    .filter(content::contains)
                    .map(forbidden -> file + ": " + forbidden)
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel inspecionar " + file, exception);
        }
    }
}
