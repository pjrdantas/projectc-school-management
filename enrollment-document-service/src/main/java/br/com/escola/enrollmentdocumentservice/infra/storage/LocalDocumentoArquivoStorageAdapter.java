package br.com.escola.enrollmentdocumentservice.infra.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.escola.enrollmentdocumentservice.application.port.out.DocumentoArquivoStoragePort;
import br.com.escola.enrollmentdocumentservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.enrollmentdocumentservice.infra.config.DocumentoArquivoStorageProperties;

@Component
public class LocalDocumentoArquivoStorageAdapter implements DocumentoArquivoStoragePort {

    private final Path rootPath;

    public LocalDocumentoArquivoStorageAdapter(DocumentoArquivoStorageProperties properties) {
        this.rootPath = properties.rootPath();
    }

    @Override
    public ReferenciaArquivoDocumento armazenar(ConteudoArquivoDocumento conteudo) {
        String referencia = UUID.randomUUID().toString();
        Path destino = resolver(referencia);
        Path temporario = destino.resolveSibling(destino.getFileName() + ".tmp");
        try {
            Files.createDirectories(rootPath);
            try (InputStream input = conteudo.conteudo()) {
                Files.copy(input, temporario, StandardCopyOption.REPLACE_EXISTING);
            }
            Files.move(temporario, destino, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            return new ReferenciaArquivoDocumento(referencia);
        } catch (IOException exception) {
            excluirSilenciosamente(temporario);
            throw new IllegalStateException("Nao foi possivel armazenar o arquivo do documento", exception);
        }
    }

    @Override
    public InputStream abrirConteudo(String referenciaArmazenamento) {
        try {
            Path arquivo = resolver(referenciaArmazenamento);
            if (Files.notExists(arquivo)) {
                throw new RecursoNaoEncontradoException("Conteudo do documento nao encontrado");
            }
            return Files.newInputStream(arquivo);
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel abrir o arquivo do documento", exception);
        }
    }

    @Override
    public void excluir(String referenciaArmazenamento) {
        try {
            Files.deleteIfExists(resolver(referenciaArmazenamento));
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel excluir o arquivo do documento", exception);
        }
    }

    private Path resolver(String referenciaArmazenamento) {
        if (referenciaArmazenamento == null || !referenciaArmazenamento.matches("[0-9a-fA-F-]{36}")) {
            throw new IllegalArgumentException("Referencia de armazenamento invalida");
        }
        Path resolved = rootPath.resolve(referenciaArmazenamento).normalize();
        if (!resolved.startsWith(rootPath)) {
            throw new IllegalArgumentException("Referencia de armazenamento invalida");
        }
        return resolved;
    }

    private void excluirSilenciosamente(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}
