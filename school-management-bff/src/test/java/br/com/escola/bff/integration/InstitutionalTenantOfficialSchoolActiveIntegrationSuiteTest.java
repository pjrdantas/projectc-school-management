package br.com.escola.bff.integration;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        AuthSessionProxyIntegrationTest.class,
        AuthSessionFallbackIntegrationTest.class
})
class InstitutionalTenantOfficialSchoolActiveIntegrationSuiteTest {
}
