package br.com.escola.shared.document.application.usecase;

import java.util.Locale;

import br.com.escola.shared.document.domain.EntidadeDocumentalTipo;
import br.com.escola.shared.document.domain.TipoDocumento;
import br.com.escola.shared.document.domain.exception.DocumentoInvalidoException;

final class DocumentoUseCaseSupport {

    private DocumentoUseCaseSupport() {
    }

    static EntidadeDocumentalTipo parseEntidadeTipo(String value) {
        if (value == null || value.isBlank()) {
            throw new DocumentoInvalidoException("Tipo da entidade documental é obrigatório");
        }
        try {
            return EntidadeDocumentalTipo.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DocumentoInvalidoException("Tipo da entidade documental inválido: " + value);
        }
    }

    static TipoDocumento parseTipoDocumento(String value) {
        if (value == null || value.isBlank()) {
            throw new DocumentoInvalidoException("Tipo de documento é obrigatório");
        }
        try {
            return TipoDocumento.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DocumentoInvalidoException("Tipo de documento inválido: " + value);
        }
    }

    static void validarCampoObrigatorio(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DocumentoInvalidoException(message);
        }
    }
}
