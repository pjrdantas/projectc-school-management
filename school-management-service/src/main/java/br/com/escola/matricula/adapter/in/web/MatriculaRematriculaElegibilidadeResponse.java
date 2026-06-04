package br.com.escola.matricula.adapter.in.web;

import java.util.List;
import java.util.UUID;

public record MatriculaRematriculaElegibilidadeResponse(
        UUID matriculaBaseId,
        UUID alunoId,
        String statusBase,
        UUID turmaBaseId,
        UUID serieBaseId,
        String serieBaseNome,
        Integer serieBaseOrdem,
        UUID turmaDestinoId,
        UUID periodoLetivoDestinoId,
        UUID serieDestinoId,
        String serieDestinoNome,
        Integer serieDestinoOrdem,
        boolean elegivel,
        List<String> motivos) {
}
