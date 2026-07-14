package br.com.escola.bff.integration;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.escola.bff.SchoolManagementBffApplication;

@Suite
@SpringBootTest(classes = SchoolManagementBffApplication.class)
@SelectClasses({
        InstitutionalTenantReadProxyIntegrationTest.class,
        AuthSessionProxyIntegrationTest.class,
        AuthSessionFallbackIntegrationTest.class
})
class InstitutionalTenantOfficialContextIntegrationSuiteTest {
}
