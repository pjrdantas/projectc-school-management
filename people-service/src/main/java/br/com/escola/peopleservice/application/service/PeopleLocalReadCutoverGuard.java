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
    private static final String MONOLITH_INTERNAL_RH_SOURCE = "monolith_internal_rh";
    private static final String LOCAL_CANDIDATE_SOURCE = "people_read_model_candidate";
    private static final String STUDENT_RESPONSIBLE_SOURCE = "people_read_model_student_responsible";
    private static final String ADDRESS_SOURCE = "people_read_model_address";
    private static final String DOCUMENT_METADATA_SOURCE = "people_documento_read_model";
    private static final String FUNCIONARIO_INTERNAL_SUMMARY_SOURCE = "people_funcionario_read_model";
    private static final ReadRouteDescriptor ADDRESS_READ_ROUTE = new ReadRouteDescriptor(
            "addressLocalRead",
            "internal-operation:PeopleAddressLocalReadPort",
            ADDRESS_SOURCE);
    private static final ReadRouteDescriptor DOCUMENT_METADATA_READ_ROUTE = new ReadRouteDescriptor(
            "documentMetadataLocalRead",
            "internal-operation:PeopleDocumentMetadataLocalReadPort",
            DOCUMENT_METADATA_SOURCE);
    private static final ReadRouteDescriptor FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE = new ReadRouteDescriptor(
            "funcionarioInternalSummaryLocalRead",
            "internal-operation:PeopleFuncionarioInternalSummaryPort",
            FUNCIONARIO_INTERNAL_SUMMARY_SOURCE);

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
                    "aluno,responsavel,aluno_responsavel"));

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
        registrarMetricaDecisao(decision);
        return decision;
    }

    public PeopleLocalReadRoutingDecision registrarDecisaoLeituraEndereco() {
        PeopleLocalReadRoutingDecision decision = avaliarLeituraEndereco();
        registrarMetricaDecisao(decision);
        Counter.builder("people.shadow.local.persistence.address.read.routing.decisions")
                .tag("selected_source", decision.selectedSource())
                .tag("reason", decision.reason())
                .register(meterRegistry)
                .increment();
        return decision;
    }

    public PeopleLocalReadRoutingDecision registrarDecisaoLeituraDocumentoMetadata() {
        PeopleLocalReadRoutingDecision decision = avaliarLeituraDocumentoMetadata();
        registrarMetricaDecisao(decision);
        Counter.builder("people.shadow.local.persistence.document.metadata.read.routing.decisions")
                .tag("selected_source", decision.selectedSource())
                .tag("reason", decision.reason())
                .register(meterRegistry)
                .increment();
        return decision;
    }

    public PeopleLocalReadRoutingDecision registrarDecisaoLeituraFuncionarioInternalSummary() {
        PeopleLocalReadRoutingDecision decision = avaliarLeituraFuncionarioInternalSummary();
        registrarMetricaDecisao(decision);
        Counter.builder("people.shadow.local.persistence.funcionario.internal.summary.read.routing.decisions")
                .tag("selected_source", decision.selectedSource())
                .tag("reason", decision.reason())
                .register(meterRegistry)
                .increment();
        return decision;
    }

    private void registrarMetricaDecisao(PeopleLocalReadRoutingDecision decision) {
        Counter.builder("people.shadow.local.persistence.read.routing.decisions")
                .tag("operation", decision.operation())
                .tag("selected_source", decision.selectedSource())
                .tag("reason", decision.reason())
                .register(meterRegistry)
                .increment();
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
        boolean localReadEligible = isEligibleReason(reason);
        String selectedSource = selectedSource(localReadEligible, route.operation());
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

    public PeopleLocalReadRoutingDecision avaliarLeituraEndereco() {
        String reason = motivoInelegibilidade(ADDRESS_READ_ROUTE);
        if ("local-read-adapter-not-configured".equals(reason)) {
            reason = "address-local-read-connection-disabled";
        }
        boolean localReadEligible = isEligibleReason(reason);
        return new PeopleLocalReadRoutingDecision(
                ADDRESS_READ_ROUTE.operation(),
                ADDRESS_READ_ROUTE.shadowRoute(),
                ADDRESS_READ_ROUTE.candidateSource(),
                selectedSource(localReadEligible, ADDRESS_READ_ROUTE.operation()),
                properties.readModelCutoverEnabled(),
                localReadEligible,
                properties.readModelFallbackEnabled(),
                false,
                reason);
    }

    public PeopleLocalReadRoutingDecision avaliarLeituraDocumentoMetadata() {
        String reason = motivoInelegibilidade(DOCUMENT_METADATA_READ_ROUTE);
        if ("local-read-adapter-not-configured".equals(reason)) {
            reason = "document-metadata-local-read-connection-disabled";
        }
        boolean localReadEligible = isEligibleReason(reason);
        return new PeopleLocalReadRoutingDecision(
                DOCUMENT_METADATA_READ_ROUTE.operation(),
                DOCUMENT_METADATA_READ_ROUTE.shadowRoute(),
                DOCUMENT_METADATA_READ_ROUTE.candidateSource(),
                selectedSource(localReadEligible, DOCUMENT_METADATA_READ_ROUTE.operation()),
                properties.readModelCutoverEnabled(),
                localReadEligible,
                properties.readModelFallbackEnabled(),
                false,
                reason);
    }

    public PeopleLocalReadRoutingDecision avaliarLeituraFuncionarioInternalSummary() {
        String reason = motivoInelegibilidade(FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE);
        if ("local-read-adapter-not-configured".equals(reason)) {
            reason = "funcionario-internal-summary-local-read-connection-disabled";
        }
        boolean localReadEligible = isEligibleReason(reason);
        return new PeopleLocalReadRoutingDecision(
                FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE.operation(),
                FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE.shadowRoute(),
                FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE.candidateSource(),
                selectedSource(localReadEligible, FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE.operation()),
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
            return "local-read-model-backfill-not-green";
        }
        if (isCatalogRoute(route.operation())) {
            return "local-catalog-read-eligible";
        }
        if ("buscarPorId".equals(route.operation())) {
            return "local-identity-read-eligible";
        }
        if ("consultarCadastro".equals(route.operation())) {
            return "local-student-responsible-read-eligible";
        }
        if (ADDRESS_READ_ROUTE.operation().equals(route.operation())) {
            return "local-address-read-eligible";
        }
        if (DOCUMENT_METADATA_READ_ROUTE.operation().equals(route.operation())) {
            return "local-document-metadata-read-eligible";
        }
        if (FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE.operation().equals(route.operation())) {
            return "local-funcionario-internal-summary-read-eligible";
        }
        return "local-read-adapter-not-configured";
    }

    private boolean isEligibleReason(String reason) {
        return "local-catalog-read-eligible".equals(reason)
                || "local-identity-read-eligible".equals(reason)
                || "local-student-responsible-read-eligible".equals(reason)
                || "local-address-read-eligible".equals(reason)
                || "local-document-metadata-read-eligible".equals(reason)
                || "local-funcionario-internal-summary-read-eligible".equals(reason);
    }

    private String selectedSource(boolean localReadEligible, String operation) {
        if (!localReadEligible) {
            if (FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE.operation().equals(operation)) {
                return MONOLITH_INTERNAL_RH_SOURCE;
            }
            return MONOLITH_SOURCE;
        }
        if ("buscarPorId".equals(operation)) {
            return "people_read_model_identity";
        }
        if ("consultarCadastro".equals(operation)) {
            return STUDENT_RESPONSIBLE_SOURCE;
        }
        if (ADDRESS_READ_ROUTE.operation().equals(operation)) {
            return ADDRESS_SOURCE;
        }
        if (DOCUMENT_METADATA_READ_ROUTE.operation().equals(operation)) {
            return DOCUMENT_METADATA_SOURCE;
        }
        if (FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE.operation().equals(operation)) {
            return FUNCIONARIO_INTERNAL_SUMMARY_SOURCE;
        }
        return "people_read_model_catalog";
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
