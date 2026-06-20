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
@Table(name = "turma")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TurmaJpaEntity {

    @Id
    @Column(name = "id_turma", nullable = false)
    private UUID id;

    @Column(name = "id_escola", nullable = false)
    private UUID escolaId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "capacidade", nullable = false)
    private Integer capacidade;

    @Column(name = "id_periodo_letivo", nullable = false)
    private UUID periodoLetivoId;

    @Column(name = "id_serie", nullable = false)
    private UUID serieId;

    @Column(name = "id_turno", nullable = false)
    private UUID turnoId;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

