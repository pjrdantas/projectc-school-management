package br.com.escola.institucional.application.port;

import java.util.UUID;

import br.com.escola.institucional.application.dto.EscolaContexto;
import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;

public interface EscolaContextoPort {

    EscolaContexto obterContextoPadrao();

    EscolaContexto resolverContexto(UsuarioEntity usuario);

    boolean usuarioPodeAcessarEscola(UsuarioEntity usuario, UUID escolaId);
}
