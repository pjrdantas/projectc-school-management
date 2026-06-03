package br.com.escola.planejamento.adapter.out.persistence.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.avaliacao.adapter.out.persistence.entity.PeriodoAvaliativoEntity;
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
@Table(name = "planejamento_bimestral")
public class PlanejamentoBimestralEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_planejamento_bimestral", nullable = false)
    private UUID id;

    @Column(name = "titulo", nullable = false, length = 180)
    private String titulo;

    @Column(name = "tema_principal", nullable = false, unique = true, length = 180)
    private String temaPrincipal;

    @Column(name = "descricao_inicial", nullable = false, columnDefinition = "TEXT")
    private String descricaoInicial;

    @Column(name = "objetivo_geral", columnDefinition = "TEXT")
    private String objetivoGeral;

    @Column(name = "observacao_professor", columnDefinition = "TEXT")
    private String observacaoProfessor;

    @Column(name = "conteudo_final_aprovado", columnDefinition = "TEXT")
    private String conteudoFinalAprovado;

    @Column(name = "reutilizavel", nullable = false)
    private Boolean reutilizavel;

    @Column(name = "criado_com_auxilio_ia", nullable = false)
    private Boolean criadoComAuxilioIA;

    @Column(name = "aprovado_pelo_professor", nullable = false)
    private Boolean aprovadoPeloProfessor;

    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_periodo_avaliativo", referencedColumnName = "id_periodo_avaliativo")
    private PeriodoAvaliativoEntity periodoAvaliativo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_professor_turma_disciplina", referencedColumnName = "id_professor_turma_disciplina", nullable = false)
    private ProfessorTurmaDisciplinaEntity professorTurmaDisciplina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_status_planejamento", referencedColumnName = "id_status_planejamento")
    private StatusPlanejamentoEntity statusPlanejamento;
}
