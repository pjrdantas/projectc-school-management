package br.com.escola.enrollmentdocumentservice.application.service;

import java.util.UUID;
import java.util.Set;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;
import br.com.escola.enrollmentdocumentservice.application.dto.AtualizarMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.AtualizarStatusMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.CancelarMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.MatriculaResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.TurmaMatriculaResumo;
import br.com.escola.enrollmentdocumentservice.application.exception.ConflitoNegocioException;
import br.com.escola.enrollmentdocumentservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.enrollmentdocumentservice.application.port.in.MatriculaWriteUseCase;
import br.com.escola.enrollmentdocumentservice.application.port.out.AlunoMatriculaPort;
import br.com.escola.enrollmentdocumentservice.application.port.out.CatalogoAcademicoMatriculaPort;
import br.com.escola.enrollmentdocumentservice.application.port.out.MatriculaWritePort;

@Service
public class MatriculaWriteService implements MatriculaWriteUseCase {

    private static final Set<String> STATUS_OPERACIONAIS = Set.of(
            "SOLICITADA", "EM_ANDAMENTO", "AGUARDANDO_DOCUMENTOS",
            "AGUARDANDO_HISTORICO_ESCOLAR", "EFETIVADA");
    private static final Set<String> STATUS_TERMINAIS = Set.of("CONCLUIDA", "CANCELADA", "INDEFERIDA", "TRANSFERIDO");

    private final AlunoMatriculaPort alunoPort;
    private final CatalogoAcademicoMatriculaPort catalogoPort;
    private final MatriculaWritePort matriculaPort;

    public MatriculaWriteService(
            AlunoMatriculaPort alunoPort,
            CatalogoAcademicoMatriculaPort catalogoPort,
            MatriculaWritePort matriculaPort) {
        this.alunoPort = alunoPort;
        this.catalogoPort = catalogoPort;
        this.matriculaPort = matriculaPort;
    }

    @Override
    public MatriculaResponse criar(CriarMatriculaCommand command, InternalRequestContext context) {
        CriarMatriculaCommand normalized = normalizar(command);
        return matriculaPort.criar(resolverDependencias(normalized, context), context.escolaId());
    }

