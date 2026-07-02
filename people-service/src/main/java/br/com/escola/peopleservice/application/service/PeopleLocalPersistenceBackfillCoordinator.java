package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleLocalPersistenceOperationReport;
import br.com.escola.peopleservice.application.dto.PeopleLocalPersistenceOperationReport.TableOperationReport;
import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PeopleLocalPersistenceBackfillCoordinator {

    private static final List<TableDescriptor> TABLES = List.of(
            new TableDescriptor("tipo_pessoa", "id_tipo_pessoa"),
            new TableDescriptor("tipo_endereco", "id_tipo_endereco"),
            new TableDescriptor("pessoa", "id_pessoa"),
            new TableDescriptor("pessoa_tipo_pessoa", "id_pessoa_tipo_pessoa"),
            new TableDescriptor("endereco", "id_endereco"),
            new TableDescriptor("pessoa_endereco", "id_pessoa_endereco"));

    private final PeopleLocalPersistenceProperties properties;
    private final MeterRegistry meterRegistry;

    public PeopleLocalPersistenceBackfillCoordinator(
            PeopleLocalPersistenceProperties properties,
            MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    public PeopleLocalPersistenceOperationReport executarCicloControlado() {
        boolean backfillEnabled = properties.backfillEnabled();
        boolean reconciliationEnabled = properties.reconciliationEnabled();
        boolean operationEnabled = backfillEnabled || reconciliationEnabled;

        String status = operationEnabled ? "planned_only" : "disabled";
        String reason = operationEnabled ? "local-read-model-storage-not-configured" : "operation-flags-disabled";

        if (operationEnabled) {
            incrementar("people.shadow.local.persistence.cycles", "status", status);
            if (backfillEnabled) {
                incrementar("people.shadow.local.persistence.backfill.tables.planned", TABLES.size());
            }
            if (reconciliationEnabled) {
                incrementar("people.shadow.local.persistence.reconciliation.tables.planned", TABLES.size());
            }
        }

        return new PeopleLocalPersistenceOperationReport(
                backfillEnabled,
                reconciliationEnabled,
                status,
                reason,
                properties.backfillBatchSize(),
                TABLES.size(),
                false,
                false,
                TABLES.stream()
                        .map(table -> new TableOperationReport(
                                table.name(),
                                table.keyColumn(),
                                "monolith_proxy",
                                "people_read_model_candidate",
                                backfillEnabled,
                                reconciliationEnabled,
                                true))
                        .toList());
    }

    private void incrementar(String metricName, String tagKey, String tagValue) {
        Counter.builder(metricName)
                .tag(tagKey, tagValue)
                .register(meterRegistry)
                .increment();
    }

    private void incrementar(String metricName, int quantidade) {
        Counter.builder(metricName)
                .register(meterRegistry)
                .increment(quantidade);
    }

    private record TableDescriptor(String name, String keyColumn) {
    }
}
