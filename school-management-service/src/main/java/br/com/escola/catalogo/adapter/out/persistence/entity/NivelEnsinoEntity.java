package br.com.escola.catalogo.adapter.out.persistence.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "nivel_ensino")
public class NivelEnsinoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_nivel_ensino")
    private UUID id;

    @Column(name = "codigo", nullable = false, length = 60)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 150)
    private String descricao;

    public UUID getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }
}

