package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PeopleLocalReadCutoverGuardTest {

    @Test
    void deveManterMonolitoQuandoCutoverDeLeituraEstaDesligado() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleLocalReadCutoverGuard guard = new PeopleLocalReadCutoverGuard(
                new PeopleLocalPersistenceProperties(false, false, false, false, false, false, 500, true),
                meterRegistry);

        var decision = guard.registrarDecisao("buscarPorId");

        assertThat(decision.operation()).isEqualTo("buscarPorId");
        assertThat(decision.selectedSource()).isEqualTo("monolith_proxy");
        assertThat(decision.localReadRequested()).isFalse();
        assertThat(decision.localReadEligible()).isFalse();
        assertThat(decision.fallbackEnabled()).isTrue();
        assertThat(decision.writesEnabled()).isFalse();
        assertThat(decision.reason()).isEqualTo("read-model-cutover-disabled");
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.read.routing.decisions",
                "operation", "buscarPorId",
                "selected_source", "monolith_proxy",
                "reason", "read-model-cutover-disabled").count()).isEqualTo(1.0d);
    }

    @Test
    void deveBloquearCutoverQuandoPersistenciaLocalEstaDesligada() {
        PeopleLocalReadCutoverGuard guard = new PeopleLocalReadCutoverGuard(
                new PeopleLocalPersistenceProperties(false, false, true, false, true, true, 500, true),
                new SimpleMeterRegistry());

        var decision = guard.avaliar("listarTiposPessoa");

        assertThat(decision.localReadRequested()).isTrue();
        assertThat(decision.localReadEligible()).isFalse();
        assertThat(decision.selectedSource()).isEqualTo("monolith_proxy");
        assertThat(decision.reason()).isEqualTo("local-persistence-disabled");
    }

    @Test
    void deveExigirFallbackObrigatorioParaCutoverControlado() {
        PeopleLocalReadCutoverGuard guard = new PeopleLocalReadCutoverGuard(
                new PeopleLocalPersistenceProperties(true, false, true, false, true, true, 500, false),
                new SimpleMeterRegistry());

        var decision = guard.avaliar("consultarCadastro");

        assertThat(decision.fallbackEnabled()).isFalse();
        assertThat(decision.localReadEligible()).isFalse();
        assertThat(decision.reason()).isEqualTo("fallback-required");
    }

    @Test
    void deveBloquearQuandoReconciliacaoTemDivergencias() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Counter.builder("people.shadow.local.persistence.reconciliation.divergences")
                .tag("tabela", "pessoa")
                .tag("tipo", "cpf")
                .register(meterRegistry)
                .increment();
        PeopleLocalReadCutoverGuard guard = new PeopleLocalReadCutoverGuard(
                new PeopleLocalPersistenceProperties(true, false, true, false, true, true, 500, true),
                meterRegistry);

        var decision = guard.avaliar("buscarPorId");

        assertThat(decision.localReadEligible()).isFalse();
        assertThat(decision.reason()).isEqualTo("reconciliation-has-divergences");
    }

    @Test
    void deveBloquearMesmoComFlagsVerdesEnquantoAdapterLocalNaoExiste() {
        PeopleLocalReadCutoverGuard guard = new PeopleLocalReadCutoverGuard(
                new PeopleLocalPersistenceProperties(true, false, true, false, true, true, 500, true),
                new SimpleMeterRegistry());

        var decisions = guard.avaliarTodas();

        assertThat(decisions).containsKeys(
                "listarTiposPessoa",
                "listarTiposEndereco",
                "buscarPorId",
                "consultarCadastro");
        assertThat(decisions.values())
                .allSatisfy(decision -> {
                    assertThat(decision.selectedSource()).isEqualTo("monolith_proxy");
                    assertThat(decision.localReadEligible()).isFalse();
                    assertThat(decision.reason()).isEqualTo("local-read-adapter-not-configured");
                });
    }
}
