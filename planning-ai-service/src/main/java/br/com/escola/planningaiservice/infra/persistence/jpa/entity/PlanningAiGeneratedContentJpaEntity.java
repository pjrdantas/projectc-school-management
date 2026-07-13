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
@Table(name = "planejamento_ia_conteudo_gerado")
public class PlanningAiGeneratedContentJpaEntity {

    @Id
    @Column(name = "id_planejamento_ia_conteudo_gerado", nullable = false)
    private UUID id;

    @Column(name = "id_escola", nullable = false)
    private UUID escolaId;

    @Column(name = "id_planejamento_bimestral", nullable = false)
    private UUID planejamentoBimestralId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_planejamento_ia_interacao")
    private PlanningAiInteractionJpaEntity interacao;

    @Column(name = "titulo", nullable = false, length = 180)
    private String titulo;

    @Column(name = "conteudo", nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "versao", nullable = false)
    private Integer versao;

    @Column(name = "hash_conteudo", length = 128)
    private String hashConteudo;

    @Column(name = "aprovado_pelo_professor", nullable = false)
    private boolean aprovadoPeloProfessor;

    @Column(name = "reutilizavel", nullable = false)
    private boolean reutilizavel;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "tipo_conteudo", nullable = false, length = 60)
    private String tipoConteudo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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

    public UUID getPlanejamentoBimestralId() {
        return planejamentoBimestralId;
    }

    public void setPlanejamentoBimestralId(UUID planejamentoBimestralId) {
        this.planejamentoBimestralId = planejamentoBimestralId;
    }

    public PlanningAiInteractionJpaEntity getInteracao() {
        return interacao;
    }

    public void setInteracao(PlanningAiInteractionJpaEntity interacao) {
        this.interacao = interacao;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public Integer getVersao() {
        return versao;
    }

    public void setVersao(Integer versao) {
        this.versao = versao;
    }

    public String getHashConteudo() {
        return hashConteudo;
    }

    public void setHashConteudo(String hashConteudo) {
        this.hashConteudo = hashConteudo;
    }

    public boolean isAprovadoPeloProfessor() {
        return aprovadoPeloProfessor;
    }

    public void setAprovadoPeloProfessor(boolean aprovadoPeloProfessor) {
        this.aprovadoPeloProfessor = aprovadoPeloProfessor;
    }

    public boolean isReutilizavel() {
        return reutilizavel;
    }

    public void setReutilizavel(boolean reutilizavel) {
        this.reutilizavel = reutilizavel;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTipoConteudo() {
        return tipoConteudo;
    }

    public void setTipoConteudo(String tipoConteudo) {
        this.tipoConteudo = tipoConteudo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
