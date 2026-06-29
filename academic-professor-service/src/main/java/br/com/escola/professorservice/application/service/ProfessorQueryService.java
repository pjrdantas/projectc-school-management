package br.com.escola.professorservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.FuncionarioElegivelResponse;
import br.com.escola.professorservice.application.dto.ProfessorAlocacaoResponse;
import br.com.escola.professorservice.application.dto.ProfessorResumoResponse;
import br.com.escola.professorservice.application.exception.ProfessorServiceResourceNotFoundException;
import br.com.escola.professorservice.application.port.in.ProfessorQueryUseCase;
import br.com.escola.professorservice.application.port.out.ProfessorReadPort;
import br.com.escola.professorservice.application.port.out.ProfessorShadowLocalReadPort;

@Service
public class ProfessorQueryService implements ProfessorQueryUseCase {

    private final ProfessorReadPort professorReadPort;
    private final ProfessorShadowLocalReadPort professorShadowLocalReadPort;

    public ProfessorQueryService(
            ProfessorReadPort professorReadPort,
            ProfessorShadowLocalReadPort professorShadowLocalReadPort) {
        this.professorReadPort = professorReadPort;
        this.professorShadowLocalReadPort = professorShadowLocalReadPort;
    }

    @Override
    public List<ProfessorResumoResponse> listarProfessores(String authorization, InternalRequestContext context) {
        return professorReadPort.listarProfessores(authorization, context);
    }

    @Override
    public ProfessorResumoResponse buscarProfessorPorId(String authorization, InternalRequestContext context, UUID professorId) {
        return professorReadPort.buscarProfessorPorId(authorization, context, professorId)
                .orElseThrow(() -> new ProfessorServiceResourceNotFoundException("Professor não encontrado"));
    }

    @Override
    public List<ProfessorAlocacaoResponse> listarAlocacoes(
            String authorization,
            InternalRequestContext context,
            UUID professorId) {
        if (professorShadowLocalReadPort.supportsListarAlocacoes(context, professorId)) {
            return professorShadowLocalReadPort.listarAlocacoes(context, professorId);
        }
        return professorReadPort.listarAlocacoes(authorization, context, professorId);
    }

    @Override
    public List<ProfessorAlocacaoResponse> listarProfessoresPorTurma(
            String authorization,
            InternalRequestContext context,
            UUID turmaId) {
        if (professorShadowLocalReadPort.supportsListarProfessoresPorTurma(context, turmaId)) {
            return professorShadowLocalReadPort.listarProfessoresPorTurma(context, turmaId);
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
