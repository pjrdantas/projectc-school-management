package br.com.escola.bff.integration;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        PedagogicalBoletimReadProxyIntegrationTest.class,
        PedagogicalDiarioClasseReadProxyIntegrationTest.class,
        PedagogicalHistoricoEscolarReadProxyIntegrationTest.class,
        PedagogicalAulaReadProxyIntegrationTest.class,
        PedagogicalAvaliacaoReadProxyIntegrationTest.class
})
class OfficialReadContextDecouplingIntegrationSuiteTest {
}
