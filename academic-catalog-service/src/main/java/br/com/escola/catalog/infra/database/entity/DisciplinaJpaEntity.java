package br.com.escola.catalog.infra.database.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "disciplina")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DisciplinaJpaEntity {

    @Id
    @Column(name = "id_disciplina", nullable = false)
    private UUID id;

    @Column(name = "id_escola", nullable = false)
    private UUID escolaId;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "carga_horaria")
    private Integer cargaHoraria;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

