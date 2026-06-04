package br.com.escola.matricula.domain.exception;

import java.util.UUID;

public class MatriculaDocumentoExigidoDuplicadoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MatriculaDocumentoExigidoDuplicadoException(UUID tipoMatriculaId, UUID tipoDocumentoId) {
        super("Documento exigido já configurado para o tipo de matrícula "
                + tipoMatriculaId + " e tipo de documento " + tipoDocumentoId);
    }
}
