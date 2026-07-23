package br.com.escola.pedagogicalservice.infra.database.adapter;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.AulaResponse;
import br.com.escola.pedagogicalservice.application.dto.FrequenciaAlunoResponse;
import br.com.escola.pedagogicalservice.application.dto.FrequenciaDocenteResponse;
import br.com.escola.pedagogicalservice.application.port.out.AulaPort;

@Repository
public class AulaAdapter implements AulaPort {

    private final PersistenciaLocalAdapter persistenciaLocalAdapter;

    public AulaAdapter(PersistenciaLocalAdapter persistenciaLocalAdapter) {
        this.persistenciaLocalAdapter = persistenciaLocalAdapter;
    }

    @Override
    public AulaResponse criar(String authorization, InternalRequestContext context, String requestBody) {
        return persistenciaLocalAdapter.criarAula(authorization, context, requestBody);
    }

    @Override
    public List<AulaResponse> listar(String authorization, InternalRequestContext context, UUID professorTurmaDisciplinaId, UUID turmaId) {
        return persistenciaLocalAdapter.listarAulas(authorization, context, professorTurmaDisciplinaId, turmaId);
    }

    @Override
    public AulaResponse buscarPorId(String authorization, InternalRequestContext context, UUID aulaId) {
        return persistenciaLocalAdapter.buscarAulaPorId(authorization, context, aulaId);
    }

    @Override
    public FrequenciaDocenteResponse registrarFrequenciaProfessor(String authorization, InternalRequestContext context, UUID aulaId, String requestBody) {
        return persistenciaLocalAdapter.registrarFrequenciaProfessor(authorization, context, aulaId, requestBody);
    }

    @Override
    public List<FrequenciaDocenteResponse> listarFrequenciaProfessor(String authorization, InternalRequestContext context, UUID aulaId) {
        return persistenciaLocalAdapter.listarFrequenciaProfessor(authorization, context, aulaId);
    }

    @Override
    public FrequenciaAlunoResponse registrarFrequenciaAluno(String authorization, InternalRequestContext context, UUID aulaId, String requestBody) {
        return persistenciaLocalAdapter.registrarFrequenciaAluno(authorization, context, aulaId, requestBody);
    }

    @Override
    public List<FrequenciaAlunoResponse> listarFrequenciasAlunos(String authorization, InternalRequestContext context, UUID aulaId) {
        return persistenciaLocalAdapter.listarFrequenciasAlunos(authorization, context, aulaId);
    }
}
