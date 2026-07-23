package br.com.escola.enrollmentdocumentservice.application.port.out;

import java.io.InputStream;

public interface DocumentoArquivoStoragePort {

    ReferenciaArquivoDocumento armazenar(ConteudoArquivoDocumento conteudo);

    InputStream abrirConteudo(String referenciaArmazenamento);

    void excluir(String referenciaArmazenamento);

    record ReferenciaArquivoDocumento(String referenciaArmazenamento) {
    }

    record ConteudoArquivoDocumento(
            String nomeArquivo,
            String tipoConteudo,
            long tamanho,
            InputStream conteudo) {
    }
}
