package br.com.escola.peopleservice.application.port.out;

import java.util.UUID;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.model.EscolaPessoa;

public interface EscolaPessoaPort {

    EscolaPessoa buscar(UUID escolaId, String authorization, InternalRequestContext context);
}
