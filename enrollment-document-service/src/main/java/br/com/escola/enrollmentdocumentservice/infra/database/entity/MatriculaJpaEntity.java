package br.com.escola.enrollmentdocumentservice.infra.database.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "enrollment_record")
public class MatriculaJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "student_id", nullable = false)
    private UUID alunoId;

    @Column(name = "class_id")
    private UUID turmaId;

    @Column(name = "school_name", length = 180)
    private String escolaNome;

    @Column(name = "grade_id")
    private UUID serieId;

    @Column(name = "grade_name", length = 120)
    private String serieNome;

    @Column(name = "term_id")
    private UUID periodoLetivoId;

    @Column(name = "status", length = 80)
    private String status;

    @Column(name = "enrollment_type", length = 80)
    private String tipoMatricula;

    @Column(name = "enrollment_date")
    private LocalDate dataMatricula;

    @Column(name = "note", length = 500)
    private String observacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    protected MatriculaJpaEntity() {
    }

    public MatriculaJpaEntity(
            UUID id,
            UUID schoolId,
            UUID alunoId,
            UUID turmaId,
            String escolaNome,
            UUID serieId,
            String serieNome,
            UUID periodoLetivoId,
            String status,
            String tipoMatricula,
            LocalDate dataMatricula,
            String observacao,
            LocalDateTime createdAt) {
        this.id = id;
        this.schoolId = schoolId;
        this.alunoId = alunoId;
        this.turmaId = turmaId;
        this.escolaNome = escolaNome;
        this.serieId = serieId;
        this.serieNome = serieNome;
        this.periodoLetivoId = periodoLetivoId;
        this.status = status;
        this.tipoMatricula = tipoMatricula;
        this.dataMatricula = dataMatricula;
        this.observacao = observacao;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public UUID getAlunoId() { return alunoId; }
    public UUID getTurmaId() { return turmaId; }
    public String getEscolaNome() { return escolaNome; }
    public UUID getSerieId() { return serieId; }
    public String getSerieNome() { return serieNome; }
    public UUID getPeriodoLetivoId() { return periodoLetivoId; }
    public String getStatus() { return status; }
    public String getTipoMatricula() { return tipoMatricula; }
    public LocalDate getDataMatricula() { return dataMatricula; }
    public String getObservacao() { return observacao; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public String getCancellationReason() { return cancellationReason; }

    public void atualizarDados(
            UUID turmaId,
            UUID serieId,
            UUID periodoLetivoId,
            String tipoMatricula,
            LocalDate dataMatricula,
            String observacao,
            LocalDateTime updatedAt) {
        this.turmaId = turmaId;
        this.serieId = serieId;
        this.periodoLetivoId = periodoLetivoId;
        this.tipoMatricula = tipoMatricula;
        this.dataMatricula = dataMatricula;
        this.observacao = observacao;
        this.updatedAt = updatedAt;
    }

    public void atualizarStatus(String status, String observacao, LocalDateTime updatedAt) {
        this.status = status;
        this.observacao = observacao;
        this.updatedAt = updatedAt;
    }

    public void cancelar(String motivo, LocalDateTime cancelledAt) {
        this.status = "CANCELADA";
        this.cancellationReason = motivo;
        this.cancelledAt = cancelledAt;
        this.updatedAt = cancelledAt;
    }
}
