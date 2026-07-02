package br.com.escola.documento.adapter.out.storage;

import org.springframework.web.multipart.MultipartFile;

final class DocumentoArquivoNome {

    private DocumentoArquivoNome() {
    }

    static String seguro(MultipartFile arquivo) {
        String nomeOriginal = arquivo.getOriginalFilename() == null || arquivo.getOriginalFilename().isBlank()
                ? "documento"
                : arquivo.getOriginalFilename();
        return nomeOriginal.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
