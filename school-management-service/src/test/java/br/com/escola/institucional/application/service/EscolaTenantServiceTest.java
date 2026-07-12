package br.com.escola.institucional.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.institucional.adapter.out.persistence.repository.EscolaJpaRepository;
import br.com.escola.institucional.application.dto.OrigemTenantAtivo;
import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;

@ExtendWith(MockitoExtension.class)
class EscolaTenantServiceTest {

    @Mock
    private EscolaJpaRepository escolaJpaRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveResolverTenantPelaEscolaDaSessaoQuandoInformada() {
        UUID escolaId = UUID.randomUUID();
        EscolaEntity escola = escola(escolaId, "Escola Sessao");
        EscolaTenantService service = new EscolaTenantService(escolaJpaRepository, jdbcTemplate);

        when(escolaJpaRepository.findById(escolaId)).thenReturn(Optional.of(escola));

        var tenant = service.resolverTenantDaSessaoOuUsuario(null, escolaId);

        assertThat(tenant.escolaId()).isEqualTo(escolaId);
        assertThat(tenant.escolaNome()).isEqualTo("Escola Sessao");
        assertThat(tenant.origem()).isEqualTo(OrigemTenantAtivo.ESCOLA_SESSAO);
    }

    @Test
    void deveResolverTenantPeloUsuarioQuandoNaoHouverEscolaNaSessao() {
        UUID escolaId = UUID.randomUUID();
        EscolaEntity escola = escola(escolaId, "Escola Usuario");
        UsuarioEntity usuario = UsuarioEntity.builder()
                .id(UUID.randomUUID())
                .username("professor52")
                .escola(escola)
                .build();
        EscolaTenantService service = new EscolaTenantService(escolaJpaRepository, jdbcTemplate);

        var tenant = service.resolverTenantDaSessaoOuUsuario(usuario, null);

        assertThat(tenant.escolaId()).isEqualTo(escolaId);
        assertThat(tenant.escolaNome()).isEqualTo("Escola Usuario");
        assertThat(tenant.origem()).isEqualTo(OrigemTenantAtivo.USUARIO_ESCOLA);
    }

    @Test
    void deveResolverTenantPadraoQuandoUsuarioNaoTiverEscola() {
        EscolaEntity escolaPadrao = escola(EscolaTenantService.ESCOLA_PADRAO_ID, "Escola padrão");
        EscolaTenantService service = new EscolaTenantService(escolaJpaRepository, jdbcTemplate);

        when(escolaJpaRepository.findById(EscolaTenantService.ESCOLA_PADRAO_ID)).thenReturn(Optional.of(escolaPadrao));

        var tenant = service.resolverTenantAtivo(UsuarioEntity.builder()
                .id(UUID.randomUUID())
                .username("sem-escola")
                .build());

        assertThat(tenant.escolaId()).isEqualTo(EscolaTenantService.ESCOLA_PADRAO_ID);
        assertThat(tenant.escolaNome()).isEqualTo("Escola padrão");
        assertThat(tenant.origem()).isEqualTo(OrigemTenantAtivo.ESCOLA_PADRAO);
        verify(escolaJpaRepository).findById(EscolaTenantService.ESCOLA_PADRAO_ID);
    }

    private EscolaEntity escola(UUID id, String nome) {
        EscolaEntity escola = new EscolaEntity();
        escola.setId(id);
        escola.setNome(nome);
        escola.setAtivo(true);
        escola.prePersist();
        return escola;
    }
}
