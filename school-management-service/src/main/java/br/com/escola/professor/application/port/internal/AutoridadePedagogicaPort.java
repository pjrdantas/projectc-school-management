package br.com.escola.professor.application.port.internal;

import java.util.Set;

import br.com.escola.professor.application.dto.internal.AutoridadePedagogicaResumo;

public interface AutoridadePedagogicaPort {

    Set<String> CARGOS_COORDENACAO_DIRECAO = Set.of("COORDENADOR", "DIRETOR");

    AutoridadePedagogicaResumo resolver(String accessToken, Set<String> cargosPermitidos);

    default AutoridadePedagogicaResumo resolverCoordenacaoOuDirecao(String accessToken) {
        return resolver(accessToken, CARGOS_COORDENACAO_DIRECAO);
    }
}
