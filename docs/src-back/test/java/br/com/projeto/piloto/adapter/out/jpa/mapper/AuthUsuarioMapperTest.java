package br.com.projeto.piloto.adapter.out.jpa.mapper;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import br.com.projeto.piloto.adapter.out.jpa.entity.AuthUsuario;
import br.com.projeto.piloto.adapter.out.jpa.entity.AuthPerfil;
import br.com.projeto.piloto.domain.model.AuthUsuarioModel;
import br.com.projeto.piloto.domain.model.AuthPerfilModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

class AuthUsuarioMapperTest {

    private final AuthUsuarioMapper mapper = new AuthUsuarioMapper();

    @Test
    @DisplayName("Deve converter Entidade para Domínio com Perfis")
    void deveConverterEntidadeParaDominio() {
        LocalDateTime agora = LocalDateTime.now();
        AuthPerfil perfilEntity = AuthPerfil.builder().id(1L).nmPerfil("ADMIN").build();
        
        AuthUsuario entity = AuthUsuario.builder()
                .id(1L).login("user").senha("pass").nome("Nome")
                .ativo("S").email("mail@mail.com")
                .criadoEm(agora).atualizadoEm(agora)
                .perfis(new HashSet<>(Set.of(perfilEntity)))
                .build();
        AuthUsuarioModel model = mapper.toDomain(entity);
        assertNotNull(model);
        assertEquals(entity.getLogin(), model.getLogin());
        assertFalse(model.getPerfis().isEmpty());
        assertEquals("ADMIN", model.getPerfis().iterator().next().getNmPerfil());
        assertNull(mapper.toDomain(null));
    }

    @Test
    @DisplayName("Deve converter Domínio para Entidade com Perfis")
    void deveConverterDominioParaEntity() {
        AuthPerfilModel perfilModel = AuthPerfilModel.builder().id(1L).nmPerfil("USER").build();
        AuthUsuarioModel model = AuthUsuarioModel.builder()
                .id(2L).login("admin").senha("123").nome("Adm")
                .ativo("N").email("adm@mail.com")
                .perfis(Set.of(perfilModel))
                .build();
        AuthUsuario entity = mapper.toEntity(model);
        assertNotNull(entity);
        assertEquals(model.getLogin(), entity.getLogin());
        assertFalse(entity.getPerfis().isEmpty());
        assertEquals(1L, entity.getPerfis().iterator().next().getId());
        assertNull(mapper.toEntity(null));
    }
}
