package br.com.escola.bff.integration;

import org.junit.jupiter.api.parallel.Isolated;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Isolated
@Suite
@SelectClasses({
        InstitutionalTenantOfficialResilienceSuiteTest.class
})
class InstitutionalTenantOfficialReadIntegrationSuiteTest {
}
