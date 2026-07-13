package br.com.escola.bff.interfaces.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.bff.application.context.TrustedHeaders;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class BearerAuthenticationWebFilterTest {

    private final BearerAuthenticationWebFilter filter =
            new BearerAuthenticationWebFilter(new ObjectMapper().findAndRegisterModules());

    @Test
    void deveRejeitarRotaProtegidaSemBearerToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/turmas/00000000-0000-0000-0000-000000000001/disciplinas")
                        .header(TrustedHeaders.CORRELATION_ID, "corr-1"));

        StepVerifier.create(filter.filter(exchange, ignored -> Mono.empty())).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("UNAUTHORIZED", "corr-1");
    }

    @Test
    void deveRemoverContextoForjadoAntesDeContinuar() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/disciplinas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer opaque-token")
                        .header("X-Usuario-Id", "forged-user")
                        .header("X-Escola-Id", "forged-school"));
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

        StepVerifier.create(filter.filter(exchange, sanitized -> {
            forwarded.set(sanitized);
            return Mono.empty();
        })).verifyComplete();

        assertThat(forwarded.get().getRequest().getHeaders().getFirst("X-Usuario-Id")).isNull();
        assertThat(forwarded.get().getRequest().getHeaders().getFirst("X-Escola-Id")).isNull();
        assertThat(forwarded.get().getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer opaque-token");
    }

    @Test
    void deveProtegerPostDePeriodoLetivoSemBearerToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/periodos-letivos")
                        .header(TrustedHeaders.CORRELATION_ID, "corr-write"));

        StepVerifier.create(filter.filter(exchange, ignored -> Mono.empty())).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("UNAUTHORIZED", "corr-write");
    }

    @Test
    void deveProtegerPostDeDisciplinaSemBearerToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/disciplinas")
                        .header(TrustedHeaders.CORRELATION_ID, "corr-disc-write"));

        StepVerifier.create(filter.filter(exchange, ignored -> Mono.empty())).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("UNAUTHORIZED", "corr-disc-write");
    }

    @Test
    void deveProtegerPostDeSerieSemBearerToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/series")
                        .header(TrustedHeaders.CORRELATION_ID, "corr-serie-write"));

        StepVerifier.create(filter.filter(exchange, ignored -> Mono.empty())).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("UNAUTHORIZED", "corr-serie-write");
    }

    @Test
    void deveProtegerPostDeTurmaSemBearerToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/turmas")
                        .header(TrustedHeaders.CORRELATION_ID, "corr-turma-write"));

        StepVerifier.create(filter.filter(exchange, ignored -> Mono.empty())).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("UNAUTHORIZED", "corr-turma-write");
    }

    @Test
    void deveProtegerPostDeTurmaDisciplinaSemBearerToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/turmas/00000000-0000-0000-0000-000000000071/disciplinas")
                        .header(TrustedHeaders.CORRELATION_ID, "corr-turma-disc-write"));

        StepVerifier.create(filter.filter(exchange, ignored -> Mono.empty())).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("UNAUTHORIZED", "corr-turma-disc-write");
    }

    @Test
    void deveProtegerConsultaCadastralSemBearerToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/consulta-cadastral")
                        .header(TrustedHeaders.CORRELATION_ID, "corr-consulta"));

        StepVerifier.create(filter.filter(exchange, ignored -> Mono.empty())).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("UNAUTHORIZED", "corr-consulta");
    }

    @Test
    void deveProtegerNovasRotasOficiaisDePeopleSemBearerToken() {
        String[] paths = {
                "/api/pessoas/catalogos/status-aluno",
                "/api/pessoas/catalogos/parentescos",
                "/api/pessoas/00000000-0000-0000-0000-000000000401",
                "/api/pessoas/00000000-0000-0000-0000-000000000401/endereco-principal",
                "/api/pessoas/00000000-0000-0000-0000-000000000401/enderecos",
                "/api/pessoas/00000000-0000-0000-0000-000000000401/contato",
                "/api/pessoas/00000000-0000-0000-0000-000000000401/documentos",
                "/api/documentos",
                "/api/documentos/00000000-0000-0000-0000-000000000501",
                "/api/documentos-alunos/alunos/00000000-0000-0000-0000-000000000601",
                "/api/matriculas"
        };

        for (String path : paths) {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.get(path)
                            .header(TrustedHeaders.CORRELATION_ID, "corr-people"));

            StepVerifier.create(filter.filter(exchange, ignored -> Mono.empty())).verifyComplete();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(exchange.getResponse().getBodyAsString().block()).contains("UNAUTHORIZED", "corr-people");
        }
    }
}
