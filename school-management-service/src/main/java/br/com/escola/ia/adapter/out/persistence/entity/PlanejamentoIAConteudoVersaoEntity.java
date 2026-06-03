package br.com.escola.ia.adapter.out.persistence.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

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
@Table(name = "planejamento_ia_conteudo_versao")
public class PlanejamentoIAConteudoVersaoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_planejamento_ia_conteudo_versao", nullable = false)
    private UUID id;

    @Column(name = "numero_versao", nullable = false, unique = true)
    private Integer numeroVersao;

    @Column(name = "conteudo", nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "motivo_alteracao", columnDefinition = "TEXT")
    private String motivoAlteracao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alterado_por", referencedColumnName = "id_usuario")
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_planejamento_ia_conteudo_gerado", referencedColumnName = "id_planejamento_ia_conteudo_gerado", nullable = false)
    private PlanejamentoIAConteudoGeradoEntity planejamentoIAConteudoGerado;
}
