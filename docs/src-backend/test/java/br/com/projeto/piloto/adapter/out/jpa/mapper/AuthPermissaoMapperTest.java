package br.com.projeto.piloto.adapter.out.jpa.mapper;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.projeto.piloto.adapter.in.web.dto.AuthPermissaoRequestDTO;
import br.com.projeto.piloto.adapter.in.web.dto.AuthPermissaoResponseDTO;
import br.com.projeto.piloto.adapter.out.jpa.entity.AuthPermissao;
import br.com.projeto.piloto.domain.model.AuthPermissaoModel;

class AuthPermissaoMapperTest {

    @Test
    @DisplayName("Deve converter DTO para Domínio com sucesso e tratar nulo")
    void deveConverterDtoParaDominio() {
        AuthPermissaoRequestDTO dto = new AuthPermissaoRequestDTO("CRIAR_USUARIO");
        
        AuthPermissaoModel model = AuthPermissaoMapper.toDomain(dto);
        
        assertNotNull(model);
        assertEquals("CRIAR_USUARIO", model.getNmPermissao());
        assertNull(AuthPermissaoMapper.toDomain((AuthPermissaoRequestDTO) null));
    }

    @Test
    @DisplayName("Deve converter Entidade para Domínio com sucesso e tratar nulo")
    void deveConverterEntidadeParaDominio() {
        AuthPermissao entity = AuthPermissao.builder()
                .id(1L)
                .nmPermissao("DELETAR_PERFIL")
                .build();

        AuthPermissaoModel model = AuthPermissaoMapper.toDomain(entity);

        assertNotNull(model);
        assertEquals(1L, model.getId());
        assertEquals("DELETAR_PERFIL", model.getNmPermissao());
        assertNull(AuthPermissaoMapper.toDomain((AuthPermissao) null));
    }

    @Test
    @DisplayName("Deve converter Domínio para Entidade com sucesso e tratar nulo")
    void deveConverterDominioParaEntidade() {
        AuthPermissaoModel model = AuthPermissaoModel.builder()
                .id(2L)
                .nmPermissao("EDITAR_APP")
                .build();

        AuthPermissao entity = AuthPermissaoMapper.toEntity(model);

        assertNotNull(entity);
        assertEquals(2L, entity.getId());
        assertEquals("EDITAR_APP", entity.getNmPermissao());
        assertNull(AuthPermissaoMapper.toEntity(null));
    }

    @Test
    @DisplayName("Deve converter Domínio para Response DTO com sucesso e tratar nulo")
    void deveConverterDominioParaResponse() {
        AuthPermissaoModel model = AuthPermissaoModel.builder()
                .id(3L)
                .nmPermissao("VISUALIZAR")
                .build();

        AuthPermissaoResponseDTO response = AuthPermissaoMapper.toResponse(model);

        assertNotNull(response);
        assertEquals(3L, response.id());
        assertEquals("VISUALIZAR", response.nmPermissao());
        assertNull(AuthPermissaoMapper.toResponse(null));
    }
    
    @Test
    void deveCobrirConstrutorEFluxos() {
        new AuthPermissaoMapper();
        assertNull(AuthPermissaoMapper.toDomain((AuthPermissaoRequestDTO) null));
        assertNull(AuthPermissaoMapper.toDomain((AuthPermissao) null));
        assertNull(AuthPermissaoMapper.toEntity(null));
        assertNull(AuthPermissaoMapper.toResponse(null));
    }

}
