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

import br.com.projeto.piloto.adapter.in.web.dto.RegisterRequestDTO;

class RegisterRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve validar RegisterRequestDTO com sucesso")
    void deveValidarDtoSucesso() {
        RegisterRequestDTO dto = new RegisterRequestDTO("Admin", "admin", "123456");

        var violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("Admin", dto.nome());
        assertEquals("admin", dto.login());
        assertEquals("123456", dto.senha());
    }

    @Test
    @DisplayName("Deve capturar erros de validação quando campos obrigatórios forem brancos")
    void deveFalharQuandoCamposVazios() {
        RegisterRequestDTO dto = new RegisterRequestDTO("", "  ", null);

        var violations = validator.validate(dto);
        assertEquals(3, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Nome é obrigatório")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Login é obrigatório")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Senha é obrigatória")));
    }

    @Test
    @DisplayName("Cobre métodos implícitos do Record (equals, hashCode e toString)")
    void deveValidarMetodosRecord() {
        RegisterRequestDTO dto1 = new RegisterRequestDTO("n", "l", "s");
        RegisterRequestDTO dto2 = new RegisterRequestDTO("n", "l", "s");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());
        assertTrue(dto1.toString().contains("login=l"));
    }
}
