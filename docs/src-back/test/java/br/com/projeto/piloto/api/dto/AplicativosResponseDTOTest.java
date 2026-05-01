package br.com.projeto.piloto.api.dto;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.projeto.piloto.adapter.in.web.dto.AplicativosResponseDTO;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

class AplicativosResponseDTOTest {

    @Test
    @DisplayName("Deve garantir a integridade dos dados e cobertura total do Record")
    void deveValidarRecord() {
        Long id = 1L;
        String nome = "App Piloto";
        String desc = "Descrição do sistema";
        String url = "http://localhost:4200";
        String modulo = "AuthModule";
        String exp = "Exposed";
        String rota = "/home";
        String ativo = "S";
        LocalDateTime agora = LocalDateTime.now();
        AplicativosResponseDTO dto = new AplicativosResponseDTO(
            id, nome, desc, url, modulo, exp, rota, ativo, agora, agora
        );
        assertAll("Verificação de campos do DTO",
            () -> assertEquals(id, dto.id()),
            () -> assertEquals(nome, dto.nome()),
            () -> assertEquals(desc, dto.descricao()),
            () -> assertEquals(url, dto.url()),
            () -> assertEquals(modulo, dto.moduleName()),
            () -> assertEquals(exp, dto.exposedModule()),
            () -> assertEquals(rota, dto.routePath()),
            () -> assertEquals(ativo, dto.ativo()),
            () -> assertEquals(agora, dto.criadoEm()),
            () -> assertEquals(agora, dto.atualizadoEm())
        );
    }

    @Test
    @DisplayName("Deve validar equals, hashCode e toString para cobertura completa")
    void deveValidarMetodosPadrao() {
        LocalDateTime agora = LocalDateTime.now();
        AplicativosResponseDTO dto1 = new AplicativosResponseDTO(1L, "A", "D", "U", "M", "E", "R", "S", agora, agora);
        AplicativosResponseDTO dto2 = new AplicativosResponseDTO(1L, "A", "D", "U", "M", "E", "R", "S", agora, agora);
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotNull(dto1.toString());
        assertTrue(dto1.toString().contains("AplicativosResponseDTO"));
    }
}
