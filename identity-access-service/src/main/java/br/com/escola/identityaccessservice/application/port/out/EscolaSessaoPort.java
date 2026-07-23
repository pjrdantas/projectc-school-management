package br.com.escola.identityaccessservice.application.port.out;

import java.util.List;

import br.com.escola.identityaccessservice.application.context.InternalRequestContext;
import br.com.escola.identityaccessservice.application.model.EscolaDisponivel;

public interface EscolaSessaoPort {

    List<EscolaDisponivel> listarDisponiveis(
            String authorization,
            InternalRequestContext context);
}
