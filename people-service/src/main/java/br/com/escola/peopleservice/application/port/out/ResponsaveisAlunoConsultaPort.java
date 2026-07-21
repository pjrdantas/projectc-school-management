package br.com.escola.peopleservice.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.PessoaResponsavelVinculadoResponse;

public interface ResponsaveisAlunoConsultaPort {

    List<PessoaResponsavelVinculadoResponse> listarResponsaveisPorAluno(
            UUID alunoId,
            String authorization,
            InternalRequestContext context);
}
