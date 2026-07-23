package br.com.escola.professorservice.application.port.out;

import java.util.List;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.FuncionarioElegivelResponse;

public interface FuncionarioElegivelPort {

    List<FuncionarioElegivelResponse> listarFuncionariosElegiveis(
            String authorization,
            InternalRequestContext context);
}
