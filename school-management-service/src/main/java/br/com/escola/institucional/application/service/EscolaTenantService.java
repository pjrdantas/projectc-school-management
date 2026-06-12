package br.com.escola.institucional.application.service;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.institucional.adapter.out.persistence.repository.EscolaJpaRepository;
import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;

@Service
public class EscolaTenantService {

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
}
