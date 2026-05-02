package br.com.projeto.piloto.accesscontrol.adapter.out.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.projeto.piloto.accesscontrol.adapter.in.web.dto.AuthPerfilRequestDTO;
import br.com.projeto.piloto.accesscontrol.adapter.in.web.dto.AuthPerfilResponseDTO;
import br.com.projeto.piloto.accesscontrol.adapter.in.web.dto.AuthPermissaoResponseDTO;
import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.entity.AuthPerfil;
import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.entity.AuthPermissao;
import br.com.projeto.piloto.accesscontrol.domain.model.AuthPerfilModel;
import br.com.projeto.piloto.accesscontrol.domain.model.AuthPermissaoModel;

class AuthPerfilMapperTest {

    @Test
    @DisplayName("Construtor - instância não nula (cobertura de classe utilitária)")
    void construtor_instanciaNaoNula() {
        assertNotNull(new AuthPerfilMapper());
    }


    @Test
    @DisplayName("toDomain(DTO, perms) - dto null → retorna null")
    void toDomainDto_dtoNull_retornaNull() {
        AuthPerfilModel result = AuthPerfilMapper.toDomain(null, Set.of(new AuthPermissaoModel()));
        assertNull(result);
    }

    @Test
    @DisplayName("toDomain(DTO, perms) - permissoes null → permissoes vazio no model")
    void toDomainDto_permissoesNull_retornaModelComPermissoesVazias() {
        AuthPerfilRequestDTO dto = new AuthPerfilRequestDTO("ROLE_TEST", Set.of(1L));

        AuthPerfilModel result = AuthPerfilMapper.toDomain(dto, null);

        assertNotNull(result);
        assertEquals("ROLE_TEST", result.getNmPerfil());
        assertTrue(result.getPermissoes().isEmpty());
    }

    @Test
    @DisplayName("toDomain(DTO, perms) - dto e permissoes válidos → model completo")
    void toDomainDto_dtoEPermissoesValidos_retornaModelCompleto() {
        AuthPerfilRequestDTO dto = new AuthPerfilRequestDTO("ROLE_ADMIN", Set.of(1L));
        AuthPermissaoModel perm = AuthPermissaoModel.builder().id(1L).nmPermissao("READ").build();

        AuthPerfilModel result = AuthPerfilMapper.toDomain(dto, Set.of(perm));

        assertNotNull(result);
        assertEquals("ROLE_ADMIN", result.getNmPerfil());
        assertFalse(result.getPermissoes().isEmpty());
        assertEquals("READ", result.getPermissoes().iterator().next().getNmPermissao());
    }

    @Test
    @DisplayName("toDomain(DTO, perms) - dto com nmPerfil null → model com nmPerfil null")
    void toDomainDto_nomePerfil_null_retornaModelComNomeNull() {
        AuthPerfilRequestDTO dto = new AuthPerfilRequestDTO(null, Set.of(1L));
        AuthPermissaoModel perm = AuthPermissaoModel.builder().id(2L).nmPermissao("WRITE").build();

        AuthPerfilModel result = AuthPerfilMapper.toDomain(dto, Set.of(perm));

        assertNotNull(result);
        assertNull(result.getNmPerfil());
    }
    
    @Test
    @DisplayName("toDomain(Entity) - entity null → retorna null")
    void toDomainEntity_entityNull_retornaNull() {
        assertNull(AuthPerfilMapper.toDomain((AuthPerfil) null));
    }

    @Test
    @DisplayName("toDomain(Entity) - permissoes null na entity → model com permissoes vazio")
    void toDomainEntity_permissoesNull_retornaModelComPermissoesVazias() {
        AuthPerfil entity = AuthPerfil.builder()
                .id(1L)
                .nmPerfil("ROLE_X")
                .permissoes(null)
                .build();

        AuthPerfilModel result = AuthPerfilMapper.toDomain(entity);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ROLE_X", result.getNmPerfil());
        assertTrue(result.getPermissoes().isEmpty());
    }

    @Test
    @DisplayName("toDomain(Entity) - permissoes não-nulas → model com permissoes mapeadas")
    void toDomainEntity_permissoesValidas_retornaModelComPermissoesMapeadas() {
        AuthPermissao permissao = AuthPermissao.builder()
                .id(10L)
                .nmPermissao("DELETE")
                .build();
        AuthPerfil entity = AuthPerfil.builder()
                .id(2L)
                .nmPerfil("ROLE_ADMIN")
                .permissoes(Set.of(permissao))
                .build();

        AuthPerfilModel result = AuthPerfilMapper.toDomain(entity);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("ROLE_ADMIN", result.getNmPerfil());
        assertFalse(result.getPermissoes().isEmpty());
        AuthPermissaoModel permResult = result.getPermissoes().iterator().next();
        assertEquals(10L, permResult.getId());
        assertEquals("DELETE", permResult.getNmPermissao());
    }

