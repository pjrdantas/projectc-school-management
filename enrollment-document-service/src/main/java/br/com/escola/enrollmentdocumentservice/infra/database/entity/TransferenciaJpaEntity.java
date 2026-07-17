package br.com.escola.enrollmentdocumentservice.infra.database.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "student_transfer")
public class TransferenciaJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "student_id", nullable = false)
    private UUID alunoId;

    @Column(name = "source_school_id", nullable = false)
    private UUID escolaOrigemId;

    @Column(name = "source_grade", nullable = false, length = 120)
    private String serieOrigem;

    @Column(name = "source_school_year", nullable = false, length = 32)
    private String anoLetivoOrigem;

    @Column(name = "transfer_date")
    private LocalDate dataTransferencia;

    @Column(name = "transfer_reason", length = 300)
    private String motivoTransferencia;

    @Column(name = "source_status", length = 80)
    private String situacaoOrigem;

    @Column(name = "documents_delivered", length = 40)
    private String documentosEntregues;

    @Column(name = "note", length = 500)
    private String observacao;

    @Column(name = "transfer_type", length = 80)
    private String tipoTransferencia;

    @Column(name = "transfer_status", length = 80)
    private String statusTransferencia;

    @Column(name = "operator_user", length = 120)
    private String usuarioOperacao;

    @Column(name = "operation_at", nullable = false)
    private LocalDateTime dataHoraOperacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected TransferenciaJpaEntity() {
    }

    public TransferenciaJpaEntity(
            UUID id,
            UUID schoolId,
            UUID alunoId,
            UUID escolaOrigemId,
            String serieOrigem,
            String anoLetivoOrigem,
            LocalDate dataTransferencia,
            String motivoTransferencia,
            String situacaoOrigem,
            String documentosEntregues,
            String observacao,
            String tipoTransferencia,
            String statusTransferencia,
            String usuarioOperacao,
            LocalDateTime dataHoraOperacao,
            LocalDateTime createdAt) {
        this.id = id;
        this.schoolId = schoolId;
        this.alunoId = alunoId;
        this.escolaOrigemId = escolaOrigemId;
        this.serieOrigem = serieOrigem;
        this.anoLetivoOrigem = anoLetivoOrigem;
        this.dataTransferencia = dataTransferencia;
        this.motivoTransferencia = motivoTransferencia;
        this.situacaoOrigem = situacaoOrigem;
        this.documentosEntregues = documentosEntregues;
        this.observacao = observacao;
        this.tipoTransferencia = tipoTransferencia;
        this.statusTransferencia = statusTransferencia;
        this.usuarioOperacao = usuarioOperacao;
        this.dataHoraOperacao = dataHoraOperacao;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public UUID getAlunoId() { return alunoId; }
    public UUID getEscolaOrigemId() { return escolaOrigemId; }
    public String getSerieOrigem() { return serieOrigem; }
    public String getAnoLetivoOrigem() { return anoLetivoOrigem; }
    public LocalDate getDataTransferencia() { return dataTransferencia; }
    public String getMotivoTransferencia() { return motivoTransferencia; }
    public String getSituacaoOrigem() { return situacaoOrigem; }
    public String getDocumentosEntregues() { return documentosEntregues; }
    public String getObservacao() { return observacao; }
    public String getTipoTransferencia() { return tipoTransferencia; }
    public String getStatusTransferencia() { return statusTransferencia; }
    public String getUsuarioOperacao() { return usuarioOperacao; }
    public LocalDateTime getDataHoraOperacao() { return dataHoraOperacao; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
