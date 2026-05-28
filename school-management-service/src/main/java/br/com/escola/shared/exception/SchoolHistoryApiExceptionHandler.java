package br.com.escola.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.escola.schoolhistory.adapter.in.web.DisciplinaController;
import br.com.escola.schoolhistory.adapter.in.web.HistoricoEscolarController;
import br.com.escola.schoolhistory.domain.exception.DisciplinaNaoEncontradaException;
import br.com.escola.schoolhistory.domain.exception.HistoricoEscolarInvalidoException;
import br.com.escola.schoolhistory.domain.exception.HistoricoEscolarNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackageClasses = { DisciplinaController.class, HistoricoEscolarController.class })
public class SchoolHistoryApiExceptionHandler extends BaseApiExceptionHandler {

    @ExceptionHandler(DisciplinaNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleDisciplinaNaoEncontrada(DisciplinaNaoEncontradaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(HistoricoEscolarNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleHistoricoNaoEncontrado(HistoricoEscolarNaoEncontradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(HistoricoEscolarInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handleHistoricoInvalido(HistoricoEscolarInvalidoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }
}
