package br.com.escola.professor.adapter.out.persistence.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaDisciplinaEntity;
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
@Table(name = "professor_turma_disciplina")
public class ProfessorTurmaDisciplinaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_professor_turma_disciplina", nullable = false)
    private UUID id;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_professor", referencedColumnName = "id_professor", nullable = false)
    private ProfessorEntity professor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_turma_disciplina", referencedColumnName = "id_turma_disciplina", nullable = false)
    private TurmaDisciplinaEntity turmaDisciplina;
}
