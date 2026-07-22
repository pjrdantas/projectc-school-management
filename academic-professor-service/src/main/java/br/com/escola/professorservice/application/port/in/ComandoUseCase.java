package br.com.escola.professorservice.application.port.in;

import java.util.UUID;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.AllocateRequest;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.CreateRequest;
import br.com.escola.professorservice.application.dto.ResumoResponse;
import br.com.escola.professorservice.application.dto.UpdateRequest;
import br.com.escola.professorservice.application.dto.UpdateAllocateRequest;

public interface ComandoUseCase {

    ResumoResponse criarProfessor(
            String authorization,
            InternalRequestContext context,
            CreateRequest request);

    ResumoResponse atualizarProfessor(
            InternalRequestContext context,
            UUID professorId,
            UpdateRequest request);

    AlocacaoResponse alocarProfessorTurmaDisciplina(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            AllocateRequest request);

    AlocacaoResponse atualizarAlocacaoProfessorTurmaDisciplina(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            UUID alocacaoId,
            UpdateAllocateRequest request);

    void encerrarAlocacaoProfessorTurmaDisciplina(
            InternalRequestContext context,
            UUID professorId,
            UUID alocacaoId);
}

