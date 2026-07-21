package br.com.escola.enrollmentdocumentservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.AtualizarMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.AtualizarStatusMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.CancelarMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.MatriculaResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.TurmaMatriculaResumo;
import br.com.escola.enrollmentdocumentservice.application.exception.ConflitoNegocioException;
import br.com.escola.enrollmentdocumentservice.application.port.out.AlunoMatriculaPort;
import br.com.escola.enrollmentdocumentservice.application.port.out.CatalogoAcademicoMatriculaPort;
import br.com.escola.enrollmentdocumentservice.application.port.out.MatriculaWritePort;

class MatriculaWriteServiceTest {

    @Test
    void deveCriarMatriculaPendenteAposValidarDependencias() {
        AlunoMatriculaPort alunoPort = mock(AlunoMatriculaPort.class);
        CatalogoAcademicoMatriculaPort catalogoPort = mock(CatalogoAcademicoMatriculaPort.class);
        MatriculaWritePort matriculaPort = mock(MatriculaWritePort.class);
        InternalRequestContext context = context();
        CriarMatriculaCommand command = command();
        MatriculaResponse response = response(command, context.escolaId());

        when(alunoPort.existeAtivoNaEscola(command.alunoId(), context)).thenReturn(true);
        when(catalogoPort.buscarTurmaNaEscola(command.turmaId(), context))
                .thenReturn(Optional.of(new TurmaMatriculaResumo(command.serieId(), command.periodoLetivoId())));
        when(catalogoPort.existeSerieNaEscola(command.serieId(), context)).thenReturn(true);
        when(catalogoPort.existePeriodoLetivoAtivoNaEscola(command.periodoLetivoId(), context)).thenReturn(true);
        when(matriculaPort.criar(command, context.escolaId())).thenReturn(response);

        MatriculaResponse created = new MatriculaWriteService(alunoPort, catalogoPort, matriculaPort)
                .criar(command, context);

        assertThat(created).isEqualTo(response);
        verify(matriculaPort).criar(command, context.escolaId());
    }

    @Test
    void deveDerivarSerieEDataQuandoOContratoPublicoNaoAsInformar() {
        AlunoMatriculaPort alunoPort = mock(AlunoMatriculaPort.class);
        CatalogoAcademicoMatriculaPort catalogoPort = mock(CatalogoAcademicoMatriculaPort.class);
        MatriculaWritePort matriculaPort = mock(MatriculaWritePort.class);
        InternalRequestContext context = context();
        UUID serieId = UUID.randomUUID();
        CriarMatriculaCommand command = new CriarMatriculaCommand(
                UUID.randomUUID(), UUID.randomUUID(), null, UUID.randomUUID(), "PRIMEIRA_MATRICULA", null, "Observacao");
        CriarMatriculaCommand resolved = new CriarMatriculaCommand(
                command.alunoId(), command.turmaId(), serieId, command.periodoLetivoId(),
                command.tipoMatricula(), LocalDate.now(), command.observacao());
        MatriculaResponse response = response(resolved, context.escolaId());

        when(alunoPort.existeAtivoNaEscola(command.alunoId(), context)).thenReturn(true);
        when(catalogoPort.buscarTurmaNaEscola(command.turmaId(), context))
                .thenReturn(Optional.of(new TurmaMatriculaResumo(serieId, command.periodoLetivoId())));
        when(catalogoPort.existeSerieNaEscola(serieId, context)).thenReturn(true);
        when(catalogoPort.existePeriodoLetivoAtivoNaEscola(command.periodoLetivoId(), context)).thenReturn(true);
        when(matriculaPort.criar(resolved, context.escolaId())).thenReturn(response);

        MatriculaResponse created = new MatriculaWriteService(alunoPort, catalogoPort, matriculaPort)
                .criar(command, context);

        assertThat(created).isEqualTo(response);
        verify(matriculaPort).criar(resolved, context.escolaId());
    }

