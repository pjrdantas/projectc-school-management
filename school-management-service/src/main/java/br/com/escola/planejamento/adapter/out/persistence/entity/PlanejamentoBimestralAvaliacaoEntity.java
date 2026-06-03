package br.com.escola.planejamento.adapter.out.persistence.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.avaliacao.adapter.out.persistence.entity.TipoAvaliacaoEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "planejamento_bimestral_avaliacao")
public class PlanejamentoBimestralAvaliacaoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_planejamento_bimestral_avaliacao", nullable = false)
    private UUID id;

    @Column(name = "titulo", nullable = false, length = 180)
    private String titulo;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "data_prevista")
    private LocalDate dataPrevista;

    @Column(name = "peso", nullable = false)
    private BigDecimal peso;

    @Column(name = "valor_maximo")
    private BigDecimal valorMaximo;

    @Column(name = "conteudo_cobrado", columnDefinition = "TEXT")
    private String conteudoCobrado;

    @Column(name = "orientacao_aplicacao", columnDefinition = "TEXT")
    private String orientacaoAplicacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_planejamento_bimestral", referencedColumnName = "id_planejamento_bimestral", nullable = false)
    private PlanejamentoBimestralEntity planejamentoBimestral;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_avaliacao", referencedColumnName = "id_tipo_avaliacao", nullable = false)
    private TipoAvaliacaoEntity tipoAvaliacao;
}
