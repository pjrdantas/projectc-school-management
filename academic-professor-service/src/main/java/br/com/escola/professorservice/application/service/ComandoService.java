package br.com.escola.professorservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.AllocateRequest;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.CreateRequest;
import br.com.escola.professorservice.application.dto.ResumoResponse;
import br.com.escola.professorservice.application.port.in.ComandoUseCase;
import br.com.escola.professorservice.application.port.out.ComandoEscritaPort;
import br.com.escola.professorservice.application.port.out.PersistenciaPort;

@Service
public class ComandoService implements ComandoUseCase {

    private final ComandoEscritaPort professorWritePort;
    private final PersistenciaPort professorShadowPersistencePort;

    public ComandoService(
            ComandoEscritaPort professorWritePort,
            PersistenciaPort professorShadowPersistencePort) {
        this.professorWritePort = professorWritePort;
        this.professorShadowPersistencePort = professorShadowPersistencePort;
    }

    @Override
    public ResumoResponse criarProfessor(
            String authorization,
            InternalRequestContext context,
            CreateRequest request) {
        ResumoResponse response = professorWritePort.criarProfessor(authorization, context, request);
        professorShadowPersistencePort.registrarCriacaoShadow(context, response);
        return response;
    }

    @Override
    public AlocacaoResponse alocarProfessorTurmaDisciplina(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            AllocateRequest request) {
        AlocacaoResponse response =
                professorWritePort.alocarProfessorTurmaDisciplina(authorization, context, professorId, request);
        professorShadowPersistencePort.registrarAlocacaoShadow(context, request, response);
        return response;
    }
}

