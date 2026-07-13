package br.com.escola.compartilhado.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.escola.matricula.adapter.in.web.MatriculaController;
import br.com.escola.matricula.adapter.in.web.MatriculaDocumentoExigidoAdminController;
import br.com.escola.matricula.adapter.in.web.internal.MatriculaInternalController;
import br.com.escola.matricula.domain.exception.MatriculaAlunoNaoEncontradoException;
import br.com.escola.matricula.domain.exception.MatriculaAtivaDuplicadaException;
import br.com.escola.matricula.domain.exception.MatriculaDocumentoExigidoDuplicadoException;
import br.com.escola.matricula.domain.exception.MatriculaDocumentoExigidoNaoEncontradoException;
import br.com.escola.matricula.domain.exception.MatriculaDocumentoNaoEncontradoException;
import br.com.escola.matricula.domain.exception.MatriculaEtapaNaoEncontradaException;
import br.com.escola.matricula.domain.exception.MatriculaNaoEncontradaException;
import br.com.escola.matricula.domain.exception.MatriculaPeriodoNaoEncontradoException;
import br.com.escola.matricula.domain.exception.MatriculaStatusInvalidoException;
import br.com.escola.matricula.domain.exception.MatriculaTipoInvalidoException;
import br.com.escola.matricula.domain.exception.MatriculaTurmaNaoEncontradaException;
import br.com.escola.matricula.domain.exception.MatriculaTurmaSemVagaException;
import br.com.escola.matricula.domain.exception.MatriculaTipoDocumentoNaoEncontradoException;
import br.com.escola.matricula.domain.exception.RematriculaNaoPermitidaException;
import br.com.escola.matricula.domain.exception.MatriculaTipoNaoEncontradoException;
import br.com.escola.matricula.domain.exception.TransferenciaDadosObrigatoriosException;
import br.com.escola.matricula.domain.exception.TurmaPeriodoInconsistenteException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice(basePackageClasses = {
        MatriculaController.class,
        MatriculaDocumentoExigidoAdminController.class,
        MatriculaInternalController.class
})
public class EnrollmentApiExceptionHandler extends BaseApiExceptionHandler {

    @ExceptionHandler(MatriculaAlunoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleAlunoNotFound(MatriculaAlunoNaoEncontradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaTurmaNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleTurmaNotFound(MatriculaTurmaNaoEncontradaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaPeriodoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handlePeriodoNotFound(MatriculaPeriodoNaoEncontradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaNotFound(MatriculaNaoEncontradaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaEtapaNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleEtapaNotFound(MatriculaEtapaNaoEncontradaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaDocumentoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleDocumentoNotFound(MatriculaDocumentoNaoEncontradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaDocumentoExigidoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleDocumentoExigidoNotFound(MatriculaDocumentoExigidoNaoEncontradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaTipoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleTipoMatriculaNotFound(MatriculaTipoNaoEncontradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaTipoDocumentoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleTipoDocumentoNotFound(MatriculaTipoDocumentoNaoEncontradoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaStatusInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handleStatusInvalido(MatriculaStatusInvalidoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaTipoInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handleTipoInvalido(MatriculaTipoInvalidoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaAtivaDuplicadaException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaAtivaDuplicada(MatriculaAtivaDuplicadaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaDocumentoExigidoDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> handleDocumentoExigidoDuplicado(MatriculaDocumentoExigidoDuplicadoException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(RematriculaNaoPermitidaException.class)
    public ResponseEntity<ApiErrorResponse> handleRematriculaNaoPermitida(RematriculaNaoPermitidaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
    }

    @ExceptionHandler(TransferenciaDadosObrigatoriosException.class)
    public ResponseEntity<ApiErrorResponse> handleTransferenciaDadosObrigatorios(TransferenciaDadosObrigatoriosException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(TurmaPeriodoInconsistenteException.class)
    public ResponseEntity<ApiErrorResponse> handleInconsistencia(TurmaPeriodoInconsistenteException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaTurmaSemVagaException.class)
    public ResponseEntity<ApiErrorResponse> handleTurmaSemVaga(MatriculaTurmaSemVagaException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
    }
}
