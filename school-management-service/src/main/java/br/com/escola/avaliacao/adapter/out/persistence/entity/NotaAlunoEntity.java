package br.com.escola.avaliacao.adapter.out.persistence.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEntity;
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
@Table(name = "nota_aluno")
public class NotaAlunoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_nota_aluno", nullable = false)
    private UUID id;

    @Column(name = "nota")
    private BigDecimal nota;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_avaliacao", referencedColumnName = "id_avaliacao", nullable = false)
    private AvaliacaoEntity avaliacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_matricula", referencedColumnName = "id_matricula", nullable = false)
    private MatriculaEntity matricula;
}
