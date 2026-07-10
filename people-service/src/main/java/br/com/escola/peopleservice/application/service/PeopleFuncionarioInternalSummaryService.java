package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PessoaFuncionarioInternalSummaryResponse;
import br.com.escola.peopleservice.application.port.out.PeopleFuncionarioInternalSummaryPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PeopleFuncionarioInternalSummaryService {

    private final ObjectProvider<PeopleFuncionarioInternalSummaryPort> funcionarioInternalSummaryPortProvider;
    private final PeopleLocalReadCutoverGuard readCutoverGuard;
    private final MeterRegistry meterRegistry;

    public PeopleFuncionarioInternalSummaryService(
            ObjectProvider<PeopleFuncionarioInternalSummaryPort> funcionarioInternalSummaryPortProvider,
            PeopleLocalReadCutoverGuard readCutoverGuard,
            MeterRegistry meterRegistry) {
        this.funcionarioInternalSummaryPortProvider = funcionarioInternalSummaryPortProvider;
        this.readCutoverGuard = readCutoverGuard;
        this.meterRegistry = meterRegistry;
    }

    public Optional<PessoaFuncionarioInternalSummaryResponse> buscarFuncionarioPorId(
            UUID funcionarioId,
            UUID escolaId) {
        var decision = readCutoverGuard.registrarDecisaoLeituraFuncionarioInternalSummary();
        if (!decision.localReadEligible()) {
            registrarLeituraFuncionarioLocal("buscarFuncionarioPorId", "fallback_guard_blocked");
            return Optional.empty();
        }
        PeopleFuncionarioInternalSummaryPort port = funcionarioInternalSummaryPortProvider.getIfAvailable();
        if (port == null) {
            registrarLeituraFuncionarioLocal("buscarFuncionarioPorId", "fallback_adapter_missing");
            return Optional.empty();
        }
        try {
            Optional<PessoaFuncionarioInternalSummaryResponse> response = port.buscarFuncionarioPorId(funcionarioId, escolaId);
            registrarLeituraFuncionarioLocal(
                    "buscarFuncionarioPorId",
                    response.isPresent() ? "success" : "fallback_not_found");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraFuncionarioLocal("buscarFuncionarioPorId", "fallback_error");
            return Optional.empty();
        }
    }

    public List<PessoaFuncionarioInternalSummaryResponse> listarFuncionariosAtivosPorEscola(UUID escolaId) {
        var decision = readCutoverGuard.registrarDecisaoLeituraFuncionarioInternalSummary();
        if (!decision.localReadEligible()) {
            registrarLeituraFuncionarioLocal("listarFuncionariosAtivosPorEscola", "fallback_guard_blocked");
            return List.of();
        }
        PeopleFuncionarioInternalSummaryPort port = funcionarioInternalSummaryPortProvider.getIfAvailable();
        if (port == null) {
            registrarLeituraFuncionarioLocal("listarFuncionariosAtivosPorEscola", "fallback_adapter_missing");
            return List.of();
        }
        try {
            List<PessoaFuncionarioInternalSummaryResponse> response = port.listarFuncionariosAtivosPorEscola(escolaId);
            registrarLeituraFuncionarioLocal("listarFuncionariosAtivosPorEscola", "success");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraFuncionarioLocal("listarFuncionariosAtivosPorEscola", "fallback_error");
            return List.of();
        }
    }

    private void registrarLeituraFuncionarioLocal(String operation, String result) {
        meterRegistry.counter(
                "people.shadow.local.persistence.funcionario.internal.summary.reads",
                "operation", operation,
                "result", result)
                .increment();
    }
}
