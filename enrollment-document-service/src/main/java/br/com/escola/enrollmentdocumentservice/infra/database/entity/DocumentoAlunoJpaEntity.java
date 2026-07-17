package br.com.escola.enrollmentdocumentservice.infra.database.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "student_document")
public class DocumentoAlunoJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "student_id", nullable = false)
    private UUID alunoId;

    @Column(name = "document_type", nullable = false, length = 120)
    private String tipoDocumento;

    @Column(name = "file_name", length = 180)
    private String nomeArquivo;

    @Column(name = "file_url", length = 500)
    private String urlArquivo;

    @Column(name = "document_number", length = 180)
    private String numeroDocumento;

    @Column(name = "file_path", length = 500)
    private String caminhoArquivo;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime dataUpload;

    @Column(name = "note", length = 500)
    private String observacao;

    protected DocumentoAlunoJpaEntity() {
    }

    public DocumentoAlunoJpaEntity(
            UUID id,
            UUID schoolId,
            UUID alunoId,
            String tipoDocumento,
            String nomeArquivo,
            String urlArquivo,
            String numeroDocumento,
            String caminhoArquivo,
            LocalDateTime dataUpload,
            String observacao) {
        this.id = id;
        this.schoolId = schoolId;
        this.alunoId = alunoId;
        this.tipoDocumento = tipoDocumento;
        this.nomeArquivo = nomeArquivo;
        this.urlArquivo = urlArquivo;
        this.numeroDocumento = numeroDocumento;
        this.caminhoArquivo = caminhoArquivo;
        this.dataUpload = dataUpload;
        this.observacao = observacao;
    }

    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public UUID getAlunoId() { return alunoId; }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getNomeArquivo() { return nomeArquivo; }
    public String getUrlArquivo() { return urlArquivo; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getCaminhoArquivo() { return caminhoArquivo; }
    public LocalDateTime getDataUpload() { return dataUpload; }
    public String getObservacao() { return observacao; }
}
