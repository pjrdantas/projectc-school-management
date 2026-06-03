package br.com.escola.compartilhado.exception;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.escola.aluno.adapter.in.web.AlunoController;
import br.com.escola.aluno.domain.exception.AlunoJaCadastradoException;
import br.com.escola.aluno.domain.exception.AlunoNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackageClasses = AlunoController.class)
public class AlunoApiExceptionHandler extends BaseApiExceptionHandler {

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

    @ExceptionHandler(AlunoJaCadastradoException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(AlunoJaCadastradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(AlunoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(AlunoNaoEncontradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        return buildError(
                HttpStatus.CONFLICT,
                "BUSINESS_CONFLICT",
                "Não é possível excluir o aluno porque ele possui matrícula vinculada.",
                request);
    }
}
