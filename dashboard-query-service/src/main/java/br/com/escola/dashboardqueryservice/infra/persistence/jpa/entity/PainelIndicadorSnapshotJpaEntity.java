package br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "painel_indicador_snapshot")
public class PainelIndicadorSnapshotJpaEntity {

    @Id
    private UUID id;

    @Column(name = "escola_id", nullable = false)
    private UUID escolaId;

    @Column(name = "publico_id", nullable = false)
    private UUID publicoId;

    @Column(name = "codigo_indicador", nullable = false, length = 100)
    private String codigoIndicador;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(name = "valor_numeric", precision = 19, scale = 4)
    private BigDecimal valorNumeric;

    @Column(name = "valor_texto", length = 255)
    private String valorTexto;

    @Column(name = "escola_nome", length = 255)
    private String escolaNome;

    @Column(name = "referencia_data", nullable = false)
    private LocalDate referenciaData;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PainelIndicadorSnapshotJpaEntity() {
    }

    public PainelIndicadorSnapshotJpaEntity(
            UUID id, UUID escolaId, UUID publicoId, String codigoIndicador, String descricao,
            BigDecimal valorNumeric, String valorTexto, String escolaNome, LocalDate referenciaData,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.escolaId = escolaId;
        this.publicoId = publicoId;
        this.codigoIndicador = codigoIndicador;
        this.descricao = descricao;
        this.valorNumeric = valorNumeric;
        this.valorTexto = valorTexto;
        this.escolaNome = escolaNome;
        this.referenciaData = referenciaData;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getEscolaId() { return escolaId; }
    public UUID getPublicoId() { return publicoId; }
    public String getCodigoIndicador() { return codigoIndicador; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValorNumeric() { return valorNumeric; }
    public String getValorTexto() { return valorTexto; }
    public String getEscolaNome() { return escolaNome; }
    public LocalDate getReferenciaData() { return referenciaData; }

    public void atualizar(
            String descricao, BigDecimal valorNumeric, String valorTexto, String escolaNome, LocalDateTime updatedAt) {
        this.descricao = descricao;
        this.valorNumeric = valorNumeric;
        this.valorTexto = valorTexto;
        this.escolaNome = escolaNome;
        this.updatedAt = updatedAt;
    }
}
