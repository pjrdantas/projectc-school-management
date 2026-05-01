package br.com.projeto.piloto.domain.model;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthPermissaoModelTest {

    @Test
    @DisplayName("Deve validar Getters, Setters e Construtores")
    void deveValidarGettersSettersEConstrutores() {
        AuthPermissaoModel model = new AuthPermissaoModel();
        model.setId(1L);
        model.setNmPermissao("ROLE_TESTE");
        assertEquals(1L, model.getId());
        assertEquals("ROLE_TESTE", model.getNmPermissao());
    }

    @Test
    @DisplayName("Deve validar o Builder e AllArgsConstructor")
    void deveValidarBuilderEAllArgs() {
        AuthPermissaoModel model = AuthPermissaoModel.builder()
                .id(2L)
                .nmPermissao("ROLE_ADMIN")
                .build();

        assertNotNull(model);
        assertEquals(2L, model.getId());
        assertEquals("ROLE_ADMIN", model.getNmPermissao());
        AuthPermissaoModel modelFull = new AuthPermissaoModel(3L, "ROLE_USER");
        assertEquals(3L, modelFull.getId());
    }
}
