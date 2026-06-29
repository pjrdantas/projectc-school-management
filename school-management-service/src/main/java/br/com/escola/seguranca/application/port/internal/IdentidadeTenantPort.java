package br.com.escola.seguranca.application.port.internal;

import java.util.List;

import br.com.escola.seguranca.application.dto.internal.ContextoAutenticadoResumo;
import br.com.escola.seguranca.application.dto.internal.PrincipalAutenticadoResumo;
import br.com.escola.seguranca.application.dto.internal.SessaoAutenticadaResumo;

public interface IdentidadeTenantPort {

    SessaoAutenticadaResumo autenticar(String login, String senha);

    SessaoAutenticadaResumo renovarSessao(String refreshToken);

    void encerrarSessao(String refreshToken);

    PrincipalAutenticadoResumo resolverPrincipal(String accessToken);

    ContextoAutenticadoResumo resolverContextoAtual(String accessToken);
}
