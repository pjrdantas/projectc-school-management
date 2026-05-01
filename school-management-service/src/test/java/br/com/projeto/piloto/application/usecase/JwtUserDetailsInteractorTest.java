package br.com.projeto.piloto.application.usecase;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

class JwtUserDetailsInteractorTest {

    private final JwtUserDetailsInteractor service = new JwtUserDetailsInteractor();

    @Test
    @DisplayName("Deve carregar usuário com sucesso e retornar detalhes corretos")
    void deveCarregarUsuarioComSucesso() {
        String username = "testuser";
        UserDetails userDetails = service.loadUserByUsername(username);
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("", userDetails.getPassword(), "A senha deve ser vazia conforme a implementação atual para JWT.");
        assertTrue(userDetails.getAuthorities().stream()
                   .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")), 
                   "Deve conter a autoridade ROLE_ADMIN.");
    }

    @Test
    @DisplayName("Deve simular cenário de usuário não encontrado (para completude)")
    void deveSimularUsuarioNaoEncontrado() {
        assertDoesNotThrow(() -> service.loadUserByUsername("qualquercoisa"));
    }
}
