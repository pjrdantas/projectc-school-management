package br.com.escola.professorservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.FuncionarioElegivelResponse;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.ResumoResponse;
import br.com.escola.professorservice.application.exception.DownstreamUnavailableException;
import br.com.escola.professorservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.professorservice.application.port.in.ConsultaUseCase;
import br.com.escola.professorservice.application.port.out.ConsultaPort;
import br.com.escola.professorservice.application.port.out.LeituraLocalPort;

@Service
public class ConsultaService implements ConsultaUseCase {

    private final ConsultaPort professorReadPort;
    private final LeituraLocalPort professorShadowLocalReadPort;

    public ConsultaService(
            ConsultaPort professorReadPort,
            LeituraLocalPort professorShadowLocalReadPort) {
        this.professorReadPort = professorReadPort;
        this.professorShadowLocalReadPort = professorShadowLocalReadPort;
    }

    @Override
    public List<ResumoResponse> listarProfessores(String authorization, InternalRequestContext context) {
        var readDecision = professorShadowLocalReadPort.decidirListarProfessores(context);
        if (readDecision.useLocal()) {
            return professorShadowLocalReadPort.listarProfessores(context);
        }
        if (readDecision.cutoverBlocked()) {
            throw new DownstreamUnavailableException(
                    "Persistencia local de professores ainda nao esta apta para cutover");
        }
        return professorReadPort.listarProfessores(authorization, context);
    }

    @Override
    public ResumoResponse buscarProfessorPorId(String authorization, InternalRequestContext context, UUID professorId) {
        var local = professorShadowLocalReadPort.buscarProfessorPorId(context, professorId);
        if (local.isPresent()) {
            return local.orElseThrow();
        }
        if (professorShadowLocalReadPort.supportsBuscarProfessorPorIdCutover(context)) {
            throw new RecursoNaoEncontradoException("Professor não encontrado");
        }
        return professorReadPort.buscarProfessorPorId(authorization, context, professorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor não encontrado"));
    }

    @Override
    public List<AlocacaoResponse> listarAlocacoes(
            String authorization,
            InternalRequestContext context,
            UUID professorId) {
        var readDecision = professorShadowLocalReadPort.decidirListarAlocacoes(context, professorId);
        if (readDecision.useLocal()) {
            return professorShadowLocalReadPort.listarAlocacoes(context, professorId);
        }
        if (readDecision.cutoverBlocked()) {
            throw new DownstreamUnavailableException(
                    "Persistencia local de alocacoes de professor ainda nao esta apta para cutover");
        }
        return professorReadPort.listarAlocacoes(authorization, context, professorId);
    }

    @Override
    public List<AlocacaoResponse> listarProfessoresPorTurma(
            String authorization,
            InternalRequestContext context,
            UUID turmaId) {
        var readDecision = professorShadowLocalReadPort.decidirListarProfessoresPorTurma(context, turmaId);
        if (readDecision.useLocal()) {
            return professorShadowLocalReadPort.listarProfessoresPorTurma(context, turmaId);
        }
        if (readDecision.cutoverBlocked()) {
            throw new DownstreamUnavailableException(
                    "Persistencia local de alocacoes por turma ainda nao esta apta para cutover");
        }
        return professorReadPort.listarProfessoresPorTurma(authorization, context, turmaId);
    }

    @Override
    public List<FuncionarioElegivelResponse> listarFuncionariosElegiveis(
            String authorization,
            InternalRequestContext context) {
        return professorReadPort.listarFuncionariosElegiveis(authorization, context);
    }
}

