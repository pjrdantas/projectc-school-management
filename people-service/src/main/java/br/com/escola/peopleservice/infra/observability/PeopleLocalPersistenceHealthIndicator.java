package br.com.escola.peopleservice.infra.observability;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;

@Component("peopleLocalPersistence")
public class PeopleLocalPersistenceHealthIndicator implements HealthIndicator {

    private static final List<String> READ_MODEL_TABLES = List.of(
            "tipo_pessoa",
            "tipo_endereco",
            "pessoa",
            "pessoa_tipo_pessoa",
            "endereco",
            "pessoa_endereco");

    private static final List<String> EXCLUDED_AUTHORITATIVE_TABLES = List.of(
            "aluno",
            "responsavel",
            "funcionario",
            "professor",
            "aluno_responsavel",
            "pessoa_documento");

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

    public PeopleLocalPersistenceHealthIndicator(
            PeopleLocalPersistenceProperties properties,
            MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("enabled", properties.enabled());
        details.put("migrationEnabled", properties.migrationEnabled());
        details.put("readModelCutoverEnabled", properties.readModelCutoverEnabled());
        details.put("failOnError", properties.failOnError());
        details.put("backfillEnabled", properties.backfillEnabled());
        details.put("reconciliationEnabled", properties.reconciliationEnabled());
        details.put("backfillBatchSize", properties.backfillBatchSize());
        details.put("mode", "read_only_shadow_foundation");
        details.put("authoritative", false);
        details.put("writeCutoverAllowed", false);
        details.put("readModelTables", READ_MODEL_TABLES);
        details.put("excludedAuthoritativeTables", EXCLUDED_AUTHORITATIVE_TABLES);
        details.put("shadowReadRoutes", diagnosticoRotasLeitura());
        details.put("backfillPlan", diagnosticoBackfill());
        details.put("rollbackStrategy", "disable_people.shadow.local-persistence.enabled");
        details.put("backfillRecordsTotal", totalContador("people.shadow.local.persistence.backfill.records"));
        details.put("backfillTablesPlannedTotal",
                totalContador("people.shadow.local.persistence.backfill.tables.planned"));
        details.put("reconciliationDivergencesTotal",
                totalContador("people.shadow.local.persistence.reconciliation.divergences"));
        details.put("reconciliationTablesPlannedTotal",
                totalContador("people.shadow.local.persistence.reconciliation.tables.planned"));
        details.put("failuresTotal", totalContador("people.shadow.local.persistence.failures"));
        details.put("operationCyclesTotal", totalContador("people.shadow.local.persistence.cycles"));

        if (properties.readModelCutoverEnabled()) {
            details.put("reason", "read-model-cutover-not-implemented");
            return Health.outOfService().withDetails(details).build();
        }

        if (properties.enabled()) {
            details.put("reason", "local-persistence-foundation-only");
            return Health.outOfService().withDetails(details).build();
        }

        return Health.up().withDetails(details).build();
    }

    private Map<String, Object> diagnosticoBackfill() {
        Map<String, Object> plano = new LinkedHashMap<>();
        for (String table : READ_MODEL_TABLES) {
            Map<String, Object> detalhe = new LinkedHashMap<>();
            detalhe.put("source", "monolith_proxy");
            detalhe.put("target", "people_read_model_candidate");
            detalhe.put("backfillEnabled", properties.backfillEnabled());
            detalhe.put("reconciliationEnabled", properties.reconciliationEnabled());
            detalhe.put("writesEnabled", false);
            detalhe.put("cutoverEnabled", false);
            detalhe.put("idempotent", true);
            plano.put(table, detalhe);
        }
        return plano;
    }

    private Map<String, Object> diagnosticoRotasLeitura() {
        Map<String, Object> rotas = new LinkedHashMap<>();
        for (ReadRouteDescriptor route : READ_ROUTES) {
            Map<String, Object> detalhe = new LinkedHashMap<>();
            detalhe.put("shadowRoute", route.shadowRoute());
            detalhe.put("candidateSource", route.candidateSource());
            detalhe.put("currentSource", "monolith_proxy");
            detalhe.put("localReadEnabled", false);
            rotas.put(route.operation(), detalhe);
        }
        return rotas;
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
