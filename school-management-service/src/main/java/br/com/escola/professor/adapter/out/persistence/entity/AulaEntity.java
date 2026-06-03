package br.com.escola.professor.adapter.out.persistence.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoAulaEntity;
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
@Table(name = "aula")
public class AulaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_aula", nullable = false)
    private UUID id;

    @Column(name = "data_aula", nullable = false)
    private LocalDate dataAula;

    @Column(name = "horario_inicio")
    private LocalTime horarioInicio;

    @Column(name = "horario_fim")
    private LocalTime horarioFim;

    @Column(name = "conteudo_ministrado", columnDefinition = "TEXT")
    private String conteudoMinistrado;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "realizada", nullable = false)
    private Boolean realizada;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_planejamento_aula", referencedColumnName = "id_planejamento_aula")
    private PlanejamentoAulaEntity planejamentoAula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_professor_turma_disciplina", referencedColumnName = "id_professor_turma_disciplina", nullable = false)
    private ProfessorTurmaDisciplinaEntity professorTurmaDisciplina;
}
