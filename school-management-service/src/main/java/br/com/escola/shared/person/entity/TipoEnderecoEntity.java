package br.com.escola.shared.person.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipo_endereco")
public class TipoEnderecoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_tipo_endereco", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 120)
    private String descricao;

    public UUID getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
}
