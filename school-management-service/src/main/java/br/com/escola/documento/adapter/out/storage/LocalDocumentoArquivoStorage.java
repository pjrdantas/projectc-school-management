package br.com.escola.documento.adapter.out.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import br.com.escola.documento.application.port.out.DocumentoArquivoReferencia;
import br.com.escola.documento.application.port.out.DocumentoArquivoStorage;
import br.com.escola.documento.domain.EntidadeDocumentalTipo;
import br.com.escola.documento.domain.exception.DocumentoInvalidoException;

@Component
@ConditionalOnProperty(prefix = "documento.storage", name = "backend", havingValue = "local", matchIfMissing = true)
public class LocalDocumentoArquivoStorage implements DocumentoArquivoStorage {

    private final Path documentosUploadDir;

    public LocalDocumentoArquivoStorage(DocumentoStorageProperties properties) {
        this.documentosUploadDir = properties.normalizedRoot();
    }

    @Override
    public DocumentoArquivoReferencia salvar(EntidadeDocumentalTipo entidadeTipo, UUID entidadeId, MultipartFile arquivo) {
        try {
            Path diretorioEntidade = documentosUploadDir
                    .resolve(entidadeTipo.name().toLowerCase())
                    .resolve(entidadeId.toString())
                    .normalize();
            if (!diretorioEntidade.startsWith(documentosUploadDir)) {
                throw new DocumentoInvalidoException("Diretório de documento inválido");
            }
            Files.createDirectories(diretorioEntidade);

            String nomeSeguro = DocumentoArquivoNome.seguro(arquivo);
            Path destino = diretorioEntidade.resolve(UUID.randomUUID() + "-" + nomeSeguro).normalize();
            if (!destino.startsWith(diretorioEntidade)) {
                throw new DocumentoInvalidoException("Nome de arquivo inválido");
            }

            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            String chave = documentosUploadDir.relativize(destino).toString().replace('\\', '/');
            String localizacao = destino.toString().replace('\\', '/');
            return DocumentoArquivoReferencia.local(chave, localizacao);
        } catch (IOException ex) {
            throw new UncheckedIOException("Não foi possível salvar o arquivo do documento", ex);
        }
    }
}
