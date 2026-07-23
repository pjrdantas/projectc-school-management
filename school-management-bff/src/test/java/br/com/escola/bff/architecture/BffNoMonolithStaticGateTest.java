package br.com.escola.bff.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class BffNoMonolithStaticGateTest {

    private static final List<String> FORBIDDEN_REFERENCES = List.of(
            "school-management-service",
            "clients.monolith",
            "MonolithClient",
            "LegacyFallback");

    @Test
    void naoDeveConterDependenciaProdutivaOuFallbackParaOMonolito() throws IOException {
        Path sources = Path.of("src/main").toAbsolutePath();
        List<String> violations;
        try (var files = Files.walk(sources)) {
            violations = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java") || path.toString().endsWith(".yml"))
                    .flatMap(path -> forbiddenOccurrences(path).stream())
                    .toList();
        }
        assertThat(violations).as("BFF nao pode reintroduzir dependencia ou fallback produtivo ao monolito")
                .isEmpty();
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
