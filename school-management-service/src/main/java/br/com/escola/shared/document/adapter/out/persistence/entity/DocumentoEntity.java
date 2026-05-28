package br.com.escola.shared.document.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "documento")
public class DocumentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_documento")
    private UUID id;

    @Column(name = "id_tipo_documento", nullable = false)
    private UUID tipoDocumentoId;

    @Column(name = "nome_arquivo", nullable = false, length = 255)
    private String numeroDocumento;

    @Column(name = "url_arquivo", nullable = false, columnDefinition = "TEXT")
    private String caminhoArquivo;

    @Column(name = "data_upload", nullable = false, updatable = false)
    private LocalDateTime dataUpload;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    public UUID getId() {
        return id;
    }

    public UUID getTipoDocumentoId() {
        return tipoDocumentoId;
    }

    public void setTipoDocumentoId(UUID tipoDocumentoId) {
        this.tipoDocumentoId = tipoDocumentoId;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public void setCaminhoArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }

    public LocalDateTime getDataUpload() {
        return dataUpload;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    @PrePersist
    public void prePersist() {
        if (dataUpload == null) {
            dataUpload = LocalDateTime.now();
        }
        ativo = true;
    }
}
