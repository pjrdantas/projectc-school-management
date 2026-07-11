package br.com.escola.peopleservice.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PessoaResponsavelVinculoResponse;
import br.com.escola.peopleservice.application.port.out.PeopleResponsiblePessoaLocalReadPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PeopleResponsiblePessoaLocalReadService {

    private final ObjectProvider<PeopleResponsiblePessoaLocalReadPort> responsiblePessoaLocalReadPortProvider;
    private final MeterRegistry meterRegistry;

    public PeopleResponsiblePessoaLocalReadService(
            ObjectProvider<PeopleResponsiblePessoaLocalReadPort> responsiblePessoaLocalReadPortProvider,
            MeterRegistry meterRegistry) {
        this.responsiblePessoaLocalReadPortProvider = responsiblePessoaLocalReadPortProvider;
        this.meterRegistry = meterRegistry;
    }

    public Optional<PessoaResponsavelVinculoResponse> buscarVinculoPorResponsavelId(UUID responsavelId, UUID escolaId) {
        PeopleResponsiblePessoaLocalReadPort port = responsiblePessoaLocalReadPortProvider.getIfAvailable();
        if (port == null) {
            registrarLookup("buscarVinculoPorResponsavelId", "adapter_missing");
            return Optional.empty();
        }
        try {
            Optional<PessoaResponsavelVinculoResponse> response = port.buscarVinculoPorResponsavelId(responsavelId, escolaId);
            registrarLookup(
                    "buscarVinculoPorResponsavelId",
                    response.isPresent() ? "success" : "not_found");
            return response;
        } catch (RuntimeException ex) {
            registrarLookup("buscarVinculoPorResponsavelId", "error");
            return Optional.empty();
        }
    }

    private void registrarLookup(String operation, String result) {
        meterRegistry.counter(
                "people.shadow.local.persistence.responsible.pessoa.lookup",
                "operation", operation,
                "result", result)
                .increment();
    }
}
