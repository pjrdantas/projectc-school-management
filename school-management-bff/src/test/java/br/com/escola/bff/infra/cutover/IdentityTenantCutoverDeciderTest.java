package br.com.escola.bff.infra.cutover;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.com.escola.bff.application.service.IdentityTenantRoute;
import br.com.escola.bff.infra.config.IdentityTenantCutoverProperties;

class IdentityTenantCutoverDeciderTest {

    @Test
    void deveDesabilitarTodasAsRotasQuandoCutoverGlobalEstiverDesligado() {
        IdentityTenantCutoverProperties properties = new IdentityTenantCutoverProperties(
                false,
                true,
                new IdentityTenantCutoverProperties.RouteFlags(true, true));

        IdentityTenantCutoverDecider decider = new IdentityTenantCutoverDecider(properties);

        assertThat(decider.decision(IdentityTenantRoute.AUTH_ESCOLAS).useNewService()).isFalse();
        assertThat(decider.decision(IdentityTenantRoute.AUTH_ESCOLAS).reason()).isEqualTo("cutover_disabled");
    }

    @Test
    void deveDesabilitarRotaPontualQuandoFlagDaRotaEstiverDesligada() {
        IdentityTenantCutoverProperties properties = new IdentityTenantCutoverProperties(
                true,
                true,
                new IdentityTenantCutoverProperties.RouteFlags(true, false));

        IdentityTenantCutoverDecider decider = new IdentityTenantCutoverDecider(properties);

        assertThat(decider.decision(IdentityTenantRoute.AUTH_ESCOLA_ATIVA).useNewService()).isFalse();
        assertThat(decider.decision(IdentityTenantRoute.AUTH_ESCOLA_ATIVA).reason()).isEqualTo("route_disabled");
        assertThat(decider.decision(IdentityTenantRoute.AUTH_TENANT_ATIVA).useNewService()).isTrue();
        assertThat(decider.decision(IdentityTenantRoute.AUTH_TENANT_ATIVA).reason()).isEqualTo("service_enabled");
    }
}
