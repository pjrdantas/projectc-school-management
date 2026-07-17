package br.com.escola.planningaiservice.infra.persistence.jpa.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "planejamento_ia_read_model_sync_state")
public class LeituraModeloSyncStateJpaEntity {

    @Id
    @Column(name = "id_sync_state", nullable = false, length = 512)
    private String id;

    @Column(name = "scope", nullable = false, length = 80)
    private String scope;

    @Column(name = "id_escola", nullable = false)
    private UUID escolaId;

    @Column(name = "id_referencia")
    private UUID referenciaId;

    @Column(name = "query_key", nullable = false, length = 255)
    private String queryKey;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public UUID getEscolaId() {
        return escolaId;
    }

    public void setEscolaId(UUID escolaId) {
        this.escolaId = escolaId;
    }

    public UUID getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(UUID referenciaId) {
        this.referenciaId = referenciaId;
    }

    public String getQueryKey() {
        return queryKey;
    }

    public void setQueryKey(String queryKey) {
        this.queryKey = queryKey;
    }

    public LocalDateTime getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(LocalDateTime syncedAt) {
        this.syncedAt = syncedAt;
    }
}

