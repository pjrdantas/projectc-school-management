package br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "painel_configuracao")
public class PainelConfiguracaoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "escola_id", nullable = false)
    private UUID escolaId;

    @Column(name = "publico_id", nullable = false)
    private UUID publicoId;

    @Column(nullable = false, length = 80)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PainelConfiguracaoJpaEntity() {
    }

    public PainelConfiguracaoJpaEntity(
            UUID id, UUID escolaId, UUID publicoId, String codigo, String nome, String descricao,
            boolean ativo, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.escolaId = escolaId;
        this.publicoId = publicoId;
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = ativo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getEscolaId() { return escolaId; }
    public UUID getPublicoId() { return publicoId; }
    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public boolean isAtivo() { return ativo; }

    public void atualizar(UUID publicoId, String codigo, String nome, String descricao, boolean ativo, LocalDateTime updatedAt) {
        this.publicoId = publicoId;
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = ativo;
        this.updatedAt = updatedAt;
    }
}
