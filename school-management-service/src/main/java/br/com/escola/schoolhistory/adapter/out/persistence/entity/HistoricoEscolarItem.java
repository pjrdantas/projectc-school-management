package br.com.escola.schoolhistory.adapter.out.persistence.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "historico_escolar_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoEscolarItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_historico_escolar_item")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_historico_escolar", nullable = false)
    private HistoricoEscolar historicoEscolar;

    @Column(name = "componente_curricular", nullable = false)
    private String componenteCurricular;

    @Column(name = "ano_letivo")
    private Integer anoLetivo;

    @Column(name = "serie_descricao")
    private String serie;

    @Transient
    private String ciclo;

    @Column(name = "nota_conceito")
    private String notaConceito;

    @Column(name = "total_aulas")
    private Integer totalAulas;

    @Column(name = "carga_horaria")
    private Integer cargaHoraria;
}
