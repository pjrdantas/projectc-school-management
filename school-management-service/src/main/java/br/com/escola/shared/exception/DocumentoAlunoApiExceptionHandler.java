package br.com.escola.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.escola.studentdocument.adapter.in.web.DocumentoAlunoController;
import br.com.escola.shared.document.domain.exception.DocumentoInvalidoException;
import br.com.escola.shared.document.domain.exception.DocumentoNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackageClasses = DocumentoAlunoController.class)
public class DocumentoAlunoApiExceptionHandler extends BaseApiExceptionHandler {

    @ExceptionHandler(DocumentoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleDocumentoNaoEncontrado(DocumentoNaoEncontradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(DocumentoInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handleDocumentoInvalido(DocumentoInvalidoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }
}
