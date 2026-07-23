package br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "painel_usuario_preferencia")
public class PainelUsuarioPreferenciaJpaEntity {

    @Id
    private UUID id;

    @Column(name = "escola_id", nullable = false)
    private UUID escolaId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "widget_id", nullable = false)
    private UUID widgetId;

    @Column(nullable = false)
    private boolean visivel;

    private Integer ordem;

    @Column(name = "configuracao_json", columnDefinition = "TEXT")
    private String configuracaoJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PainelUsuarioPreferenciaJpaEntity() {
    }

    public PainelUsuarioPreferenciaJpaEntity(
            UUID id, UUID escolaId, UUID usuarioId, UUID widgetId, boolean visivel, Integer ordem,
            String configuracaoJson, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.escolaId = escolaId;
        this.usuarioId = usuarioId;
        this.widgetId = widgetId;
        this.visivel = visivel;
        this.ordem = ordem;
        this.configuracaoJson = configuracaoJson;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getEscolaId() { return escolaId; }
    public UUID getUsuarioId() { return usuarioId; }
    public UUID getWidgetId() { return widgetId; }
    public boolean isVisivel() { return visivel; }
    public Integer getOrdem() { return ordem; }
    public String getConfiguracaoJson() { return configuracaoJson; }

    public void atualizar(boolean visivel, Integer ordem, String configuracaoJson, LocalDateTime updatedAt) {
        this.visivel = visivel;
        this.ordem = ordem;
        this.configuracaoJson = configuracaoJson;
        this.updatedAt = updatedAt;
    }
}
