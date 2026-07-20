package br.com.escola.responsiblesservice.infra.observability;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import br.com.escola.responsiblesservice.infra.config.LeituraModeloProperties;

class LeituraModeloHealthIndicatorTest {

    @Test
    void deveReportarOperacaoLocalSemTrafegoLegado() {
        var properties = new LeituraModeloProperties(true, true, true, false, false, 500, true, false);
        var health = new LeituraModeloHealthIndicator(properties).health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("operationalMode", "local_only")
                .containsEntry("legacyTrafficEnabled", false);
    }
}
