package br.com.escola.matricula.application.port.internal;

import java.util.UUID;

import br.com.escola.matricula.adapter.in.web.MatriculaDocumentoEntregueResponse;
import br.com.escola.matricula.application.dto.internal.RegistrarMatriculaDocumentoEntregueSolicitacao;

public interface MatriculaDocumentoEntreguePort {

    MatriculaDocumentoEntregueResponse registrarDocumentoEntregue(
            UUID matriculaId,
            RegistrarMatriculaDocumentoEntregueSolicitacao solicitacao);
}
