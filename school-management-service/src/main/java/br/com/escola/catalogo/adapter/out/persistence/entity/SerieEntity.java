package br.com.escola.catalogo.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "serie")
public class SerieEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_serie")
    private UUID id;

    @Column(name = "nome", nullable = false, length = 80)
    private String nome;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    @ManyToOne
    @JoinColumn(name = "id_nivel_ensino")
    private NivelEnsinoEntity nivelEnsino;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public String getNivelEnsino() {
        return nivelEnsino == null ? null : nivelEnsino.getCodigo();
    }

    public NivelEnsinoEntity getNivelEnsinoEntity() {
        return nivelEnsino;
    }

    public void setNivelEnsino(NivelEnsinoEntity nivelEnsino) {
        this.nivelEnsino = nivelEnsino;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
