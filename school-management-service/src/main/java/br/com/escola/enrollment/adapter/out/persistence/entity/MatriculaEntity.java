package br.com.escola.enrollment.adapter.out.persistence.entity;

import java.util.UUID;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.escola.academiccatalog.adapter.out.persistence.entity.PeriodoLetivoEntity;
import br.com.escola.academiccatalog.adapter.out.persistence.entity.TurmaEntity;
import br.com.escola.studentmanagement.adapter.out.persistence.entity.AlunoEntity;
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

@Entity
@Table(name = "matricula")
public class MatriculaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_matricula")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_aluno", nullable = false)
    private AlunoEntity aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_turma", nullable = false)
    private TurmaEntity turma;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_periodo_letivo", nullable = false)
    private PeriodoLetivoEntity periodoLetivo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_status_matricula", nullable = false)
    private StatusMatriculaEntity status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_matricula", nullable = false)
    private TipoMatriculaEntity tipoMatricula;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDate dataSolicitacao;

    @Column(name = "data_efetivacao")
    private LocalDate dataEfetivacao;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public AlunoEntity getAluno() {
        return aluno;
    }

    public void setAluno(AlunoEntity aluno) {
        this.aluno = aluno;
    }

    public TurmaEntity getTurma() {
        return turma;
    }

    public void setTurma(TurmaEntity turma) {
        this.turma = turma;
    }

    public PeriodoLetivoEntity getPeriodoLetivo() {
        return periodoLetivo;
    }

    public void setPeriodoLetivo(PeriodoLetivoEntity periodoLetivo) {
        this.periodoLetivo = periodoLetivo;
    }

    public StatusMatriculaEntity getStatus() {
        return status;
    }

    public void setStatus(StatusMatriculaEntity status) {
        this.status = status;
    }

    public TipoMatriculaEntity getTipoMatricula() {
        return tipoMatricula;
    }

    public void setTipoMatricula(TipoMatriculaEntity tipoMatricula) {
        this.tipoMatricula = tipoMatricula;
    }

    public LocalDate getDataMatricula() {
        return dataSolicitacao;
    }

    public LocalDate getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDate dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public LocalDate getDataEfetivacao() {
        return dataEfetivacao;
    }

    public void setDataEfetivacao(LocalDate dataEfetivacao) {
        this.dataEfetivacao = dataEfetivacao;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        if (dataSolicitacao == null) {
            dataSolicitacao = LocalDate.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
