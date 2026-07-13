package br.com.escola.planningaiservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.BibliotecaConteudoPedagogicoResponse;
import br.com.escola.planningaiservice.application.dto.PlanejamentoIaInteracaoResponse;
import br.com.escola.planningaiservice.application.port.in.PlanningAiReadUseCase;
import br.com.escola.planningaiservice.application.port.out.PlanningAiReadPort;

@Service
public class PlanningAiReadService implements PlanningAiReadUseCase {

    private final PlanningAiReadPort planningAiReadPort;

    public PlanningAiReadService(PlanningAiReadPort planningAiReadPort) {
        this.planningAiReadPort = planningAiReadPort;
    }

    @Override
    public List<BibliotecaConteudoPedagogicoResponse> listarBiblioteca(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema) {
        return planningAiReadPort.listarBiblioteca(
                authorization,
                context,
                professorId,
                disciplinaId,
                tipoConteudo,
                tema);
    }

    @Override
    public List<PlanejamentoIaInteracaoResponse> listarInteracoes(
            String authorization,
            InternalRequestContext context,
            UUID planejamentoId) {
        return planningAiReadPort.listarInteracoes(
                authorization,
                context,
                planejamentoId);
    }
}
