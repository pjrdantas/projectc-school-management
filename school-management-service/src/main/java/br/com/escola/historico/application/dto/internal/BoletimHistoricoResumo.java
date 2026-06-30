package br.com.escola.historico.application.dto.internal;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BoletimHistoricoResumo(
        UUID boletimId,
        UUID matriculaId,
        UUID alunoId,
        UUID periodoLetivoId,
        String nomeAluno,
        String rg,
        String ra,
        String rm,
        LocalDate dataNascimento,
        String naturalidade,
        String nacionalidade,
        Integer anoConclusao,
        String periodoReferencia,
        List<BoletimHistoricoItemResumo> itens) {
}
