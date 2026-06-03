package br.com.escola.planejamento.adapter.out.persistence.entity;

import java.io.Serializable;
import java.time.LocalDate;
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
@Table(name = "planejamento_aula")
public class PlanejamentoAulaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_planejamento_aula", nullable = false)
    private UUID id;

    @Column(name = "data_prevista")
    private LocalDate dataPrevista;

    @Column(name = "conteudo", nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "habilidade_bncc", length = 80)
    private String habilidadeBncc;

    @Column(name = "avaliacao_prevista", columnDefinition = "TEXT")
    private String avaliacaoPrevista;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_planejamento_professor", referencedColumnName = "id_planejamento_professor", nullable = false)
    private PlanejamentoProfessorEntity planejamentoProfessor;
}
