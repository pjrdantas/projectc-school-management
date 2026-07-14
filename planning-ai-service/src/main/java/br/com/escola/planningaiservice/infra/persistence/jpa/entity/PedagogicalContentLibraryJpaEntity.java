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
@Table(name = "biblioteca_conteudo_pedagogico")
public class PedagogicalContentLibraryJpaEntity {

    @Id
    @Column(name = "id_biblioteca_conteudo_pedagogico", nullable = false)
    private UUID id;

    @Column(name = "id_escola", nullable = false)
    private UUID escolaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conteudo_origem")
    private PlanningAiGeneratedContentJpaEntity conteudoOrigem;

    @Column(name = "id_professor")
    private UUID professorId;

    @Column(name = "id_disciplina")
    private UUID disciplinaId;

    @Column(name = "tipo_conteudo", nullable = false, length = 60)
    private String tipoConteudo;

    @Column(name = "titulo", nullable = false, length = 180)
    private String titulo;

    @Column(name = "tema", length = 180)
    private String tema;

    @Column(name = "conteudo", nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "origem", nullable = false, length = 40)
    private String origem;

    @Column(name = "reutilizavel", nullable = false)
    private boolean reutilizavel;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

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

    public PlanningAiGeneratedContentJpaEntity getConteudoOrigem() {
        return conteudoOrigem;
    }

    public void setConteudoOrigem(PlanningAiGeneratedContentJpaEntity conteudoOrigem) {
        this.conteudoOrigem = conteudoOrigem;
    }

    public UUID getProfessorId() {
        return professorId;
    }

    public void setProfessorId(UUID professorId) {
        this.professorId = professorId;
    }

    public UUID getDisciplinaId() {
        return disciplinaId;
    }

    public void setDisciplinaId(UUID disciplinaId) {
        this.disciplinaId = disciplinaId;
    }

    public String getTipoConteudo() {
        return tipoConteudo;
    }

    public void setTipoConteudo(String tipoConteudo) {
        this.tipoConteudo = tipoConteudo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
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
