package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PessoaEnderecoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaEnderecoPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PessoaEnderecoService {

    private final PessoaEnderecoPort enderecoPort;
    private final PeopleDataAccessPolicy readRoutingPolicy;
    private final MeterRegistry meterRegistry;

    public PessoaEnderecoService(
            PessoaEnderecoPort enderecoPort,
            PeopleDataAccessPolicy readRoutingPolicy,
            MeterRegistry meterRegistry) {
        this.enderecoPort = enderecoPort;
        this.readRoutingPolicy = readRoutingPolicy;
        this.meterRegistry = meterRegistry;
    }

    public Optional<PessoaEnderecoResponse> buscarEnderecoPrincipalPorPessoa(UUID pessoaId, UUID escolaId) {
        var decision = readRoutingPolicy.registrarDecisaoLeituraEndereco();
        if (!decision.localReadEligible()) {
            registrarLeituraEnderecoLocal("buscarEnderecoPrincipalPorPessoa", "fallback_guard_blocked");
            return Optional.empty();
        }
        try {
            Optional<PessoaEnderecoResponse> response =
                    enderecoPort.buscarEnderecoPrincipalPorPessoa(pessoaId, escolaId);
            registrarLeituraEnderecoLocal(
                    "buscarEnderecoPrincipalPorPessoa",
                    response.isPresent() ? "success" : "fallback_not_found");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraEnderecoLocal("buscarEnderecoPrincipalPorPessoa", "fallback_error");
            return Optional.empty();
        }
    }

    public List<PessoaEnderecoResponse> listarEnderecosPorPessoa(UUID pessoaId, UUID escolaId) {
        var decision = readRoutingPolicy.registrarDecisaoLeituraEndereco();
        if (!decision.localReadEligible()) {
            registrarLeituraEnderecoLocal("listarEnderecosPorPessoa", "fallback_guard_blocked");
            return List.of();
        }
        try {
            List<PessoaEnderecoResponse> response =
                    enderecoPort.listarEnderecosPorPessoa(pessoaId, escolaId);
            registrarLeituraEnderecoLocal("listarEnderecosPorPessoa", "success");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraEnderecoLocal("listarEnderecosPorPessoa", "fallback_error");
            return List.of();
        }
    }

    private void registrarLeituraEnderecoLocal(String operation, String result) {
        meterRegistry.counter(
                "people.address.reads",
                "operation", operation,
                "result", result)
                .increment();
    }
}


