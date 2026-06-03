package br.com.escola.frequencia.adapter.out.persistence.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.professor.adapter.out.persistence.entity.AulaEntity;
import br.com.escola.professor.adapter.out.persistence.entity.ProfessorEntity;
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
@Table(name = "frequencia_professor")
public class FrequenciaProfessorEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_frequencia_professor", nullable = false)
    private UUID id;

    @Column(name = "presente", nullable = false)
    private Boolean presente;

    @Column(name = "justificativa", columnDefinition = "TEXT")
    private String justificativa;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aula", referencedColumnName = "id_aula", nullable = false)
    private AulaEntity aula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_professor", referencedColumnName = "id_professor", nullable = false)
    private ProfessorEntity professor;
}
