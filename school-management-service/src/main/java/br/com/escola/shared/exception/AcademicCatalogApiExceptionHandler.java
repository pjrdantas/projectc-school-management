package br.com.escola.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.escola.academiccatalog.adapter.in.web.controller.PeriodoLetivoController;
import br.com.escola.academiccatalog.domain.exception.PeriodoLetivoInvalidoException;
import br.com.escola.academiccatalog.domain.exception.PeriodoLetivoNaoEncontradoException;
import br.com.escola.academiccatalog.domain.exception.TurmaJaCadastradaException;
import br.com.escola.academiccatalog.domain.exception.TurmaNaoEncontradaException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackageClasses = PeriodoLetivoController.class)
public class AcademicCatalogApiExceptionHandler extends BaseApiExceptionHandler {

    @ExceptionHandler(PeriodoLetivoInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handlePeriodoInvalido(PeriodoLetivoInvalidoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
    }

    @ExceptionHandler(PeriodoLetivoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handlePeriodoNotFound(PeriodoLetivoNaoEncontradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(TurmaNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleTurmaNotFound(TurmaNaoEncontradaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(TurmaJaCadastradaException.class)
    public ResponseEntity<ApiErrorResponse> handleTurmaDuplicada(TurmaJaCadastradaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }
}
