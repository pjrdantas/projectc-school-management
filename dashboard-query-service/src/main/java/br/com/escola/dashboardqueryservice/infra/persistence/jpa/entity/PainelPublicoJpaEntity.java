package br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "painel_publico")
public class PainelPublicoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "escola_id", nullable = false)
    private UUID escolaId;

    @Column(nullable = false, length = 40)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String descricao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PainelPublicoJpaEntity() {
    }

    public PainelPublicoJpaEntity(
            UUID id, UUID escolaId, String codigo, String descricao, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.escolaId = escolaId;
        this.codigo = codigo;
        this.descricao = descricao;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getEscolaId() { return escolaId; }
    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }

    public void atualizar(String codigo, String descricao, LocalDateTime updatedAt) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.updatedAt = updatedAt;
    }
}
