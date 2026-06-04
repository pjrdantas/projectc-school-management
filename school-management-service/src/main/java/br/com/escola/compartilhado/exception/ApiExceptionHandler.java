package br.com.escola.compartilhado.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import br.com.escola.matricula.domain.exception.MatriculaAlunoNaoEncontradoException;
import br.com.escola.matricula.domain.exception.MatriculaAtivaDuplicadaException;
import br.com.escola.matricula.domain.exception.MatriculaConclusaoAcademicaInvalidaException;
import br.com.escola.matricula.domain.exception.MatriculaDocumentoExigidoDuplicadoException;
import br.com.escola.matricula.domain.exception.MatriculaDocumentoExigidoNaoEncontradoException;
import br.com.escola.matricula.domain.exception.MatriculaDocumentoNaoEncontradoException;
import br.com.escola.matricula.domain.exception.MatriculaEtapaNaoEncontradaException;
import br.com.escola.matricula.domain.exception.MatriculaNaoEncontradaException;
import br.com.escola.matricula.domain.exception.MatriculaPeriodoNaoEncontradoException;
import br.com.escola.matricula.domain.exception.MatriculaStatusInvalidoException;
import br.com.escola.matricula.domain.exception.MatriculaTipoDocumentoNaoEncontradoException;
import br.com.escola.matricula.domain.exception.MatriculaTipoInvalidoException;
import br.com.escola.matricula.domain.exception.MatriculaTipoNaoEncontradoException;
import br.com.escola.matricula.domain.exception.MatriculaTurmaNaoEncontradaException;
import br.com.escola.matricula.domain.exception.MatriculaTurmaSemVagaException;
import br.com.escola.matricula.domain.exception.RematriculaNaoPermitidaException;
import br.com.escola.matricula.domain.exception.TransferenciaDadosObrigatoriosException;
import br.com.escola.matricula.domain.exception.TurmaPeriodoInconsistenteException;
import br.com.escola.professor.domain.exception.AulaFrequenciaAlunoDuplicadaException;
import br.com.escola.professor.domain.exception.AulaFrequenciaProfessorDuplicadaException;
import br.com.escola.professor.domain.exception.AulaMatriculaTurmaInconsistenteException;
import br.com.escola.professor.domain.exception.AulaNaoEncontradaException;
import br.com.escola.professor.domain.exception.ProfessorFuncionarioInativoException;
import br.com.escola.professor.domain.exception.ProfessorJaCadastradoException;
import br.com.escola.professor.domain.exception.ProfessorNaoEncontradoException;
import br.com.escola.professor.domain.exception.ProfessorTurmaDisciplinaDuplicadaException;
import br.com.escola.professor.domain.exception.ProfessorTurmaDisciplinaNaoEncontradaException;
import br.com.escola.professor.domain.exception.SituacaoFrequenciaNaoEncontradaException;
import br.com.escola.responsavel.domain.exception.ResponsavelJaCadastradoException;
import br.com.escola.responsavel.domain.exception.ResponsavelNaoEncontradoException;
import br.com.escola.responsavel.domain.exception.AlunoResponsavelVinculoDuplicadoException;
import br.com.escola.responsavel.domain.exception.AlunoResponsavelVinculoNaoEncontradoException;
import br.com.escola.seguranca.domain.exception.CredenciaisInvalidasException;
import br.com.escola.seguranca.domain.exception.TokenInvalidoOuExpiradoException;
import br.com.escola.historico.domain.exception.HistoricoEscolarInvalidoException;
import br.com.escola.historico.domain.exception.HistoricoEscolarNaoEncontradoException;
import br.com.escola.compartilhado.viacep.CepInvalidoException;
import br.com.escola.compartilhado.viacep.CepNaoEncontradoException;
import br.com.escola.compartilhado.viacep.ViaCepIndisponivelException;
import br.com.escola.documento.domain.exception.DocumentoInvalidoException;
import br.com.escola.documento.domain.exception.DocumentoNaoEncontradoException;
import br.com.escola.avaliacao.domain.exception.AvaliacaoMatriculaTurmaInconsistenteException;
import br.com.escola.avaliacao.domain.exception.AvaliacaoNaoEncontradaException;
import br.com.escola.avaliacao.domain.exception.AvaliacaoNotaDuplicadaException;
import br.com.escola.avaliacao.domain.exception.AvaliacaoNotaInvalidaException;
import br.com.escola.avaliacao.domain.exception.TipoAvaliacaoNaoEncontradoException;
import br.com.escola.historico.domain.exception.BoletimFechadoNaoEncontradoException;
import br.com.escola.historico.domain.exception.BoletimFechamentoDuplicadoException;
import br.com.escola.historico.domain.exception.HistoricoEscolarDuplicadoException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler extends BaseApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

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
        String message = ex.getMessage() == null ? "Requisição inválida" : ex.getMessage();
        String normalized = message.toLowerCase();

        if (normalized.contains("não encontrado") || normalized.contains("nao encontrado") || normalized.contains("inexistente")) {
            return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message, request);
        }

        if (normalized.contains("obrigatório") || normalized.contains("obrigatorio")
                || normalized.contains("não pode ser nulo") || normalized.contains("nao pode ser nulo")
                || normalized.contains("não pode ser vazio") || normalized.contains("nao pode ser vazio")
                || normalized.contains("inválido") || normalized.contains("invalido")) {
            return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
        }

        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", message, request);
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

    @ExceptionHandler(CepNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleCepNaoEncontrado(
            CepNaoEncontradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(CepInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handleCepInvalido(
            CepInvalidoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(ViaCepIndisponivelException.class)
    public ResponseEntity<ApiErrorResponse> handleViaCepIndisponivel(
            ViaCepIndisponivelException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", ex.getMessage(), request);
    }

    @ExceptionHandler(DocumentoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleDocumentoNaoEncontrado(
            DocumentoNaoEncontradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(DocumentoInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handleDocumentoInvalido(
            DocumentoInvalidoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(HistoricoEscolarNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleHistoricoEscolarNaoEncontrado(
            HistoricoEscolarNaoEncontradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(HistoricoEscolarInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handleHistoricoEscolarInvalido(
            HistoricoEscolarInvalidoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaNotFound(
            MatriculaNaoEncontradaException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaEtapaNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaEtapaNotFound(
            MatriculaEtapaNaoEncontradaException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaDocumentoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaDocumentoNotFound(
            MatriculaDocumentoNaoEncontradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaDocumentoExigidoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaDocumentoExigidoNotFound(
            MatriculaDocumentoExigidoNaoEncontradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaTipoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaTipoNotFound(
            MatriculaTipoNaoEncontradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaTipoDocumentoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaTipoDocumentoNotFound(
            MatriculaTipoDocumentoNaoEncontradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaStatusInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaStatusInvalido(
            MatriculaStatusInvalidoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaConclusaoAcademicaInvalidaException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaConclusaoAcademicaInvalida(
            MatriculaConclusaoAcademicaInvalidaException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaTipoInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaTipoInvalido(
            MatriculaTipoInvalidoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaAtivaDuplicadaException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaAtivaDuplicada(
            MatriculaAtivaDuplicadaException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaDocumentoExigidoDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaDocumentoExigidoDuplicado(
            MatriculaDocumentoExigidoDuplicadoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(RematriculaNaoPermitidaException.class)
    public ResponseEntity<ApiErrorResponse> handleRematriculaNaoPermitida(
            RematriculaNaoPermitidaException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
    }

    @ExceptionHandler(TransferenciaDadosObrigatoriosException.class)
    public ResponseEntity<ApiErrorResponse> handleTransferenciaDadosObrigatorios(
            TransferenciaDadosObrigatoriosException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(TurmaPeriodoInconsistenteException.class)
    public ResponseEntity<ApiErrorResponse> handleTurmaPeriodoInconsistente(
            TurmaPeriodoInconsistenteException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
    }

    @ExceptionHandler(MatriculaTurmaSemVagaException.class)
    public ResponseEntity<ApiErrorResponse> handleMatriculaTurmaSemVaga(
            MatriculaTurmaSemVagaException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
    }

    @ExceptionHandler(ProfessorNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleProfessorNotFound(
            ProfessorNaoEncontradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(ProfessorTurmaDisciplinaNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleProfessorTurmaDisciplinaNotFound(
            ProfessorTurmaDisciplinaNaoEncontradaException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(ProfessorJaCadastradoException.class)
    public ResponseEntity<ApiErrorResponse> handleProfessorDuplicado(
            ProfessorJaCadastradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(ProfessorTurmaDisciplinaDuplicadaException.class)
    public ResponseEntity<ApiErrorResponse> handleProfessorTurmaDisciplinaDuplicado(
            ProfessorTurmaDisciplinaDuplicadaException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(ProfessorFuncionarioInativoException.class)
    public ResponseEntity<ApiErrorResponse> handleProfessorFuncionarioInativo(
            ProfessorFuncionarioInativoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
    }

    @ExceptionHandler(AulaNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleAulaNotFound(
            AulaNaoEncontradaException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(SituacaoFrequenciaNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleSituacaoFrequenciaNotFound(
            SituacaoFrequenciaNaoEncontradaException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(AulaFrequenciaProfessorDuplicadaException.class)
    public ResponseEntity<ApiErrorResponse> handleAulaFrequenciaProfessorDuplicada(
            AulaFrequenciaProfessorDuplicadaException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(AulaFrequenciaAlunoDuplicadaException.class)
    public ResponseEntity<ApiErrorResponse> handleAulaFrequenciaAlunoDuplicada(
            AulaFrequenciaAlunoDuplicadaException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(AulaMatriculaTurmaInconsistenteException.class)
    public ResponseEntity<ApiErrorResponse> handleAulaMatriculaTurmaInconsistente(
            AulaMatriculaTurmaInconsistenteException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
    }

    @ExceptionHandler(AvaliacaoNaoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleAvaliacaoNotFound(
            AvaliacaoNaoEncontradaException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(TipoAvaliacaoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleTipoAvaliacaoNotFound(
            TipoAvaliacaoNaoEncontradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(AvaliacaoNotaDuplicadaException.class)
    public ResponseEntity<ApiErrorResponse> handleAvaliacaoNotaDuplicada(
            AvaliacaoNotaDuplicadaException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(AvaliacaoNotaInvalidaException.class)
    public ResponseEntity<ApiErrorResponse> handleAvaliacaoNotaInvalida(
            AvaliacaoNotaInvalidaException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
    }

    @ExceptionHandler(AvaliacaoMatriculaTurmaInconsistenteException.class)
    public ResponseEntity<ApiErrorResponse> handleAvaliacaoMatriculaTurmaInconsistente(
            AvaliacaoMatriculaTurmaInconsistenteException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
    }

    @ExceptionHandler(BoletimFechamentoDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> handleBoletimFechamentoDuplicado(
            BoletimFechamentoDuplicadoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(BoletimFechadoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleBoletimFechadoNaoEncontrado(
            BoletimFechadoNaoEncontradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(HistoricoEscolarDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> handleHistoricoEscolarDuplicado(
            HistoricoEscolarDuplicadoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }


    @ExceptionHandler(ResponsavelJaCadastradoException.class)
    public ResponseEntity<ApiErrorResponse> handleResponsavelDuplicado(
            ResponsavelJaCadastradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(ResponsavelNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleResponsavelNotFound(
            ResponsavelNaoEncontradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }


    @ExceptionHandler(AlunoResponsavelVinculoDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> handleVinculoDuplicado(
            AlunoResponsavelVinculoDuplicadoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, "BUSINESS_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler(AlunoResponsavelVinculoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleVinculoNotFound(
            AlunoResponsavelVinculoNaoEncontradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }


    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ApiErrorResponse> handleCredenciaisInvalidas(
            CredenciaisInvalidasException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), request);
    }

    @ExceptionHandler(TokenInvalidoOuExpiradoException.class)
    public ResponseEntity<ApiErrorResponse> handleTokenInvalido(
            TokenInvalidoOuExpiradoException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), request);
    }


    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            AuthenticationException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Não autenticado", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        return buildError(HttpStatus.FORBIDDEN, "FORBIDDEN", "Acesso negado", request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return buildError(status, status.name(), message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Erro interno não tratado em {}", request.getRequestURI(), ex);
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Erro interno não tratado",
                request);
    }
}
