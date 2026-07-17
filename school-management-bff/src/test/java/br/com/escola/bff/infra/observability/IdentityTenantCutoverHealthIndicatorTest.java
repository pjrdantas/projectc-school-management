package br.com.escola.bff.infra.observability;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.com.escola.bff.infra.config.IdentityTenantCutoverProperties;

class IdentityTenantCutoverHealthIndicatorTest {

    @Test
    void deveExporDetalhesDoCutoverIdentityTenant() {
        IdentityTenantCutoverProperties properties = new IdentityTenantCutoverProperties(
                true,
                true,
                new IdentityTenantCutoverProperties.RouteFlags(true, false));

        IdentityTenantCutoverHealthIndicator indicator = new IdentityTenantCutoverHealthIndicator(properties);

        assertThat(indicator.health().getStatus().getCode()).isEqualTo("UP");
        assertThat(indicator.health().getDetails()).containsEntry("cutoverEnabled", true);
        assertThat(indicator.health().getDetails()).containsEntry("fallbackToLegacyOnError", true);
        assertThat(indicator.health().getDetails()).containsEntry("authEscolasEnabled", true);
        assertThat(indicator.health().getDetails()).containsEntry("authEscolaAtivaEnabled", false);
    }
}

