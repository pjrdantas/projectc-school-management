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

import br.com.projeto.piloto.adapter.in.web.dto.AuthPermissaoRequestDTO;

class AuthPermissaoRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve validar DTO com permissão válida")
    void deveValidarDtoCorreto() {
        AuthPermissaoRequestDTO dto = new AuthPermissaoRequestDTO("ROLE_USER");

        var violations = validator.validate(dto);
        
        assertTrue(violations.isEmpty());
        assertEquals("ROLE_USER", dto.nmPermissao());
    }

    @Test
    @DisplayName("Deve capturar erro quando nmPermissao for vazio ou nulo")
    void deveFalharQuandoVazio() {
        AuthPermissaoRequestDTO dto = new AuthPermissaoRequestDTO("");

        var violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("nmPermissao")));
    }

    @Test
    @DisplayName("Deve cobrir métodos implícitos do Record para 100% de cobertura")
    void deveValidarMetodosRecord() {
        AuthPermissaoRequestDTO dto1 = new AuthPermissaoRequestDTO("ADMIN");
        AuthPermissaoRequestDTO dto2 = new AuthPermissaoRequestDTO("ADMIN");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());
    }
}
