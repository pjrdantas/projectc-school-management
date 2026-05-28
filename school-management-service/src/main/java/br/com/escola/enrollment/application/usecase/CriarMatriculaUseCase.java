package br.com.escola.enrollment.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.enrollment.application.dto.MatriculaInput;
import br.com.escola.enrollment.application.dto.MatriculaOutput;
import br.com.escola.enrollment.application.port.out.AlunoConsultaGateway;
import br.com.escola.enrollment.application.port.out.MatriculaGateway;
import br.com.escola.enrollment.application.port.out.PeriodoLetivoConsultaGateway;
import br.com.escola.enrollment.application.port.out.TurmaConsultaGateway;
import br.com.escola.enrollment.domain.MatriculaStatus;
import br.com.escola.enrollment.domain.MatriculaTipo;
import br.com.escola.enrollment.domain.exception.MatriculaAtivaDuplicadaException;
import br.com.escola.enrollment.domain.exception.MatriculaAlunoNaoEncontradoException;
import br.com.escola.enrollment.domain.exception.MatriculaPeriodoNaoEncontradoException;
import br.com.escola.enrollment.domain.exception.MatriculaTipoInvalidoException;
import br.com.escola.enrollment.domain.exception.MatriculaTurmaSemVagaException;
import br.com.escola.enrollment.domain.exception.MatriculaTurmaNaoEncontradaException;
import br.com.escola.enrollment.domain.exception.RematriculaNaoPermitidaException;
import br.com.escola.enrollment.domain.exception.TransferenciaDadosObrigatoriosException;
import br.com.escola.enrollment.domain.exception.TurmaPeriodoInconsistenteException;

@Service
public class CriarMatriculaUseCase {

    private final MatriculaGateway matriculaGateway;
    private final AlunoConsultaGateway alunoConsultaGateway;
    private final TurmaConsultaGateway turmaConsultaGateway;
    private final PeriodoLetivoConsultaGateway periodoLetivoConsultaGateway;

    public CriarMatriculaUseCase(
            MatriculaGateway matriculaGateway,
            AlunoConsultaGateway alunoConsultaGateway,
            TurmaConsultaGateway turmaConsultaGateway,
            PeriodoLetivoConsultaGateway periodoLetivoConsultaGateway) {
        this.matriculaGateway = matriculaGateway;
        this.alunoConsultaGateway = alunoConsultaGateway;
        this.turmaConsultaGateway = turmaConsultaGateway;
        this.periodoLetivoConsultaGateway = periodoLetivoConsultaGateway;
    }

    public MatriculaOutput executar(MatriculaInput input) {
        if (!alunoConsultaGateway.existsById(input.alunoId())) {
            throw new MatriculaAlunoNaoEncontradoException(input.alunoId());
        }

        if (!turmaConsultaGateway.existsById(input.turmaId())) {
            throw new MatriculaTurmaNaoEncontradaException(input.turmaId());
        }

        if (!periodoLetivoConsultaGateway.existsById(input.periodoLetivoId())) {
            throw new MatriculaPeriodoNaoEncontradoException(input.periodoLetivoId());
        }

        UUID periodoDaTurma = turmaConsultaGateway.findPeriodoLetivoIdByTurmaId(input.turmaId())
                .orElseThrow(() -> new MatriculaTurmaNaoEncontradaException(input.turmaId()));
        if (!periodoDaTurma.equals(input.periodoLetivoId())) {
            throw new TurmaPeriodoInconsistenteException(input.turmaId(), input.periodoLetivoId());
        }

        if (matriculaGateway.existsByAlunoIdAndPeriodoLetivoId(
                input.alunoId(),
                input.periodoLetivoId())) {
            throw new MatriculaAtivaDuplicadaException(input.alunoId(), input.periodoLetivoId());
        }

        Integer capacidade = turmaConsultaGateway.findCapacidadeByTurmaId(input.turmaId())
                .orElseThrow(() -> new MatriculaTurmaNaoEncontradaException(input.turmaId()));
        long matriculasQueOcupamVaga = matriculaGateway.countMatriculasQueOcupamVagaByTurmaId(input.turmaId());
        if (matriculasQueOcupamVaga >= capacidade) {
            throw new MatriculaTurmaSemVagaException(input.turmaId(), capacidade);
        }

        MatriculaTipo tipoMatricula = parseTipo(input.tipoMatricula());
        validarRegrasPorTipo(input, tipoMatricula);

        MatriculaStatus statusInicial = tipoMatricula == MatriculaTipo.PRIMEIRA_MATRICULA
                ? MatriculaStatus.EM_ANDAMENTO
                : MatriculaStatus.SOLICITADA;

        return matriculaGateway.save(
                input.alunoId(),
                input.turmaId(),
                input.periodoLetivoId(),
                statusInicial,
                tipoMatricula,
                input.observacao());
    }

    private MatriculaTipo parseTipo(String tipoMatricula) {
        if (tipoMatricula == null || tipoMatricula.isBlank()) {
            return MatriculaTipo.PRIMEIRA_MATRICULA;
        }
        String normalized = tipoMatricula.trim().toUpperCase();
        if (normalized.equals("NOVA")) {
            return MatriculaTipo.PRIMEIRA_MATRICULA;
        }
        if (normalized.equals("REMATRICULA")) {
            return MatriculaTipo.RENOVACAO;
        }
        if (normalized.equals("TRANSFERENCIA")) {
            return MatriculaTipo.TRANSFERENCIA_ENTRADA;
        }
        try {
            return MatriculaTipo.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new MatriculaTipoInvalidoException(tipoMatricula);
        }
    }

    private void validarRegrasPorTipo(MatriculaInput input, MatriculaTipo tipoMatricula) {
        if (tipoMatricula == MatriculaTipo.RENOVACAO) {
            validarRematricula(input);
            return;
        }

        if (tipoMatricula == MatriculaTipo.TRANSFERENCIA_ENTRADA && isBlank(input.observacao())) {
            throw new TransferenciaDadosObrigatoriosException();
        }
    }

    private void validarRematricula(MatriculaInput input) {
        throw new RematriculaNaoPermitidaException(
                "validação de rematrícula deve ser redefinida para a nova estrutura documental de histórico escolar");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
