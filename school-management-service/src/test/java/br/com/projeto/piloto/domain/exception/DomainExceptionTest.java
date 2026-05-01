package br.com.projeto.piloto.domain.exception;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DomainExceptionTest {

    @Test
    @DisplayName("Deve criar a exceção com a mensagem correta")
    void deveCriarExcecaoComMensagem() {
        String mensagem = "Erro de domínio específico";
        
        DomainException exception = new DomainException(mensagem);
        
        assertEquals(mensagem, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }
}
