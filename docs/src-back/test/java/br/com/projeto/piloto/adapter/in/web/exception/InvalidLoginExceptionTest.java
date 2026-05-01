package br.com.projeto.piloto.adapter.in.web.exception;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

class InvalidLoginExceptionTest {

    @Test
    @DisplayName("Deve criar a exceção de login inválido com a mensagem e status corretos")
    void deveCriarExcecaoComMensagem() {
        String mensagem = "Usuário ou senha incorretos";
        InvalidLoginException exception = new InvalidLoginException(mensagem);
        assertNotNull(exception);
        assertEquals(mensagem, exception.getMessage());
        ResponseStatus responseStatus = InvalidLoginException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(responseStatus);
        assertEquals(HttpStatus.UNAUTHORIZED, responseStatus.value());
    }
}
