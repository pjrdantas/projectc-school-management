package br.com.escola.aluno.adapter.out.persistence.entity;

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
@Table(name = "solicitacao_exclusao_aluno")
public class SolicitacaoExclusaoAlunoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_solicitacao_exclusao_aluno", nullable = false)
    private UUID id;

    @Column(name = "solicitado_por", nullable = false)
    private UUID solicitadoPor;

    @Column(name = "autorizado_por")
    private UUID autorizadoPor;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_autorizacao")
    private LocalDateTime dataAutorizacao;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "observacao_autorizacao", columnDefinition = "TEXT")
    private String observacaoAutorizacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aluno", referencedColumnName = "id_aluno", nullable = false)
    private AlunoEntity aluno;
}
