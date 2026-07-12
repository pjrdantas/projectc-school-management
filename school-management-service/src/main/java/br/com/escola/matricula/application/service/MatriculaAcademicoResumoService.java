package br.com.escola.matricula.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.historico.application.dto.internal.FrequenciaAcademicaResumo;
import br.com.escola.historico.application.dto.internal.NotaAcademicaResumo;
import br.com.escola.historico.application.port.internal.RendimentoAcademicoPort;
import br.com.escola.matricula.adapter.in.web.dto.MatriculaAcademicoFrequenciaResponse;
import br.com.escola.matricula.adapter.in.web.dto.MatriculaAcademicoIndicadoresResponse;
import br.com.escola.matricula.adapter.in.web.dto.MatriculaAcademicoNotaResponse;
import br.com.escola.matricula.adapter.in.web.dto.MatriculaAcademicoResumoResponse;
import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.matricula.domain.exception.MatriculaNaoEncontradaException;
import br.com.escola.institucional.application.port.EscolaContextoPort;

@Service
public class MatriculaAcademicoResumoService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final MatriculaJpaRepository matriculaJpaRepository;
    private final RendimentoAcademicoPort rendimentoAcademicoPort;
    private final EscolaContextoPort escolaContextoPort;

    public MatriculaAcademicoResumoService(
            MatriculaJpaRepository matriculaJpaRepository,
            RendimentoAcademicoPort rendimentoAcademicoPort,
            EscolaContextoPort escolaContextoPort) {
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.rendimentoAcademicoPort = rendimentoAcademicoPort;
        this.escolaContextoPort = escolaContextoPort;
    }

    @Transactional(readOnly = true)
    public MatriculaAcademicoResumoResponse consultar(UUID matriculaId) {
        UUID escolaId = escolaContextoPort.obterContextoPadrao().escolaId();
        MatriculaEntity matricula = matriculaJpaRepository
                .findByIdAndTurma_Escola_Id(matriculaId, escolaId)
                .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaId));

        var rendimento = rendimentoAcademicoPort.consultarPorMatricula(matriculaId, escolaId);
        var frequencias = rendimento.frequencias();
        var notas = rendimento.notas();

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
            java.util.List<FrequenciaAcademicaResumo> frequencias,
            java.util.List<NotaAcademicaResumo> notas) {
        long presencas = countSituacao(frequencias, "PRESENTE");
        long faltas = countSituacao(frequencias, "FALTA");
        long faltasJustificadas = countSituacao(frequencias, "FALTA_JUSTIFICADA");

        BigDecimal mediaNotas = BigDecimal.ZERO;
        BigDecimal mediaPercentual = BigDecimal.ZERO;
        if (!notas.isEmpty()) {
            mediaNotas = notas.stream()
                    .map(NotaAcademicaResumo::nota)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(notas.size()), 2, RoundingMode.HALF_UP);

            mediaPercentual = notas.stream()
                    .map(nota -> nota.nota()
                            .multiply(CEM)
                            .divide(nota.valorMaximo(), 2, RoundingMode.HALF_UP))
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

    private long countSituacao(java.util.List<FrequenciaAcademicaResumo> frequencias, String situacao) {
        return frequencias.stream()
                .filter(frequencia -> situacao.equals(frequencia.situacao()))
                .count();
    }

    private MatriculaAcademicoFrequenciaResponse toFrequenciaResponse(FrequenciaAcademicaResumo entity) {
        return new MatriculaAcademicoFrequenciaResponse(
                entity.id(),
                entity.aulaId(),
                entity.dataAula(),
                entity.disciplinaId(),
                entity.disciplinaNome(),
                entity.situacao(),
                entity.justificativa());
    }

    private MatriculaAcademicoNotaResponse toNotaResponse(NotaAcademicaResumo entity) {
        return new MatriculaAcademicoNotaResponse(
                entity.id(),
                entity.avaliacaoId(),
                entity.avaliacaoTitulo(),
                entity.dataAplicacao(),
                entity.disciplinaId(),
                entity.disciplinaNome(),
                entity.tipoAvaliacao(),
                entity.nota(),
                entity.valorMaximo(),
                entity.peso(),
                entity.observacao());
    }
}
