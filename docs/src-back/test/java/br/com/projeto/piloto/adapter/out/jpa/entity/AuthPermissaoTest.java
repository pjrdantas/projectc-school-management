package br.com.projeto.piloto.adapter.out.jpa.entity;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

class AuthPermissaoTest {

    @Test
    @DisplayName("Deve validar Getters, Setters e Builder do Lombok para AuthPermissao")
    void deveTestarLombokEBuilder() {
        Set<AuthPerfil> perfis = new HashSet<>();
        perfis.add(new AuthPerfil());
        AuthPermissao permissao = AuthPermissao.builder()
                .id(1L)
                .nmPermissao("READ_PRIVILEGE")
                .perfis(perfis)
                .build();
        assertAll("Atributos da Entity AuthPermissao",
            () -> assertEquals(1L, permissao.getId()),
            () -> assertEquals("READ_PRIVILEGE", permissao.getNmPermissao()),
            () -> assertEquals(perfis, permissao.getPerfis()),
            () -> assertFalse(permissao.getPerfis().isEmpty())
        );
        AuthPermissao permissaoSet = new AuthPermissao();
        permissaoSet.setId(5L);
        permissaoSet.setNmPermissao("WRITE_PRIVILEGE");
        
        assertEquals(5L, permissaoSet.getId());
        assertEquals("WRITE_PRIVILEGE", permissaoSet.getNmPermissao());
    }

    @Test
    @DisplayName("Deve validar a inicialização padrão da coleção de perfis")
    void deveTestarColecaoPadrao() {
        AuthPermissao emptyEntity = new AuthPermissao();
        assertNotNull(emptyEntity.getPerfis());
        AuthPermissao builderEntity = AuthPermissao.builder().build();
        assertNotNull(builderEntity.getPerfis());
        assertTrue(builderEntity.getPerfis().isEmpty());
    }

    @Test
    @DisplayName("Deve validar o AllArgsConstructor para cobertura total")
    void deveTestarAllArgsConstructor() {
        Set<AuthPerfil> perfis = new HashSet<>();
        AuthPermissao permissao = new AuthPermissao(1L, "DELETE_PRIVILEGE", perfis);
        
        assertNotNull(permissao);
        assertEquals(1L, permissao.getId());
        assertEquals("DELETE_PRIVILEGE", permissao.getNmPermissao());
    }
}
