package br.com.escola.responsiblesservice.application.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import br.com.escola.responsiblesservice.application.context.InternalRequestContext;
import br.com.escola.responsiblesservice.application.dto.CriacaoAlunoResponsavelResultado;
import br.com.escola.responsiblesservice.application.dto.DesvinculoAlunoResponsavelResultado;
import br.com.escola.responsiblesservice.application.dto.VincularAlunoResponsavelCommand;
import br.com.escola.responsiblesservice.application.exception.AlunoNaoEncontradoException;
import br.com.escola.responsiblesservice.application.exception.AlunoResponsavelVinculoDuplicadoException;
import br.com.escola.responsiblesservice.application.exception.AlunoResponsavelVinculoNaoEncontradoException;
import br.com.escola.responsiblesservice.application.exception.DadosResponsavelInvalidosException;
import br.com.escola.responsiblesservice.application.exception.ResponsavelNaoEncontradoException;
import br.com.escola.responsiblesservice.application.port.in.AlunoResponsavelWriteUseCase;
import br.com.escola.responsiblesservice.application.port.out.AlunoConsultaPort;
import br.com.escola.responsiblesservice.application.port.out.AlunoResponsavelWritePort;

@Service
public class AlunoResponsavelWriteService implements AlunoResponsavelWriteUseCase {

    private static final String PARENTESCO_PADRAO = "RESPONSAVEL_LEGAL";

    private final AlunoConsultaPort alunoConsultaPort;
    private final AlunoResponsavelWritePort alunoResponsavelWritePort;

    public AlunoResponsavelWriteService(
            AlunoConsultaPort alunoConsultaPort,
            AlunoResponsavelWritePort alunoResponsavelWritePort) {
        this.alunoConsultaPort = alunoConsultaPort;
        this.alunoResponsavelWritePort = alunoResponsavelWritePort;
    }

    @Override
    public void vincular(UUID alunoId, VincularAlunoResponsavelCommand command, InternalRequestContext context) {
        requireContext(alunoId, context);
        VincularAlunoResponsavelCommand normalizado = normalizar(command);
        if (!alunoConsultaPort.existeAtivoNaEscola(alunoId, context)) {
            throw new AlunoNaoEncontradoException();
        }

        CriacaoAlunoResponsavelResultado resultado = alunoResponsavelWritePort.vincular(
                alunoId, normalizado, context.escolaId());
        switch (resultado) {
            case CRIADO -> {
                return;
            }
            case RESPONSAVEL_NAO_ENCONTRADO -> throw new ResponsavelNaoEncontradoException();
            case PARENTESCO_NAO_ENCONTRADO ->
                throw new DadosResponsavelInvalidosException("Parentesco nao cadastrado: " + normalizado.parentesco());
            case VINCULO_DUPLICADO -> throw new AlunoResponsavelVinculoDuplicadoException();
        }
    }

    @Override
    public void desvincular(UUID alunoId, UUID responsavelId, InternalRequestContext context) {
        requireContext(alunoId, context);
        if (responsavelId == null) {
            throw new DadosResponsavelInvalidosException("Identificador do responsavel e obrigatorio");
        }
        if (!alunoConsultaPort.existeAtivoNaEscola(alunoId, context)) {
            throw new AlunoNaoEncontradoException();
        }
        if (alunoResponsavelWritePort.desvincular(alunoId, responsavelId, context.escolaId())
                == DesvinculoAlunoResponsavelResultado.VINCULO_NAO_ENCONTRADO) {
            throw new AlunoResponsavelVinculoNaoEncontradoException();
        }
    }

    private VincularAlunoResponsavelCommand normalizar(VincularAlunoResponsavelCommand command) {
        if (command == null || command.responsavelId() == null) {
            throw new DadosResponsavelInvalidosException("Responsavel do vinculo e obrigatorio");
        }
        String parentesco = StringUtils.hasText(command.parentesco())
                ? command.parentesco().trim().toUpperCase(Locale.ROOT)
                : PARENTESCO_PADRAO;
        return new VincularAlunoResponsavelCommand(
                command.responsavelId(),
                parentesco,
                Boolean.TRUE.equals(command.responsavelFinanceiro()),
                Boolean.TRUE.equals(command.responsavelPedagogico()),
                Boolean.TRUE.equals(command.autorizadoRetirar()));
    }

    private void requireContext(UUID alunoId, InternalRequestContext context) {
        if (alunoId == null) {
            throw new DadosResponsavelInvalidosException("Identificador do aluno e obrigatorio");
        }
        if (context == null || context.escolaId() == null) {
            throw new DadosResponsavelInvalidosException("Escola do contexto interno e obrigatoria");
        }
    }
}
