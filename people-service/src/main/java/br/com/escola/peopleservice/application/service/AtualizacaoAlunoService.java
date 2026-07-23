package br.com.escola.peopleservice.application.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.AlunoAtualizacaoRequest;
import br.com.escola.peopleservice.application.exception.InvalidRequestContextException;
import br.com.escola.peopleservice.application.model.AlunoAlteracao;
import br.com.escola.peopleservice.application.model.AlunoAtualizado;
import br.com.escola.peopleservice.application.port.in.AtualizarAlunoUseCase;
import br.com.escola.peopleservice.application.port.out.AlunoAtualizacaoPort;

@Service
public class AtualizacaoAlunoService implements AtualizarAlunoUseCase {

    private final AlunoAtualizacaoPort alunoAtualizacaoPort;

    public AtualizacaoAlunoService(AlunoAtualizacaoPort alunoAtualizacaoPort) {
        this.alunoAtualizacaoPort = alunoAtualizacaoPort;
    }

    @Override
    public AlunoAtualizado atualizar(
            UUID alunoId,
            AlunoAtualizacaoRequest request,
            InternalRequestContext context) {
        UUID escolaId = request.escolaId() == null ? context.escolaId() : request.escolaId();
        if (!context.escolaId().equals(escolaId)) {
            throw new InvalidRequestContextException("Escola do aluno difere do contexto autenticado");
        }
        validarEndereco(request);
        return alunoAtualizacaoPort.atualizar(alunoId, toModel(request, escolaId));
    }

    private AlunoAlteracao toModel(AlunoAtualizacaoRequest request, UUID escolaId) {
        return new AlunoAlteracao(
                request.nomeCompleto().trim(),
                request.cpf().trim(),
                normalizeLower(request.email()),
                normalize(request.telefone()),
                request.dataNascimento(),
                normalize(request.rg()),
                normalize(request.orgaoEmissorRg()),
                normalizeUpper(request.ufRg()),
                normalize(request.nacionalidade()),
                normalize(request.naturalidade()),
                normalizeUpper(request.sexo()),
                normalize(request.nomeSocial()),
                normalize(request.cep()),
                normalize(request.logradouro()),
                normalize(request.numero()),
                normalize(request.complemento()),
                normalize(request.bairro()),
                normalize(request.cidade()),
                normalizeUpper(request.uf()),
                request.statusAluno() == null || request.statusAluno().isBlank()
                        ? "ATIVO"
                        : request.statusAluno().trim().toUpperCase(Locale.ROOT),
                escolaId);
    }

    private void validarEndereco(AlunoAtualizacaoRequest request) {
        if (request.cep() != null && !request.cep().isBlank()
                && (request.numero() == null || request.numero().isBlank())) {
            throw new IllegalArgumentException("numero e obrigatorio quando cep e informado");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeLower(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeUpper(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }
}
