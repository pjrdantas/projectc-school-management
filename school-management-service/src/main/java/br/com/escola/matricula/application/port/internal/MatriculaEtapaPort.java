package br.com.escola.matricula.application.port.internal;

import java.util.UUID;

import br.com.escola.matricula.application.dto.MatriculaEtapaOutput;
import br.com.escola.matricula.application.dto.internal.AtualizarMatriculaEtapaStatusSolicitacao;

public interface MatriculaEtapaPort {

    MatriculaEtapaOutput atualizarStatusEtapa(
            UUID matriculaId,
            UUID etapaId,
            AtualizarMatriculaEtapaStatusSolicitacao solicitacao);
}
