package br.com.escola.professor.application.dto.internal;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public record AutoridadePedagogicaResumo(
        UUID usuarioId,
        UUID escolaId,
        UUID funcionarioId,
        UUID pessoaId,
        String nome,
        String cargoCodigo,
        String cargoDescricao,
        List<String> perfis,
        List<String> permissoes
) {

    public AutoridadePedagogicaResumo {
        Objects.requireNonNull(usuarioId, "usuarioId nao pode ser nulo");
        Objects.requireNonNull(escolaId, "escolaId nao pode ser nulo");
        Objects.requireNonNull(funcionarioId, "funcionarioId nao pode ser nulo");
        Objects.requireNonNull(pessoaId, "pessoaId nao pode ser nulo");
        cargoCodigo = normalizar(cargoCodigo);
        perfis = perfis == null ? List.of() : List.copyOf(perfis);
        permissoes = permissoes == null ? List.of() : List.copyOf(permissoes);
    }

    public boolean ehCoordenador() {
        return possuiCargo("COORDENADOR");
    }

    public boolean ehDiretor() {
        return possuiCargo("DIRETOR");
    }

    public boolean possuiCargo(String codigo) {
        return cargoCodigo.equals(normalizar(codigo));
    }

    private static String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
    }
}
