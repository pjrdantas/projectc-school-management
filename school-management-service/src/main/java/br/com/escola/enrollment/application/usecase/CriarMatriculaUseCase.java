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
import br.com.escola.enrollment.domain.exception.MatriculaAlunoNaoEncontradoException;
import br.com.escola.enrollment.domain.exception.MatriculaPeriodoNaoEncontradoException;
import br.com.escola.enrollment.domain.exception.MatriculaTurmaNaoEncontradaException;
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

        return matriculaGateway.save(
                input.alunoId(),
                input.turmaId(),
                input.periodoLetivoId(),
                MatriculaStatus.ATIVA);
    }
}
