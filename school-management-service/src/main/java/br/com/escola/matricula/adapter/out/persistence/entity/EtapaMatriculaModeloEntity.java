package br.com.escola.matricula.adapter.out.persistence.entity;

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

@Entity
@Table(name = "etapa_matricula_modelo")
public class EtapaMatriculaModeloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_etapa_matricula_modelo")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_matricula", nullable = false)
    private TipoMatriculaEntity tipoMatricula;

    @Column(name = "codigo", nullable = false, length = 60)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 150)
    private String descricao;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    @Column(name = "obrigatoria", nullable = false)
    private Boolean obrigatoria;

    public UUID getId() {
        return id;
    }

    public TipoMatriculaEntity getTipoMatricula() {
        return tipoMatricula;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public Boolean getObrigatoria() {
        return obrigatoria;
    }
}

