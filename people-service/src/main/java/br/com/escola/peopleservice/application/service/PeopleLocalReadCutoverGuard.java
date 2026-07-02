package br.com.escola.peopleservice.application.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleLocalPersistenceOperationReport;
import br.com.escola.peopleservice.application.dto.PeopleLocalReadRoutingDecision;
import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;

@Service
public class PeopleLocalReadCutoverGuard {

    private static final String MONOLITH_SOURCE = "monolith_proxy";
    private static final String LOCAL_CANDIDATE_SOURCE = "people_read_model_candidate";

    private static final List<ReadRouteDescriptor> READ_ROUTES = List.of(
            new ReadRouteDescriptor(
                    "listarTiposPessoa",
                    "GET /internal/v1/pessoas/catalogos/tipos-pessoa",
                    "tipo_pessoa"),
            new ReadRouteDescriptor(
                    "listarTiposEndereco",
                    "GET /internal/v1/pessoas/catalogos/tipos-endereco",
                    "tipo_endereco"),
            new ReadRouteDescriptor(
                    "buscarPorId",
                    "GET /internal/v1/pessoas/{id}",
                    "pessoa"),
            new ReadRouteDescriptor(
                    "consultarCadastro",
                    "GET /internal/v1/pessoas/consulta-cadastral",
                    "pessoa,pessoa_tipo_pessoa,endereco,pessoa_endereco"));

    private final PeopleLocalPersistenceProperties properties;
    private final MeterRegistry meterRegistry;
    private final PeopleLocalPersistenceOperationState operationState;

    public PeopleLocalReadCutoverGuard(
            PeopleLocalPersistenceProperties properties,
            MeterRegistry meterRegistry,
            PeopleLocalPersistenceOperationState operationState) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        this.operationState = operationState;
    }

    public PeopleLocalReadRoutingDecision registrarDecisao(String operation) {
        PeopleLocalReadRoutingDecision decision = avaliar(operation);
        Counter.builder("people.shadow.local.persistence.read.routing.decisions")
                .tag("operation", decision.operation())
                .tag("selected_source", decision.selectedSource())
                .tag("reason", decision.reason())
                .register(meterRegistry)
                .increment();
        return decision;
    }

    public Map<String, PeopleLocalReadRoutingDecision> avaliarTodas() {
        Map<String, PeopleLocalReadRoutingDecision> decisions = new LinkedHashMap<>();
        for (ReadRouteDescriptor route : READ_ROUTES) {
            decisions.put(route.operation(), avaliar(route.operation()));
        }
        return decisions;
    }

    public PeopleLocalReadRoutingDecision avaliar(String operation) {
        ReadRouteDescriptor route = READ_ROUTES.stream()
                .filter(candidate -> candidate.operation().equals(operation))
                .findFirst()
                .orElse(new ReadRouteDescriptor(operation, "unknown", "unknown"));

        String reason = motivoInelegibilidade(route);
        boolean localReadEligible = "local-catalog-read-eligible".equals(reason);
        String selectedSource = localReadEligible ? "people_read_model_catalog" : MONOLITH_SOURCE;
        return new PeopleLocalReadRoutingDecision(
                route.operation(),
                route.shadowRoute(),
                route.candidateSource(),
                selectedSource,
                properties.readModelCutoverEnabled(),
                localReadEligible,
                properties.readModelFallbackEnabled(),
                false,
                reason);
    }

    private String motivoInelegibilidade(ReadRouteDescriptor route) {
        if (!properties.readModelCutoverEnabled()) {
            return "read-model-cutover-disabled";
        }
        if (!properties.readModelFallbackEnabled()) {
            return "fallback-required";
        }
        if (!properties.enabled()) {
            return "local-persistence-disabled";
        }
        if (!properties.backfillEnabled() || !properties.reconciliationEnabled()) {
            return "backfill-and-reconciliation-required";
        }
        if (totalContador("people.shadow.local.persistence.reconciliation.divergences") > 0.0d) {
            return "reconciliation-has-divergences";
        }
        if (totalContador("people.shadow.local.persistence.failures") > 0.0d) {
            return "local-persistence-has-failures";
        }
        PeopleLocalPersistenceOperationReport lastReport = operationState.currentReport();
        if (!"completed".equals(lastReport.status()) || lastReport.divergences() > 0) {
            return "catalog-backfill-not-green";
        }
        if (isCatalogRoute(route.operation())) {
            return "local-catalog-read-eligible";
        }
        return "local-read-adapter-not-configured";
    }

    private boolean isCatalogRoute(String operation) {
        return "listarTiposPessoa".equals(operation) || "listarTiposEndereco".equals(operation);
    }

    private double totalContador(String meterName) {
        return meterRegistry.getMeters().stream()
                .filter(meter -> meterName.equals(meter.getId().getName()))
                .mapToDouble(this::valorContador)
                .sum();
    }

    private double valorContador(Meter meter) {
        for (Measurement measurement : meter.measure()) {
            if (measurement.getStatistic() == Statistic.COUNT) {
                return measurement.getValue();
            }
        }
        return 0.0d;
    }

    private record ReadRouteDescriptor(String operation, String shadowRoute, String candidateSource) {
    }
}
