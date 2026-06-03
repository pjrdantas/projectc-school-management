package br.com.escola.documento.adapter.out.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import br.com.escola.documento.application.port.out.DocumentoArquivoStorage;
import br.com.escola.documento.domain.EntidadeDocumentalTipo;
import br.com.escola.documento.domain.exception.DocumentoInvalidoException;

@Component
public class LocalDocumentoArquivoStorage implements DocumentoArquivoStorage {

    private static final Path DOCUMENTOS_UPLOAD_DIR = Path.of("uploads", "documentos");

    @Override
    public String salvar(EntidadeDocumentalTipo entidadeTipo, UUID entidadeId, MultipartFile arquivo) {
        try {
            Path diretorioEntidade = DOCUMENTOS_UPLOAD_DIR
                    .resolve(entidadeTipo.name().toLowerCase())
                    .resolve(entidadeId.toString());
            Files.createDirectories(diretorioEntidade);

            String nomeOriginal = arquivo.getOriginalFilename() == null ? "documento" : arquivo.getOriginalFilename();
            String nomeSeguro = nomeOriginal.replaceAll("[^A-Za-z0-9._-]", "_");
            Path destino = diretorioEntidade.resolve(UUID.randomUUID() + "-" + nomeSeguro).normalize();
            if (!destino.startsWith(diretorioEntidade)) {
                throw new DocumentoInvalidoException("Nome de arquivo inválido");
            }

            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return destino.toString().replace('\\', '/');
        } catch (IOException ex) {
            throw new UncheckedIOException("Não foi possível salvar o arquivo do documento", ex);
        }
    }
}
