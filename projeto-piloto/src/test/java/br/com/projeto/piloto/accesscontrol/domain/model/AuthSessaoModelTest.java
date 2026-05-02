package br.com.projeto.piloto.accesscontrol.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthSessaoModelTest {

    @Test
    @DisplayName("Builder, getters e isAtiva() com valores padrão e mutações")
    void builderAndGettersAndIsAtiva() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime futuro = agora.plusDays(1);

        AuthSessaoModel model = AuthSessaoModel.builder()
                .id(123L)
                .authUsuarioId(321L)
                .token("tkn")
                .refreshToken("rftk")
                .dataCriacao(agora)
                .dataExpiracao(futuro)
                .ativo("S")
                .build();

        assertNotNull(model);
        assertEquals(123L, model.getId());
        assertEquals(321L, model.getAuthUsuarioId());
        assertEquals("tkn", model.getToken());
        assertEquals("rftk", model.getRefreshToken());
        assertEquals(agora, model.getDataCriacao());
        assertEquals(futuro, model.getDataExpiracao());
        assertTrue(model.isAtiva());

        model.setAtivo("s");
        assertTrue(model.isAtiva());

        model.setAtivo("N");
        assertFalse(model.isAtiva());

        model.setAtivo(null);
        assertFalse(model.isAtiva());
    }

    @Test
    @DisplayName("isExpirada() retorna true quando dataExpiracao é passada")
    void isExpiradaTrueWhenPast() {
        AuthSessaoModel model = new AuthSessaoModel();
        model.setDataExpiracao(LocalDateTime.now().minusDays(1));

        assertTrue(model.isExpirada());
    }

    @Test
    @DisplayName("isExpirada() retorna false quando dataExpiracao é futura")
    void isExpiradaFalseWhenFuture() {
        AuthSessaoModel model = new AuthSessaoModel();
        model.setDataExpiracao(LocalDateTime.now().plusDays(1));

        assertFalse(model.isExpirada());
    }
}
