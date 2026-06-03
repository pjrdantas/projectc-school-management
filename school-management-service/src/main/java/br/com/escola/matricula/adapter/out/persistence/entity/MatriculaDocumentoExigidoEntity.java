package br.com.escola.matricula.adapter.out.persistence.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.documento.adapter.out.persistence.entity.TipoDocumentoEntity;
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
@Table(name = "matricula_documento_exigido")
public class MatriculaDocumentoExigidoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_matricula_documento_exigido", nullable = false)
    private UUID id;

    @Column(name = "obrigatorio", nullable = false)
    private Boolean obrigatorio;

    @Column(name = "ordem")
    private Integer ordem;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento", referencedColumnName = "id_tipo_documento", nullable = false)
    private TipoDocumentoEntity tipoDocumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_matricula", referencedColumnName = "id_tipo_matricula", nullable = false)
    private TipoMatriculaEntity tipoMatricula;
}
