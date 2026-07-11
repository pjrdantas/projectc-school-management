package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PessoaEnderecoCleanupCommand;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoWriteCommand;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoWriteResult;
import br.com.escola.peopleservice.application.port.out.PeopleAddressWritePort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PeopleAddressCommandService implements PeopleAddressWritePort {

    private static final String METRIC_NAME = "people.address.write.commands";
    private static final String SELECTED_SOURCE = "monolith_proxy";
    private static final String STATUS = "monolith_write_selected_no_local_persistence";

    private final MeterRegistry meterRegistry;

    public PeopleAddressCommandService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public PessoaEnderecoWriteResult criarOuAtualizarEnderecoPrincipal(PessoaEnderecoWriteCommand command) {
        validar(command.commandId(), command.pessoaId(), command.escolaId(), command.idempotencyKey());
        registrarComando("criarOuAtualizarEnderecoPrincipal", "fallback_required");
        return new PessoaEnderecoWriteResult(
                command.commandId(),
                command.pessoaId(),
                null,
                null,
                STATUS,
                SELECTED_SOURCE,
                false,
                true,
                List.of(
                        "people-service-address-write-monolith-only",
                        "monolith-remains-write-authority",
                        "local-address-persistence-disabled"));
    }

    @Override
    public PessoaEnderecoWriteResult removerEnderecosDaPessoa(PessoaEnderecoCleanupCommand command) {
        validar(command.commandId(), command.pessoaId(), command.escolaId(), command.idempotencyKey());
        registrarComando("removerEnderecosDaPessoa", "fallback_required");
        return new PessoaEnderecoWriteResult(
                command.commandId(),
                command.pessoaId(),
                null,
                null,
                STATUS,
                SELECTED_SOURCE,
                false,
                true,
                List.of(
                        "people-service-address-cleanup-monolith-only",
                        "monolith-remains-write-authority",
                        "local-address-persistence-disabled"));
    }

    private void validar(Object commandId, Object pessoaId, Object escolaId, String idempotencyKey) {
        if (commandId == null || pessoaId == null || escolaId == null || idempotencyKey == null
                || idempotencyKey.isBlank()) {
            registrarComando("invalid", "rejected");
            throw new IllegalArgumentException(
                    "commandId, pessoaId, escolaId and idempotencyKey are required for address write commands");
        }
    }

    private void registrarComando(String operation, String result) {
        meterRegistry.counter(
                METRIC_NAME,
                "operation", operation,
                "result", result,
                "selectedSource", SELECTED_SOURCE)
                .increment();
    }
}

