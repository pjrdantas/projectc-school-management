package br.com.escola.compartilhado.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.escola.catalogo.adapter.in.web.controller.PeriodoLetivoController;
import br.com.escola.catalogo.adapter.in.web.controller.SerieController;
import br.com.escola.catalogo.adapter.in.web.controller.TurmaController;
import br.com.escola.catalogo.adapter.in.web.controller.TurnoController;
import br.com.escola.catalogo.domain.exception.PeriodoLetivoInvalidoException;
import br.com.escola.catalogo.domain.exception.PeriodoLetivoNaoEncontradoException;
import br.com.escola.catalogo.domain.exception.SerieNaoEncontradaException;
import br.com.escola.catalogo.domain.exception.TurmaCapacidadeInvalidaException;
import br.com.escola.catalogo.domain.exception.TurmaJaCadastradaException;
import br.com.escola.catalogo.domain.exception.TurmaNaoEncontradaException;
import br.com.escola.catalogo.domain.exception.TurnoNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackageClasses = {
        PeriodoLetivoController.class,
        SerieController.class,
        TurmaController.class,
        TurnoController.class
})
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

    @ExceptionHandler(SerieNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleSerieNotFound(SerieNaoEncontradaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(TurnoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleTurnoNotFound(TurnoNaoEncontradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(TurmaJaCadastradaException.class)
    public ResponseEntity<ApiErrorResponse> handleTurmaDuplicada(TurmaJaCadastradaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(TurmaCapacidadeInvalidaException.class)
    public ResponseEntity<ApiErrorResponse> handleTurmaCapacidadeInvalida(TurmaCapacidadeInvalidaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
    }
}
