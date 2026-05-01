package br.com.projeto.piloto.api.dto;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.projeto.piloto.adapter.in.web.dto.AuthPerfilResumoDTO;
import br.com.projeto.piloto.adapter.in.web.dto.AuthUsuarioResponseDTO;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Set;

class AuthUsuarioResponseDTOTest {

    @Test
    @DisplayName("Deve validar a integridade dos dados e o aninhamento de perfis no Record")
    void deveValidarDadosEAccessors() {
        Long id = 1L;
        String login = "user.teste";
        String nome = "Usuário Teste";
        String ativo = "S";
        String email = "teste@projeto.com.br";
        LocalDateTime agora = LocalDateTime.now();
        AuthPerfilResumoDTO perfil = new AuthPerfilResumoDTO(10L, "ADMIN");
        Set<AuthPerfilResumoDTO> perfis = Set.of(perfil);
        AuthUsuarioResponseDTO dto = new AuthUsuarioResponseDTO(
            id, login, nome, ativo, email, agora, agora, perfis
        );
        assertAll("Campos do AuthUsuarioResponseDTO",
            () -> assertEquals(id, dto.id()),
            () -> assertEquals(login, dto.login()),
            () -> assertEquals(nome, dto.nome()),
            () -> assertEquals(ativo, dto.ativo()),
            () -> assertEquals(email, dto.email()),
            () -> assertEquals(agora, dto.criadoEm()),
            () -> assertEquals(agora, dto.atualizadoEm()),
            () -> assertEquals(perfis, dto.perfisIds()),
            () -> assertTrue(dto.perfisIds().contains(perfil))
        );
    }

    @Test
    @DisplayName("Deve cobrir métodos gerados automaticamente para 100% de cobertura no Jacoco")
    void deveValidarMetodosPadraoDoRecord() {
        LocalDateTime data = LocalDateTime.of(2023, 10, 1, 10, 0);
        AuthUsuarioResponseDTO dto1 = new AuthUsuarioResponseDTO(1L, "L", "N", "S", "E", data, data, Set.of());
        AuthUsuarioResponseDTO dto2 = new AuthUsuarioResponseDTO(1L, "L", "N", "S", "E", data, data, Set.of());
        AuthUsuarioResponseDTO dto3 = new AuthUsuarioResponseDTO(2L, "X", "N", "S", "E", data, data, Set.of());
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());
        assertTrue(dto1.toString().contains("login=L"));
    }
}
