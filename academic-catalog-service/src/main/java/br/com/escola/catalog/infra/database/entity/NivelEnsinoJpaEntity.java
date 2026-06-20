package br.com.escola.catalog.infra.database.entity;

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
@Table(name = "nivel_ensino")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NivelEnsinoJpaEntity {

    @Id
    @Column(name = "id_nivel_ensino", nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, length = 40)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 120)
    private String descricao;
}
