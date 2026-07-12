package br.com.escola.matricula.application.dto.internal;

import java.util.List;
import java.util.UUID;

public record MatriculaRematriculaElegibilidadeResumo(
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
