package br.com.escola.planningaiservice.infra.persistence.jpa.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "planejamento_bimestral_avaliacao")
public class PlanejamentoBimestralAvaliacaoJpaEntity {

    @Id
    @Column(name = "id_planejamento_bimestral_avaliacao", nullable = false)
    private UUID id;

    @Column(name = "id_planejamento_bimestral", nullable = false)
    private UUID planejamentoBimestralId;

    @Column(nullable = false, length = 180)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "data_prevista")
    private LocalDate dataPrevista;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal peso;

    @Column(name = "valor_maximo", precision = 12, scale = 4)
    private BigDecimal valorMaximo;

    @Column(name = "tipo_avaliacao", nullable = false, length = 50)
    private String tipoAvaliacao;

    @Column(name = "conteudo_cobrado", columnDefinition = "TEXT")
    private String conteudoCobrado;

    @Column(name = "orientacao_aplicacao", columnDefinition = "TEXT")
    private String orientacaoAplicacao;

    @Column(name = "chave_idempotencia", nullable = false, length = 128)
    private String chaveIdempotencia;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PlanejamentoBimestralAvaliacaoJpaEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID value) { id = value; }
    public UUID getPlanejamentoBimestralId() { return planejamentoBimestralId; }
    public void setPlanejamentoBimestralId(UUID value) { planejamentoBimestralId = value; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String value) { titulo = value; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String value) { descricao = value; }
    public LocalDate getDataPrevista() { return dataPrevista; }
    public void setDataPrevista(LocalDate value) { dataPrevista = value; }
    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal value) { peso = value; }
    public BigDecimal getValorMaximo() { return valorMaximo; }
    public void setValorMaximo(BigDecimal value) { valorMaximo = value; }
    public String getTipoAvaliacao() { return tipoAvaliacao; }
    public void setTipoAvaliacao(String value) { tipoAvaliacao = value; }
    public void setConteudoCobrado(String value) { conteudoCobrado = value; }
    public void setOrientacaoAplicacao(String value) { orientacaoAplicacao = value; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String value) { chaveIdempotencia = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
}