    @Test
    void deveBloquearTurmaIncompativelSemPersistirMatricula() {
        AlunoMatriculaPort alunoPort = mock(AlunoMatriculaPort.class);
        CatalogoAcademicoMatriculaPort catalogoPort = mock(CatalogoAcademicoMatriculaPort.class);
        MatriculaWritePort matriculaPort = mock(MatriculaWritePort.class);
        InternalRequestContext context = context();
        CriarMatriculaCommand command = command();

        when(alunoPort.existeAtivoNaEscola(command.alunoId(), context)).thenReturn(true);
        when(catalogoPort.buscarTurmaNaEscola(command.turmaId(), context))
                .thenReturn(Optional.of(new TurmaMatriculaResumo(UUID.randomUUID(), command.periodoLetivoId())));

        assertThatThrownBy(() -> new MatriculaWriteService(alunoPort, catalogoPort, matriculaPort).criar(command, context))
                .isInstanceOf(ConflitoNegocioException.class)
                .hasMessage("Turma nao pertence a serie e periodo letivo informados");
        verifyNoInteractions(matriculaPort);
    }

    @Test
    void deveAtualizarMatriculaPendenteAposRevalidarDependencias() {
        AlunoMatriculaPort alunoPort = mock(AlunoMatriculaPort.class);
        CatalogoAcademicoMatriculaPort catalogoPort = mock(CatalogoAcademicoMatriculaPort.class);
        MatriculaWritePort matriculaPort = mock(MatriculaWritePort.class);
        InternalRequestContext context = context();
        UUID matriculaId = UUID.randomUUID();
        CriarMatriculaCommand created = command();
        AtualizarMatriculaCommand update = new AtualizarMatriculaCommand(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "REMATRICULA",
                LocalDate.of(2026, 7, 22), "Atualizada");
        MatriculaResponse current = response(created, context.escolaId());
        MatriculaResponse updated = new MatriculaResponse(
                matriculaId, current.alunoId(), update.turmaId(), context.escolaId(), null,
                update.serieId(), null, update.periodoLetivoId(), "PENDENTE", update.tipoMatricula(),
                update.dataMatricula(), update.observacao(), null, List.of());

        when(matriculaPort.buscar(matriculaId, context.escolaId())).thenReturn(Optional.of(current));
        when(alunoPort.existeAtivoNaEscola(current.alunoId(), context)).thenReturn(true);
        when(catalogoPort.buscarTurmaNaEscola(update.turmaId(), context))
                .thenReturn(Optional.of(new TurmaMatriculaResumo(update.serieId(), update.periodoLetivoId())));
        when(catalogoPort.existeSerieNaEscola(update.serieId(), context)).thenReturn(true);
        when(catalogoPort.existePeriodoLetivoAtivoNaEscola(update.periodoLetivoId(), context)).thenReturn(true);
        when(matriculaPort.atualizar(matriculaId, update, context.escolaId())).thenReturn(updated);

        MatriculaResponse result = new MatriculaWriteService(alunoPort, catalogoPort, matriculaPort)
                .atualizar(matriculaId, update, context);

        assertThat(result).isEqualTo(updated);
        verify(matriculaPort).atualizar(matriculaId, update, context.escolaId());
    }

    @Test
    void deveAtualizarStatusOperacionalDaMatricula() {
        AlunoMatriculaPort alunoPort = mock(AlunoMatriculaPort.class);
        CatalogoAcademicoMatriculaPort catalogoPort = mock(CatalogoAcademicoMatriculaPort.class);
        MatriculaWritePort matriculaPort = mock(MatriculaWritePort.class);
        InternalRequestContext context = context();
        UUID matriculaId = UUID.randomUUID();
        CriarMatriculaCommand created = command();
        AtualizarStatusMatriculaCommand command = new AtualizarStatusMatriculaCommand("ATIVA", "Documentos conferidos");
        MatriculaResponse current = response(created, context.escolaId());
        MatriculaResponse updated = new MatriculaResponse(
                matriculaId, current.alunoId(), current.turmaId(), context.escolaId(), null,
                current.serieId(), null, current.periodoLetivoId(), "EFETIVADA", current.tipoMatricula(),
                current.dataMatricula(), "Documentos conferidos", null, List.of());
        AtualizarStatusMatriculaCommand expected = new AtualizarStatusMatriculaCommand("EFETIVADA", "Documentos conferidos");

        when(matriculaPort.buscar(matriculaId, context.escolaId())).thenReturn(Optional.of(current));
        when(matriculaPort.atualizarStatus(matriculaId, expected, context.escolaId())).thenReturn(updated);

        MatriculaResponse result = new MatriculaWriteService(alunoPort, catalogoPort, matriculaPort)
                .atualizarStatus(matriculaId, command, context);

        assertThat(result).isEqualTo(updated);
        verify(matriculaPort).atualizarStatus(matriculaId, expected, context.escolaId());
    }

