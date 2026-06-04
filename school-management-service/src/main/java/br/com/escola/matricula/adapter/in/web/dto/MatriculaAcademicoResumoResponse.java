package br.com.escola.matricula.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

public record MatriculaAcademicoResumoResponse(
        UUID matriculaId,
        UUID alunoId,
        String alunoNome,
        UUID turmaId,
        String turmaNome,
        UUID periodoLetivoId,
        String periodoLetivoNome,
        String status,
        String tipoMatricula,
        MatriculaAcademicoIndicadoresResponse indicadores,
        List<MatriculaAcademicoFrequenciaResponse> frequencias,
        List<MatriculaAcademicoNotaResponse> notas) {
}
