package br.com.escola.peopleservice.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PessoaContatoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaContatoPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PessoaContatoService {

    private final ObjectProvider<PessoaContatoPort> contatoPortProvider;
    private final PeopleDataAccessPolicy readRoutingPolicy;
    private final MeterRegistry meterRegistry;

    public PessoaContatoService(
            ObjectProvider<PessoaContatoPort> contatoPortProvider,
            PeopleDataAccessPolicy readRoutingPolicy,
            MeterRegistry meterRegistry) {
        this.contatoPortProvider = contatoPortProvider;
        this.readRoutingPolicy = readRoutingPolicy;
        this.meterRegistry = meterRegistry;
    }

    public Optional<PessoaContatoResponse> buscarContatoPorPessoa(UUID pessoaId, UUID escolaId) {
        var decision = readRoutingPolicy.registrarDecisaoLeituraContato();
        if (!decision.localReadEligible()) {
            registrarLeituraContatoLocal("buscarContatoPorPessoa", "fallback_guard_blocked");
            return Optional.empty();
        }
        PessoaContatoPort port = contatoPortProvider.getIfAvailable();
        if (port == null) {
            registrarLeituraContatoLocal("buscarContatoPorPessoa", "fallback_adapter_missing");
            return Optional.empty();
        }
        try {
            Optional<PessoaContatoResponse> response = port.buscarContatoPorPessoa(pessoaId, escolaId);
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
                "people.contact.reads",
                "operation", operation,
                "result", result)
                .increment();
    }
}

