package br.com.escola.documento.application.port.out;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import br.com.escola.documento.domain.EntidadeDocumentalTipo;

public interface DocumentoArquivoStorage {

    DocumentoArquivoReferencia salvar(EntidadeDocumentalTipo entidadeTipo, UUID entidadeId, MultipartFile arquivo);
}
