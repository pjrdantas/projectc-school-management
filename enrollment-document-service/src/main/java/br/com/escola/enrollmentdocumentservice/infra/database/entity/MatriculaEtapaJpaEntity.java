package br.com.escola.enrollmentdocumentservice.infra.database.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "enrollment_step")
public class MatriculaEtapaJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "enrollment_id", nullable = false)
    private UUID matriculaId;

    @Column(name = "description", nullable = false, length = 180)
    private String descricao;

    @Column(name = "step_order")
    private Integer ordem;

    @Column(name = "status", length = 80)
    private String status;

    @Column(name = "started_at")
    private LocalDateTime dataInicio;

    @Column(name = "completed_at")
    private LocalDateTime dataConclusao;

    @Column(name = "note", length = 500)
    private String observacao;

    protected MatriculaEtapaJpaEntity() {
    }

    public MatriculaEtapaJpaEntity(
            UUID id,
            UUID matriculaId,
            String descricao,
            Integer ordem,
            String status,
            LocalDateTime dataInicio,
            LocalDateTime dataConclusao,
            String observacao) {
        this.id = id;
        this.matriculaId = matriculaId;
        this.descricao = descricao;
        this.ordem = ordem;
        this.status = status;
        this.dataInicio = dataInicio;
        this.dataConclusao = dataConclusao;
        this.observacao = observacao;
    }

    public UUID getId() { return id; }
    public UUID getMatriculaId() { return matriculaId; }
    public String getDescricao() { return descricao; }
    public Integer getOrdem() { return ordem; }
    public String getStatus() { return status; }
    public LocalDateTime getDataInicio() { return dataInicio; }
    public LocalDateTime getDataConclusao() { return dataConclusao; }
    public String getObservacao() { return observacao; }
}