    @Test
    void deveCancelarMatriculaNaoTerminalComMotivo() {
        AlunoMatriculaPort alunoPort = mock(AlunoMatriculaPort.class);
        CatalogoAcademicoMatriculaPort catalogoPort = mock(CatalogoAcademicoMatriculaPort.class);
        MatriculaWritePort matriculaPort = mock(MatriculaWritePort.class);
        InternalRequestContext context = context();
        UUID matriculaId = UUID.randomUUID();
        CriarMatriculaCommand created = command();
        MatriculaResponse current = response(created, context.escolaId());
        CancelarMatriculaCommand command = new CancelarMatriculaCommand("Desistencia formalizada");
        MatriculaResponse cancelled = new MatriculaResponse(
                matriculaId, current.alunoId(), current.turmaId(), context.escolaId(), null,
                current.serieId(), null, current.periodoLetivoId(), "CANCELADA", current.tipoMatricula(),
                current.dataMatricula(), current.observacao(), null, List.of());

        when(matriculaPort.buscar(matriculaId, context.escolaId())).thenReturn(Optional.of(current));
        when(matriculaPort.cancelar(matriculaId, command, context.escolaId())).thenReturn(cancelled);

        MatriculaResponse result = new MatriculaWriteService(alunoPort, catalogoPort, matriculaPort)
                .cancelar(matriculaId, command, context);

        assertThat(result).isEqualTo(cancelled);
        verify(matriculaPort).cancelar(matriculaId, command, context.escolaId());
    }

    @Test
    void deveBloquearCancelamentoDeMatriculaTerminal() {
        AlunoMatriculaPort alunoPort = mock(AlunoMatriculaPort.class);
        CatalogoAcademicoMatriculaPort catalogoPort = mock(CatalogoAcademicoMatriculaPort.class);
        MatriculaWritePort matriculaPort = mock(MatriculaWritePort.class);
        InternalRequestContext context = context();
        UUID matriculaId = UUID.randomUUID();
        CriarMatriculaCommand created = command();
        MatriculaResponse completed = new MatriculaResponse(
                matriculaId, created.alunoId(), created.turmaId(), context.escolaId(), null,
                created.serieId(), null, created.periodoLetivoId(), "CONCLUIDA", created.tipoMatricula(),
                created.dataMatricula(), created.observacao(), null, List.of());

        when(matriculaPort.buscar(matriculaId, context.escolaId())).thenReturn(Optional.of(completed));

        assertThatThrownBy(() -> new MatriculaWriteService(alunoPort, catalogoPort, matriculaPort)
                .cancelar(matriculaId, new CancelarMatriculaCommand("Motivo"), context))
                .isInstanceOf(ConflitoNegocioException.class)
                .hasMessage("Matricula em status terminal nao pode ser cancelada");
        verifyNoInteractions(alunoPort, catalogoPort);
    }

    private InternalRequestContext context() {
        return new InternalRequestContext("corr", UUID.randomUUID(), UUID.randomUUID());
    }

    private CriarMatriculaCommand command() {
        return new CriarMatriculaCommand(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "PRIMEIRA_MATRICULA", LocalDate.of(2026, 7, 21), "Observacao");
    }

    private MatriculaResponse response(CriarMatriculaCommand command, UUID escolaId) {
        return new MatriculaResponse(
                UUID.randomUUID(), command.alunoId(), command.turmaId(), escolaId, null,
                command.serieId(), null, command.periodoLetivoId(), "PENDENTE", command.tipoMatricula(),
                command.dataMatricula(), command.observacao(), null, List.of());
    }
}
