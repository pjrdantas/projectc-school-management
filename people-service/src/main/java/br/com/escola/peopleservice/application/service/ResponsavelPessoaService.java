package br.com.escola.peopleservice.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PessoaResponsavelVinculoResponse;
import br.com.escola.peopleservice.application.port.out.ResponsavelPessoaPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class ResponsavelPessoaService {

    private final ObjectProvider<ResponsavelPessoaPort> responsavelPortProvider;
    private final PeopleReadSourcePolicy readRoutingPolicy;
    private final MeterRegistry meterRegistry;

    public ResponsavelPessoaService(
            ObjectProvider<ResponsavelPessoaPort> responsavelPortProvider,
            PeopleReadSourcePolicy readRoutingPolicy,
            MeterRegistry meterRegistry) {
        this.responsavelPortProvider = responsavelPortProvider;
        this.readRoutingPolicy = readRoutingPolicy;
        this.meterRegistry = meterRegistry;
    }

    public Optional<PessoaResponsavelVinculoResponse> buscarVinculoPorResponsavelId(UUID responsavelId, UUID escolaId) {
        var decision = readRoutingPolicy.registrarDecisaoLeituraResponsavelVinculo();
        if (!decision.localReadEligible()) {
            registrarLookup("buscarVinculoPorResponsavelId", "guard_blocked");
            return Optional.empty();
        }
        ResponsavelPessoaPort port = responsavelPortProvider.getIfAvailable();
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
                "people.responsible.lookup",
                "operation", operation,
                "result", result)
                .increment();
    }
}

