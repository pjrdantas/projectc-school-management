package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.dto.PeopleLocalPersistenceOperationReport;
import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PeopleLocalReadCutoverGuardTest {

    @Test
    void deveManterMonolitoQuandoCutoverDeLeituraEstaDesligado() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleLocalReadCutoverGuard guard = new PeopleLocalReadCutoverGuard(
                new PeopleLocalPersistenceProperties(false, false, false, false, false, false, 500, true),
                meterRegistry,
                new PeopleLocalPersistenceOperationState());

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
                new SimpleMeterRegistry(),
                new PeopleLocalPersistenceOperationState());

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
                new SimpleMeterRegistry(),
                new PeopleLocalPersistenceOperationState());

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
                meterRegistry,
                greenState());

        var decision = guard.avaliar("buscarPorId");

        assertThat(decision.localReadEligible()).isFalse();
        assertThat(decision.reason()).isEqualTo("reconciliation-has-divergences");
    }

    @Test
    void deveLiberarCatalogosEBuscarPorIdQuandoRelatorioLocalEstaVerde() {
        PeopleLocalReadCutoverGuard guard = new PeopleLocalReadCutoverGuard(
                new PeopleLocalPersistenceProperties(true, false, true, false, true, true, 500, true),
                new SimpleMeterRegistry(),
                greenState());

        var decisions = guard.avaliarTodas();

        assertThat(decisions).containsKeys(
                "listarTiposPessoa",
                "listarTiposEndereco",
                "buscarPorId",
                "consultarCadastro");
        assertThat(decisions.get("listarTiposPessoa").selectedSource()).isEqualTo("people_read_model_catalog");
        assertThat(decisions.get("listarTiposPessoa").localReadEligible()).isTrue();
        assertThat(decisions.get("listarTiposPessoa").reason()).isEqualTo("local-catalog-read-eligible");
        assertThat(decisions.get("listarTiposEndereco").selectedSource()).isEqualTo("people_read_model_catalog");
        assertThat(decisions.get("listarTiposEndereco").localReadEligible()).isTrue();
        assertThat(decisions.get("buscarPorId").selectedSource()).isEqualTo("people_read_model_identity");
        assertThat(decisions.get("buscarPorId").localReadEligible()).isTrue();
        assertThat(decisions.get("buscarPorId").reason()).isEqualTo("local-identity-read-eligible");
        assertThat(decisions.get("consultarCadastro").selectedSource()).isEqualTo("monolith_proxy");
        assertThat(decisions.get("consultarCadastro").reason()).isEqualTo("local-read-adapter-not-configured");
    }

    @Test
    void deveBloquearCatalogosQuandoRelatorioLocalAindaNaoEstaVerde() {
        PeopleLocalReadCutoverGuard guard = new PeopleLocalReadCutoverGuard(
                new PeopleLocalPersistenceProperties(true, false, true, false, true, true, 500, true),
                new SimpleMeterRegistry(),
                new PeopleLocalPersistenceOperationState());

        var decision = guard.avaliar("listarTiposPessoa");

        assertThat(decision.selectedSource()).isEqualTo("monolith_proxy");
        assertThat(decision.localReadEligible()).isFalse();
        assertThat(decision.reason()).isEqualTo("local-read-model-backfill-not-green");
    }

    private PeopleLocalPersistenceOperationState greenState() {
        PeopleLocalPersistenceOperationState state = new PeopleLocalPersistenceOperationState();
        state.update(new PeopleLocalPersistenceOperationReport(
                true,
                true,
                "completed",
                "local-read-model-backfill-and-reconciliation-completed",
                500,
                7,
                7,
                15,
                15,
                15,
                0,
                false,
                false,
                List.of()));
        return state;
    }
}
