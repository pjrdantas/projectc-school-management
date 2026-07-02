package br.com.escola.historico.application.dto.internal;

import java.util.List;

public record HistoricoEscolarPendenciaContexto(
        Integer serieMatriculaAtual,
        Integer serieConcluidaOrigem,
        List<String> seriesComponentes) {
}
