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

@Service
public class ProfessorQueryService implements ProfessorQueryUseCase {

    private final ProfessorReadPort professorReadPort;

    public ProfessorQueryService(ProfessorReadPort professorReadPort) {
        this.professorReadPort = professorReadPort;
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
        return professorReadPort.listarAlocacoes(authorization, context, professorId);
    }

    @Override
    public List<FuncionarioElegivelResponse> listarFuncionariosElegiveis(
            String authorization,
            InternalRequestContext context) {
        return professorReadPort.listarFuncionariosElegiveis(authorization, context);
    }
}
