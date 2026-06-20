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
@Table(name = "serie")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SerieJpaEntity {

    @Id
    @Column(name = "id_serie", nullable = false)
    private UUID id;

    @Column(name = "id_escola", nullable = false)
    private UUID escolaId;

    @Column(name = "nome", nullable = false, length = 80)
    private String nome;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    @Column(name = "id_nivel_ensino", nullable = false)
    private UUID nivelEnsinoId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

