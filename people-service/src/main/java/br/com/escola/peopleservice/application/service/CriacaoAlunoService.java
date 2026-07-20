package br.com.escola.peopleservice.application.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.AlunoCriacaoRequest;
import br.com.escola.peopleservice.application.exception.InvalidRequestContextException;
import br.com.escola.peopleservice.application.model.AlunoCriado;
import br.com.escola.peopleservice.application.model.AlunoNovo;
import br.com.escola.peopleservice.application.model.EscolaPessoa;
import br.com.escola.peopleservice.application.port.in.CriarAlunoUseCase;
import br.com.escola.peopleservice.application.port.out.AlunoCriacaoPort;
import br.com.escola.peopleservice.application.port.out.EscolaPessoaPort;

@Service
public class CriacaoAlunoService implements CriarAlunoUseCase {

    private final AlunoCriacaoPort alunoCriacaoPort;
    private final EscolaPessoaPort escolaPessoaPort;

    public CriacaoAlunoService(AlunoCriacaoPort alunoCriacaoPort, EscolaPessoaPort escolaPessoaPort) {
        this.alunoCriacaoPort = alunoCriacaoPort;
        this.escolaPessoaPort = escolaPessoaPort;
    }

    @Override
    public AlunoCriado criar(
            AlunoCriacaoRequest request,
            String authorization,
            InternalRequestContext context) {
        UUID escolaId = request.escolaId() == null ? context.escolaId() : request.escolaId();
        if (!context.escolaId().equals(escolaId)) {
            throw new InvalidRequestContextException("Escola do aluno difere do contexto autenticado");
        }
        validarEndereco(request);
        EscolaPessoa escola = escolaPessoaPort.buscar(escolaId, authorization, context);
        if (!escola.ativa()) {
            throw new IllegalArgumentException("Escola inativa nao permite cadastro de aluno");
        }
        return alunoCriacaoPort.criar(toModel(request, escola));
    }

    private AlunoNovo toModel(AlunoCriacaoRequest request, EscolaPessoa escola) {
        return new AlunoNovo(
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
                escola.id(),
                escola.nome());
    }

    private void validarEndereco(AlunoCriacaoRequest request) {
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
