package br.com.escola.matricula.adapter.out.persistence.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.documento.adapter.out.persistence.entity.DocumentoEntity;
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
@Table(name = "matricula_documento_entregue")
public class MatriculaDocumentoEntregueEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_matricula_documento_entregue", nullable = false)
    private UUID id;

    @Column(name = "conferido", nullable = false)
    private Boolean conferido;

    @Column(name = "conferido_por")
    private UUID conferidoPor;

    @Column(name = "data_conferencia")
    private LocalDateTime dataConferencia;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_documento", referencedColumnName = "id_documento", nullable = false)
    private DocumentoEntity documento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_matricula", referencedColumnName = "id_matricula", nullable = false)
    private MatriculaEntity matricula;
}
