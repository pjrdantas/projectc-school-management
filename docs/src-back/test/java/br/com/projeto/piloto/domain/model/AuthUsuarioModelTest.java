package br.com.projeto.piloto.domain.model;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthUsuarioModelTest {

    @Test
    @DisplayName("Deve validar Getters, Setters e Construtores")
    void deveValidarGettersSettersEConstrutores() {
        LocalDateTime agora = LocalDateTime.now();
        Set<AuthPerfilModel> perfis = Set.of(new AuthPerfilModel());
        AuthUsuarioModel model = new AuthUsuarioModel();
        model.setId(1L);
        model.setLogin("admin");
        model.setSenha("123");
        model.setNome("Administrador");
        model.setAtivo("S");
        model.setEmail("admin@projeto.com");
        model.setCriadoEm(agora);
        model.setAtualizadoEm(agora);
        model.setPerfis(perfis);
        assertAll("Validação de campos do Model",
            () -> assertEquals(1L, model.getId()),
            () -> assertEquals("admin", model.getLogin()),
            () -> assertEquals("123", model.getSenha()),
            () -> assertEquals("Administrador", model.getNome()),
            () -> assertEquals("S", model.getAtivo()),
            () -> assertEquals("admin@projeto.com", model.getEmail()),
            () -> assertEquals(agora, model.getCriadoEm()),
            () -> assertEquals(agora, model.getAtualizadoEm()),
            () -> assertEquals(perfis, model.getPerfis())
        );
    }

    @Test
    @DisplayName("Cobre Linhas do Builder e Valor Default de Perfis")
    void deveValidarBuilderEDefaults() {
        AuthUsuarioModel model = AuthUsuarioModel.builder()
                .login("user")
                .build();

        assertNotNull(model.getPerfis());
        assertTrue(model.getPerfis().isEmpty(), "O Set de perfis deve iniciar vazio por padrão via @Builder.Default");
    }

    @Test
    @DisplayName("Cobre Linhas do método isAtivo (Ramo Positivo e Negativo)")
    void deveValidarMetodoIsAtivo() {
        AuthUsuarioModel model = new AuthUsuarioModel();
        model.setAtivo("S");
        assertTrue(model.isAtivo());
        model.setAtivo("s");
        assertTrue(model.isAtivo());
        model.setAtivo("N");
        assertFalse(model.isAtivo());
        model.setAtivo(null);
        assertFalse(model.isAtivo());
    }

    @Test
    @DisplayName("Cobre AllArgsConstructor")
    void deveValidarAllArgsConstructor() {
        LocalDateTime data = LocalDateTime.now();
        AuthUsuarioModel model = new AuthUsuarioModel(1L, "L", "S", "N", "A", "E", data, data, Set.of());
        assertNotNull(model);
        assertEquals(1L, model.getId());
    }
}
