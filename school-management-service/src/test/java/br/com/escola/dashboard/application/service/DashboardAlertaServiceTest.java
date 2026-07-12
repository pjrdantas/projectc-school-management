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

import br.com.escola.dashboard.adapter.in.web.dto.DashboardAlertaResponse;
import br.com.escola.dashboard.application.dto.internal.DashboardAcademicoResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardDiretorResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardProfessorResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardSecretariaResumo;
import br.com.escola.dashboard.application.port.internal.DashboardAcademicoPort;
import br.com.escola.dashboard.application.port.internal.DashboardDiretorPort;
import br.com.escola.dashboard.application.port.internal.DashboardProfessorPort;
import br.com.escola.dashboard.application.port.internal.DashboardSecretariaPort;

@ExtendWith(MockitoExtension.class)
class DashboardAlertaServiceTest {

    @Mock
    private DashboardAcademicoPort dashboardAcademicoPort;

    @Mock
    private DashboardSecretariaPort dashboardSecretariaPort;

    @Mock
    private DashboardDiretorPort dashboardDiretorPort;

    @Mock
    private DashboardProfessorPort dashboardProfessorPort;

    @Test
    void deveRetornarAlertasDoDiretorOrdenadosPorSeveridade() {
        when(dashboardDiretorPort.consultarResumo()).thenReturn(new DashboardDiretorResumo(
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
        when(dashboardProfessorPort.consultarResumo(professorId)).thenReturn(new DashboardProfessorResumo(
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
        when(dashboardSecretariaPort.consultarResumo()).thenReturn(new DashboardSecretariaResumo(
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
        when(dashboardAcademicoPort.consultarResumo()).thenReturn(new DashboardAcademicoResumo(
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
                dashboardAcademicoPort,
                dashboardSecretariaPort,
                dashboardDiretorPort,
                dashboardProfessorPort,
                limitePadrao);
    }
}