    @Test
    @DisplayName("toDomain(Entity) - permissoes vazio → model com permissoes vazio")
    void toDomainEntity_permissoesVazias_retornaModelComPermissoesVazias() {
        AuthPerfil entity = AuthPerfil.builder()
                .id(3L)
                .nmPerfil("ROLE_EMPTY")
                .permissoes(Collections.emptySet())
                .build();

        AuthPerfilModel result = AuthPerfilMapper.toDomain(entity);

        assertNotNull(result);
        assertTrue(result.getPermissoes().isEmpty());
    }

    @Test
    @DisplayName("toEntity - model null → retorna null")
    void toEntity_modelNull_retornaNull() {
        assertNull(AuthPerfilMapper.toEntity(null));
    }

    @Test
    @DisplayName("toEntity - permissoes null no model → entity com permissoes vazio")
    void toEntity_permissoesNull_retornaEntityComPermissoesVazias() {
        AuthPerfilModel model = AuthPerfilModel.builder()
                .id(5L)
                .nmPerfil("ROLE_NULL")
                .permissoes(null)
                .build();

        AuthPerfil result = AuthPerfilMapper.toEntity(model);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("ROLE_NULL", result.getNmPerfil());
        assertTrue(result.getPermissoes().isEmpty());
    }

    @Test
    @DisplayName("toEntity - permissoes válidas no model → entity com permissoes mapeadas")
    void toEntity_permissoesValidas_retornaEntityComPermissoesMapeadas() {
        AuthPermissaoModel perm = AuthPermissaoModel.builder()
                .id(20L)
                .nmPermissao("WRITE")
                .build();
        AuthPerfilModel model = AuthPerfilModel.builder()
                .id(6L)
                .nmPerfil("ROLE_B")
                .permissoes(Set.of(perm))
                .build();

        AuthPerfil result = AuthPerfilMapper.toEntity(model);

        assertNotNull(result);
        assertEquals(6L, result.getId());
        assertEquals("ROLE_B", result.getNmPerfil());
        assertFalse(result.getPermissoes().isEmpty());
        assertEquals("WRITE", result.getPermissoes().iterator().next().getNmPermissao());
        assertEquals(20L, result.getPermissoes().iterator().next().getId());
    }

    @Test
    @DisplayName("toResponse - model null → retorna null")
    void toResponse_modelNull_retornaNull() {
        assertNull(AuthPerfilMapper.toResponse(null));
    }

    @Test
    @DisplayName("toResponse - permissoes null no model → response com permissoes vazio")
    void toResponse_permissoesNull_retornaResponseComPermissoesVazias() {
        AuthPerfilModel model = AuthPerfilModel.builder()
                .id(7L)
                .nmPerfil("ROLE_C")
                .permissoes(null)
                .build();

        AuthPerfilResponseDTO result = AuthPerfilMapper.toResponse(model);

        assertNotNull(result);
        assertEquals(7L, result.id());
        assertEquals("ROLE_C", result.nmPerfil());
        assertTrue(result.permissoes().isEmpty());
    }

    @Test
    @DisplayName("toResponse - permissoes válidas → response com permissoes mapeadas")
    void toResponse_permissoesValidas_retornaResponseComPermissoesMapeadas() {
        AuthPermissaoModel perm = AuthPermissaoModel.builder()
                .id(30L)
                .nmPermissao("EXECUTE")
                .build();
        AuthPerfilModel model = AuthPerfilModel.builder()
                .id(8L)
                .nmPerfil("ROLE_D")
                .permissoes(Set.of(perm))
                .build();

        AuthPerfilResponseDTO result = AuthPerfilMapper.toResponse(model);

        assertNotNull(result);
        assertEquals(8L, result.id());
        assertEquals("ROLE_D", result.nmPerfil());
        assertFalse(result.permissoes().isEmpty());
        AuthPermissaoResponseDTO permResp = result.permissoes().iterator().next();
        assertEquals(30L, permResp.id());
        assertEquals("EXECUTE", permResp.nmPermissao());
    }

    @Test
    @DisplayName("toResponse - permissoes com elemento null dentro do Set → null propagado via AuthPermissaoMapper")
    void toResponse_permissaoNulaDentroDoSet_propagaNullNoResponse() {
        Set<AuthPermissaoModel> permsComNull = new HashSet<>();
        permsComNull.add(null);

        AuthPerfilModel model = AuthPerfilModel.builder()
                .id(9L)
                .nmPerfil("ROLE_E")
                .permissoes(permsComNull)
                .build();

        AuthPerfilResponseDTO result = AuthPerfilMapper.toResponse(model);

        assertNotNull(result);
        assertEquals(1, result.permissoes().size());
        assertTrue(result.permissoes().contains(null));
    }
}
