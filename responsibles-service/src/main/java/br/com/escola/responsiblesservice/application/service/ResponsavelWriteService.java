package br.com.escola.responsiblesservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import br.com.escola.responsiblesservice.application.context.InternalRequestContext;
import br.com.escola.responsiblesservice.application.dto.AtualizarResponsavelCommand;
import br.com.escola.responsiblesservice.application.dto.CadastrarResponsavelCommand;
import br.com.escola.responsiblesservice.application.dto.ExclusaoResponsavelResultado;
import br.com.escola.responsiblesservice.application.dto.ResponsavelReadModelResponse;
import br.com.escola.responsiblesservice.application.exception.DadosResponsavelInvalidosException;
import br.com.escola.responsiblesservice.application.exception.ResponsavelNaoEncontradoException;
import br.com.escola.responsiblesservice.application.exception.ResponsavelComAlunoVinculadoException;
import br.com.escola.responsiblesservice.application.port.in.ResponsavelWriteUseCase;
import br.com.escola.responsiblesservice.application.port.out.ResponsavelWritePort;

@Service
public class ResponsavelWriteService implements ResponsavelWriteUseCase {

    private final ResponsavelWritePort responsavelWritePort;

    public ResponsavelWriteService(ResponsavelWritePort responsavelWritePort) {
        this.responsavelWritePort = responsavelWritePort;
    }

    @Override
    public ResponsavelReadModelResponse criar(CadastrarResponsavelCommand command, InternalRequestContext context) {
        requireEscola(context);
        return responsavelWritePort.criar(normalizar(command), context.escolaId());
    }

    @Override
    public ResponsavelReadModelResponse atualizar(
            UUID responsavelId,
            AtualizarResponsavelCommand command,
            InternalRequestContext context) {
        requireEscola(context);
        if (responsavelId == null) {
            throw new DadosResponsavelInvalidosException("Identificador do responsavel e obrigatorio");
        }
        return responsavelWritePort.atualizar(responsavelId, normalizar(command), context.escolaId())
                .orElseThrow(ResponsavelNaoEncontradoException::new);
    }

    @Override
    public void excluir(UUID responsavelId, InternalRequestContext context) {
        requireEscola(context);
        if (responsavelId == null) {
            throw new DadosResponsavelInvalidosException("Identificador do responsavel e obrigatorio");
        }
        ExclusaoResponsavelResultado resultado = responsavelWritePort.excluir(responsavelId, context.escolaId());
        if (resultado == ExclusaoResponsavelResultado.NAO_ENCONTRADO) {
            throw new ResponsavelNaoEncontradoException();
        }
        if (resultado == ExclusaoResponsavelResultado.POSSUI_ALUNO_VINCULADO) {
            throw new ResponsavelComAlunoVinculadoException();
        }
    }

    private CadastrarResponsavelCommand normalizar(CadastrarResponsavelCommand command) {
        if (command == null) {
            throw new DadosResponsavelInvalidosException("Dados do responsavel sao obrigatorios");
        }

        String nomeCompleto = required(command.nomeCompleto(), "Nome completo");
        String cpf = onlyDigits(required(command.cpf(), "CPF"));
        if (cpf.length() != 11) {
            throw new DadosResponsavelInvalidosException("CPF deve conter 11 digitos");
        }

        String email = trim(command.email());
        if (email != null && !email.contains("@")) {
            throw new DadosResponsavelInvalidosException("E-mail invalido");
        }

        return new CadastrarResponsavelCommand(
                nomeCompleto,
                cpf,
                email,
                trim(command.telefone()),
                trim(command.rg()),
                trim(command.cep()),
                trim(command.logradouro()),
                trim(command.numero()),
                trim(command.complemento()),
                trim(command.bairro()),
                trim(command.cidade()),
                trim(command.uf()));
    }

    private AtualizarResponsavelCommand normalizar(AtualizarResponsavelCommand command) {
        if (command == null) {
            throw new DadosResponsavelInvalidosException("Dados do responsavel sao obrigatorios");
        }
        CadastrarResponsavelCommand normalizado = normalizar(new CadastrarResponsavelCommand(
                command.nomeCompleto(), command.cpf(), command.email(), command.telefone(), command.rg(), command.cep(),
                command.logradouro(), command.numero(), command.complemento(), command.bairro(), command.cidade(), command.uf()));
        return new AtualizarResponsavelCommand(
                normalizado.nomeCompleto(), normalizado.cpf(), normalizado.email(), normalizado.telefone(), normalizado.rg(),
                normalizado.cep(), normalizado.logradouro(), normalizado.numero(), normalizado.complemento(),
                normalizado.bairro(), normalizado.cidade(), normalizado.uf());
    }

    private void requireEscola(InternalRequestContext context) {
        if (context == null || context.escolaId() == null) {
            throw new DadosResponsavelInvalidosException("Escola do contexto interno e obrigatoria");
        }
    }

    private String required(String value, String field) {
        String normalized = trim(value);
        if (normalized == null) {
            throw new DadosResponsavelInvalidosException(field + " e obrigatorio");
        }
        return normalized;
    }

    private String onlyDigits(String value) {
        return value.replaceAll("\\D", "");
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
