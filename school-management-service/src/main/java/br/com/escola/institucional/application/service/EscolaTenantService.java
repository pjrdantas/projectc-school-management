package br.com.escola.institucional.application.service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.institucional.adapter.out.persistence.repository.EscolaJpaRepository;
import br.com.escola.institucional.application.dto.EscolaContexto;
import br.com.escola.institucional.application.port.EscolaContextoPort;
import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;

@Service
public class EscolaTenantService implements EscolaContextoPort {

    public static final UUID ESCOLA_PADRAO_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");

    private static final String ESCOLA_PADRAO_NOME = "Escola padrão";

    private final EscolaJpaRepository escolaJpaRepository;
    private final JdbcTemplate jdbcTemplate;

    public EscolaTenantService(EscolaJpaRepository escolaJpaRepository, JdbcTemplate jdbcTemplate) {
        this.escolaJpaRepository = escolaJpaRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public EscolaEntity obterOuCriarEscolaPadrao() {
        return escolaJpaRepository.findById(ESCOLA_PADRAO_ID)
                .orElseGet(() -> {
                    jdbcTemplate.update("""
                            INSERT INTO escola (id_escola, nome, ativo, created_at)
                            VALUES (?, ?, true, CURRENT_TIMESTAMP)
                            """, ESCOLA_PADRAO_ID, ESCOLA_PADRAO_NOME);
                    return escolaJpaRepository.findById(ESCOLA_PADRAO_ID)
                            .orElseThrow(() -> new IllegalStateException("Escola padrão não encontrada após criação."));
                });
    }

    @Transactional
    public EscolaEntity resolverEscolaAtiva(UsuarioEntity usuario) {
        if (usuario != null && usuario.getEscola() != null) {
            return usuario.getEscola();
        }

        return obterOuCriarEscolaPadrao();
    }

    @Override
    @Transactional
    public EscolaContexto obterContextoPadrao() {
        EscolaEntity escola = obterOuCriarEscolaPadrao();
        return new EscolaContexto(escola.getId(), escola.getNome(), null, Set.of(), Set.of());
    }

    @Override
    @Transactional
    public EscolaContexto resolverContexto(UsuarioEntity usuario) {
        EscolaEntity escola = resolverEscolaAtiva(usuario);
        return new EscolaContexto(
                escola.getId(),
                escola.getNome(),
                usuario == null ? null : usuario.getId(),
                perfis(usuario),
                permissoes(usuario));
    }

    @Override
    @Transactional
    public boolean usuarioPodeAcessarEscola(UsuarioEntity usuario, UUID escolaId) {
        if (escolaId == null) {
            return false;
        }
        EscolaEntity escola = resolverEscolaAtiva(usuario);
        return escolaId.equals(escola.getId());
    }

    private Set<String> perfis(UsuarioEntity usuario) {
        if (usuario == null || usuario.getPerfis() == null) {
            return Set.of();
        }
        return usuario.getPerfis().stream()
                .map(perfil -> perfil.getCodigo())
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<String> permissoes(UsuarioEntity usuario) {
        if (usuario == null || usuario.getPerfis() == null) {
            return Set.of();
        }
        return usuario.getPerfis().stream()
                .flatMap(perfil -> perfil.getPermissoes().stream())
                .map(permissao -> permissao.getCodigo())
                .collect(Collectors.toUnmodifiableSet());
    }
}
