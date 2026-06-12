package br.com.escola.seguranca.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_escola")
    private EscolaEntity escola;

    @Column(name = "refresh_token_hash", nullable = false, length = 255)
    private String refreshTokenHash;

    @Column(name = "access_token_hash", length = 255)
    private String accessTokenHash;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "access_expira_em")
    private LocalDateTime accessExpiraEm;

    @Column(name = "revogado", nullable = false)
    private boolean revogado;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected SessaoAutenticacaoEntity() {}

    public SessaoAutenticacaoEntity(
            UsuarioEntity usuario,
            EscolaEntity escola,
            String refreshTokenHash,
            String accessTokenHash,
            LocalDateTime expiraEm,
            LocalDateTime accessExpiraEm) {
        this.usuario = usuario;
        this.escola = escola;
        this.refreshTokenHash = refreshTokenHash;
        this.accessTokenHash = accessTokenHash;
        this.expiraEm = expiraEm;
        this.accessExpiraEm = accessExpiraEm;
        this.revogado = false;
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UsuarioEntity getUsuario() { return usuario; }
    public EscolaEntity getEscola() { return escola; }
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public String getAccessTokenHash() { return accessTokenHash; }
    public LocalDateTime getExpiraEm() { return expiraEm; }
    public LocalDateTime getAccessExpiraEm() { return accessExpiraEm; }
    public boolean isRevogado() { return revogado; }

    public void renovar(String novoRefreshHash, String novoAccessHash, LocalDateTime novaExpiracaoRefresh, LocalDateTime novaExpiracaoAccess) {
        this.refreshTokenHash = novoRefreshHash;
        this.accessTokenHash = novoAccessHash;
        this.expiraEm = novaExpiracaoRefresh;
        this.accessExpiraEm = novaExpiracaoAccess;
        this.revogado = false;
    }

    public void revogar() { this.revogado = true; }
}
