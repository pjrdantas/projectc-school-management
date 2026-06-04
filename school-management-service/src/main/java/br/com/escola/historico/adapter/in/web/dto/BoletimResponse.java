package br.com.escola.historico.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BoletimResponse(
        UUID boletimId,
        UUID matriculaId,
        UUID alunoId,
        String alunoNome,
        UUID turmaId,
        String turmaNome,
        UUID periodoLetivoId,
        String periodoLetivoNome,
        LocalDate dataGeracao,
        String periodoReferencia,
        LocalDate dataFechamento,
        String observacao,
        boolean persistido,
        BoletimIndicadoresResponse indicadores,
        List<BoletimItemResponse> itens) {
}