    @Override
    public MatriculaResponse atualizar(UUID matriculaId, AtualizarMatriculaCommand command, InternalRequestContext context) {
        if (matriculaId == null) {
            throw new IllegalArgumentException("Matricula deve ser informada");
        }
        MatriculaResponse atual = matriculaPort.buscar(matriculaId, context.escolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Matricula nao encontrada para a escola informada"));
        if (!"PENDENTE".equals(atual.status())) {
            throw new ConflitoNegocioException("Somente matricula pendente pode ser atualizada");
        }
        AtualizarMatriculaCommand normalized = normalizar(command);
        validarAluno(atual.alunoId(), context);
        validarCatalogo(normalized.turmaId(), normalized.serieId(), normalized.periodoLetivoId(), context);
        return matriculaPort.atualizar(matriculaId, normalized, context.escolaId());
    }

    @Override
    public MatriculaResponse atualizarStatus(
            UUID matriculaId,
            AtualizarStatusMatriculaCommand command,
            InternalRequestContext context) {
        MatriculaResponse atual = buscarAtiva(matriculaId, context);
        String status = normalizarStatus(command);
        if (STATUS_TERMINAIS.contains(atual.status())) {
            throw new ConflitoNegocioException("Matricula em status terminal nao pode ser alterada");
        }
        if (status.equals(atual.status())) {
            throw new ConflitoNegocioException("Matricula ja possui o status informado");
        }
        return matriculaPort.atualizarStatus(
                matriculaId,
                new AtualizarStatusMatriculaCommand(status, normalizeOptional(command.observacao())),
                context.escolaId());
    }

    @Override
    public MatriculaResponse cancelar(UUID matriculaId, CancelarMatriculaCommand command, InternalRequestContext context) {
        MatriculaResponse atual = buscarAtiva(matriculaId, context);
        if (STATUS_TERMINAIS.contains(atual.status())) {
            throw new ConflitoNegocioException("Matricula em status terminal nao pode ser cancelada");
        }
        if (command == null || command.motivo() == null || command.motivo().isBlank()) {
            throw new IllegalArgumentException("Cancelamento exige motivo");
        }
        return matriculaPort.cancelar(
                matriculaId,
                new CancelarMatriculaCommand(command.motivo().trim()),
                context.escolaId());
    }

    private CriarMatriculaCommand normalizar(CriarMatriculaCommand command) {
        if (command == null || command.alunoId() == null || command.turmaId() == null
                || command.periodoLetivoId() == null || command.tipoMatricula() == null || command.tipoMatricula().isBlank()) {
            throw new IllegalArgumentException("Matricula exige aluno, turma, periodo e tipo");
        }
        return new CriarMatriculaCommand(
                command.alunoId(), command.turmaId(), command.serieId(), command.periodoLetivoId(),
                command.tipoMatricula().trim(),
                command.dataMatricula() == null ? LocalDate.now() : command.dataMatricula(),
                normalizeOptional(command.observacao()));
    }

    private MatriculaResponse buscarAtiva(UUID matriculaId, InternalRequestContext context) {
        if (matriculaId == null) {
            throw new IllegalArgumentException("Matricula deve ser informada");
        }
        return matriculaPort.buscar(matriculaId, context.escolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Matricula nao encontrada para a escola informada"));
    }

    private String normalizarStatus(AtualizarStatusMatriculaCommand command) {
        if (command == null || command.status() == null || command.status().isBlank()) {
            throw new IllegalArgumentException("Status da matricula deve ser informado");
        }
        String status = command.status().trim().toUpperCase();
        if ("ATIVA".equals(status)) {
            status = "EFETIVADA";
        }
        if ("TRANCADA".equals(status)) {
            throw new ConflitoNegocioException("Use o cancelamento seguro para cancelar matricula");
        }
        if ("CANCELADA".equals(status)) {
            throw new ConflitoNegocioException("Use o cancelamento seguro para cancelar matricula");
        }
        if (!STATUS_OPERACIONAIS.contains(status)) {
            throw new ConflitoNegocioException("Status exige fluxo academico ou de encerramento dedicado");
        }
        return status;
    }

    private AtualizarMatriculaCommand normalizar(AtualizarMatriculaCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Matricula exige turma, serie, periodo, tipo e data");
        }
        validarDadosAtualizacao(
                command.turmaId(), command.serieId(), command.periodoLetivoId(), command.tipoMatricula(), command.dataMatricula());
        return new AtualizarMatriculaCommand(
                command.turmaId(), command.serieId(), command.periodoLetivoId(), command.tipoMatricula().trim(),
                command.dataMatricula(), normalizeOptional(command.observacao()));
    }

    private void validarDadosAtualizacao(
            UUID turmaId,
            UUID serieId,
            UUID periodoLetivoId,
            String tipoMatricula,
            java.time.LocalDate dataMatricula) {
        if (turmaId == null || serieId == null || periodoLetivoId == null || dataMatricula == null
                || tipoMatricula == null || tipoMatricula.isBlank()) {
            throw new IllegalArgumentException("Matricula exige turma, serie, periodo, tipo e data");
        }
    }

    private CriarMatriculaCommand resolverDependencias(CriarMatriculaCommand command, InternalRequestContext context) {
        validarAluno(command.alunoId(), context);
        TurmaMatriculaResumo turma = catalogoPort.buscarTurmaNaEscola(command.turmaId(), context)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Turma nao encontrada para a escola informada"));
        if (command.serieId() != null && !command.serieId().equals(turma.serieId())) {
            throw new ConflitoNegocioException("Turma nao pertence a serie e periodo letivo informados");
        }
        validarCatalogo(command.turmaId(), turma.serieId(), command.periodoLetivoId(), context);
        return new CriarMatriculaCommand(
                command.alunoId(), command.turmaId(), turma.serieId(), command.periodoLetivoId(),
                command.tipoMatricula(), command.dataMatricula(), command.observacao());
    }

    private void validarAluno(UUID alunoId, InternalRequestContext context) {
        if (!alunoPort.existeAtivoNaEscola(alunoId, context)) {
            throw new RecursoNaoEncontradoException("Aluno ativo nao encontrado para a escola informada");
        }
    }

    private void validarCatalogo(UUID turmaId, UUID serieId, UUID periodoLetivoId, InternalRequestContext context) {
        TurmaMatriculaResumo turma = catalogoPort.buscarTurmaNaEscola(turmaId, context)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Turma nao encontrada para a escola informada"));
        if (!serieId.equals(turma.serieId()) || !periodoLetivoId.equals(turma.periodoLetivoId())) {
            throw new ConflitoNegocioException("Turma nao pertence a serie e periodo letivo informados");
        }
        if (!catalogoPort.existeSerieNaEscola(serieId, context)) {
            throw new RecursoNaoEncontradoException("Serie nao encontrada para a escola informada");
        }
        if (!catalogoPort.existePeriodoLetivoAtivoNaEscola(periodoLetivoId, context)) {
            throw new RecursoNaoEncontradoException("Periodo letivo ativo nao encontrado para a escola informada");
        }
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
