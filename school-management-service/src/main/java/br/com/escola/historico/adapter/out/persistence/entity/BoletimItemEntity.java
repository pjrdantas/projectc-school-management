package br.com.escola.historico.adapter.out.persistence.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import br.com.escola.catalogo.adapter.out.persistence.entity.DisciplinaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "boletim_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_boletim_item_boletim_disciplina",
                columnNames = {"id_boletim", "id_disciplina"}))
public class BoletimItemEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_boletim_item", nullable = false)
    private UUID id;

    @Column(name = "media")
    private BigDecimal media;

    @Column(name = "frequencia_percentual")
    private BigDecimal frequenciaPercentual;

    @Column(name = "resultado", length = 40)
    private String resultado;

    @Column(name = "carga_horaria")
    private Integer cargaHoraria;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boletim", referencedColumnName = "id_boletim", nullable = false)
    private BoletimEntity boletim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_disciplina", referencedColumnName = "id_disciplina", nullable = false)
    private DisciplinaEntity disciplina;
}
