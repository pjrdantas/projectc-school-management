package br.com.escola.compartilhado.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.escola.historico.adapter.in.web.HistoricoEscolarController;
import br.com.escola.historico.domain.exception.HistoricoEscolarInvalidoException;
import br.com.escola.historico.domain.exception.HistoricoEscolarNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackageClasses = HistoricoEscolarController.class)
public class SchoolHistoryApiExceptionHandler extends BaseApiExceptionHandler {

    @ExceptionHandler(HistoricoEscolarNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleHistoricoNaoEncontrado(HistoricoEscolarNaoEncontradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(HistoricoEscolarInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handleHistoricoInvalido(HistoricoEscolarInvalidoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }
}
