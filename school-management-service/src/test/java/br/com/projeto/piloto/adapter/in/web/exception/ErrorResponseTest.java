package br.com.projeto.piloto.adapter.in.web.exception;



import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ErrorResponseTest {

    @Test
    @DisplayName("Deve validar Getters, Setters e Construtores")
    void deveValidarGettersSettersEConstrutores() {
        LocalDateTime agora = LocalDateTime.now();
        Map<String, String> errors = Map.of("campo", "erro");
        ErrorResponse response = new ErrorResponse();
        response.setTimestamp(agora);
        response.setStatus(400);
        response.setError("Bad Request");
        response.setMessage("Erro de teste");
        response.setPath("/api/teste");
        response.setValidationErrors(errors);
        assertAll("Validação de campos do ErrorResponse",
            () -> assertEquals(agora, response.getTimestamp()),
            () -> assertEquals(400, response.getStatus()),
            () -> assertEquals("Bad Request", response.getError()),
            () -> assertEquals("Erro de teste", response.getMessage()),
            () -> assertEquals("/api/teste", response.getPath()),
            () -> assertEquals(errors, response.getValidationErrors())
        );
    }

    @Test
    @DisplayName("Deve validar o Builder")
    void deveValidarBuilder() {
        ErrorResponse response = ErrorResponse.builder()
                .status(404)
                .message("Não encontrado")
                .build();

        assertNotNull(response);
        assertEquals(404, response.getStatus());
        assertEquals("Não encontrado", response.getMessage());
    }

    @Test
    @DisplayName("Deve validar Equals, HashCode e ToString para 100% de cobertura")
    void deveValidarEqualsHashCodeToString() {
        ErrorResponse r1 = ErrorResponse.builder().status(500).message("Erro").build();
        ErrorResponse r2 = ErrorResponse.builder().status(500).message("Erro").build();
        ErrorResponse r3 = ErrorResponse.builder().status(403).message("Negado").build();
        assertEquals(r1, r2);
        assertNotEquals(r1, r3);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(null, r1);
        assertNotEquals("Objeto Diferente", r1);
        assertNotNull(r1.toString());
        assertTrue(r1.toString().contains("status=500"));
    }
}
