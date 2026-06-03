package br.com.escola.ia.adapter.out.persistence.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "tipo_conteudo_ia")
public class TipoConteudoIAEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_tipo_conteudo_ia", nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 60)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 150)
    private String descricao;
}
