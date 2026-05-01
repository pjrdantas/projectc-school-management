package br.com.projeto.piloto.api.dto;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.projeto.piloto.adapter.in.web.dto.AuthPermissaoResponseDTO;

class AuthPermissaoResponseDTOTest {

    @Test
    @DisplayName("Deve validar a criação e os métodos de acesso do Record")
    void deveValidarDadosEAccessors() {
        Long id = 1L;
        String permissao = "READ_PRIVILEGE";
        AuthPermissaoResponseDTO dto = new AuthPermissaoResponseDTO(id, permissao);
        assertEquals(id, dto.id());
        assertEquals(permissao, dto.nmPermissao());
    }

    @Test
    @DisplayName("Deve cobrir métodos implícitos (equals, hashCode e toString) para 100% de cobertura")
    void deveValidarMetodosPadraoDoRecord() {
        AuthPermissaoResponseDTO dto1 = new AuthPermissaoResponseDTO(1L, "WRITE");
        AuthPermissaoResponseDTO dto2 = new AuthPermissaoResponseDTO(1L, "WRITE");
        AuthPermissaoResponseDTO dto3 = new AuthPermissaoResponseDTO(2L, "DELETE");
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());
        assertTrue(dto1.toString().contains("nmPermissao=WRITE"));
    }
}
