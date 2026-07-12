package br.com.escola.historico.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.avaliacao.adapter.out.persistence.repository.NotaAlunoJpaRepository;
import br.com.escola.frequencia.adapter.out.persistence.repository.FrequenciaAlunoJpaRepository;
import br.com.escola.historico.application.dto.internal.FrequenciaAcademicaResumo;
import br.com.escola.historico.application.dto.internal.NotaAcademicaResumo;
import br.com.escola.historico.application.dto.internal.RendimentoAcademicoResumo;
import br.com.escola.historico.application.port.internal.RendimentoAcademicoPort;

@Service
public class RendimentoAcademicoService implements RendimentoAcademicoPort {

    private final FrequenciaAlunoJpaRepository frequenciaAlunoJpaRepository;
    private final NotaAlunoJpaRepository notaAlunoJpaRepository;

    public RendimentoAcademicoService(
            FrequenciaAlunoJpaRepository frequenciaAlunoJpaRepository,
            NotaAlunoJpaRepository notaAlunoJpaRepository) {
        this.frequenciaAlunoJpaRepository = frequenciaAlunoJpaRepository;
        this.notaAlunoJpaRepository = notaAlunoJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public RendimentoAcademicoResumo consultarPorMatricula(UUID matriculaId, UUID escolaId) {
        return new RendimentoAcademicoResumo(
                frequenciaAlunoJpaRepository.findByMatricula_IdAndMatricula_Turma_Escola_Id(matriculaId, escolaId).stream()
                        .map(entity -> new FrequenciaAcademicaResumo(
                                entity.getId(),
                                entity.getAula().getId(),
                                entity.getAula().getDataAula(),
                                entity.getAula().getProfessorTurmaDisciplina().getTurmaDisciplina().getDisciplina().getId(),
                                entity.getAula().getProfessorTurmaDisciplina().getTurmaDisciplina().getDisciplina().getNome(),
                                entity.getSituacaoFrequencia().getCodigo(),
                                entity.getJustificativa()))
                        .toList(),
                notaAlunoJpaRepository.findByMatricula_IdAndMatricula_Turma_Escola_Id(matriculaId, escolaId).stream()
                        .map(entity -> new NotaAcademicaResumo(
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
                                entity.getObservacao()))
                        .toList());
    }
}
