package br.com.projeto.piloto.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.projeto.piloto.accesscontrol.adapter.in.web.dto.AuthPerfilRequestDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class AuthPerfilRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve validar DTO com sucesso")
    void deveValidarDtoSucesso() {
        Set<Long> ids = Set.of(1L, 2L);

        AuthPerfilRequestDTO dto = new AuthPerfilRequestDTO("ADMIN", ids);

        var violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
        assertEquals("ADMIN", dto.nmPerfil());
        assertEquals(ids, dto.permissoesIds());
    }

    @Test
    @DisplayName("Deve falhar quando campos obrigatórios são nulos ou vazios")
    void deveFalharCamposObrigatorios() {

        AuthPerfilRequestDTO dto = new AuthPerfilRequestDTO("", null);

        var violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
    }

    @Test
    @DisplayName("Cobre métodos implícitos do Record (equals, hashCode e toString)")
    void deveCobrirMetodosImplicitos() {
        Set<Long> ids = Set.of(1L);
        AuthPerfilRequestDTO dto1 = new AuthPerfilRequestDTO("USER", ids);
        AuthPerfilRequestDTO dto2 = new AuthPerfilRequestDTO("USER", ids);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());
    }
}
