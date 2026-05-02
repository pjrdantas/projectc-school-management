package br.com.projeto.piloto.api.dto;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.projeto.piloto.accesscontrol.adapter.in.web.dto.AuthPerfilResponseDTO;
import br.com.projeto.piloto.accesscontrol.adapter.in.web.dto.AuthPermissaoResponseDTO;

class AuthPerfilResponseDTOTest {

    @Test
    @DisplayName("Deve validar a criação e acesso aos dados do Record")
    void deveValidarDados() {
        Long id = 10L;
        String perfil = "GERENTE";
        Set<AuthPermissaoResponseDTO> permissoes = Collections.emptySet(); 
        
        AuthPerfilResponseDTO dto = new AuthPerfilResponseDTO(id, perfil, permissoes);
        
        assertAll("Campos do DTO",
            () -> assertEquals(id, dto.id()),
            () -> assertEquals(perfil, dto.nmPerfil()),
            () -> assertEquals(permissoes, dto.permissoes()) 
        );
    }

    @Test
    @DisplayName("Cobre métodos automáticos do Record (equals, hashCode e toString)")
    void deveValidarMetodosPadrao() {

        AuthPerfilResponseDTO dto1 = new AuthPerfilResponseDTO(1L, "ADMIN", Collections.emptySet());
        AuthPerfilResponseDTO dto2 = new AuthPerfilResponseDTO(1L, "ADMIN", Collections.emptySet());
        AuthPerfilResponseDTO dto3 = new AuthPerfilResponseDTO(2L, "USER", Collections.emptySet());
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());
        assertTrue(dto1.toString().contains("nmPerfil=ADMIN"));
    }
}
