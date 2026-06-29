package br.com.escola.professorservice.infra.database.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "professor_turma_disciplina")
public class ProfessorAlocacaoShadowJpaEntity {

    @Id
    @Column(name = "id_professor_turma_disciplina", nullable = false)
    private UUID id;

    @Column(name = "id_professor", nullable = false)
    private UUID professorId;

    @Column(name = "id_turma_disciplina", nullable = false)
    private UUID turmaDisciplinaId;

    @Column(name = "id_turma", nullable = false)
    private UUID turmaId;

    @Column(name = "nome_turma", length = 120)
    private String turmaNome;

    @Column(name = "id_disciplina", nullable = false)
    private UUID disciplinaId;

    @Column(name = "nome_disciplina", length = 120)
    private String disciplinaNome;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected ProfessorAlocacaoShadowJpaEntity() {
    }

    public ProfessorAlocacaoShadowJpaEntity(
            UUID id,
            UUID professorId,
            UUID turmaDisciplinaId,
            UUID turmaId,
            String turmaNome,
            UUID disciplinaId,
            String disciplinaNome,
            LocalDate dataInicio,
            LocalDate dataFim,
            boolean ativo,
            LocalDateTime createdAt) {
        this.id = id;
        this.professorId = professorId;
        this.turmaDisciplinaId = turmaDisciplinaId;
        this.turmaId = turmaId;
        this.turmaNome = turmaNome;
        this.disciplinaId = disciplinaId;
        this.disciplinaNome = disciplinaNome;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.ativo = ativo;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProfessorId() {
        return professorId;
    }

    public UUID getTurmaDisciplinaId() {
        return turmaDisciplinaId;
    }

    public UUID getTurmaId() {
        return turmaId;
    }

    public String getTurmaNome() {
        return turmaNome;
    }

    public UUID getDisciplinaId() {
        return disciplinaId;
    }

    public String getDisciplinaNome() {
        return disciplinaNome;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
