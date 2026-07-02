package br.com.escola.documento.adapter.out.storage;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import br.com.escola.documento.domain.EntidadeDocumentalTipo;

class LocalDocumentoArquivoStorageTest {

    @TempDir
    private Path tempDir;

    @Test
    void deveSalvarArquivoEmRaizConfiguradaComNomeSeguro() throws Exception {
        DocumentoStorageProperties properties = new DocumentoStorageProperties();
        Path root = tempDir.resolve("documentos");
        properties.setRoot(root);
        LocalDocumentoArquivoStorage storage = new LocalDocumentoArquivoStorage(properties);
        UUID alunoId = UUID.randomUUID();
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "../historico aluno.pdf",
                "application/pdf",
                "conteudo".getBytes(UTF_8));

        String caminho = storage.salvar(EntidadeDocumentalTipo.ALUNO, alunoId, arquivo);

        Path destino = Path.of(caminho);
        assertThat(destino).startsWith(root);
        assertThat(destino.getParent()).isEqualTo(root.resolve("aluno").resolve(alunoId.toString()));
        assertThat(destino.getFileName().toString()).endsWith(".._historico_aluno.pdf");
        assertThat(Files.readString(destino, UTF_8)).isEqualTo("conteudo");
    }

    @Test
    void deveUsarNomePadraoQuandoNomeOriginalEstaVazio() {
        DocumentoStorageProperties properties = new DocumentoStorageProperties();
        Path root = tempDir.resolve("documentos");
        properties.setRoot(root);
        LocalDocumentoArquivoStorage storage = new LocalDocumentoArquivoStorage(properties);
        UUID alunoId = UUID.randomUUID();
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "",
                "application/pdf",
                "conteudo".getBytes(UTF_8));

        String caminho = storage.salvar(EntidadeDocumentalTipo.ALUNO, alunoId, arquivo);

        assertThat(Path.of(caminho).getFileName().toString()).endsWith("-documento");
    }
}
