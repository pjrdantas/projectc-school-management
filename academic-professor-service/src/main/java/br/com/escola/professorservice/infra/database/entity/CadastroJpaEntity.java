package br.com.escola.professorservice.infra.database.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "professor")
public class CadastroJpaEntity {

    @Id
    @Column(name = "id_professor", nullable = false)
    private UUID id;

    @Column(name = "id_pessoa", nullable = false)
    private UUID pessoaId;

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    @Column(name = "id_escola", nullable = false)
    private UUID escolaId;

    @Column(name = "escola_nome", length = 120)
    private String escolaNome;

    @Column(name = "registro_profissional", length = 80)
    private String registroProfissional;

    @Column(name = "formacao", length = 150)
    private String formacao;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "id_usuario")
    private UUID usuarioId;

    protected CadastroJpaEntity() {
    }

    public CadastroJpaEntity(
            UUID id,
            UUID pessoaId,
            String nomeCompleto,
            UUID escolaId,
            String escolaNome,
            String registroProfissional,
            String formacao,
            Boolean ativo,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UUID usuarioId) {
        this.id = id;
        this.pessoaId = pessoaId;
        this.nomeCompleto = nomeCompleto;
        this.escolaId = escolaId;
        this.escolaNome = escolaNome;
        this.registroProfissional = registroProfissional;
        this.formacao = formacao;
        this.ativo = ativo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.usuarioId = usuarioId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPessoaId() {
        return pessoaId;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public UUID getEscolaId() {
        return escolaId;
    }

    public String getEscolaNome() {
        return escolaNome;
    }

    public String getRegistroProfissional() {
        return registroProfissional;
    }

    public String getFormacao() {
        return formacao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }
}

