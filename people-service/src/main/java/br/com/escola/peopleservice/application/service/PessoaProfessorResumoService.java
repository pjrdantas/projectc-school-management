package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PessoaProfessorResumoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaProfessorResumoPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PessoaProfessorResumoService {

    private final ObjectProvider<PessoaProfessorResumoPort> professorPortProvider;
    private final OrigemLeituraPolicy readRoutingPolicy;
    private final MeterRegistry meterRegistry;

    public PessoaProfessorResumoService(
            ObjectProvider<PessoaProfessorResumoPort> professorPortProvider,
            OrigemLeituraPolicy readRoutingPolicy,
            MeterRegistry meterRegistry) {
        this.professorPortProvider = professorPortProvider;
        this.readRoutingPolicy = readRoutingPolicy;
        this.meterRegistry = meterRegistry;
    }

    public Optional<PessoaProfessorResumoResponse> buscarProfessorPorId(
            UUID professorId,
            UUID escolaId) {
        var decision = readRoutingPolicy.registrarDecisaoLeituraProfessorResumo();
        if (!decision.localReadEligible()) {
            registrarLeituraProfessorLocal("buscarProfessorPorId", "fallback_guard_blocked");
            return Optional.empty();
        }
        PessoaProfessorResumoPort port = professorPortProvider.getIfAvailable();
        if (port == null) {
            registrarLeituraProfessorLocal("buscarProfessorPorId", "fallback_adapter_missing");
            return Optional.empty();
        }
        try {
            Optional<PessoaProfessorResumoResponse> response = port.buscarProfessorPorId(professorId, escolaId);
            registrarLeituraProfessorLocal(
                    "buscarProfessorPorId",
                    response.isPresent() ? "success" : "fallback_not_found");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraProfessorLocal("buscarProfessorPorId", "fallback_error");
            return Optional.empty();
        }
    }

    public List<PessoaProfessorResumoResponse> listarProfessoresPorEscola(UUID escolaId) {
        var decision = readRoutingPolicy.registrarDecisaoLeituraProfessorResumo();
        if (!decision.localReadEligible()) {
            registrarLeituraProfessorLocal("listarProfessoresPorEscola", "fallback_guard_blocked");
            return List.of();
        }
        PessoaProfessorResumoPort port = professorPortProvider.getIfAvailable();
        if (port == null) {
            registrarLeituraProfessorLocal("listarProfessoresPorEscola", "fallback_adapter_missing");
            return List.of();
        }
        try {
            List<PessoaProfessorResumoResponse> response = port.listarProfessoresPorEscola(escolaId);
            registrarLeituraProfessorLocal("listarProfessoresPorEscola", "success");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraProfessorLocal("listarProfessoresPorEscola", "fallback_error");
            return List.of();
        }
    }

    private void registrarLeituraProfessorLocal(String operation, String result) {
        meterRegistry.counter(
                "people.professor.reads",
                "operation", operation,
                "result", result)
                .increment();
    }
}


