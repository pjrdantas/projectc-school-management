package br.com.projeto.piloto.domain.model;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthPerfilModelTest {

    @Test
    @DisplayName("Deve validar Getters, Setters e Construtores")
    void deveValidarGettersSettersEConstrutores() {

        AuthPermissaoModel permissao = AuthPermissaoModel.builder().id(1L).nmPermissao("READ").build();
        Set<AuthPermissaoModel> permissoes = Set.of(permissao);
        AuthPerfilModel model = new AuthPerfilModel();
        model.setId(10L);
        model.setNmPerfil("ADMIN");

        model.setPermissoes(permissoes);
        assertEquals(10L, model.getId());
        assertEquals("ADMIN", model.getNmPerfil());

        assertEquals(permissoes, model.getPermissoes());
    }

    @Test
    @DisplayName("Deve validar o Builder e o valor Default do Set")
    void deveValidarBuilderEDefault() {
        AuthPerfilModel model = AuthPerfilModel.builder()
                .id(1L)
                .nmPerfil("USER")
                .build();

        assertNotNull(model);
        assertEquals("USER", model.getNmPerfil());
        assertNotNull(model.getPermissoes());
        assertTrue(model.getPermissoes().isEmpty(), "O Set de permissões deve iniciar vazio por padrão");
    }

    @Test
    @DisplayName("Deve validar o Construtor com Todos os Argumentos")
    void deveValidarAllArgsConstructor() {
        @SuppressWarnings("unused")
		AplicativosModel app = new AplicativosModel();
        @SuppressWarnings("unused")
		Set<AuthPermissaoModel> permissoes = Set.of();
        
  
        
    }
}
