package br.com.escola.accesscontrol.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "sessao_autenticacao")
public class SessaoAutenticacaoEntity {

    @Id
    @Column(name = "id_sessao_autenticacao", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @Column(name = "refresh_token_hash", nullable = false, length = 255)
    private String refreshTokenHash;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "revogado", nullable = false)
    private boolean revogado;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected SessaoAutenticacaoEntity() {}

    public SessaoAutenticacaoEntity(UsuarioEntity usuario, String refreshTokenHash, LocalDateTime expiraEm) {
        this.usuario = usuario;
        this.refreshTokenHash = refreshTokenHash;
        this.expiraEm = expiraEm;
        this.revogado = false;
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UsuarioEntity getUsuario() { return usuario; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public LocalDateTime getExpiraEm() { return expiraEm; }
    public boolean isRevogado() { return revogado; }

    public void renovar(String novoHash, LocalDateTime novaExpiracao) {
        this.refreshTokenHash = novoHash;
        this.expiraEm = novaExpiracao;
        this.revogado = false;
    }

    public void revogar() { this.revogado = true; }
}
