package br.com.escola.pedagogicalservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.AulaResponse;
import br.com.escola.pedagogicalservice.application.dto.FrequenciaAlunoResponse;
import br.com.escola.pedagogicalservice.application.dto.FrequenciaProfessorResponse;

public interface AulaUseCase {

    AulaResponse criar(
            String authorization,
            InternalRequestContext context,
            String requestBody);

    List<AulaResponse> listar(
            String authorization,
            InternalRequestContext context,
            UUID professorTurmaDisciplinaId,
            UUID turmaId);

    AulaResponse buscarPorId(
            String authorization,
            InternalRequestContext context,
            UUID aulaId);

    FrequenciaProfessorResponse registrarFrequenciaProfessor(
            String authorization,
            InternalRequestContext context,
            UUID aulaId,
            String requestBody);

    List<FrequenciaProfessorResponse> listarFrequenciaProfessor(
            String authorization,
            InternalRequestContext context,
            UUID aulaId);

    FrequenciaAlunoResponse registrarFrequenciaAluno(
            String authorization,
            InternalRequestContext context,
            UUID aulaId,
            String requestBody);

    List<FrequenciaAlunoResponse> listarFrequenciasAlunos(
            String authorization,
            InternalRequestContext context,
            UUID aulaId);
}
