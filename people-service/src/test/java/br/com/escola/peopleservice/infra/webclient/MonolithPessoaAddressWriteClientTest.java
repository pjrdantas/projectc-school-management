package br.com.escola.peopleservice.infra.webclient;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import br.com.escola.peopleservice.application.dto.PessoaEnderecoCleanupCommand;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoWriteCommand;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

class MonolithPessoaAddressWriteClientTest {

    private MockWebServer mockWebServer;
    private SimpleMeterRegistry meterRegistry;
    private MonolithPessoaAddressWriteClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        meterRegistry = new SimpleMeterRegistry();
        client = new MonolithPessoaAddressWriteClient(
                RestClient.builder().baseUrl(mockWebServer.url("/").toString()).build(),
                meterRegistry);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void deveAtualizarEnderecoPrincipalNoContratoInternoDoMonolito() throws Exception {
        UUID commandId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID enderecoId = UUID.randomUUID();
        UUID pessoaEnderecoId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "commandId": "address-write:%s",
                          "pessoaId": "%s",
                          "enderecoId": "%s",
                          "pessoaEnderecoId": "%s",
                          "status": "monolith_address_write_completed",
                          "selectedSource": "school_management_service",
                          "persistedLocally": false,
                          "fallbackRequired": false,
                          "warnings": ["monolith-remains-write-authority"]
                        }
                        """.formatted(commandId, pessoaId, enderecoId, pessoaEnderecoId)));

        var result = client.criarOuAtualizarEnderecoPrincipal(new PessoaEnderecoWriteCommand(
                commandId,
                pessoaId,
                escolaId,
                UUID.randomUUID(),
                "RESIDENCIAL",
                true,
                "01001000",
                "Praca da Se",
                "100",
                "Apto 10",
                "Se",
                "Sao Paulo",
                "SP",
                "address-write:" + commandId,
                usuarioId.toString()));

        assertThat(result.commandId()).isEqualTo(commandId);
        assertThat(result.pessoaId()).isEqualTo(pessoaId);
        assertThat(result.enderecoId()).isEqualTo(enderecoId);
        assertThat(result.pessoaEnderecoId()).isEqualTo(pessoaEnderecoId);
        assertThat(result.status()).isEqualTo("monolith_address_write_completed");
        assertThat(result.selectedSource()).isEqualTo("school_management_service");
        assertThat(result.persistedLocally()).isFalse();
        assertThat(result.fallbackRequired()).isFalse();
        assertThat(result.warnings()).contains(
                "monolith-remains-write-authority",
                "people-service-monolith-address-write-adapter-guarded",
                "local-address-persistence-disabled");

        RecordedRequest request = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("PUT");
        assertThat(request.getPath()).isEqualTo("/internal/pessoas/" + pessoaId + "/endereco-principal");
        assertThat(request.getHeader("X-Escola-Id")).isEqualTo(escolaId.toString());
        assertThat(request.getHeader("X-Usuario-Id")).isEqualTo(usuarioId.toString());
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo(commandId.toString());
        assertThat(request.getHeader("Idempotency-Key")).isEqualTo("address-write:" + commandId);
        assertThat(request.getBody().readUtf8()).contains(
                "\"tipoEnderecoCodigo\":\"RESIDENCIAL\"",
                "\"logradouro\":\"Praca da Se\"",
                "\"principal\":true");
        assertThat(meterRegistry.counter(
                "people.monolith.address.write.requests",
                "operation", "criarOuAtualizarEnderecoPrincipal",
                "result", "success",
                "selectedSource", "school_management_service").count()).isEqualTo(1.0d);
    }

    @Test
    void deveExecutarCleanupNoContratoInternoDoMonolito() throws Exception {
        UUID commandId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "commandId": "address-cleanup:%s",
                          "pessoaId": "%s",
                          "status": "monolith_address_cleanup_completed",
                          "selectedSource": "school_management_service",
                          "persistedLocally": false,
                          "fallbackRequired": false,
                          "warnings": ["monolith-remains-write-authority"]
                        }
                        """.formatted(commandId, pessoaId)));

        var result = client.removerEnderecosDaPessoa(new PessoaEnderecoCleanupCommand(
                commandId,
                pessoaId,
                escolaId,
                true,
                "address-cleanup:" + commandId,
                usuarioId.toString()));

        assertThat(result.status()).isEqualTo("monolith_address_cleanup_completed");
        assertThat(result.selectedSource()).isEqualTo("school_management_service");
        assertThat(result.fallbackRequired()).isFalse();

        RecordedRequest request = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getMethod()).isEqualTo("DELETE");
        assertThat(request.getPath()).isEqualTo("/internal/pessoas/" + pessoaId + "/enderecos");
        assertThat(request.getHeader("X-Escola-Id")).isEqualTo(escolaId.toString());
        assertThat(request.getHeader("X-Usuario-Id")).isEqualTo(usuarioId.toString());
        assertThat(request.getHeader("X-Correlation-Id")).isEqualTo(commandId.toString());
        assertThat(request.getHeader("Idempotency-Key")).isEqualTo("address-cleanup:" + commandId);
    }

    @Test
    void deveRetornarFallbackObrigatorioQuandoMonolitoFalha() {
        UUID commandId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        mockWebServer.enqueue(new MockResponse().setResponseCode(503));

        var result = client.removerEnderecosDaPessoa(new PessoaEnderecoCleanupCommand(
                commandId,
                pessoaId,
                UUID.randomUUID(),
                true,
                "address-cleanup:" + commandId,
                UUID.randomUUID().toString()));

        assertThat(result.commandId()).isEqualTo(commandId);
        assertThat(result.pessoaId()).isEqualTo(pessoaId);
        assertThat(result.status()).isEqualTo("monolith_address_cleanup_adapter_fallback_required");
        assertThat(result.selectedSource()).isEqualTo("monolith_proxy");
        assertThat(result.persistedLocally()).isFalse();
        assertThat(result.fallbackRequired()).isTrue();
        assertThat(result.warnings()).contains(
                "people-service-monolith-address-write-adapter-fallback",
                "keep-monolith-command-as-fallback",
                "local-address-persistence-disabled");
        assertThat(meterRegistry.counter(
                "people.monolith.address.write.failures",
                "operation", "removerEnderecosDaPessoa",
                "cause", "ServiceUnavailable").count()).isEqualTo(1.0d);
    }
}

