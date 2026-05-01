package br.com.projeto.piloto.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.projeto.piloto.adapter.in.web.dto.AplicativosRequestDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class AplicativosRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve validar DTO com todos os campos corretos")
    void deveValidarDtoCorreto() {
        AplicativosRequestDTO dto = new AplicativosRequestDTO(
                "App Teste", "Descricao", "http://url.com", "Modulo", "Exposed", "/rota", "S"
        );

        var violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Não deveria haver violações de validação");
        assertEquals("App Teste", dto.nome());
        assertEquals("/rota", dto.routePath());
    }

    @Test
    @DisplayName("Deve capturar erros de validação quando campos obrigatórios estão vazios")
    void deveFalharQuandoCamposVazios() {
        AplicativosRequestDTO dto = new AplicativosRequestDTO(
                "", "", "", "", "", null, null
        );

        var violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(5, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("O campo 'nome'")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("O campo 'url'")));
    }

    @Test
    @DisplayName("Deve permitir campos opcionais como nulos")
    void devePermitirOpcionaisNulos() {
        AplicativosRequestDTO dto = new AplicativosRequestDTO(
                "Nome", "Desc", "URL", "Mod", "Exp", null, null
        );

        var violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Campos opcionais não devem gerar erros");
    }
}
