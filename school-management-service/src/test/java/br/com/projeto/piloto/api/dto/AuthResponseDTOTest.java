package br.com.projeto.piloto.api.dto;


import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.projeto.piloto.adapter.in.web.dto.AuthResponseDTO;

class AuthResponseDTOTest {

    @Test
    @DisplayName("Deve validar a criação e acesso aos dados de autenticação")
    void deveValidarAcessoDados() {
        String token = "jwt-token";
        String refresh = "refresh-token";
        String user = "admin";
        String nome = "Administrador do Sistema";
        Set<String> perfis = Set.of("ADMIN", "USER");
        Set<String> permissoes = Set.of("READ", "CREATE", "UPDATE", "DELETE");
        AuthResponseDTO dto = new AuthResponseDTO(token, refresh, user, nome, perfis, permissoes);
        assertAll("Verificação de campos do DTO de Resposta Auth",
            () -> assertEquals(token, dto.token()),
            () -> assertEquals(refresh, dto.refreshToken()),
            () -> assertEquals(user, dto.usuario()),
            () -> assertEquals(nome, dto.nome()),
            () -> assertEquals(perfis, dto.perfis()),
            () -> assertEquals(permissoes, dto.permissoes())
        );
    }

    @Test
    @DisplayName("Deve cobrir métodos implícitos do Record (equals, hashCode e toString)")
    void deveValidarMetodosPadrao() {
        Set<String> perfis = Set.of("USER");
        Set<String> permissoes = Set.of("READ");
        AuthResponseDTO dto1 = new AuthResponseDTO("tk", "rf", "u", "n", perfis, permissoes);
        AuthResponseDTO dto2 = new AuthResponseDTO("tk", "rf", "u", "n", perfis, permissoes);
        AuthResponseDTO dto3 = new AuthResponseDTO("outro", "rf", "u", "n", perfis, permissoes);
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());
        assertTrue(dto1.toString().contains("usuario=u"));
        assertTrue(dto1.toString().contains("perfis=[USER]"));
    }
}
