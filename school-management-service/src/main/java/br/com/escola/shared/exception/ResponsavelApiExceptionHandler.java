package br.com.escola.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.escola.responsavelmanagement.adapter.in.web.ResponsavelController;
import br.com.escola.responsavelmanagement.adapter.in.web.vinculo.AlunoResponsavelVinculoController;
import br.com.escola.responsavelmanagement.domain.exception.ResponsavelJaCadastradoException;
import br.com.escola.responsavelmanagement.domain.exception.ResponsavelNaoEncontradoException;
import br.com.escola.responsavelmanagement.domain.exception.AlunoResponsavelVinculoDuplicadoException;
import br.com.escola.responsavelmanagement.domain.exception.AlunoResponsavelVinculoNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackageClasses = { ResponsavelController.class, AlunoResponsavelVinculoController.class })
public class ResponsavelApiExceptionHandler extends BaseApiExceptionHandler {

    @ExceptionHandler(ResponsavelJaCadastradoException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ResponsavelJaCadastradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(ResponsavelNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResponsavelNaoEncontradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }
    @ExceptionHandler(AlunoResponsavelVinculoDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> handleVinculoDuplicado(AlunoResponsavelVinculoDuplicadoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(AlunoResponsavelVinculoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleVinculoNotFound(AlunoResponsavelVinculoNaoEncontradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

}
