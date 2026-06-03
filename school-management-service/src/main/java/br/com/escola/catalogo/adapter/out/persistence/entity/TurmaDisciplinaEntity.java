package br.com.escola.catalogo.adapter.out.persistence.entity;

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
@Table(name = "turma_disciplina")
public class TurmaDisciplinaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_turma_disciplina", nullable = false)
    private UUID id;

    @Column(name = "carga_horaria")
    private Integer cargaHoraria;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_disciplina", referencedColumnName = "id_disciplina", nullable = false)
    private DisciplinaEntity disciplina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_turma", referencedColumnName = "id_turma", nullable = false)
    private TurmaEntity turma;
}
