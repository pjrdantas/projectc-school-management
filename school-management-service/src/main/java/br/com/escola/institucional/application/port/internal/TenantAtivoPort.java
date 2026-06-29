package br.com.escola.institucional.application.port.internal;

import java.util.UUID;

import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.institucional.application.dto.TenantAtivoResumo;
import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;

public interface TenantAtivoPort {

    TenantAtivoResumo resolverTenantAtivo(UsuarioEntity usuario);

    TenantAtivoResumo resolverTenantDaSessaoOuUsuario(UsuarioEntity usuario, UUID escolaIdSessao);

    EscolaEntity carregarEscola(UUID escolaId);
}
