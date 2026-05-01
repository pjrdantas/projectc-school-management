package br.com.projeto.piloto.adapter.out.jpa.entity;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AplicativoTest {

    @Test
    @DisplayName("Deve validar Getters, Setters e Builder do Lombok")
    void deveTestarLombok() {

        LocalDateTime agora = LocalDateTime.now();
        Aplicativo app = Aplicativo.builder()
                .id(1L)
                .nmAplicativo("Portal")
                .dsAplicativo("Desc")
                .dsUrl("http://url")
                .nmModulo("Modulo")
                .moduloExposto("Exposto")
                .dsRota("/rota")
                .flAtivo("S")
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build();

        assertAll("Atributos da Entity",
            () -> assertEquals(1L, app.getId()),
            () -> assertEquals("Portal", app.getNmAplicativo()),
            () -> assertEquals("Desc", app.getDsAplicativo()),
            () -> assertEquals("http://url", app.getDsUrl()),
            () -> assertEquals("Modulo", app.getNmModulo()),
            () -> assertEquals("Exposto", app.getModuloExposto()),
            () -> assertEquals("/rota", app.getDsRota()),
            () -> assertEquals("S", app.getFlAtivo()),
            () -> assertEquals(agora, app.getCriadoEm()),
            () -> assertEquals(agora, app.getAtualizadoEm())

        );
        Aplicativo appSet = new Aplicativo();
        appSet.setId(2L);
        appSet.setNmAplicativo("Novo");
        assertEquals(2L, appSet.getId());
        assertEquals("Novo", appSet.getNmAplicativo());
    }

    @Test
    @DisplayName("Deve validar o funcionamento do @PrePersist")
    void deveTestarPrePersist() {
        Aplicativo app = new Aplicativo();
        app.prePersist();

        assertNotNull(app.getCriadoEm());
        assertNotNull(app.getAtualizadoEm());
        assertEquals(app.getCriadoEm(), app.getAtualizadoEm());
    }

    @Test
    @DisplayName("Deve validar o funcionamento do @PreUpdate")
    void deveTestarPreUpdate() {
        Aplicativo app = new Aplicativo();
        LocalDateTime antigo = LocalDateTime.now().minusDays(1);
        app.setAtualizadoEm(antigo);

        app.preUpdate();

        assertNotEquals(antigo, app.getAtualizadoEm());
        assertTrue(app.getAtualizadoEm().isAfter(antigo));
    }

    @Test
    @DisplayName("Deve validar NoArgsConstructor e AllArgsConstructor")
    void deveTestarConstrutores() {
        Aplicativo appEmpty = new Aplicativo();
        assertNotNull(appEmpty);

        Aplicativo appFull = new Aplicativo(1L, "Desc", "URL", "Mod", "Exp", "Rota", "S", null, null, null);
        assertEquals(1L, appFull.getId());
    }
}
