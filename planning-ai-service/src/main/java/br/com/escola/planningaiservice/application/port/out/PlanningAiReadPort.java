package br.com.escola.planningaiservice.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.BibliotecaConteudoPedagogicoResponse;
import br.com.escola.planningaiservice.application.dto.ConteudoIaResponse;
import br.com.escola.planningaiservice.application.dto.ConteudoIaVersaoResponse;
import br.com.escola.planningaiservice.application.dto.PlanejamentoIaInteracaoResponse;

public interface PlanningAiReadPort {

    List<BibliotecaConteudoPedagogicoResponse> listarBiblioteca(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema);

    List<PlanejamentoIaInteracaoResponse> listarInteracoes(
            String authorization,
            InternalRequestContext context,
            UUID planejamentoId);

    List<ConteudoIaResponse> listarConteudos(
            String authorization,
            InternalRequestContext context,
            UUID planejamentoId);

    ConteudoIaResponse buscarConteudo(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId);

    List<ConteudoIaVersaoResponse> listarVersoes(
            String authorization,
            InternalRequestContext context,
            UUID conteudoId);
}
