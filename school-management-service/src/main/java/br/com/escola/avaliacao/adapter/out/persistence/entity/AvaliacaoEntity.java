package br.com.escola.avaliacao.adapter.out.persistence.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoBimestralAvaliacaoEntity;
import br.com.escola.professor.adapter.out.persistence.entity.ProfessorTurmaDisciplinaEntity;
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
@Table(name = "avaliacao")
public class AvaliacaoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_avaliacao", nullable = false)
    private UUID id;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "data_aplicacao")
    private LocalDate dataAplicacao;

    @Column(name = "valor_maximo", nullable = false)
    private BigDecimal valorMaximo;

    @Column(name = "peso", nullable = false)
    private BigDecimal peso;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_planejamento_bimestral_avaliacao", referencedColumnName = "id_planejamento_bimestral_avaliacao")
    private PlanejamentoBimestralAvaliacaoEntity planejamentoBimestralAvaliacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_professor_turma_disciplina", referencedColumnName = "id_professor_turma_disciplina", nullable = false)
    private ProfessorTurmaDisciplinaEntity professorTurmaDisciplina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_avaliacao", referencedColumnName = "id_tipo_avaliacao", nullable = false)
    private TipoAvaliacaoEntity tipoAvaliacao;
}
