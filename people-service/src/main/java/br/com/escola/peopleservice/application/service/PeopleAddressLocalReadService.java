package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PessoaEnderecoLocalReadResponse;
import br.com.escola.peopleservice.application.port.out.PeopleAddressLocalReadPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PeopleAddressLocalReadService {

    private final PeopleAddressLocalReadPort addressLocalReadPort;
    private final PeopleLocalReadCutoverGuard readCutoverGuard;
    private final MeterRegistry meterRegistry;

    public PeopleAddressLocalReadService(
            PeopleAddressLocalReadPort addressLocalReadPort,
            PeopleLocalReadCutoverGuard readCutoverGuard,
            MeterRegistry meterRegistry) {
        this.addressLocalReadPort = addressLocalReadPort;
        this.readCutoverGuard = readCutoverGuard;
        this.meterRegistry = meterRegistry;
    }

    public Optional<PessoaEnderecoLocalReadResponse> buscarEnderecoPrincipalPorPessoa(UUID pessoaId, UUID escolaId) {
        var decision = readCutoverGuard.registrarDecisaoLeituraEndereco();
        if (!decision.localReadEligible()) {
            registrarLeituraEnderecoLocal("buscarEnderecoPrincipalPorPessoa", "fallback_guard_blocked");
            return Optional.empty();
        }
        try {
            Optional<PessoaEnderecoLocalReadResponse> response =
                    addressLocalReadPort.buscarEnderecoPrincipalPorPessoa(pessoaId, escolaId);
            registrarLeituraEnderecoLocal(
                    "buscarEnderecoPrincipalPorPessoa",
                    response.isPresent() ? "success" : "fallback_not_found");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraEnderecoLocal("buscarEnderecoPrincipalPorPessoa", "fallback_error");
            return Optional.empty();
        }
    }

    public List<PessoaEnderecoLocalReadResponse> listarEnderecosPorPessoa(UUID pessoaId, UUID escolaId) {
        var decision = readCutoverGuard.registrarDecisaoLeituraEndereco();
        if (!decision.localReadEligible()) {
            registrarLeituraEnderecoLocal("listarEnderecosPorPessoa", "fallback_guard_blocked");
            return List.of();
        }
        try {
            List<PessoaEnderecoLocalReadResponse> response =
                    addressLocalReadPort.listarEnderecosPorPessoa(pessoaId, escolaId);
            registrarLeituraEnderecoLocal("listarEnderecosPorPessoa", "success");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraEnderecoLocal("listarEnderecosPorPessoa", "fallback_error");
            return List.of();
        }
    }

    private void registrarLeituraEnderecoLocal(String operation, String result) {
        meterRegistry.counter(
                "people.shadow.local.persistence.address.reads",
                "operation", operation,
                "result", result)
                .increment();
    }
}
