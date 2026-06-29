package br.com.escola.seguranca.application.port.internal;

import java.util.List;
import java.util.UUID;

import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;
import br.com.escola.seguranca.application.dto.internal.ContextoAutenticadoResumo;
import br.com.escola.seguranca.application.dto.internal.SessaoAutenticadaResumo;

public interface IdentidadeTenantPort {

    SessaoAutenticadaResumo autenticar(String login, String senha);

    SessaoAutenticadaResumo renovarSessao(String refreshToken);

    void encerrarSessao(String refreshToken);

    UsuarioEntity validarAccessToken(String accessToken);

    ContextoAutenticadoResumo resolverContextoAtual(String accessToken);

    List<String> buscarPermissoes(UUID idUsuario);
}
