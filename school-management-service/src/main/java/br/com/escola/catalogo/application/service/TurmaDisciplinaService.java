package br.com.escola.catalogo.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalogo.adapter.in.web.dto.TurmaDisciplinaRequest;
import br.com.escola.catalogo.adapter.in.web.dto.TurmaDisciplinaResponse;
import br.com.escola.catalogo.adapter.out.persistence.entity.DisciplinaEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaDisciplinaEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.DisciplinaJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurmaDisciplinaJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurmaJpaRepository;
import br.com.escola.catalogo.domain.exception.DisciplinaNaoEncontradaException;
import br.com.escola.catalogo.domain.exception.TurmaDisciplinaJaCadastradaException;
import br.com.escola.catalogo.domain.exception.TurmaNaoEncontradaException;

@Service
public class TurmaDisciplinaService {

    private final TurmaJpaRepository turmaJpaRepository;
    private final DisciplinaJpaRepository disciplinaJpaRepository;
    private final TurmaDisciplinaJpaRepository turmaDisciplinaJpaRepository;

    public TurmaDisciplinaService(
            TurmaJpaRepository turmaJpaRepository,
            DisciplinaJpaRepository disciplinaJpaRepository,
            TurmaDisciplinaJpaRepository turmaDisciplinaJpaRepository) {
        this.turmaJpaRepository = turmaJpaRepository;
        this.disciplinaJpaRepository = disciplinaJpaRepository;
        this.turmaDisciplinaJpaRepository = turmaDisciplinaJpaRepository;
    }

    @Transactional
    public TurmaDisciplinaResponse vincular(UUID turmaId, TurmaDisciplinaRequest request) {
        TurmaEntity turma = turmaJpaRepository.findById(turmaId)
                .orElseThrow(() -> new TurmaNaoEncontradaException(turmaId));
        DisciplinaEntity disciplina = disciplinaJpaRepository.findById(request.disciplinaId())
                .orElseThrow(() -> new DisciplinaNaoEncontradaException(request.disciplinaId()));

        turmaDisciplinaJpaRepository.findByTurmaIdAndDisciplinaId(turmaId, request.disciplinaId())
                .ifPresent(vinculo -> {
                    throw new TurmaDisciplinaJaCadastradaException(turmaId, request.disciplinaId());
                });

        TurmaDisciplinaEntity entity = TurmaDisciplinaEntity.builder()
                .turma(turma)
                .disciplina(disciplina)
                .cargaHoraria(request.cargaHoraria())
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(turmaDisciplinaJpaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<TurmaDisciplinaResponse> listarPorTurma(UUID turmaId) {
        if (!turmaJpaRepository.existsById(turmaId)) {
            throw new TurmaNaoEncontradaException(turmaId);
        }
        return turmaDisciplinaJpaRepository.findByTurmaId(turmaId).stream()
                .map(this::toResponse)
                .toList();
    }

    private TurmaDisciplinaResponse toResponse(TurmaDisciplinaEntity entity) {
        return new TurmaDisciplinaResponse(
                entity.getId(),
                entity.getTurma().getId(),
                entity.getDisciplina().getId(),
                entity.getDisciplina().getNome(),
                entity.getCargaHoraria(),
                entity.getCreatedAt());
    }
}
