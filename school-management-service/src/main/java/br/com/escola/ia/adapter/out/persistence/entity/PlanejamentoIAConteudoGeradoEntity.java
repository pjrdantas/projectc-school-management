package br.com.escola.ia.adapter.out.persistence.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoBimestralEntity;
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
@Table(name = "planejamento_ia_conteudo_gerado")
public class PlanejamentoIAConteudoGeradoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_planejamento_ia_conteudo_gerado", nullable = false)
    private UUID id;

    @Column(name = "titulo", nullable = false, length = 180)
    private String titulo;

    @Column(name = "conteudo", nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "versao", nullable = false)
    private Integer versao;

    @Column(name = "hash_conteudo", length = 128)
    private String hashConteudo;

    @Column(name = "aprovado_pelo_professor", nullable = false)
    private Boolean aprovadoPeloProfessor;

    @Column(name = "reutilizavel", nullable = false)
    private Boolean reutilizavel;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_planejamento_ia_interacao", referencedColumnName = "id_planejamento_ia_interacao")
    private PlanejamentoIAInteracaoEntity planejamentoIAInteracao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_planejamento_bimestral", referencedColumnName = "id_planejamento_bimestral", nullable = false)
    private PlanejamentoBimestralEntity planejamentoBimestral;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_status_conteudo_ia", referencedColumnName = "id_status_conteudo_ia")
    private StatusConteudoIAEntity statusConteudoIA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_conteudo_ia", referencedColumnName = "id_tipo_conteudo_ia", nullable = false)
    private TipoConteudoIAEntity tipoConteudoIA;
}
