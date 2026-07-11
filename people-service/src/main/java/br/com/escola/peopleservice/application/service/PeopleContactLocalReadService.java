package br.com.escola.peopleservice.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PessoaContatoLocalReadResponse;
import br.com.escola.peopleservice.application.port.out.PeopleContactLocalReadPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PeopleContactLocalReadService {

    private final ObjectProvider<PeopleContactLocalReadPort> contactLocalReadPortProvider;
    private final MeterRegistry meterRegistry;

    public PeopleContactLocalReadService(
            ObjectProvider<PeopleContactLocalReadPort> contactLocalReadPortProvider,
            MeterRegistry meterRegistry) {
        this.contactLocalReadPortProvider = contactLocalReadPortProvider;
        this.meterRegistry = meterRegistry;
    }

    public Optional<PessoaContatoLocalReadResponse> buscarContatoPorPessoa(UUID pessoaId, UUID escolaId) {
        PeopleContactLocalReadPort port = contactLocalReadPortProvider.getIfAvailable();
        if (port == null) {
            registrarLeituraContatoLocal("buscarContatoPorPessoa", "fallback_adapter_missing");
            return Optional.empty();
        }
        try {
            Optional<PessoaContatoLocalReadResponse> response = port.buscarContatoPorPessoa(pessoaId, escolaId);
            registrarLeituraContatoLocal(
                    "buscarContatoPorPessoa",
                    response.isPresent() ? "success" : "fallback_not_found");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraContatoLocal("buscarContatoPorPessoa", "fallback_error");
            return Optional.empty();
        }
    }

    private void registrarLeituraContatoLocal(String operation, String result) {
        meterRegistry.counter(
                "people.shadow.local.persistence.contact.reads",
                "operation", operation,
                "result", result)
                .increment();
    }
}
