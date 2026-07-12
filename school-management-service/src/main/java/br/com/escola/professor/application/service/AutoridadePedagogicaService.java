package br.com.escola.professor.application.service;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import br.com.escola.compartilhado.pessoa.entity.PessoaEntity;
import br.com.escola.professor.application.dto.internal.AutoridadePedagogicaResumo;
import br.com.escola.professor.application.port.internal.AutoridadePedagogicaPort;
import br.com.escola.rh.adapter.out.persistence.entity.CargoEntity;
import br.com.escola.rh.adapter.out.persistence.entity.FuncionarioEntity;
import br.com.escola.rh.adapter.out.persistence.repository.FuncionarioJpaRepository;
import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;
import br.com.escola.seguranca.adapter.out.persistence.repository.SpringUsuarioJpaRepository;
import br.com.escola.seguranca.application.dto.internal.ContextoAutenticadoResumo;
import br.com.escola.seguranca.application.port.internal.IdentidadeTenantPort;

@Service
public class AutoridadePedagogicaService implements AutoridadePedagogicaPort {

    private final IdentidadeTenantPort identidadeTenantPort;
    private final SpringUsuarioJpaRepository usuarioRepository;
    private final FuncionarioJpaRepository funcionarioRepository;

    public AutoridadePedagogicaService(
            IdentidadeTenantPort identidadeTenantPort,
            SpringUsuarioJpaRepository usuarioRepository,
            FuncionarioJpaRepository funcionarioRepository) {
        this.identidadeTenantPort = identidadeTenantPort;
        this.usuarioRepository = usuarioRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    @Override
    public AutoridadePedagogicaResumo resolver(String accessToken, Set<String> cargosPermitidos) {
        Set<String> cargosNormalizados = normalizarCargos(cargosPermitidos);
        if (cargosNormalizados.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos um cargo pedagogico permitido.");
        }

        ContextoAutenticadoResumo contexto = identidadeTenantPort.resolverContextoAtual(accessToken);
        if (contexto.usuarioId() == null || contexto.escolaId() == null) {
            throw acessoNegado();
        }

        UsuarioEntity usuario = usuarioRepository.findById(contexto.usuarioId())
                .filter(UsuarioEntity::isAtivo)
                .orElseThrow(AutoridadePedagogicaService::acessoNegado);

        String email = normalizarEmail(usuario.getEmail());
        if (email.isBlank()) {
            throw acessoNegado();
        }

        FuncionarioEntity funcionario = funcionarioRepository.findAtivosByPessoaEmailAndEscolaId(email, contexto.escolaId())
                .stream()
                .filter(this::vinculoAtivo)
                .filter(vinculo -> cargosNormalizados.contains(normalizar(obterCargo(vinculo).getCodigo())))
                .findFirst()
                .orElseThrow(AutoridadePedagogicaService::acessoNegado);

        PessoaEntity pessoa = funcionario.getPessoa();
        CargoEntity cargo = obterCargo(funcionario);
        return new AutoridadePedagogicaResumo(
                contexto.usuarioId(),
                contexto.escolaId(),
                funcionario.getId(),
                pessoa.getId(),
                pessoa.getNomeCompleto(),
                cargo.getCodigo(),
                cargo.getDescricao(),
                contexto.perfis(),
                contexto.permissoes());
    }

    private boolean vinculoAtivo(FuncionarioEntity funcionario) {
        return funcionario != null
                && Boolean.TRUE.equals(funcionario.getAtivo())
                && funcionario.getPessoa() != null
                && funcionario.getPessoa().isAtivo()
                && funcionario.getPessoa().getId() != null
                && funcionario.getCargo() != null
                && !normalizar(funcionario.getCargo().getCodigo()).isBlank();
    }

    private static CargoEntity obterCargo(FuncionarioEntity funcionario) {
        CargoEntity cargo = funcionario.getCargo();
        if (cargo == null) {
            throw acessoNegado();
        }
        return cargo;
    }

    private static Set<String> normalizarCargos(Set<String> cargosPermitidos) {
        if (cargosPermitidos == null) {
            return Set.of();
        }
        return cargosPermitidos.stream()
                .map(AutoridadePedagogicaService::normalizar)
                .filter(codigo -> !codigo.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static String normalizarEmail(String email) {
        return email == null ? "" : email.trim();
    }

    private static String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
    }

    private static AccessDeniedException acessoNegado() {
        return new AccessDeniedException("Usuario autenticado nao possui autoridade pedagogica para esta operacao.");
    }
}
