package br.com.escola.transferencia.adapter.out.persistence.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.aluno.adapter.out.persistence.entity.AlunoEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "transferencia_aluno")
public class TransferenciaAlunoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_transferencia_aluno")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_aluno", nullable = false)
    private AlunoEntity aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_escola_origem", nullable = false)
    private EscolaOrigemEntity escolaOrigem;

    @Column(name = "id_tipo_transferencia", nullable = false)
    private UUID tipoTransferenciaId;

    @Column(name = "id_status_transferencia", nullable = false)
    private UUID statusTransferenciaId;

    @Column(name = "serie_origem", nullable = false, length = 80)
    private String serieOrigem;

    @Column(name = "ano_letivo_origem", nullable = false, length = 20)
    private String anoLetivoOrigem;

    @Column(name = "data_solicitacao")
    private LocalDate dataTransferencia;

    @Column(name = "motivo_transferencia", columnDefinition = "TEXT")
    private String motivoTransferencia;

    @Transient
    private String situacaoOrigem;

    @Transient
    private String documentosEntregues;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Transient
    private String tipoTransferencia;

    @Transient
    private String statusTransferencia;

    @Transient
    private String usuarioOperacao;

    @Transient
    private LocalDateTime dataHoraOperacao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UUID getId() { return id; }
    public AlunoEntity getAluno() { return aluno; }
    public void setAluno(AlunoEntity aluno) { this.aluno = aluno; }
    public EscolaOrigemEntity getEscolaOrigem() { return escolaOrigem; }
    public void setEscolaOrigem(EscolaOrigemEntity escolaOrigem) { this.escolaOrigem = escolaOrigem; }
    public UUID getTipoTransferenciaId() { return tipoTransferenciaId; }
    public void setTipoTransferenciaId(UUID tipoTransferenciaId) { this.tipoTransferenciaId = tipoTransferenciaId; }
    public UUID getStatusTransferenciaId() { return statusTransferenciaId; }
    public void setStatusTransferenciaId(UUID statusTransferenciaId) { this.statusTransferenciaId = statusTransferenciaId; }
    public String getSerieOrigem() { return serieOrigem; }
    public void setSerieOrigem(String serieOrigem) { this.serieOrigem = serieOrigem; }
    public String getAnoLetivoOrigem() { return anoLetivoOrigem; }
    public void setAnoLetivoOrigem(String anoLetivoOrigem) { this.anoLetivoOrigem = anoLetivoOrigem; }
    public LocalDate getDataTransferencia() { return dataTransferencia; }
    public void setDataTransferencia(LocalDate dataTransferencia) { this.dataTransferencia = dataTransferencia; }
    public String getMotivoTransferencia() { return motivoTransferencia; }
    public void setMotivoTransferencia(String motivoTransferencia) { this.motivoTransferencia = motivoTransferencia; }
    public String getSituacaoOrigem() { return situacaoOrigem; }
    public void setSituacaoOrigem(String situacaoOrigem) { this.situacaoOrigem = situacaoOrigem; }
    public String getDocumentosEntregues() { return documentosEntregues; }
    public void setDocumentosEntregues(String documentosEntregues) { this.documentosEntregues = documentosEntregues; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getTipoTransferencia() { return tipoTransferencia; }
    public void setTipoTransferencia(String tipoTransferencia) { this.tipoTransferencia = tipoTransferencia; }
    public String getStatusTransferencia() { return statusTransferencia; }
    public void setStatusTransferencia(String statusTransferencia) { this.statusTransferencia = statusTransferencia; }
    public String getUsuarioOperacao() { return usuarioOperacao; }
    public void setUsuarioOperacao(String usuarioOperacao) { this.usuarioOperacao = usuarioOperacao; }
    public LocalDateTime getDataHoraOperacao() { return dataHoraOperacao; }
    public void setDataHoraOperacao(LocalDateTime dataHoraOperacao) { this.dataHoraOperacao = dataHoraOperacao; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (dataHoraOperacao == null) {
            dataHoraOperacao = LocalDateTime.now();
        }
        if (tipoTransferencia == null || tipoTransferencia.isBlank()) {
            tipoTransferencia = "ENTRADA";
        }
        if (statusTransferencia == null || statusTransferencia.isBlank()) {
            statusTransferencia = "EM_ANDAMENTO";
        }
    }
}
