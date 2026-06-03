package br.com.escola.frequencia.adapter.out.persistence.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEntity;
import br.com.escola.professor.adapter.out.persistence.entity.AulaEntity;
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
@Table(name = "frequencia_aluno")
public class FrequenciaAlunoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_frequencia_aluno", nullable = false)
    private UUID id;

    @Column(name = "justificativa", columnDefinition = "TEXT")
    private String justificativa;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aula", referencedColumnName = "id_aula", nullable = false)
    private AulaEntity aula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_matricula", referencedColumnName = "id_matricula", nullable = false)
    private MatriculaEntity matricula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_situacao_frequencia", referencedColumnName = "id_situacao_frequencia", nullable = false)
    private SituacaoFrequenciaEntity situacaoFrequencia;
}
