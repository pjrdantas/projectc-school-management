package br.com.escola.peopleservice.infra.webclient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import br.com.escola.peopleservice.application.context.InternalHeaders;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoCleanupCommand;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoWriteCommand;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoWriteResult;
import br.com.escola.peopleservice.application.port.out.PeopleAddressWritePort;
import io.micrometer.core.instrument.MeterRegistry;

@Component
@ConditionalOnProperty(
        prefix = "people.shadow.monolith",
        name = "address-write-adapter-enabled",
        havingValue = "true")
public class MonolithPessoaAddressWriteClient implements PeopleAddressWritePort {

    private static final String METRIC_NAME = "people.shadow.monolith.address.write.requests";
    private static final String SELECTED_SOURCE = "school_management_service";
    private static final String FALLBACK_SOURCE = "monolith_proxy";

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    public MonolithPessoaAddressWriteClient(RestClient monolithPeopleRestClient, MeterRegistry meterRegistry) {
        this.restClient = monolithPeopleRestClient;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public PessoaEnderecoWriteResult criarOuAtualizarEnderecoPrincipal(PessoaEnderecoWriteCommand command) {
        validar(command.commandId(), command.pessoaId(), command.escolaId(), command.idempotencyKey(), command.requestedBy());
        try {
            MonolithAddressWriteResponse response = restClient.put()
                    .uri("/internal/pessoas/{id}/endereco-principal", command.pessoaId())
                    .headers(headers -> enrichHeaders(headers, command))
                    .body(new MonolithAddressWriteRequest(
                            command.tipoEnderecoCodigo(),
                            command.principal(),
                            command.cep(),
                            command.logradouro(),
                            command.numero(),
                            command.complemento(),
                            command.bairro(),
                            command.cidade(),
                            command.uf()))
                    .retrieve()
                    .body(MonolithAddressWriteResponse.class);
            registrarRequisicao("criarOuAtualizarEnderecoPrincipal", "success");
            return toResult(command.commandId(), command.pessoaId(), response);
        } catch (RestClientResponseException | ResourceAccessException exception) {
            registrarErro("criarOuAtualizarEnderecoPrincipal", exception);
            return fallbackResult(command.commandId(), command.pessoaId(), "monolith_address_write_adapter_fallback_required");
        }
    }

    @Override
    public PessoaEnderecoWriteResult removerEnderecosDaPessoa(PessoaEnderecoCleanupCommand command) {
        validar(command.commandId(), command.pessoaId(), command.escolaId(), command.idempotencyKey(), command.requestedBy());
        try {
            MonolithAddressWriteResponse response = restClient.delete()
                    .uri("/internal/pessoas/{id}/enderecos", command.pessoaId())
                    .headers(headers -> enrichHeaders(headers, command))
                    .retrieve()
                    .body(MonolithAddressWriteResponse.class);
            registrarRequisicao("removerEnderecosDaPessoa", "success");
            return toResult(command.commandId(), command.pessoaId(), response);
        } catch (RestClientResponseException | ResourceAccessException exception) {
            registrarErro("removerEnderecosDaPessoa", exception);
            return fallbackResult(command.commandId(), command.pessoaId(), "monolith_address_cleanup_adapter_fallback_required");
        }
    }

    private void enrichHeaders(HttpHeaders headers, PessoaEnderecoWriteCommand command) {
        headers.set(InternalHeaders.ESCOLA_ID, command.escolaId().toString());
        headers.set(InternalHeaders.USUARIO_ID, usuarioId(command.requestedBy()).toString());
        headers.set(InternalHeaders.CORRELATION_ID, command.commandId().toString());
        headers.set("Idempotency-Key", command.idempotencyKey());
    }

    private void enrichHeaders(HttpHeaders headers, PessoaEnderecoCleanupCommand command) {
        headers.set(InternalHeaders.ESCOLA_ID, command.escolaId().toString());
        headers.set(InternalHeaders.USUARIO_ID, usuarioId(command.requestedBy()).toString());
        headers.set(InternalHeaders.CORRELATION_ID, command.commandId().toString());
        headers.set("Idempotency-Key", command.idempotencyKey());
    }

    private PessoaEnderecoWriteResult toResult(
            UUID commandId,
            UUID pessoaId,
            MonolithAddressWriteResponse response) {
        if (response == null) {
            return fallbackResult(commandId, pessoaId, "monolith_address_write_adapter_empty_response");
        }
        List<String> warnings = new ArrayList<>();
        if (response.warnings() != null) {
            warnings.addAll(response.warnings());
        }
        warnings.add("people-service-monolith-address-write-adapter-guarded");
        warnings.add("local-address-persistence-disabled");
        return new PessoaEnderecoWriteResult(
                commandId,
                pessoaId,
                response.enderecoId(),
                response.pessoaEnderecoId(),
                response.status(),
                response.selectedSource() == null ? SELECTED_SOURCE : response.selectedSource(),
                false,
                false,
                List.copyOf(warnings));
    }

    private PessoaEnderecoWriteResult fallbackResult(UUID commandId, UUID pessoaId, String status) {
        return new PessoaEnderecoWriteResult(
                commandId,
                pessoaId,
                null,
                null,
                status,
                FALLBACK_SOURCE,
                false,
                true,
                List.of(
                        "people-service-monolith-address-write-adapter-fallback",
                        "keep-shadow-command-as-fallback",
                        "local-address-persistence-disabled"));
    }

    private void validar(UUID commandId, UUID pessoaId, UUID escolaId, String idempotencyKey, String requestedBy) {
        if (commandId == null || pessoaId == null || escolaId == null || idempotencyKey == null
                || idempotencyKey.isBlank() || requestedBy == null || requestedBy.isBlank()) {
            throw new IllegalArgumentException(
                    "commandId, pessoaId, escolaId, idempotencyKey and requestedBy are required");
        }
        usuarioId(requestedBy);
    }

    private UUID usuarioId(String requestedBy) {
        return UUID.fromString(requestedBy);
    }

    private void registrarRequisicao(String operation, String result) {
        meterRegistry.counter(
                METRIC_NAME,
                "operation", operation,
                "result", result,
                "selectedSource", SELECTED_SOURCE)
                .increment();
    }

    private void registrarErro(String operation, Exception exception) {
        registrarRequisicao(operation, "fallback_required");
        meterRegistry.counter(
                "people.shadow.monolith.address.write.failures",
                "operation", operation,
                "cause", exception.getClass().getSimpleName())
                .increment();
    }

    private record MonolithAddressWriteRequest(
            String tipoEnderecoCodigo,
            boolean principal,
            String cep,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String cidade,
            String uf) {
    }

    private record MonolithAddressWriteResponse(
            String commandId,
            UUID pessoaId,
            UUID enderecoId,
            UUID pessoaEnderecoId,
            String status,
            String selectedSource,
            boolean persistedLocally,
            boolean fallbackRequired,
            List<String> warnings) {
    }
}
