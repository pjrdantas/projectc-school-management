package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PessoaFuncionarioInternalSummaryResponse;
import br.com.escola.peopleservice.application.port.out.PeopleFuncionarioInternalSummaryPort;

@Service
public class PeopleFuncionarioInternalSummaryService {

    private final ObjectProvider<PeopleFuncionarioInternalSummaryPort> funcionarioInternalSummaryPortProvider;

    public PeopleFuncionarioInternalSummaryService(
            ObjectProvider<PeopleFuncionarioInternalSummaryPort> funcionarioInternalSummaryPortProvider) {
        this.funcionarioInternalSummaryPortProvider = funcionarioInternalSummaryPortProvider;
    }

    public Optional<PessoaFuncionarioInternalSummaryResponse> buscarFuncionarioPorId(
            UUID funcionarioId,
            UUID escolaId) {
        PeopleFuncionarioInternalSummaryPort port = funcionarioInternalSummaryPortProvider.getIfAvailable();
        if (port == null) {
            return Optional.empty();
        }
        try {
            return port.buscarFuncionarioPorId(funcionarioId, escolaId);
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    public List<PessoaFuncionarioInternalSummaryResponse> listarFuncionariosAtivosPorEscola(UUID escolaId) {
        PeopleFuncionarioInternalSummaryPort port = funcionarioInternalSummaryPortProvider.getIfAvailable();
        if (port == null) {
            return List.of();
        }
        try {
            return port.listarFuncionariosAtivosPorEscola(escolaId);
        } catch (RuntimeException ex) {
            return List.of();
        }
    }
}
