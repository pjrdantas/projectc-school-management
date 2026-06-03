package br.com.escola.planejamento.adapter.out.persistence.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

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
@Table(name = "planejamento_professor")
public class PlanejamentoProfessorEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_planejamento_professor", nullable = false)
    private UUID id;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "objetivo", columnDefinition = "TEXT")
    private String objetivo;

    @Column(name = "metodologia", columnDefinition = "TEXT")
    private String metodologia;

    @Column(name = "recursos", columnDefinition = "TEXT")
    private String recursos;

    @Column(name = "periodo_inicio")
    private LocalDate periodoInicio;

    @Column(name = "periodo_fim")
    private LocalDate periodoFim;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_professor_turma_disciplina", referencedColumnName = "id_professor_turma_disciplina", nullable = false)
    private ProfessorTurmaDisciplinaEntity professorTurmaDisciplina;
}
