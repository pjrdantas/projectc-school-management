package br.com.escola.enrollmentdocumentservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarDocumentoAlunoCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarDocumentoAlunoRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarDocumentoCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarDocumentoRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoAlunoResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.UploadDocumentoAlunoCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.UploadDocumentoCommand;
import br.com.escola.enrollmentdocumentservice.application.port.out.DocumentoArquivoStoragePort;
import br.com.escola.enrollmentdocumentservice.application.port.out.DocumentoMetadataPort;

class DocumentoServiceTest {

    @Test
    void deveCriarDocumentoDeEntidadeNoTenantDoContexto() {
        DocumentoMetadataPort port = Mockito.mock(DocumentoMetadataPort.class);
        DocumentoService service = new DocumentoService(port, Mockito.mock(DocumentoArquivoStoragePort.class));
        UUID escolaId = UUID.randomUUID();
        UUID entidadeId = UUID.randomUUID();
        DocumentoResponse response = new DocumentoResponse(
                UUID.randomUUID(), "MATRICULA", entidadeId, escolaId, null, "CPF", "123", "arquivos/cpf.pdf", null, null);
        when(port.criarDocumento(org.mockito.ArgumentMatchers.eq(escolaId), org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        DocumentoResponse criado = service.criarDocumento(
                new InternalRequestContext("corr-documento", UUID.randomUUID(), escolaId),
                new CriarDocumentoRequest(" MATRICULA ", entidadeId, " CPF ", " 123 ", " arquivos/cpf.pdf ", " obs "));

        ArgumentCaptor<CriarDocumentoCommand> commandCaptor = ArgumentCaptor.forClass(CriarDocumentoCommand.class);
        verify(port).criarDocumento(org.mockito.ArgumentMatchers.eq(escolaId), commandCaptor.capture());
        assertThat(commandCaptor.getValue()).isEqualTo(new CriarDocumentoCommand(
                "MATRICULA", entidadeId, "CPF", null, "123", "arquivos/cpf.pdf", "obs", null, null));
        assertThat(criado).isEqualTo(response);
    }

    @Test
    void deveArmazenarUploadDeDocumentoDeEntidadeAntesDosMetadados() {
        DocumentoMetadataPort metadataPort = Mockito.mock(DocumentoMetadataPort.class);
        DocumentoArquivoStoragePort storagePort = Mockito.mock(DocumentoArquivoStoragePort.class);
        DocumentoService service = new DocumentoService(metadataPort, storagePort);
        UUID escolaId = UUID.randomUUID();
        UUID entidadeId = UUID.randomUUID();
        when(storagePort.armazenar(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new DocumentoArquivoStoragePort.ReferenciaArquivoDocumento("2fa5c8d1-10bb-4b96-a0f3-25d94966063f"));
        when(metadataPort.criarDocumento(org.mockito.ArgumentMatchers.eq(escolaId), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new DocumentoResponse(
                        UUID.randomUUID(), "MATRICULA", entidadeId, escolaId, null, "CPF", "123", null, null, null));

        service.enviarDocumento(
                new InternalRequestContext("corr-upload", UUID.randomUUID(), escolaId),
                new UploadDocumentoCommand(
                        "MATRICULA",
                        entidadeId,
                        "CPF",
                        "123",
                        null,
                        "cpf.pdf",
                        "application/pdf",
                        3,
                        new ByteArrayInputStream(new byte[] { 1, 2, 3 })));

        ArgumentCaptor<CriarDocumentoCommand> commandCaptor = ArgumentCaptor.forClass(CriarDocumentoCommand.class);
        verify(metadataPort).criarDocumento(org.mockito.ArgumentMatchers.eq(escolaId), commandCaptor.capture());
        assertThat(commandCaptor.getValue()).isEqualTo(new CriarDocumentoCommand(
                "MATRICULA", entidadeId, "CPF", "cpf.pdf", "123", "2fa5c8d1-10bb-4b96-a0f3-25d94966063f", null, "application/pdf", 3L));
    }

    @Test
    void deveCriarDocumentoDoAlunoNoTenantDoContexto() {
        DocumentoMetadataPort port = Mockito.mock(DocumentoMetadataPort.class);
        DocumentoService service = new DocumentoService(port, Mockito.mock(DocumentoArquivoStoragePort.class));
        UUID escolaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        DocumentoAlunoResponse response = new DocumentoAlunoResponse(
                UUID.randomUUID(), alunoId, "RG", "rg.pdf", "arquivos/rg.pdf", "123", "arquivos/rg.pdf", null, null);
        when(port.criarDocumentoAluno(org.mockito.ArgumentMatchers.eq(escolaId), org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        DocumentoAlunoResponse criado = service.criarDocumentoAluno(
                new InternalRequestContext("corr-documento", UUID.randomUUID(), escolaId),
                new CriarDocumentoAlunoRequest(alunoId, " RG ", " rg.pdf ", null, " 123 ", " arquivos/rg.pdf ", " obs "));

        ArgumentCaptor<CriarDocumentoAlunoCommand> commandCaptor = ArgumentCaptor.forClass(CriarDocumentoAlunoCommand.class);
        verify(port).criarDocumentoAluno(org.mockito.ArgumentMatchers.eq(escolaId), commandCaptor.capture());
        assertThat(commandCaptor.getValue()).isEqualTo(new CriarDocumentoAlunoCommand(
                alunoId, "RG", "rg.pdf", "arquivos/rg.pdf", "123", "arquivos/rg.pdf", "obs", null, null));
        assertThat(criado).isEqualTo(response);
    }

    @Test
    void deveArmazenarUploadAntesDePersistirMetadadosDoAluno() {
        DocumentoMetadataPort metadataPort = Mockito.mock(DocumentoMetadataPort.class);
        DocumentoArquivoStoragePort storagePort = Mockito.mock(DocumentoArquivoStoragePort.class);
        DocumentoService service = new DocumentoService(metadataPort, storagePort);
        UUID escolaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        when(storagePort.armazenar(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new DocumentoArquivoStoragePort.ReferenciaArquivoDocumento("1fa5c8d1-10bb-4b96-a0f3-25d94966063f"));
        DocumentoAlunoResponse response = new DocumentoAlunoResponse(
                UUID.randomUUID(), alunoId, "RG", "rg.pdf", null, null, "1fa5c8d1-10bb-4b96-a0f3-25d94966063f", null, null);
        when(metadataPort.criarDocumentoAluno(org.mockito.ArgumentMatchers.eq(escolaId), org.mockito.ArgumentMatchers.any()))
                .thenReturn(response);

        DocumentoAlunoResponse criado = service.enviarDocumentoAluno(
                new InternalRequestContext("corr-upload", UUID.randomUUID(), escolaId),
                new UploadDocumentoAlunoCommand(
                        alunoId,
                        "RG",
                        "123",
                        "obs",
                        "rg.pdf",
                        "application/pdf; charset=UTF-8",
                        3,
                        new ByteArrayInputStream(new byte[] { 1, 2, 3 })));

        ArgumentCaptor<CriarDocumentoAlunoCommand> commandCaptor = ArgumentCaptor.forClass(CriarDocumentoAlunoCommand.class);
        verify(metadataPort).criarDocumentoAluno(org.mockito.ArgumentMatchers.eq(escolaId), commandCaptor.capture());
        assertThat(commandCaptor.getValue()).isEqualTo(new CriarDocumentoAlunoCommand(
                alunoId,
                "RG",
                "rg.pdf",
                null,
                "123",
                "1fa5c8d1-10bb-4b96-a0f3-25d94966063f",
                "obs",
                "application/pdf",
                3L));
        assertThat(criado).isEqualTo(response);
    }

    @Test
    void deveRestringirConsultaDeDocumentoDoAlunoAoTenantDoContexto() {
        DocumentoMetadataPort port = Mockito.mock(DocumentoMetadataPort.class);
        DocumentoService service = new DocumentoService(port, Mockito.mock(DocumentoArquivoStoragePort.class));
        UUID escolaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        DocumentoAlunoResponse response = new DocumentoAlunoResponse(
                UUID.randomUUID(), alunoId, "RG", "rg.pdf", null, "123", "arquivos/rg.pdf", null, null);
        when(port.listarDocumentosPorAluno(escolaId, alunoId)).thenReturn(List.of(response));

        List<DocumentoAlunoResponse> documentos = service.listarDocumentosPorAluno(
                new InternalRequestContext("corr-documento", UUID.randomUUID(), escolaId),
                alunoId);

        ArgumentCaptor<UUID> escolaCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(port).listarDocumentosPorAluno(escolaCaptor.capture(), org.mockito.ArgumentMatchers.eq(alunoId));
        assertThat(escolaCaptor.getValue()).isEqualTo(escolaId);
        assertThat(documentos).containsExactly(response);
    }
}
