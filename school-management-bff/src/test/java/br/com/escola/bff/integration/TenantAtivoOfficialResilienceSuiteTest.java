package br.com.escola.bff.integration;

import org.junit.jupiter.api.parallel.Isolated;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import br.com.escola.bff.application.service.IdentityTenantContextFallbackObservabilityTest;

@Isolated
@Suite
@SelectClasses({
        TenantAtivoReadProxyIntegrationTest.class,
        AuthSessionProxyIntegrationTest.class,
        AuthSessionFallbackIntegrationTest.class,
        IdentityTenantContextFallbackObservabilityTest.class
})
public class TenantAtivoOfficialResilienceSuiteTest {
}

