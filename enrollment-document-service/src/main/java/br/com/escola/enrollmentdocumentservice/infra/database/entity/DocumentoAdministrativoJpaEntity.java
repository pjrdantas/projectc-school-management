package br.com.escola.enrollmentdocumentservice.infra.database.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "administrative_document")
public class DocumentoAdministrativoJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "school_name", length = 180)
    private String escolaNome;

    @Column(name = "entity_type", nullable = false, length = 80)
    private String entidadeTipo;

    @Column(name = "entity_id", nullable = false)
    private UUID entidadeId;

    @Column(name = "document_type", nullable = false, length = 120)
    private String tipoDocumento;

    @Column(name = "document_number", length = 180)
    private String numeroDocumento;

    @Column(name = "file_path", length = 500)
    private String caminhoArquivo;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime dataUpload;

    @Column(name = "note", length = 500)
    private String observacao;

    protected DocumentoAdministrativoJpaEntity() {
    }

    public DocumentoAdministrativoJpaEntity(
            UUID id,
            UUID schoolId,
            String escolaNome,
            String entidadeTipo,
            UUID entidadeId,
            String tipoDocumento,
            String numeroDocumento,
            String caminhoArquivo,
            LocalDateTime dataUpload,
            String observacao) {
        this.id = id;
        this.schoolId = schoolId;
        this.escolaNome = escolaNome;
        this.entidadeTipo = entidadeTipo;
        this.entidadeId = entidadeId;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.caminhoArquivo = caminhoArquivo;
        this.dataUpload = dataUpload;
        this.observacao = observacao;
    }

    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public String getEscolaNome() { return escolaNome; }
    public String getEntidadeTipo() { return entidadeTipo; }
    public UUID getEntidadeId() { return entidadeId; }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getCaminhoArquivo() { return caminhoArquivo; }
    public LocalDateTime getDataUpload() { return dataUpload; }
    public String getObservacao() { return observacao; }
}
