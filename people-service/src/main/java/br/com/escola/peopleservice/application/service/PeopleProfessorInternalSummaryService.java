package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PessoaProfessorInternalSummaryResponse;
import br.com.escola.peopleservice.application.port.out.PeopleProfessorInternalSummaryPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PeopleProfessorInternalSummaryService {

    private final ObjectProvider<PeopleProfessorInternalSummaryPort> professorInternalSummaryPortProvider;
    private final MeterRegistry meterRegistry;

    public PeopleProfessorInternalSummaryService(
            ObjectProvider<PeopleProfessorInternalSummaryPort> professorInternalSummaryPortProvider,
            MeterRegistry meterRegistry) {
        this.professorInternalSummaryPortProvider = professorInternalSummaryPortProvider;
        this.meterRegistry = meterRegistry;
    }

    public Optional<PessoaProfessorInternalSummaryResponse> buscarProfessorPorId(
            UUID professorId,
            UUID escolaId) {
        PeopleProfessorInternalSummaryPort port = professorInternalSummaryPortProvider.getIfAvailable();
        if (port == null) {
            registrarLeituraProfessorLocal("buscarProfessorPorId", "fallback_adapter_missing");
            return Optional.empty();
        }
        try {
            Optional<PessoaProfessorInternalSummaryResponse> response = port.buscarProfessorPorId(professorId, escolaId);
            registrarLeituraProfessorLocal(
                    "buscarProfessorPorId",
                    response.isPresent() ? "success" : "fallback_not_found");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraProfessorLocal("buscarProfessorPorId", "fallback_error");
            return Optional.empty();
        }
    }

    public List<PessoaProfessorInternalSummaryResponse> listarProfessoresPorEscola(UUID escolaId) {
        PeopleProfessorInternalSummaryPort port = professorInternalSummaryPortProvider.getIfAvailable();
        if (port == null) {
            registrarLeituraProfessorLocal("listarProfessoresPorEscola", "fallback_adapter_missing");
            return List.of();
        }
        try {
            List<PessoaProfessorInternalSummaryResponse> response = port.listarProfessoresPorEscola(escolaId);
            registrarLeituraProfessorLocal("listarProfessoresPorEscola", "success");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraProfessorLocal("listarProfessoresPorEscola", "fallback_error");
            return List.of();
        }
    }

    private void registrarLeituraProfessorLocal(String operation, String result) {
        meterRegistry.counter(
                "people.shadow.local.persistence.professor.internal.summary.reads",
                "operation", operation,
                "result", result)
                .increment();
    }
}
