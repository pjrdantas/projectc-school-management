package br.com.escola.planejamento.adapter.out.persistence.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
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
@Table(name = "planejamento_bimestral_aula")
public class PlanejamentoBimestralAulaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_planejamento_bimestral_aula", nullable = false)
    private UUID id;

    @Column(name = "numero_aula", nullable = false)
    private Integer numeroAula;

    @Column(name = "tema_aula", nullable = false, length = 180)
    private String temaAula;

    @Column(name = "objetivo_aula", columnDefinition = "TEXT")
    private String objetivoAula;

    @Column(name = "conteudo_previsto", columnDefinition = "TEXT")
    private String conteudoPrevisto;

    @Column(name = "metodologia", columnDefinition = "TEXT")
    private String metodologia;

    @Column(name = "recursos", columnDefinition = "TEXT")
    private String recursos;

    @Column(name = "atividade_prevista", columnDefinition = "TEXT")
    private String atividadePrevista;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_planejamento_aula", referencedColumnName = "id_planejamento_aula")
    private PlanejamentoAulaEntity planejamentoAula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_planejamento_bimestral", referencedColumnName = "id_planejamento_bimestral", nullable = false)
    private PlanejamentoBimestralEntity planejamentoBimestral;
}
