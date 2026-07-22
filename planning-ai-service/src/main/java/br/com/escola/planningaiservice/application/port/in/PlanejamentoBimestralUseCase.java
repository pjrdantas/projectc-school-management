package br.com.escola.planningaiservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.PlanejamentoBimestralRequest;
import br.com.escola.planningaiservice.application.dto.PlanejamentoBimestralResponse;
import br.com.escola.planningaiservice.application.dto.PlanejamentoBimestralStatusRequest;
import br.com.escola.planningaiservice.application.dto.PlanejamentoBimestralAulaRequest;
import br.com.escola.planningaiservice.application.dto.PlanejamentoBimestralAulaResponse;
import br.com.escola.planningaiservice.application.dto.PlanejamentoBimestralAvaliacaoRequest;
import br.com.escola.planningaiservice.application.dto.PlanejamentoBimestralAvaliacaoResponse;

/** Internal contract for the bimonthly planning aggregate. */
public interface PlanejamentoBimestralUseCase {

    PlanejamentoBimestralResponse criar(InternalRequestContext context, PlanejamentoBimestralRequest request);

    List<PlanejamentoBimestralResponse> listar(
            InternalRequestContext context,
            UUID professorTurmaDisciplinaId,
            UUID periodoAvaliativoId);

    PlanejamentoBimestralResponse buscarPorId(InternalRequestContext context, UUID planejamentoId);

    PlanejamentoBimestralResponse atualizar(
            InternalRequestContext context,
            UUID planejamentoId,
            PlanejamentoBimestralRequest request);

    PlanejamentoBimestralResponse alterarStatus(
            InternalRequestContext context,
            UUID planejamentoId,
            PlanejamentoBimestralStatusRequest request);

    PlanejamentoBimestralAulaResponse adicionarAula(
            InternalRequestContext context,
            UUID planejamentoId,
            PlanejamentoBimestralAulaRequest request);

    PlanejamentoBimestralAvaliacaoResponse adicionarAvaliacao(
            InternalRequestContext context,
            UUID planejamentoId,
            PlanejamentoBimestralAvaliacaoRequest request);

}
