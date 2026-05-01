package br.com.projeto.piloto.api.dto;


import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.projeto.piloto.adapter.in.web.dto.RefreshTokenRequestDTO;

class RefreshTokenRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve validar RefreshTokenRequestDTO com sucesso")
    void deveValidarDtoSucesso() {
        String token = "meu-refresh-token-123";
        RefreshTokenRequestDTO dto = new RefreshTokenRequestDTO(token);

        var violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals(token, dto.refreshToken());
    }

    @Test
    @DisplayName("Deve capturar erro quando refreshToken for branco ou nulo")
    void deveFalharQuandoVazio() {
        RefreshTokenRequestDTO dto = new RefreshTokenRequestDTO("");

        var violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("refreshToken é obrigatório")));
    }

    @Test
    @DisplayName("Cobre métodos implícitos do Record (equals, hashCode e toString)")
    void deveValidarMetodosRecord() {
        RefreshTokenRequestDTO dto1 = new RefreshTokenRequestDTO("tokenA");
        RefreshTokenRequestDTO dto2 = new RefreshTokenRequestDTO("tokenA");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());
        assertTrue(dto1.toString().contains("refreshToken=tokenA"));
    }
}
