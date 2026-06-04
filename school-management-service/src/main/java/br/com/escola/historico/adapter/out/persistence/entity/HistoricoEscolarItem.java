package br.com.escola.historico.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.escola.catalogo.adapter.out.persistence.entity.DisciplinaEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.PeriodoLetivoEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.SerieEntity;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_periodo_letivo")
    private PeriodoLetivoEntity periodoLetivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_serie")
    private SerieEntity serieEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_disciplina")
    private DisciplinaEntity disciplina;

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

    @Column(name = "frequencia_percentual")
    private BigDecimal frequenciaPercentual;

    @Column(name = "total_aulas")
    private Integer totalAulas;

    @Column(name = "carga_horaria")
    private Integer cargaHoraria;

    @Column(name = "resultado")
    private String resultado;
}
