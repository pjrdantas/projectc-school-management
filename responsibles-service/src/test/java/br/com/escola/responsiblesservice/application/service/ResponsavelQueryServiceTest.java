package br.com.escola.responsiblesservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.responsiblesservice.application.context.InternalRequestContext;
import br.com.escola.responsiblesservice.application.dto.ResponsavelAlunoVinculadoReadModelResponse;
import br.com.escola.responsiblesservice.application.dto.ResponsavelReadModelResponse;
import br.com.escola.responsiblesservice.application.port.out.ResponsavelLocalReadPort;
import br.com.escola.responsiblesservice.application.port.out.ResponsavelReadPort;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelProperties;

class ResponsavelQueryServiceTest {

    @Test
    void devePreferirListagemLocalQuandoReadModelEstaHabilitado() {
        AtomicInteger monolithCalls = new AtomicInteger();
        ResponsavelQueryService service = new ResponsavelQueryService(
                provider(new FakeLocalReadPort(
                        Optional.of(List.of(responsavel("Maria Local"))),
                        Optional.empty(),
                        Optional.empty(),
                        false)),
                new FakeMonolithReadPort(monolithCalls),
                new ResponsiblesReadModelProperties(true, false, true, false, 500, false, true),
                new ObjectMapper().findAndRegisterModules());

        var response = service.listarResponsaveis("Bearer token", context(), "Maria", null);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getBody()).contains("Maria Local");
        assertThat(monolithCalls).hasValue(0);
    }

    @Test
    void deveFazerFallbackParaOMonolitoQuandoDetalheLocalNaoExiste() {
        AtomicInteger monolithCalls = new AtomicInteger();
        UUID responsavelId = UUID.fromString("00000000-0000-0000-0000-000000000601");
        ResponsavelQueryService service = new ResponsavelQueryService(
                provider(new FakeLocalReadPort(Optional.empty(), Optional.empty(), Optional.empty(), false)),
                new FakeMonolithReadPort(monolithCalls),
                new ResponsiblesReadModelProperties(true, false, true, false, 500, false, true),
                new ObjectMapper().findAndRegisterModules());

        var response = service.buscarResponsavelPorId("Bearer token", context(), responsavelId);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("Monolito");
        assertThat(monolithCalls).hasValue(1);
    }

    @Test
    void devePreferirLeituraLocalPorAlunoQuandoVinculoExisteNoReadModel() {
        AtomicInteger monolithCalls = new AtomicInteger();
        ResponsavelQueryService service = new ResponsavelQueryService(
                provider(new FakeLocalReadPort(
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(List.of(responsavelVinculado("Mae Local"))),
                        false)),
                new FakeMonolithReadPort(monolithCalls),
                new ResponsiblesReadModelProperties(true, false, true, false, 500, false, true),
                new ObjectMapper().findAndRegisterModules());

        var response = service.listarResponsaveisPorAluno("Bearer token", context(),
                UUID.fromString("00000000-0000-0000-0000-000000000401"));

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("Mae Local");
        assertThat(monolithCalls).hasValue(0);
    }

    @Test
    void deveFazerFallbackParaOMonolitoQuandoVinculoLocalNaoExiste() {
        AtomicInteger monolithCalls = new AtomicInteger();
        ResponsavelQueryService service = new ResponsavelQueryService(
                provider(new FakeLocalReadPort(Optional.empty(), Optional.empty(), Optional.empty(), false)),
                new FakeMonolithReadPort(monolithCalls),
                new ResponsiblesReadModelProperties(true, false, true, false, 500, false, true),
                new ObjectMapper().findAndRegisterModules());

        var response = service.listarResponsaveisPorAluno("Bearer token", context(),
                UUID.fromString("00000000-0000-0000-0000-000000000401"));

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).contains("Monolito");
        assertThat(monolithCalls).hasValue(1);
    }

    private ObjectProvider<ResponsavelLocalReadPort> provider(ResponsavelLocalReadPort port) {
        return new ObjectProvider<>() {
            @Override
            public ResponsavelLocalReadPort getObject(Object... args) {
                return port;
            }

            @Override
            public ResponsavelLocalReadPort getIfAvailable() {
                return port;
            }

            @Override
            public ResponsavelLocalReadPort getIfUnique() {
                return port;
            }

            @Override
            public ResponsavelLocalReadPort getObject() {
                return port;
            }
        };
    }

    private InternalRequestContext context() {
        return new InternalRequestContext(
                "corr-id",
                UUID.fromString("00000000-0000-0000-0000-000000000101"),
                UUID.fromString("00000000-0000-0000-0000-000000000047"));
    }

    private ResponsavelReadModelResponse responsavel(String nome) {
        return new ResponsavelReadModelResponse(
                UUID.fromString("00000000-0000-0000-0000-000000000601"),
                nome,
                "98765432100",
                "maria@example.com",
                "11988887777",
                "1234567",
                "01001000",
                "Rua Central",
                "100",
                "Casa",
                "Centro",
                "Sao Paulo",
                "SP",
                UUID.fromString("00000000-0000-0000-0000-000000000047"),
                "Escola padrao",
                LocalDateTime.of(2026, 7, 16, 10, 0));
    }

    private ResponsavelAlunoVinculadoReadModelResponse responsavelVinculado(String nome) {
        return new ResponsavelAlunoVinculadoReadModelResponse(
                UUID.fromString("00000000-0000-0000-0000-000000000601"),
                nome,
                "98765432100",
                "maria@example.com",
                "11988887777",
                "1234567",
                "01001000",
                "Rua Central",
                "100",
                "Casa",
                "Centro",
                "Sao Paulo",
                "SP",
                "MAE",
                true,
                false,
                true,
                LocalDateTime.of(2026, 7, 16, 10, 0));
    }

    private static class FakeLocalReadPort implements ResponsavelLocalReadPort {
        private final Optional<List<ResponsavelReadModelResponse>> listResponse;
        private final Optional<ResponsavelReadModelResponse> detailResponse;
        private final Optional<List<ResponsavelAlunoVinculadoReadModelResponse>> studentLinkResponse;
        private final boolean fail;

        private FakeLocalReadPort(
                Optional<List<ResponsavelReadModelResponse>> listResponse,
                Optional<ResponsavelReadModelResponse> detailResponse,
                Optional<List<ResponsavelAlunoVinculadoReadModelResponse>> studentLinkResponse,
                boolean fail) {
            this.listResponse = listResponse;
            this.detailResponse = detailResponse;
            this.studentLinkResponse = studentLinkResponse;
            this.fail = fail;
        }

        @Override
        public Optional<List<ResponsavelReadModelResponse>> listarResponsaveis(UUID escolaId, String nome, String cpf) {
            if (fail) {
                throw new IllegalStateException("forced-local-error");
            }
            return listResponse;
        }

        @Override
        public Optional<ResponsavelReadModelResponse> buscarResponsavelPorId(UUID responsavelId, UUID escolaId) {
            if (fail) {
                throw new IllegalStateException("forced-local-error");
            }
            return detailResponse;
        }

        @Override
        public Optional<List<ResponsavelAlunoVinculadoReadModelResponse>> listarResponsaveisPorAluno(
                UUID alunoId,
                UUID escolaId) {
            if (fail) {
                throw new IllegalStateException("forced-local-error");
            }
            return studentLinkResponse;
        }
    }

    private static class FakeMonolithReadPort implements ResponsavelReadPort {
        private final AtomicInteger calls;

        private FakeMonolithReadPort(AtomicInteger calls) {
            this.calls = calls;
        }

        @Override
        public ResponseEntity<String> listarResponsaveis(
                String authorization,
                InternalRequestContext context,
                String nome,
                String cpf) {
            calls.incrementAndGet();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("[{\"nomeCompleto\":\"Monolito\"}]");
        }

        @Override
        public ResponseEntity<String> buscarResponsavelPorId(
                String authorization,
                InternalRequestContext context,
                UUID responsavelId) {
            calls.incrementAndGet();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"id\":\"" + responsavelId + "\",\"nomeCompleto\":\"Monolito\"}");
        }

        @Override
        public ResponseEntity<String> listarResponsaveisPorAluno(
                String authorization,
                InternalRequestContext context,
                UUID alunoId) {
            calls.incrementAndGet();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("[{\"id\":\"" + UUID.fromString("00000000-0000-0000-0000-000000000601")
                            + "\",\"nomeCompleto\":\"Monolito\",\"parentesco\":\"MAE\"}]");
        }
    }
}
