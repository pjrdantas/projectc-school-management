package br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "painel_widget")
public class PainelWidgetJpaEntity {

    @Id
    private UUID id;

    @Column(name = "escola_id", nullable = false)
    private UUID escolaId;

    @Column(name = "painel_id", nullable = false)
    private UUID painelId;

    @Column(nullable = false, length = 80)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "tipo_widget", nullable = false, length = 40)
    private String tipoWidget;

    @Column(nullable = false)
    private int ordem;

    @Column(name = "query_referencia", length = 150)
    private String queryReferencia;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PainelWidgetJpaEntity() {
    }

    public PainelWidgetJpaEntity(
            UUID id, UUID escolaId, UUID painelId, String codigo, String titulo, String descricao,
            String tipoWidget, int ordem, String queryReferencia, boolean ativo,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.escolaId = escolaId;
        this.painelId = painelId;
        this.codigo = codigo;
        this.titulo = titulo;
        this.descricao = descricao;
        this.tipoWidget = tipoWidget;
        this.ordem = ordem;
        this.queryReferencia = queryReferencia;
        this.ativo = ativo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getEscolaId() { return escolaId; }
    public UUID getPainelId() { return painelId; }
    public String getCodigo() { return codigo; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getTipoWidget() { return tipoWidget; }
    public int getOrdem() { return ordem; }
    public String getQueryReferencia() { return queryReferencia; }
    public boolean isAtivo() { return ativo; }

    public void atualizar(
            UUID painelId, String codigo, String titulo, String descricao, String tipoWidget, int ordem,
            String queryReferencia, boolean ativo, LocalDateTime updatedAt) {
        this.painelId = painelId;
        this.codigo = codigo;
        this.titulo = titulo;
        this.descricao = descricao;
        this.tipoWidget = tipoWidget;
        this.ordem = ordem;
        this.queryReferencia = queryReferencia;
        this.ativo = ativo;
        this.updatedAt = updatedAt;
    }
}
