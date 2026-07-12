package br.com.escola.professor.adapter.out.persistence.entity;

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
@Table(name = "diario_classe_lancamento")
public class DiarioClasseLancamentoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_diario_classe_lancamento", nullable = false)
    private UUID id;

    @Column(name = "data_lancamento", nullable = false)
    private LocalDate dataLancamento;

    @Column(name = "mes", nullable = false)
    private Integer mes;

    @Column(name = "ano", nullable = false)
    private Integer ano;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "bloqueado", nullable = false)
    private Boolean bloqueado;

    @Column(name = "assinatura_professor", length = 150)
    private String assinaturaProfessor;

    @Column(name = "data_assinatura")
    private LocalDate dataAssinatura;

    @Column(name = "salvo_em")
    private LocalDateTime salvoEm;

    @Column(name = "checado_coordenacao_por_funcionario")
    private UUID checadoCoordenacaoPorFuncionario;

    @Column(name = "checado_coordenacao_em")
    private LocalDateTime checadoCoordenacaoEm;

    @Column(name = "observacao_coordenacao", length = 500)
    private String observacaoCoordenacao;

    @Column(name = "checado_direcao_por_funcionario")
    private UUID checadoDirecaoPorFuncionario;

    @Column(name = "checado_direcao_em")
    private LocalDateTime checadoDirecaoEm;

    @Column(name = "observacao_direcao", length = 500)
    private String observacaoDirecao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_professor_turma_disciplina", referencedColumnName = "id_professor_turma_disciplina", nullable = false)
    private ProfessorTurmaDisciplinaEntity professorTurmaDisciplina;
}
