package br.com.projeto.piloto.domain.model;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AplicativosModelTest {

    @Test
    @DisplayName("Deve validar Getters, Setters e Construtores")
    void deveValidarGettersSettersEConstrutores() {
        LocalDateTime agora = LocalDateTime.now();
        AplicativosModel model = new AplicativosModel();
        model.setId(1L);
        model.setNmAplicativo("App Teste");
        model.setDsAplicativo("Desc");
        model.setDsUrl("http://url");
        model.setNmModulo("Mod");
        model.setModuloExposto("Exp");
        model.setDsRota("/rota");
        model.setFlAtivo("S");
        model.setCriadoEm(agora);
        model.setAtualizadoEm(agora);
        assertEquals(1L, model.getId());
        assertEquals("App Teste", model.getNmAplicativo());
        assertEquals("Desc", model.getDsAplicativo());
        assertEquals("http://url", model.getDsUrl());
        assertEquals("Mod", model.getNmModulo());
        assertEquals("Exp", model.getModuloExposto());
        assertEquals("/rota", model.getDsRota());
        assertEquals("S", model.getFlAtivo());
        assertEquals(agora, model.getCriadoEm());
        assertEquals(agora, model.getAtualizadoEm());
    }

    @Test
    @DisplayName("Deve validar o Builder")
    void deveValidarBuilder() {
        AplicativosModel model = AplicativosModel.builder()
                .id(1L)
                .nmAplicativo("BuilderApp")
                .build();

        assertNotNull(model);
        assertEquals(1L, model.getId());
        assertEquals("BuilderApp", model.getNmAplicativo());
    }

    @Test
    @DisplayName("Deve validar Equals, HashCode e ToString para 100% de cobertura")
    void deveValidarEqualsHashCodeToString() {
        LocalDateTime data = LocalDateTime.now();
        
        AplicativosModel m1 = new AplicativosModel(1L, "A", "D", "U", "M", "E", "R", "S", data, data);
        AplicativosModel m2 = new AplicativosModel(1L, "A", "D", "U", "M", "E", "R", "S", data, data);
        AplicativosModel m3 = new AplicativosModel(2L, "B", "D", "U", "M", "E", "R", "S", data, data);
        assertEquals(m1, m2);
        assertNotEquals(m1, m3);
        assertEquals(m1.hashCode(), m2.hashCode());
        assertNotEquals(m1.hashCode(), m3.hashCode());
        assertNotEquals(null, m1);
        assertNotEquals("String", m1);
        assertNotNull(m1.toString());
        assertTrue(m1.toString().contains("nmAplicativo=A"));
    }
}
