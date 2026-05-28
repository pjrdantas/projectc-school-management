package br.com.escola.shared.endereco;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import br.com.escola.shared.viacep.CepInvalidoException;
import br.com.escola.shared.viacep.CepNaoEncontradoException;
import br.com.escola.shared.viacep.ViaCepIndisponivelException;
import br.com.escola.shared.viacep.ViaCepResponse;
import br.com.escola.shared.viacep.ViaCepService;

@SpringBootTest
@AutoConfigureMockMvc
class EnderecoCepControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
	@MockBean
    private ViaCepService viaCepService;

    @Test
    @WithMockUser
    void deveConsultarCepRemovendoCaracteresEspeciais() throws Exception {
        when(viaCepService.consultar(eq("01001-000")))
                .thenReturn(new ViaCepResponse(
                        "01001-000",
                        "Praça da Sé",
                        "",
                        "Sé",
                        "São Paulo",
                        "SP",
                        false));
        when(viaCepService.normalizar(eq("01001-000"))).thenReturn("01001000");

        mockMvc.perform(get("/enderecos/cep/{cep}", "01001-000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01001000"))
                .andExpect(jsonPath("$.logradouro").value("Praça da Sé"))
                .andExpect(jsonPath("$.bairro").value("Sé"))
                .andExpect(jsonPath("$.cidade").value("São Paulo"))
                .andExpect(jsonPath("$.uf").value("SP"))
                .andExpect(jsonPath("$.complemento").value(""));
    }

    @Test
    @WithMockUser
    void deveRetornarBadRequestQuandoCepNaoPossuirOitoDigitos() throws Exception {
        when(viaCepService.consultar(eq("123"))).thenThrow(new CepInvalidoException());

        mockMvc.perform(get("/enderecos/cep/{cep}", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CEP deve possuir 8 dígitos"));
    }

    @Test
    @WithMockUser
    void deveRetornarNotFoundQuandoCepNaoExistir() throws Exception {
        when(viaCepService.consultar(eq("99999999"))).thenThrow(new CepNaoEncontradoException("99999999"));

        mockMvc.perform(get("/enderecos/cep/{cep}", "99999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("CEP não encontrado: 99999999"));
    }

    @Test
    @WithMockUser
    void deveRetornarServiceUnavailableQuandoViaCepEstiverForaDoAr() throws Exception {
        when(viaCepService.consultar(eq("01001000"))).thenThrow(new ViaCepIndisponivelException());

        mockMvc.perform(get("/enderecos/cep/{cep}", "01001000"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Serviço ViaCEP indisponível no momento. Tente novamente mais tarde."));
    }
}
