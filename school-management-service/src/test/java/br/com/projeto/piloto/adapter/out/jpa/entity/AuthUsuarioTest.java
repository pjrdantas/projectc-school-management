package br.com.projeto.piloto.adapter.out.jpa.entity;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

class AuthUsuarioTest {

    @Test
    @DisplayName("Deve validar Getters, Setters e Builder do Lombok para AuthUsuario")
    void deveTestarLombokEBuilder() {
        Set<AuthPerfil> perfis = new HashSet<>();
        LocalDateTime agora = LocalDateTime.now();

        AuthUsuario usuario = AuthUsuario.builder()
                .id(1L)
                .login("admin")
                .senha("hash")
                .nome("Administrador")
                .ativo("S")
                .email("admin@teste.com")
                .criadoEm(agora)
                .atualizadoEm(agora)
                .perfis(perfis)
                .build();

        assertAll("Atributos da Entity AuthUsuario",
            () -> assertEquals(1L, usuario.getId()),
            () -> assertEquals("admin", usuario.getLogin()),
            () -> assertEquals("hash", usuario.getSenha()),
            () -> assertEquals("Administrador", usuario.getNome()),
            () -> assertEquals("S", usuario.getAtivo()),
            () -> assertEquals("admin@teste.com", usuario.getEmail()),
            () -> assertEquals(agora, usuario.getCriadoEm()),
            () -> assertEquals(agora, usuario.getAtualizadoEm()),
            () -> assertEquals(perfis, usuario.getPerfis())
        );
        AuthUsuario usuarioSet = new AuthUsuario();
        usuarioSet.setLogin("novo.login");
        assertEquals("novo.login", usuarioSet.getLogin());
    }

    @Test
    @DisplayName("Deve validar o funcionamento do @PrePersist e @PreUpdate")
    void deveTestarCicloDeVida() {
        AuthUsuario usuario = new AuthUsuario();
        usuario.prePersist();
        assertNotNull(usuario.getCriadoEm());
        assertEquals(usuario.getCriadoEm(), usuario.getAtualizadoEm());
        usuario.preUpdate();
        assertNotNull(usuario.getAtualizadoEm());
    }

    @Test
    @DisplayName("Deve validar a inicialização padrão da coleção de perfis via Builder")
    void deveTestarBuilderDefault() {
        AuthUsuario usuario = AuthUsuario.builder().build();
        assertNotNull(usuario.getPerfis());
        assertTrue(usuario.getPerfis().isEmpty());
    }

    @Test
    @DisplayName("Deve validar NoArgsConstructor e AllArgsConstructor")
    void deveTestarConstrutores() {
        AuthUsuario empty = new AuthUsuario();
        assertNotNull(empty);

        AuthUsuario full = new AuthUsuario(1L, "log", "sen", "nom", "S", "mail", null, null, null);
        assertEquals(1L, full.getId());
        assertEquals("log", full.getLogin());
    }
}
