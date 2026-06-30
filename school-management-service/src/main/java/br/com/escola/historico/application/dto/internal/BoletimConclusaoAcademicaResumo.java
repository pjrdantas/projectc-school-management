package br.com.escola.historico.application.dto.internal;

import java.util.List;
import java.util.UUID;

public record BoletimConclusaoAcademicaResumo(
        UUID boletimId,
        UUID matriculaId,
        List<BoletimConclusaoAcademicaItemResumo> itens) {
}
