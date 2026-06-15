package br.com.escola.dashboard.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardAcademicoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardAlertaResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardDiretorResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardProfessorResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardSecretariaResponse;

@ExtendWith(MockitoExtension.class)
class DashboardAlertaServiceTest {

    @Mock
    private DashboardAcademicoService dashboardAcademicoService;

    @Mock
    private DashboardSecretariaService dashboardSecretariaService;

    @Mock
    private DashboardDiretorService dashboardDiretorService;

    @Mock
    private DashboardProfessorService dashboardProfessorService;

    @Test
    void deveRetornarAlertasDoDiretorOrdenadosPorSeveridade() {
        when(dashboardDiretorService.consultar()).thenReturn(new DashboardDiretorResponse(
                UUID.randomUUID(),
                "Escola teste",
                10,
                2,
                3,
                4,
                1,
                20,
                1,
                5,
                1,
                4,
                12,
                6,
                2,
                3,
                2,
                1,
                0,
                1,
                List.of(),
                List.of()));

        List<DashboardAlertaResponse> alertas = service(0).consultar("diretor", null);

        assertThat(alertas).extracting(DashboardAlertaResponse::codigo)
                .containsExactly(
                        "MATRICULAS_COM_DOCUMENTOS_PENDENTES",
                        "TURMAS_LOTADAS",
                        "AVALIACOES_COM_NOTAS_PENDENTES",
                        "MATRICULAS_PENDENTES");
        assertThat(alertas.getFirst().severidade()).isEqualTo("CRITICO");
    }

    @Test
    void deveExigirProfessorIdParaAlertasDoProfessor() {
        assertThatThrownBy(() -> service(0).consultar("professor", null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("professorId é obrigatório");
    }

    @Test
    void deveRetornarAlertasDoProfessorComProfessorId() {
        UUID professorId = UUID.randomUUID();
        when(dashboardProfessorService.consultar(professorId)).thenReturn(new DashboardProfessorResponse(
                UUID.randomUUID(),
                "Escola teste",
                professorId,
                1,
                1,
                2,
                1,
                1,
                1,
                1,
                1,
                1,
                List.of()));

        List<DashboardAlertaResponse> alertas = service(0).consultar("professor", professorId);

        assertThat(alertas).hasSize(3);
        assertThat(alertas).allMatch(alerta -> professorId.equals(alerta.professorId()));
        assertThat(alertas.getFirst().codigo()).isEqualTo("FREQUENCIAS_PENDENTES");
        assertThat(alertas.getFirst().severidade()).isEqualTo("CRITICO");
    }

    @Test
    void deveRespeitarLimitePadrao() {
        when(dashboardSecretariaService.consultar()).thenReturn(new DashboardSecretariaResponse(
                UUID.randomUUID(),
                "Escola teste",
                10,
                1,
                1,
                1,
                1,
                1,
                0,
                0,
                0,
                0,
                0,
                List.of(),
                List.of()));

        List<DashboardAlertaResponse> alertas = service(1).consultar("secretaria", null);

        assertThat(alertas).isEmpty();
    }

    @Test
    void deveRetornarAlertaAcademicoQuandoExistirReprovacao() {
        when(dashboardAcademicoService.consultar()).thenReturn(new DashboardAcademicoResponse(
                UUID.randomUUID(),
                "Escola teste",
                10,
                0,
                5,
                5,
                1,
                1,
                1,
                3,
                1,
                List.of(),
                List.of()));

        List<DashboardAlertaResponse> alertas = service(0).consultar("academico", null);

        assertThat(alertas).hasSize(1);
        assertThat(alertas.getFirst().codigo()).isEqualTo("ALUNOS_REPROVADOS");
        assertThat(alertas.getFirst().severidade()).isEqualTo("CRITICO");
    }

    private DashboardAlertaService service(long limitePadrao) {
        return new DashboardAlertaService(
                dashboardAcademicoService,
                dashboardSecretariaService,
                dashboardDiretorService,
                dashboardProfessorService,
                limitePadrao);
    }
}
