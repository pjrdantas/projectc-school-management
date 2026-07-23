package br.com.escola.enrollmentdocumentservice.infra.webclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import br.com.escola.enrollmentdocumentservice.application.context.InternalHeaders;
import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;
import br.com.escola.enrollmentdocumentservice.infra.config.AlunoMatriculaClientProperties;
import br.com.escola.enrollmentdocumentservice.infra.config.CatalogoAcademicoMatriculaClientProperties;

class MatriculaDependenciasHttpAdapterTest {

    @Test
    void devePropagarContextoETratarAlunoAusenteComoInvalido() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://people-service");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        InternalRequestContext context = context();
        UUID alunoId = UUID.randomUUID();
        server.expect(requestTo("http://people-service/internal/v1/alunos/" + alunoId))
                .andExpect(header(InternalHeaders.INTERNAL_TOKEN, "people-token"))
                .andExpect(header(InternalHeaders.CORRELATION_ID, context.correlationId()))
                .andExpect(header(InternalHeaders.USUARIO_ID, context.usuarioId().toString()))
                .andExpect(header(InternalHeaders.ESCOLA_ID, context.escolaId().toString()))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        AlunoMatriculaHttpAdapter adapter = new AlunoMatriculaHttpAdapter(
                builder.build(),
                new AlunoMatriculaClientProperties(
                        URI.create("http://people-service"), "people-token", Duration.ofSeconds(1), Duration.ofSeconds(1)));

        assertThat(adapter.existeAtivoNaEscola(alunoId, context)).isFalse();
        server.verify();
    }

    @Test
    void deveBloquearPeriodoLetivoInativo() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://catalog-service");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        UUID periodoId = UUID.randomUUID();
        server.expect(requestTo("http://catalog-service/internal/v1/periodos-letivos/" + periodoId))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"ativo\":false}"));

        CatalogoAcademicoMatriculaHttpAdapter adapter = new CatalogoAcademicoMatriculaHttpAdapter(
                builder.build(),
                new CatalogoAcademicoMatriculaClientProperties(
                        URI.create("http://catalog-service"), "catalog-token", Duration.ofSeconds(1), Duration.ofSeconds(1)));

        assertThat(adapter.existePeriodoLetivoAtivoNaEscola(periodoId, context())).isFalse();
        server.verify();
    }

    private InternalRequestContext context() {
        return new InternalRequestContext(
                "corr-matricula",
                UUID.fromString("00000000-0000-0000-0000-000000000101"),
                UUID.fromString("00000000-0000-0000-0000-000000000047"));
    }
}
