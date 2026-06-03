package br.com.escola.compartilhado.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.escola.transferencia.adapter.in.web.EscolaOrigemController;
import br.com.escola.transferencia.adapter.in.web.TransferenciaAlunoController;
import br.com.escola.transferencia.domain.exception.EscolaOrigemNaoEncontradaException;
import br.com.escola.transferencia.domain.exception.TransferenciaAlunoInvalidaException;
import br.com.escola.transferencia.domain.exception.TransferenciaAlunoNaoEncontradaException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackageClasses = { EscolaOrigemController.class, TransferenciaAlunoController.class })
public class TransferenciaApiExceptionHandler extends BaseApiExceptionHandler {

    @ExceptionHandler(EscolaOrigemNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleEscolaOrigemNaoEncontrada(EscolaOrigemNaoEncontradaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(TransferenciaAlunoNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleTransferenciaNaoEncontrada(TransferenciaAlunoNaoEncontradaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(TransferenciaAlunoInvalidaException.class)
    public ResponseEntity<ApiErrorResponse> handleTransferenciaInvalida(TransferenciaAlunoInvalidaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }
}
