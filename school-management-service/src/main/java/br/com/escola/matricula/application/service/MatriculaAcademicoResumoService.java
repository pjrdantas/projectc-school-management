package br.com.escola.matricula.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.avaliacao.adapter.out.persistence.entity.NotaAlunoEntity;
import br.com.escola.avaliacao.adapter.out.persistence.repository.NotaAlunoJpaRepository;
import br.com.escola.frequencia.adapter.out.persistence.entity.FrequenciaAlunoEntity;
import br.com.escola.frequencia.adapter.out.persistence.repository.FrequenciaAlunoJpaRepository;
import br.com.escola.matricula.adapter.in.web.dto.MatriculaAcademicoFrequenciaResponse;
import br.com.escola.matricula.adapter.in.web.dto.MatriculaAcademicoIndicadoresResponse;
import br.com.escola.matricula.adapter.in.web.dto.MatriculaAcademicoNotaResponse;
import br.com.escola.matricula.adapter.in.web.dto.MatriculaAcademicoResumoResponse;
import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.matricula.domain.exception.MatriculaNaoEncontradaException;

@Service
public class MatriculaAcademicoResumoService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final MatriculaJpaRepository matriculaJpaRepository;
    private final FrequenciaAlunoJpaRepository frequenciaAlunoJpaRepository;
    private final NotaAlunoJpaRepository notaAlunoJpaRepository;

    public MatriculaAcademicoResumoService(
            MatriculaJpaRepository matriculaJpaRepository,
            FrequenciaAlunoJpaRepository frequenciaAlunoJpaRepository,
            NotaAlunoJpaRepository notaAlunoJpaRepository) {
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.frequenciaAlunoJpaRepository = frequenciaAlunoJpaRepository;
        this.notaAlunoJpaRepository = notaAlunoJpaRepository;
    }

    @Transactional(readOnly = true)
    public MatriculaAcademicoResumoResponse consultar(UUID matriculaId) {
        MatriculaEntity matricula = matriculaJpaRepository.findById(matriculaId)
                .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaId));

        List<FrequenciaAlunoEntity> frequencias = frequenciaAlunoJpaRepository.findByMatriculaId(matriculaId);
        List<NotaAlunoEntity> notas = notaAlunoJpaRepository.findByMatriculaId(matriculaId);

        return new MatriculaAcademicoResumoResponse(
                matricula.getId(),
                matricula.getAluno().getId(),
                matricula.getAluno().getPessoa().getNomeCompleto(),
                matricula.getTurma().getId(),
                matricula.getTurma().getNome(),
                matricula.getPeriodoLetivo().getId(),
                matricula.getPeriodoLetivo().getNome(),
                matricula.getStatus().getCodigo(),
                matricula.getTipoMatricula().getCodigo(),
                toIndicadores(frequencias, notas),
                frequencias.stream().map(this::toFrequenciaResponse).toList(),
                notas.stream().map(this::toNotaResponse).toList());
    }

    private MatriculaAcademicoIndicadoresResponse toIndicadores(
            List<FrequenciaAlunoEntity> frequencias,
            List<NotaAlunoEntity> notas) {
        long presencas = countSituacao(frequencias, "PRESENTE");
        long faltas = countSituacao(frequencias, "FALTA");
        long faltasJustificadas = countSituacao(frequencias, "FALTA_JUSTIFICADA");

        BigDecimal mediaNotas = BigDecimal.ZERO;
        BigDecimal mediaPercentual = BigDecimal.ZERO;
        if (!notas.isEmpty()) {
            mediaNotas = notas.stream()
                    .map(NotaAlunoEntity::getNota)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(notas.size()), 2, RoundingMode.HALF_UP);

            mediaPercentual = notas.stream()
                    .map(nota -> nota.getNota()
                            .multiply(CEM)
                            .divide(nota.getAvaliacao().getValorMaximo(), 2, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(notas.size()), 2, RoundingMode.HALF_UP);
        }

        return new MatriculaAcademicoIndicadoresResponse(
                frequencias.size(),
                presencas,
                faltas,
                faltasJustificadas,
                notas.size(),
                mediaNotas,
                mediaPercentual);
    }

    private long countSituacao(List<FrequenciaAlunoEntity> frequencias, String situacao) {
        return frequencias.stream()
                .filter(frequencia -> situacao.equals(frequencia.getSituacaoFrequencia().getCodigo()))
                .count();
    }

    private MatriculaAcademicoFrequenciaResponse toFrequenciaResponse(FrequenciaAlunoEntity entity) {
        return new MatriculaAcademicoFrequenciaResponse(
                entity.getId(),
                entity.getAula().getId(),
                entity.getAula().getDataAula(),
                entity.getAula().getProfessorTurmaDisciplina().getTurmaDisciplina().getDisciplina().getId(),
                entity.getAula().getProfessorTurmaDisciplina().getTurmaDisciplina().getDisciplina().getNome(),
                entity.getSituacaoFrequencia().getCodigo(),
                entity.getJustificativa());
    }

    private MatriculaAcademicoNotaResponse toNotaResponse(NotaAlunoEntity entity) {
        return new MatriculaAcademicoNotaResponse(
                entity.getId(),
                entity.getAvaliacao().getId(),
                entity.getAvaliacao().getTitulo(),
                entity.getAvaliacao().getDataAplicacao(),
                entity.getAvaliacao().getProfessorTurmaDisciplina().getTurmaDisciplina().getDisciplina().getId(),
                entity.getAvaliacao().getProfessorTurmaDisciplina().getTurmaDisciplina().getDisciplina().getNome(),
                entity.getAvaliacao().getTipoAvaliacao().getCodigo(),
                entity.getNota(),
                entity.getAvaliacao().getValorMaximo(),
                entity.getAvaliacao().getPeso(),
                entity.getObservacao());
    }
}
