package br.com.escola.ia.adapter.out.persistence.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoBimestralEntity;
import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;
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
@Table(name = "planejamento_ia_interacao")
public class PlanejamentoIAInteracaoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_planejamento_ia_interacao", nullable = false)
    private UUID id;

    @Column(name = "prompt_professor", nullable = false, columnDefinition = "TEXT")
    private String promptProfessor;

    @Column(name = "resposta_ia", nullable = false, columnDefinition = "TEXT")
    private String respostaIA;

    @Column(name = "modelo_ia", length = 120)
    private String modeloIA;

    @Column(name = "tokens_entrada")
    private Integer tokensEntrada;

    @Column(name = "tokens_saida")
    private Integer tokensSaida;

    @Column(name = "custo_estimado")
    private BigDecimal custoEstimado;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario")
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_planejamento_bimestral", referencedColumnName = "id_planejamento_bimestral", nullable = false)
    private PlanejamentoBimestralEntity planejamentoBimestral;
}
