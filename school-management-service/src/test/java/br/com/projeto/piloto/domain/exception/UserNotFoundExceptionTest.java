package br.com.projeto.piloto.domain.exception;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserNotFoundExceptionTest {

    @Test
    @DisplayName("Deve criar a exceção de usuário não encontrado com a mensagem correta")
    void deveCriarExcecaoComMensagem() {
        String mensagem = "Usuário com ID 1 não localizado";
        UserNotFoundException exception = new UserNotFoundException(mensagem);
        assertNotNull(exception);
        assertEquals(mensagem, exception.getMessage());
        assertTrue(exception instanceof DomainException, "Deve herdar de DomainException");
    }
}
