package br.com.escola.aluno.adapter.out.persistence.entity;

import java.io.Serializable;
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
@Table(name = "aluno_historico_evento")
public class AlunoHistoricoEventoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_aluno_historico_evento", nullable = false)
    private UUID id;

    @Column(name = "id_usuario")
    private UUID idUsuario;

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aluno", referencedColumnName = "id_aluno", nullable = false)
    private AlunoEntity aluno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_matricula", referencedColumnName = "id_matricula")
    private MatriculaEntity matricula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_evento_aluno", referencedColumnName = "id_tipo_evento_aluno", nullable = false)
    private TipoEventoAlunoEntity tipoEventoAluno;
}
