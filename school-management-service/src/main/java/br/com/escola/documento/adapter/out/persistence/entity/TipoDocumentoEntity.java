package br.com.escola.documento.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipo_documento")
public class TipoDocumentoEntity {

    @Id
    @Column(name = "id_tipo_documento", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, length = 80)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 150)
    private String descricao;

    @Column(name = "obrigatorio_padrao", nullable = false)
    private boolean obrigatorioPadrao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isObrigatorioPadrao() {
        return obrigatorioPadrao;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
