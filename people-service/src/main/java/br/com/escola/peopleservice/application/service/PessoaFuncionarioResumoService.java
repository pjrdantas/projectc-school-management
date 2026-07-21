package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PessoaFuncionarioResumoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaFuncionarioResumoPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PessoaFuncionarioResumoService {

    private final ObjectProvider<PessoaFuncionarioResumoPort> funcionarioPortProvider;
    private final PeopleDataAccessPolicy readRoutingPolicy;
    private final MeterRegistry meterRegistry;

    public PessoaFuncionarioResumoService(
            ObjectProvider<PessoaFuncionarioResumoPort> funcionarioPortProvider,
            PeopleDataAccessPolicy readRoutingPolicy,
            MeterRegistry meterRegistry) {
        this.funcionarioPortProvider = funcionarioPortProvider;
        this.readRoutingPolicy = readRoutingPolicy;
        this.meterRegistry = meterRegistry;
    }

    public Optional<PessoaFuncionarioResumoResponse> buscarFuncionarioPorId(
            UUID funcionarioId,
            UUID escolaId) {
        var decision = readRoutingPolicy.registrarDecisaoLeituraFuncionarioResumo();
        if (!decision.localReadEligible()) {
            registrarLeituraFuncionarioLocal("buscarFuncionarioPorId", "fallback_guard_blocked");
            return Optional.empty();
        }
        PessoaFuncionarioResumoPort port = funcionarioPortProvider.getIfAvailable();
        if (port == null) {
            registrarLeituraFuncionarioLocal("buscarFuncionarioPorId", "fallback_adapter_missing");
            return Optional.empty();
        }
        try {
            Optional<PessoaFuncionarioResumoResponse> response = port.buscarFuncionarioPorId(funcionarioId, escolaId);
            registrarLeituraFuncionarioLocal(
                    "buscarFuncionarioPorId",
                    response.isPresent() ? "success" : "fallback_not_found");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraFuncionarioLocal("buscarFuncionarioPorId", "fallback_error");
            return Optional.empty();
        }
    }

    public List<PessoaFuncionarioResumoResponse> listarFuncionariosAtivosPorEscola(UUID escolaId) {
        var decision = readRoutingPolicy.registrarDecisaoLeituraFuncionarioResumo();
        if (!decision.localReadEligible()) {
            registrarLeituraFuncionarioLocal("listarFuncionariosAtivosPorEscola", "fallback_guard_blocked");
            return List.of();
        }
        PessoaFuncionarioResumoPort port = funcionarioPortProvider.getIfAvailable();
        if (port == null) {
            registrarLeituraFuncionarioLocal("listarFuncionariosAtivosPorEscola", "fallback_adapter_missing");
            return List.of();
        }
        try {
            List<PessoaFuncionarioResumoResponse> response = port.listarFuncionariosAtivosPorEscola(escolaId);
            registrarLeituraFuncionarioLocal("listarFuncionariosAtivosPorEscola", "success");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraFuncionarioLocal("listarFuncionariosAtivosPorEscola", "fallback_error");
            return List.of();
        }
    }

    private void registrarLeituraFuncionarioLocal(String operation, String result) {
        meterRegistry.counter(
                "people.funcionario.reads",
                "operation", operation,
                "result", result)
                .increment();
    }
}


