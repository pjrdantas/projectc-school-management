package br.com.escola.professorservice.application.port.out;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.AllocateRequest;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.ResumoResponse;

public interface PersistenciaPort {

    void registrarCriacaoShadow(InternalRequestContext context, ResumoResponse response);

    void registrarAlocacaoShadow(
            InternalRequestContext context,
            AllocateRequest request,
            AlocacaoResponse response);
}

