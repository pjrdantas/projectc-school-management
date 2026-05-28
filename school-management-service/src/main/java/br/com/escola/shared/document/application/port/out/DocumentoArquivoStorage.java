package br.com.escola.shared.document.application.port.out;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import br.com.escola.shared.document.domain.EntidadeDocumentalTipo;

public interface DocumentoArquivoStorage {

    String salvar(EntidadeDocumentalTipo entidadeTipo, UUID entidadeId, MultipartFile arquivo);
}
