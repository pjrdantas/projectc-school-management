package br.com.escola.bff.interfaces.rest;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import br.com.escola.bff.application.context.TrustedHeaders;
import br.com.escola.bff.application.usecase.PainelWriteUseCase;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnProperty(name = "features.dashboard-read-proxy-enabled", havingValue = "true", matchIfMissing = true)
public class PainelWriteController {

    private final PainelWriteUseCase useCase;
    private final ObjectMapper objectMapper;

    public PainelWriteController(PainelWriteUseCase useCase, ObjectMapper objectMapper) {
        this.useCase = useCase;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/api/dashboard/configuracoes/publicos")
    public Mono<ResponseEntity<String>> criarPublico(@RequestBody String body, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return enviar(authorization, correlationId, HttpMethod.POST, "/internal/v1/dashboard/configuracoes/publicos", body);
    }

    @PutMapping("/api/dashboard/configuracoes/publicos/{id}")
    public Mono<ResponseEntity<String>> atualizarPublico(@PathVariable UUID id, @RequestBody String body, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return enviar(authorization, correlationId, HttpMethod.PUT, "/internal/v1/dashboard/configuracoes/publicos/" + id, body);
    }

    @DeleteMapping("/api/dashboard/configuracoes/publicos/{id}")
    public Mono<ResponseEntity<String>> excluirPublico(@PathVariable UUID id, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return enviar(authorization, correlationId, HttpMethod.DELETE, "/internal/v1/dashboard/configuracoes/publicos/" + id, null);
    }

    @PostMapping("/api/dashboard/configuracoes/dashboards")
    public Mono<ResponseEntity<String>> criarPainel(@RequestBody String body, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return enviar(authorization, correlationId, HttpMethod.POST, "/internal/v1/dashboard/configuracoes/dashboards", compatibilizar(body, "publicoDashboardId", "publicoId"));
    }

    @PutMapping("/api/dashboard/configuracoes/dashboards/{id}")
    public Mono<ResponseEntity<String>> atualizarPainel(@PathVariable UUID id, @RequestBody String body, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return enviar(authorization, correlationId, HttpMethod.PUT, "/internal/v1/dashboard/configuracoes/dashboards/" + id, compatibilizar(body, "publicoDashboardId", "publicoId"));
    }

    @DeleteMapping("/api/dashboard/configuracoes/dashboards/{id}")
    public Mono<ResponseEntity<String>> excluirPainel(@PathVariable UUID id, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return enviar(authorization, correlationId, HttpMethod.DELETE, "/internal/v1/dashboard/configuracoes/dashboards/" + id, null);
    }

    @PostMapping("/api/dashboard/configuracoes/widgets")
    public Mono<ResponseEntity<String>> criarWidget(@RequestBody String body, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return enviar(authorization, correlationId, HttpMethod.POST, "/internal/v1/dashboard/configuracoes/widgets", compatibilizar(body, "dashboardId", "painelId"));
    }

    @PutMapping("/api/dashboard/configuracoes/widgets/{id}")
    public Mono<ResponseEntity<String>> atualizarWidget(@PathVariable UUID id, @RequestBody String body, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return enviar(authorization, correlationId, HttpMethod.PUT, "/internal/v1/dashboard/configuracoes/widgets/" + id, compatibilizar(body, "dashboardId", "painelId"));
    }

    @DeleteMapping("/api/dashboard/configuracoes/widgets/{id}")
    public Mono<ResponseEntity<String>> excluirWidget(@PathVariable UUID id, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return enviar(authorization, correlationId, HttpMethod.DELETE, "/internal/v1/dashboard/configuracoes/widgets/" + id, null);
    }

    @PutMapping("/api/dashboard/usuarios/{usuarioId}/widgets/{widgetId}/configuracao")
    public Mono<ResponseEntity<String>> salvarPreferencia(@PathVariable UUID usuarioId, @PathVariable UUID widgetId, @RequestBody String body,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return enviar(authorization, correlationId, HttpMethod.PUT, "/internal/v1/dashboard/usuarios/" + usuarioId + "/widgets/" + widgetId + "/configuracao", body);
    }

    @DeleteMapping("/api/dashboard/usuarios/{usuarioId}/widgets/{widgetId}/configuracao")
    public Mono<ResponseEntity<String>> excluirPreferencia(@PathVariable UUID usuarioId, @PathVariable UUID widgetId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return enviar(authorization, correlationId, HttpMethod.DELETE, "/internal/v1/dashboard/usuarios/" + usuarioId + "/widgets/" + widgetId + "/configuracao", null);
    }

    @PutMapping("/api/dashboard/snapshots")
    public Mono<ResponseEntity<String>> salvarSnapshot(@RequestBody String body, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return enviar(authorization, correlationId, HttpMethod.PUT, "/internal/v1/dashboard/snapshots/locais", compatibilizar(body, "publicoDashboardId", "publicoId"));
    }

    @DeleteMapping("/api/dashboard/snapshots/{id}")
    public Mono<ResponseEntity<String>> excluirSnapshot(@PathVariable UUID id, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return enviar(authorization, correlationId, HttpMethod.DELETE, "/internal/v1/dashboard/snapshots/locais/" + id, null);
    }

    @PostMapping("/api/dashboard/snapshots/geracoes/professores/{professorId}")
    public Mono<ResponseEntity<String>> consultarGeracaoProfessor(
            @PathVariable UUID professorId,
            @RequestParam(required = false) LocalDate referenciaData,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return enviar(authorization, correlationId, HttpMethod.POST,
                caminhoComReferencia("/internal/v1/dashboard/snapshots/geracoes/professores/" + professorId, referenciaData), null);
    }

    @PostMapping("/api/dashboard/snapshots/geracoes/{publicoCodigo}")
    public Mono<ResponseEntity<String>> consultarGeracaoPublico(
            @PathVariable String publicoCodigo,
            @RequestParam(required = false) LocalDate referenciaData,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(TrustedHeaders.CORRELATION_ID) String correlationId) {
        return enviar(authorization, correlationId, HttpMethod.POST,
                caminhoComReferencia("/internal/v1/dashboard/snapshots/geracoes/" + publicoCodigo, referenciaData), null);
    }

    private Mono<ResponseEntity<String>> enviar(String authorization, String correlationId, HttpMethod method, String caminho, String body) {
        return useCase.encaminhar(authorization, correlationId, method, caminho, body);
    }

    private String caminhoComReferencia(String caminho, LocalDate referenciaData) {
        return referenciaData == null ? caminho : caminho + "?referenciaData=" + referenciaData;
    }

    private String compatibilizar(String body, String campoExterno, String campoInterno) {
        try {
            JsonNode json = objectMapper.readTree(body);
            if (json instanceof ObjectNode objeto && objeto.has(campoExterno) && !objeto.has(campoInterno)) {
                objeto.set(campoInterno, objeto.get(campoExterno));
                objeto.remove(campoExterno);
            }
            return objectMapper.writeValueAsString(json);
        } catch (Exception exception) {
            return body;
        }
    }
}
