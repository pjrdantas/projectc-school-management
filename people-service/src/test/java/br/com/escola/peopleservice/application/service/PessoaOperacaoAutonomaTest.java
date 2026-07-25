package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Status;

import br.com.escola.peopleservice.application.dto.PessoaEnderecoWriteCommand;
import br.com.escola.peopleservice.infra.config.RuntimeProperties;
import br.com.escola.peopleservice.infra.observability.PersistenceHealthIndicator;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PessoaOperacaoAutonomaTest {

    @Test
    void deveSelecionarSomenteReadModelLocalSemFallback() {
        var properties = properties();
        var policy = new DataAccessPolicy(properties, new SimpleMeterRegistry());

        assertThat(policy.avaliarTodas().values())
                .allSatisfy(decision -> {
                    assertThat(decision.localReadEligible()).isTrue();
                    assertThat(decision.fallbackEnabled()).isFalse();
                    assertThat(decision.selectedSource()).doesNotContain("monolith");
                });
    }

    @Test
    void deveExporSaudeLocalOnlySemTrafegoLegado() {
        var properties = properties();
        var policy = new DataAccessPolicy(properties, new SimpleMeterRegistry());
        var health = new PersistenceHealthIndicator(
                properties,
                policy);

        assertThat(health.health().getStatus()).isEqualTo(Status.UP);
        assertThat(health.health().getDetails())
                .containsEntry("operationalMode", "local_only");
    }

    @Test
    void deveRecusarEncaminhamentoDeEscritaDeEndereco() {
        var service = new EnderecoCommandService(new SimpleMeterRegistry());
        var result = service.criarOuAtualizarEnderecoPrincipal(new PessoaEnderecoWriteCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RESIDENCIAL",
                true,
                "01001000",
                "Rua Local",
                "10",
                null,
                "Centro",
                "Sao Paulo",
                "SP",
                "idempotency",
                "d15"));

        assertThat(result.selectedSource()).isEqualTo("unsupported");
        assertThat(result.status()).isEqualTo("address_write_not_supported");
        assertThat(result.fallbackRequired()).isTrue();
    }

    private RuntimeProperties properties() {
        return new RuntimeProperties(true, true, false);
    }
}
