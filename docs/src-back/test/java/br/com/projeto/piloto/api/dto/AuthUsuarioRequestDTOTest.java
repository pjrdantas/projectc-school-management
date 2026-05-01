package br.com.projeto.piloto.api.dto;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.projeto.piloto.adapter.in.web.dto.AuthUsuarioRequestDTO;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

class AuthUsuarioRequestDTOTest {

    @Test
    @DisplayName("Deve validar a criação e acesso aos dados do Usuário")
    void deveValidarDadosEAccessors() {
        Set<Long> perfis = Set.of(1L, 2L);
        AuthUsuarioRequestDTO dto = new AuthUsuarioRequestDTO(
            "admin", "senha123", "Admin Teste", "S", "admin@teste.com", perfis
        );
        assertAll("Verificação de campos do record",
            () -> assertEquals("admin", dto.login()),
            () -> assertEquals("senha123", dto.senha()),
            () -> assertEquals("Admin Teste", dto.nome()),
            () -> assertEquals("S", dto.ativo()),
            () -> assertEquals("admin@teste.com", dto.email()),
            () -> assertEquals(perfis, dto.perfisIds())
        );
    }

    @Test
    @DisplayName("Cobre métodos implícitos (equals, hashCode e toString) para 100% de cobertura")
    void deveValidarMetodosPadraoDoRecord() {
        Set<Long> ids = Set.of(1L);
        AuthUsuarioRequestDTO dto1 = new AuthUsuarioRequestDTO("u", "s", "n", "S", "e", ids);
        AuthUsuarioRequestDTO dto2 = new AuthUsuarioRequestDTO("u", "s", "n", "S", "e", ids);
        AuthUsuarioRequestDTO dto3 = new AuthUsuarioRequestDTO("x", "s", "n", "S", "e", ids);
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());
        assertTrue(dto1.toString().contains("login=u"));
    }
}
