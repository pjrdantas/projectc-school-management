package br.com.escola.pedagogicalservice.application.dto;

import java.time.Instant;
import java.util.List;

public record HistoricoEscolarPdfImportResponse(
        HistoricoEscolarTelaResponse historico,
        String nomeArquivo,
        int confiancaGeral,
        List<String> avisos,
        Instant importadoEm) {
}
