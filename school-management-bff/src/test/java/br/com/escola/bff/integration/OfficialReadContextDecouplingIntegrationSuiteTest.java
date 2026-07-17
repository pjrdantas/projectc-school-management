package br.com.escola.bff.integration;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        BoletimReadProxyIntegrationTest.class,
        DiarioClasseReadProxyIntegrationTest.class,
        HistoricoEscolarReadProxyIntegrationTest.class,
        AulaReadProxyIntegrationTest.class,
        AvaliacaoReadProxyIntegrationTest.class
})
class OfficialReadContextDecouplingIntegrationSuiteTest {
}

