package br.com.escola.shared.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import br.com.escola.enrollment.domain.exception.MatriculaAlunoNaoEncontradoException;
import br.com.escola.enrollment.domain.exception.MatriculaPeriodoNaoEncontradoException;
import br.com.escola.enrollment.domain.exception.MatriculaStatusInvalidoException;
import br.com.escola.enrollment.domain.exception.MatriculaTurmaNaoEncontradaException;
import br.com.escola.enrollment.domain.exception.TurmaPeriodoInconsistenteException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler extends BaseApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<ApiFieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiFieldError(error.getField(), error.getDefaultMessage()))
                .toList();

        return buildError(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Dados de entrada inválidos",
                request,
                fieldErrors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String message = "Parâmetro inválido: %s".formatted(ex.getName());
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Corpo da requisição inválido", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(IllegalArgumentException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaAlunoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaAlunoNotFound(
            MatriculaAlunoNaoEncontradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaTurmaNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaTurmaNotFound(
            MatriculaTurmaNaoEncontradaException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaPeriodoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaPeriodoNotFound(
            MatriculaPeriodoNaoEncontradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaStatusInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaStatusInvalido(
            MatriculaStatusInvalidoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(TurmaPeriodoInconsistenteException.class)
    public ResponseEntity<ApiErrorResponse> handleTurmaPeriodoInconsistente(
            TurmaPeriodoInconsistenteException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Erro interno não tratado",
                request);
    }
}
