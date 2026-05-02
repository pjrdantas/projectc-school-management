package br.com.escola.accesscontrol.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "perfil")
public class PerfilEntity {

    @Id
    @Column(name = "id_perfil", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "perfis")
    private Set<UsuarioEntity> usuarios = new HashSet<>();

    @ManyToMany
    @jakarta.persistence.JoinTable(name = "perfil_permissao",
            joinColumns = @jakarta.persistence.JoinColumn(name = "id_perfil"),
            inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "id_permissao"))
    private Set<PermissaoEntity> permissoes = new HashSet<>();

    protected PerfilEntity() {}

    public PerfilEntity(UUID id, String codigo, String nome, String descricao) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Set<UsuarioEntity> getUsuarios() { return usuarios; }
    public Set<PermissaoEntity> getPermissoes() { return permissoes; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setNome(String nome) { this.nome = nome; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
