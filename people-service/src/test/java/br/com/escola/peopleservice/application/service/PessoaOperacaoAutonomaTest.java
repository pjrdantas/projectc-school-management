package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import br.com.escola.peopleservice.application.dto.PessoaEnderecoWriteCommand;
import br.com.escola.peopleservice.infra.config.LeituraModeloProperties;
import br.com.escola.peopleservice.infra.observability.LeituraModeloHealthIndicator;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PessoaOperacaoAutonomaTest {

    @Test
    void deveSelecionarSomenteReadModelLocalSemFallback() {
        var properties = properties();
        var policy = new OrigemLeituraPolicy(properties, new SimpleMeterRegistry());

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
        var policy = new OrigemLeituraPolicy(properties, new SimpleMeterRegistry());
        var health = new LeituraModeloHealthIndicator(
                properties,
                policy,
                new LeituraModeloMigrationState());

        assertThat(health.health().getStatus()).isEqualTo(Status.UP);
        assertThat(health.health().getDetails())
                .containsEntry("operationalMode", "local_only")
                .containsEntry("legacyTrafficEnabled", false);
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

    private LeituraModeloProperties properties() {
        return new LeituraModeloProperties(true, true, true, true, false, false, 500, false);
    }
}
