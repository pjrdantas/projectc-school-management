package br.com.escola.documento.adapter.out.storage;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import br.com.escola.documento.application.port.out.DocumentoArquivoReferencia;
import br.com.escola.documento.application.port.out.DocumentoArquivoStorageTipo;
import br.com.escola.documento.domain.EntidadeDocumentalTipo;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

class S3DocumentoArquivoStorageTest {

    @Test
    void deveEnviarArquivoParaBucketConfiguradoERetornarReferenciaS3() {
        DocumentoS3StorageProperties properties = new DocumentoS3StorageProperties();
        properties.setBucket("documentos-escola");
        properties.setPrefix("tenant-a/documentos");
        S3Client s3Client = mock(S3Client.class);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().eTag("etag").build());
        S3DocumentoArquivoStorage storage = new S3DocumentoArquivoStorage(properties, s3Client);
        UUID alunoId = UUID.randomUUID();
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "../historico aluno.pdf",
                "application/pdf",
                "conteudo".getBytes(UTF_8));

        DocumentoArquivoReferencia referencia = storage.salvar(EntidadeDocumentalTipo.ALUNO, alunoId, arquivo);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo("documentos-escola");
        assertThat(request.key()).startsWith("tenant-a/documentos/aluno/" + alunoId + "/");
        assertThat(request.key()).endsWith(".._historico_aluno.pdf");
        assertThat(request.contentType()).isEqualTo("application/pdf");
        assertThat(request.contentLength()).isEqualTo(8L);
        assertThat(referencia.tipo()).isEqualTo(DocumentoArquivoStorageTipo.OBJECT_STORAGE);
        assertThat(referencia.chave()).isEqualTo(request.key());
        assertThat(referencia.caminhoPersistencia()).isEqualTo("s3://documentos-escola/" + request.key());
    }

    @Test
    void deveTratarEndpointVazioComoAusente() {
        DocumentoS3StorageProperties properties = new DocumentoS3StorageProperties();

        properties.setEndpoint("");

        assertThat(properties.hasEndpoint()).isFalse();
        assertThat(properties.getEndpoint()).isNull();
    }
}
