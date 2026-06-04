package br.com.escola.dashboard.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardAlertaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void deveConsultarAlertasDoDiretor() throws Exception {
        mockMvc.perform(get("/api/dashboard/alertas")
                        .param("publicoCodigo", "diretor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    void deveRejeitarAlertasDoProfessorSemProfessorId() throws Exception {
        mockMvc.perform(get("/api/dashboard/alertas")
                        .param("publicoCodigo", "professor"))
                .andExpect(status().isBadRequest());
    }
}
