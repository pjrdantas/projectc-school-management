package br.com.escola.professorservice.application.port.out;

import java.util.UUID;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.AllocateRequest;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.CreateRequest;
import br.com.escola.professorservice.application.dto.ResumoResponse;

public interface ComandoEscritaPort {

    ResumoResponse criarProfessor(
            String authorization,
            InternalRequestContext context,
            CreateRequest request);

    AlocacaoResponse alocarProfessorTurmaDisciplina(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            AllocateRequest request);
}

