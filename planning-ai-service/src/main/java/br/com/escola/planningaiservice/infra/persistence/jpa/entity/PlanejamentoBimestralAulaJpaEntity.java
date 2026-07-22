package br.com.escola.planningaiservice.infra.persistence.jpa.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "planejamento_bimestral_aula")
public class PlanejamentoBimestralAulaJpaEntity {

    @Id
    @Column(name = "id_planejamento_bimestral_aula", nullable = false)
    private UUID id;

    @Column(name = "id_planejamento_bimestral", nullable = false)
    private UUID planejamentoBimestralId;

    @Column(name = "numero_aula", nullable = false)
    private int numeroAula;

    @Column(name = "tema_aula", nullable = false, length = 180)
    private String temaAula;

    @Column(name = "objetivo_aula", columnDefinition = "TEXT")
    private String objetivoAula;

    @Column(name = "conteudo_previsto", columnDefinition = "TEXT")
    private String conteudoPrevisto;

    @Column(columnDefinition = "TEXT")
    private String metodologia;

    @Column(columnDefinition = "TEXT")
    private String recursos;

    @Column(name = "atividade_prevista", columnDefinition = "TEXT")
    private String atividadePrevista;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PlanejamentoBimestralAulaJpaEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID value) { id = value; }
    public UUID getPlanejamentoBimestralId() { return planejamentoBimestralId; }
    public void setPlanejamentoBimestralId(UUID value) { planejamentoBimestralId = value; }
    public int getNumeroAula() { return numeroAula; }
    public void setNumeroAula(int value) { numeroAula = value; }
    public String getTemaAula() { return temaAula; }
    public void setTemaAula(String value) { temaAula = value; }
    public String getObjetivoAula() { return objetivoAula; }
    public void setObjetivoAula(String value) { objetivoAula = value; }
    public String getConteudoPrevisto() { return conteudoPrevisto; }
    public void setConteudoPrevisto(String value) { conteudoPrevisto = value; }
    public String getMetodologia() { return metodologia; }
    public void setMetodologia(String value) { metodologia = value; }
    public String getRecursos() { return recursos; }
    public void setRecursos(String value) { recursos = value; }
    public String getAtividadePrevista() { return atividadePrevista; }
    public void setAtividadePrevista(String value) { atividadePrevista = value; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String value) { observacao = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
}
