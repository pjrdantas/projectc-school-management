package br.com.escola.planningaiservice.infra.persistence.jpa.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "planejamento_ia_conteudo_versao")
public class PlanningAiContentVersionJpaEntity {

    @Id
    @Column(name = "id_planejamento_ia_conteudo_versao", nullable = false)
    private UUID id;

    @Column(name = "id_escola", nullable = false)
    private UUID escolaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_planejamento_ia_conteudo_gerado", nullable = false)
    private PlanningAiGeneratedContentJpaEntity conteudoGerado;

    @Column(name = "alterado_por")
    private UUID alteradoPor;

    @Column(name = "numero_versao", nullable = false)
    private Integer numeroVersao;

    @Column(name = "conteudo", nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "motivo_alteracao", columnDefinition = "TEXT")
    private String motivoAlteracao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEscolaId() {
        return escolaId;
    }

    public void setEscolaId(UUID escolaId) {
        this.escolaId = escolaId;
    }

    public PlanningAiGeneratedContentJpaEntity getConteudoGerado() {
        return conteudoGerado;
    }

    public void setConteudoGerado(PlanningAiGeneratedContentJpaEntity conteudoGerado) {
        this.conteudoGerado = conteudoGerado;
    }

    public UUID getAlteradoPor() {
        return alteradoPor;
    }

    public void setAlteradoPor(UUID alteradoPor) {
        this.alteradoPor = alteradoPor;
    }

    public Integer getNumeroVersao() {
        return numeroVersao;
    }

    public void setNumeroVersao(Integer numeroVersao) {
        this.numeroVersao = numeroVersao;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getMotivoAlteracao() {
        return motivoAlteracao;
    }

    public void setMotivoAlteracao(String motivoAlteracao) {
        this.motivoAlteracao = motivoAlteracao;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
