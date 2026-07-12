package br.com.escola.documento.adapter.out.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import br.com.escola.documento.application.port.out.DocumentoArquivoReferencia;
import br.com.escola.documento.application.port.out.DocumentoArquivoStorage;
import br.com.escola.documento.domain.EntidadeDocumentalTipo;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@ConditionalOnProperty(prefix = "documento.storage", name = "backend", havingValue = "s3")
public class S3DocumentoArquivoStorage implements DocumentoArquivoStorage {

    private final DocumentoS3StorageProperties properties;
    private final S3Client s3Client;

    public S3DocumentoArquivoStorage(DocumentoS3StorageProperties properties, S3Client s3Client) {
        this.properties = properties;
        this.s3Client = s3Client;
    }

    @Override
    public DocumentoArquivoReferencia salvar(EntidadeDocumentalTipo entidadeTipo, UUID entidadeId, MultipartFile arquivo) {
        String chave = chaveObjeto(entidadeTipo, entidadeId, DocumentoArquivoNome.seguro(arquivo));
        PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(chave)
                .contentLength(arquivo.getSize());

        if (arquivo.getContentType() != null && !arquivo.getContentType().isBlank()) {
            requestBuilder.contentType(arquivo.getContentType());
        }

        try {
            s3Client.putObject(requestBuilder.build(), RequestBody.fromInputStream(arquivo.getInputStream(), arquivo.getSize()));
            return DocumentoArquivoReferencia.objectStorage(chave, "s3://" + properties.getBucket() + "/" + chave);
        } catch (IOException ex) {
            throw new UncheckedIOException("Não foi possível ler o arquivo do documento", ex);
        }
    }

    private String chaveObjeto(EntidadeDocumentalTipo entidadeTipo, UUID entidadeId, String nomeSeguro) {
        String chaveEntidade = entidadeTipo.name().toLowerCase() + "/" + entidadeId + "/" + UUID.randomUUID() + "-" + nomeSeguro;
        String prefix = properties.normalizedPrefix();
        return prefix.isBlank() ? chaveEntidade : prefix + "/" + chaveEntidade;
    }
}
