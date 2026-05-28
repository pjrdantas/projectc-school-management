package br.com.escola.schoolhistory.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "disciplina")
public class DisciplinaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_disciplina")
    private UUID id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "carga_horaria")
    private Integer cargaHoraria;

    @Column(name = "ativo")
    private Boolean ativo = true;

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

    public Integer getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(Integer cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }

    public String getStatus() {
        return Boolean.FALSE.equals(ativo) ? "INATIVA" : "ATIVA";
    }

    public void setStatus(String status) {
        this.ativo = status == null || !status.equalsIgnoreCase("INATIVA");
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        if (ativo == null) {
            ativo = true;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
