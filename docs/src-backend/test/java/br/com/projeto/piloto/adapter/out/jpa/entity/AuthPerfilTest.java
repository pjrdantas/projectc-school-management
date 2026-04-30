package br.com.projeto.piloto.adapter.out.jpa.entity;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

class AuthPerfilTest {

    @Test
    @DisplayName("Deve validar Getters, Setters e Builder do Lombok para AuthPerfil")
    void deveTestarLombokEConstrutores() {

        Set<AuthUsuario> usuarios = new HashSet<>();
        Set<AuthPermissao> permissoes = new HashSet<>();
        AuthPerfil perfil = AuthPerfil.builder()
                .id(10L)
                .nmPerfil("GERENTE")
                .usuarios(usuarios)
                .permissoes(permissoes)
                .build();
        assertAll("Atributos da Entity AuthPerfil",
            () -> assertEquals(10L, perfil.getId()),
            () -> assertEquals("GERENTE", perfil.getNmPerfil()),
            () -> assertEquals(usuarios, perfil.getUsuarios()),
            () -> assertEquals(permissoes, perfil.getPermissoes())
        );
        AuthPerfil perfilSet = new AuthPerfil();
        perfilSet.setNmPerfil("NOVO_PERFIL");
        assertEquals("NOVO_PERFIL", perfilSet.getNmPerfil());
    }

    @Test
    @DisplayName("Deve validar a inicialização padrão das coleções (HashSet)")
    void deveTestarColecoesPadrao() {
        AuthPerfil perfilNovo = new AuthPerfil();
        assertNotNull(perfilNovo.getUsuarios()); 
        assertTrue(perfilNovo.getUsuarios().isEmpty());
        assertNotNull(perfilNovo.getPermissoes());
        AuthPerfil perfilBuilder = AuthPerfil.builder().build();
        assertNotNull(perfilBuilder.getUsuarios());
        assertNotNull(perfilBuilder.getPermissoes());
    }


    @Test
    @DisplayName("Deve validar o construtor com todos os argumentos")
    void deveTestarAllArgsConstructor() {

        Set<AuthUsuario> users = new HashSet<>();
        Set<AuthPermissao> perms = new HashSet<>();
        
        AuthPerfil perfil = new AuthPerfil(1L, "ADMIN", users, perms);
        
        assertEquals("ADMIN", perfil.getNmPerfil());

    }
}
