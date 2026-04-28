package br.com.escola.enrollment.adapter.out.persistence;

import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import br.com.escola.academiccatalog.adapter.out.persistence.entity.PeriodoLetivoEntity;
import br.com.escola.academiccatalog.adapter.out.persistence.entity.TurmaEntity;
import br.com.escola.enrollment.adapter.out.persistence.entity.MatriculaEntity;
import br.com.escola.enrollment.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.enrollment.application.dto.MatriculaFiltro;
import br.com.escola.enrollment.application.dto.MatriculaOutput;
import br.com.escola.enrollment.application.port.out.MatriculaGateway;
import br.com.escola.enrollment.domain.MatriculaStatus;
import br.com.escola.studentmanagement.adapter.out.persistence.entity.AlunoEntity;
import jakarta.persistence.EntityManager;

@Component
public class MatriculaPersistenceGateway implements MatriculaGateway {

    private final MatriculaJpaRepository matriculaJpaRepository;
    private final EntityManager entityManager;

    public MatriculaPersistenceGateway(MatriculaJpaRepository matriculaJpaRepository, EntityManager entityManager) {
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public MatriculaOutput save(UUID alunoId, UUID turmaId, UUID periodoLetivoId, MatriculaStatus status) {
        AlunoEntity aluno = entityManager.getReference(AlunoEntity.class, alunoId);
        TurmaEntity turma = entityManager.getReference(TurmaEntity.class, turmaId);
        PeriodoLetivoEntity periodoLetivo = entityManager.getReference(PeriodoLetivoEntity.class, periodoLetivoId);

        MatriculaEntity matriculaEntity = new MatriculaEntity();
        matriculaEntity.setAluno(aluno);
        matriculaEntity.setTurma(turma);
        matriculaEntity.setPeriodoLetivo(periodoLetivo);
        matriculaEntity.setStatus(status);
        matriculaEntity.setCreatedAt(LocalDateTime.now());

        return toOutput(matriculaJpaRepository.save(matriculaEntity));
    }

    @Override
    public List<MatriculaOutput> findByFiltro(MatriculaFiltro filtro, MatriculaStatus status) {
        Specification<MatriculaEntity> spec = (root, query, cb) -> cb.conjunction();

        if (filtro.alunoId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("aluno").get("id"), filtro.alunoId()));
        }
        if (filtro.turmaId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("turma").get("id"), filtro.turmaId()));
        }
        if (filtro.periodoLetivoId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("periodoLetivo").get("id"), filtro.periodoLetivoId()));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return matriculaJpaRepository.findAll(spec).stream().map(this::toOutput).toList();
    }

    private MatriculaOutput toOutput(MatriculaEntity entity) {
        return new MatriculaOutput(
                entity.getId(),
                entity.getAluno().getId(),
                entity.getTurma().getId(),
                entity.getPeriodoLetivo().getId(),
                entity.getStatus().name(),
                entity.getCreatedAt());
    }
}
