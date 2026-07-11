package br.com.escola.peopleservice.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PessoaAlunoVinculoResponse;
import br.com.escola.peopleservice.application.port.out.AlunoPessoaPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class AlunoPessoaService {

    private final ObjectProvider<AlunoPessoaPort> alunoPortProvider;
    private final MeterRegistry meterRegistry;

    public AlunoPessoaService(
            ObjectProvider<AlunoPessoaPort> alunoPortProvider,
            MeterRegistry meterRegistry) {
        this.alunoPortProvider = alunoPortProvider;
        this.meterRegistry = meterRegistry;
    }

    public Optional<PessoaAlunoVinculoResponse> buscarVinculoPorAlunoId(UUID alunoId, UUID escolaId) {
        AlunoPessoaPort port = alunoPortProvider.getIfAvailable();
        if (port == null) {
            registrarLookup("buscarVinculoPorAlunoId", "adapter_missing");
            return Optional.empty();
        }
        try {
            Optional<PessoaAlunoVinculoResponse> response = port.buscarVinculoPorAlunoId(alunoId, escolaId);
            registrarLookup(
                    "buscarVinculoPorAlunoId",
                    response.isPresent() ? "success" : "not_found");
            return response;
        } catch (RuntimeException ex) {
            registrarLookup("buscarVinculoPorAlunoId", "error");
            return Optional.empty();
        }
    }

    private void registrarLookup(String operation, String result) {
        meterRegistry.counter(
                "people.student.lookup",
                "operation", operation,
                "result", result)
                .increment();
    }
}

