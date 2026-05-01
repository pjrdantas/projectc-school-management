package br.com.projeto.piloto.api.dto;



import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.projeto.piloto.adapter.in.web.dto.LoginRequestDTO;

class LoginRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve validar LoginRequestDTO com sucesso")
    void deveValidarDtoSucesso() {
        LoginRequestDTO dto = new LoginRequestDTO("admin", "123456");

        var violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("admin", dto.login());
        assertEquals("123456", dto.senha());
    }

    @Test
    @DisplayName("Deve capturar erros de validação quando login ou senha forem brancos")
    void deveFalharQuandoCamposVazios() {
        LoginRequestDTO dto = new LoginRequestDTO("", "  ");

        var violations = validator.validate(dto);

        assertEquals(2, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Login é obrigatório")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Senha é obrigatória")));
    }

    @Test
    @DisplayName("Cobre métodos implícitos do Record (equals, hashCode e toString)")
    void deveValidarMetodosRecord() {
        LoginRequestDTO dto1 = new LoginRequestDTO("user", "pass");
        LoginRequestDTO dto2 = new LoginRequestDTO("user", "pass");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());
        assertTrue(dto1.toString().contains("login=user"));
    }
}
