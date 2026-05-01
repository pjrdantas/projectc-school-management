package br.com.projeto.piloto.api.dto;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.projeto.piloto.adapter.in.web.dto.AuthPerfilResumoDTO;

class AuthPerfilResumoDTOTest {

    @Test
    @DisplayName("Deve validar a integridade e acesso aos dados do Record Resumo")
    void deveValidarAcessoDados() {
        Long id = 5L;
        String nmPerfil = "OPERADOR";
        AuthPerfilResumoDTO dto = new AuthPerfilResumoDTO(id, nmPerfil);
        assertEquals(id, dto.id());
        assertEquals(nmPerfil, dto.nmPerfil());
    }

    @Test
    @DisplayName("Deve cobrir métodos gerados automaticamente (equals, hashCode, toString)")
    void deveValidarMetodosAutoGerados() {
        AuthPerfilResumoDTO dto1 = new AuthPerfilResumoDTO(1L, "TESTE");
        AuthPerfilResumoDTO dto2 = new AuthPerfilResumoDTO(1L, "TESTE");
        AuthPerfilResumoDTO dto3 = new AuthPerfilResumoDTO(2L, "OUTRO");
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());
        assertTrue(dto1.toString().contains("id=1"));
        assertTrue(dto1.toString().contains("nmPerfil=TESTE"));
    }
}
