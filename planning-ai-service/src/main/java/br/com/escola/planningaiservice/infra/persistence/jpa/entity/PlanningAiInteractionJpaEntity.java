package br.com.escola.planningaiservice.infra.persistence.jpa.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "planejamento_ia_interacao")
public class PlanningAiInteractionJpaEntity {

    @Id
    @Column(name = "id_planejamento_ia_interacao", nullable = false)
    private UUID id;

    @Column(name = "id_escola", nullable = false)
    private UUID escolaId;

    @Column(name = "id_planejamento_bimestral", nullable = false)
    private UUID planejamentoBimestralId;

    @Column(name = "id_usuario")
    private UUID usuarioId;

    @Column(name = "prompt_professor", nullable = false, columnDefinition = "TEXT")
    private String promptProfessor;

    @Column(name = "resposta_ia", nullable = false, columnDefinition = "TEXT")
    private String respostaIa;

    @Column(name = "modelo_ia", length = 120)
    private String modeloIa;

    @Column(name = "tokens_entrada")
    private Integer tokensEntrada;

    @Column(name = "tokens_saida")
    private Integer tokensSaida;

    @Column(name = "custo_estimado", precision = 12, scale = 4)
    private BigDecimal custoEstimado;

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

    public UUID getPlanejamentoBimestralId() {
        return planejamentoBimestralId;
    }

    public void setPlanejamentoBimestralId(UUID planejamentoBimestralId) {
        this.planejamentoBimestralId = planejamentoBimestralId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getPromptProfessor() {
        return promptProfessor;
    }

    public void setPromptProfessor(String promptProfessor) {
        this.promptProfessor = promptProfessor;
    }

    public String getRespostaIa() {
        return respostaIa;
    }

    public void setRespostaIa(String respostaIa) {
        this.respostaIa = respostaIa;
    }

    public String getModeloIa() {
        return modeloIa;
    }

    public void setModeloIa(String modeloIa) {
        this.modeloIa = modeloIa;
    }

    public Integer getTokensEntrada() {
        return tokensEntrada;
    }

    public void setTokensEntrada(Integer tokensEntrada) {
        this.tokensEntrada = tokensEntrada;
    }

    public Integer getTokensSaida() {
        return tokensSaida;
    }

    public void setTokensSaida(Integer tokensSaida) {
        this.tokensSaida = tokensSaida;
    }

    public BigDecimal getCustoEstimado() {
        return custoEstimado;
    }

    public void setCustoEstimado(BigDecimal custoEstimado) {
        this.custoEstimado = custoEstimado;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
