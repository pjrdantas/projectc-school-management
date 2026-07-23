package br.com.escola.bff.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

@SpringBootTest
class BffExternalConsumerContractGateTest {

    private static final Pattern HTTP_CALL = Pattern.compile(
            "\\.(get|post|put|delete|patch)(?:<[^>]+>)?\\s*\\(\\s*`([^`]*?/api/[^`]*)`",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping mappings;

    @Test
    void cadaChamadaApiDoConsumidorDeveTerMappingPublicoNoBff() throws IOException {
        List<Contract> consumerContracts = consumerContracts();
        List<Contract> bffContracts = bffContracts();
        List<String> missing = consumerContracts.stream()
                .filter(contract -> bffContracts.stream().noneMatch(contract::matches))
                .map(Contract::toString)
                .distinct()
                .toList();

        assertThat(missing)
                .as("cada chamada /api do school-management-web deve ter metodo e mapping publico no BFF")
                .isEmpty();
    }

    private List<Contract> consumerContracts() throws IOException {
        Path web = Path.of("..", "school-management-web").toAbsolutePath().normalize();
        List<Contract> contracts = new ArrayList<>();
        try (var files = Files.walk(web)) {
            files.filter(path -> path.toString().endsWith(".ts")).forEach(path -> {
                try {
                    Matcher matcher = HTTP_CALL.matcher(Files.readString(path));
                    while (matcher.find()) {
                        contracts.add(new Contract(method(matcher.group(1)), normalize(matcher.group(2))));
                    }
                } catch (IOException exception) {
                    throw new IllegalStateException("Nao foi possivel ler " + path, exception);
                }
            });
        }
        return contracts;
    }

    private List<Contract> bffContracts() {
        return mappings.getHandlerMethods().keySet().stream()
                .flatMap(info -> info.getPatternsCondition().getPatterns().stream()
                        .map(Object::toString)
                        .flatMap(path -> info.getMethodsCondition().getMethods().stream()
                                .map(method -> new Contract(method, normalize(path)))))
                .toList();
    }

    private static RequestMethod method(String method) {
        return RequestMethod.valueOf(method.toUpperCase());
    }

    private static String normalize(String path) {
        int apiStart = path.indexOf("/api/");
        String apiPath = apiStart >= 0 ? path.substring(apiStart) : path;
        return apiPath.replaceAll("\\$\\{[^}]+}", "{}")
                .replaceAll("\\{[^}]+}", "{}")
                .replaceAll("\\?.*$", "");
    }

    private record Contract(RequestMethod method, String path) {
        boolean matches(Contract other) {
            return method == other.method
                    && path.matches(other.path.replace("{}", "[^/]+"));
        }
    }
}
