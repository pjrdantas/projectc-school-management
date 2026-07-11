package br.com.escola.peopleservice.application.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.state.PeopleReadModelSyncSummary;
import br.com.escola.peopleservice.infra.config.PeopleReadModelProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;

@Service
public class PeopleReadSourcePolicy {

    private static final String MONOLITH_SOURCE = "monolith_proxy";
    private static final String MONOLITH_INTERNAL_RH_SOURCE = "monolith_internal_rh";
    private static final String STUDENT_RESPONSIBLE_SOURCE = "people_read_model_student_responsible";
    private static final String ADDRESS_SOURCE = "people_read_model_address";
    private static final String DOCUMENT_METADATA_SOURCE = "people_documento_read_model";
    private static final String FUNCIONARIO_INTERNAL_SUMMARY_SOURCE = "people_funcionario_read_model";
    private static final String PROFESSOR_INTERNAL_SUMMARY_SOURCE = "people_professor_read_model";
    private static final ReadRouteDescriptor ADDRESS_READ_ROUTE = new ReadRouteDescriptor(
            "endereco",
            "internal-operation:PessoaEnderecoPort",
            ADDRESS_SOURCE);
    private static final ReadRouteDescriptor DOCUMENT_METADATA_READ_ROUTE = new ReadRouteDescriptor(
            "documentoMetadata",
            "internal-operation:PessoaDocumentoMetadataPort",
            DOCUMENT_METADATA_SOURCE);
    private static final ReadRouteDescriptor RESPONSAVEL_VINCULO_READ_ROUTE = new ReadRouteDescriptor(
            "responsavelVinculo",
            "internal-operation:ResponsavelPessoaPort",
            "responsavel");
    private static final ReadRouteDescriptor FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE = new ReadRouteDescriptor(
            "funcionarioResumo",
            "internal-operation:PessoaFuncionarioResumoPort",
            FUNCIONARIO_INTERNAL_SUMMARY_SOURCE);
    private static final ReadRouteDescriptor PROFESSOR_INTERNAL_SUMMARY_READ_ROUTE = new ReadRouteDescriptor(
            "professorResumo",
            "internal-operation:PessoaProfessorResumoPort",
            PROFESSOR_INTERNAL_SUMMARY_SOURCE);

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

    private final PeopleReadModelProperties properties;
    private final MeterRegistry meterRegistry;
    private final PeopleReadModelSyncState operationState;

    public PeopleReadSourcePolicy(
            PeopleReadModelProperties properties,
            MeterRegistry meterRegistry,
            PeopleReadModelSyncState operationState) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        this.operationState = operationState;
    }

    public PeopleReadSourceDecision registrarDecisao(String operation) {
        PeopleReadSourceDecision decision = avaliar(operation);
        registrarMetricaDecisao(decision);
        return decision;
    }

    public PeopleReadSourceDecision registrarDecisaoLeituraEndereco() {
        PeopleReadSourceDecision decision = avaliarLeituraEndereco();
        registrarMetricaDecisao(decision);
        Counter.builder("people.address.read.routing.decisions")
                .tag("selected_source", decision.selectedSource())
                .tag("reason", decision.reason())
                .register(meterRegistry)
                .increment();
        return decision;
    }

    public PeopleReadSourceDecision registrarDecisaoLeituraDocumentoMetadata() {
        PeopleReadSourceDecision decision = avaliarLeituraDocumentoMetadata();
        registrarMetricaDecisao(decision);
        Counter.builder("people.document.read.routing.decisions")
                .tag("selected_source", decision.selectedSource())
                .tag("reason", decision.reason())
                .register(meterRegistry)
                .increment();
        return decision;
    }

    public PeopleReadSourceDecision registrarDecisaoLeituraResponsavelVinculo() {
        PeopleReadSourceDecision decision = avaliarLeituraResponsavelVinculo();
        registrarMetricaDecisao(decision);
        Counter.builder("people.responsible.read.routing.decisions")
                .tag("selected_source", decision.selectedSource())
                .tag("reason", decision.reason())
                .register(meterRegistry)
                .increment();
        return decision;
    }

    public PeopleReadSourceDecision registrarDecisaoLeituraFuncionarioResumo() {
        PeopleReadSourceDecision decision = avaliarLeituraFuncionarioResumo();
        registrarMetricaDecisao(decision);
        Counter.builder("people.funcionario.read.routing.decisions")
                .tag("selected_source", decision.selectedSource())
                .tag("reason", decision.reason())
                .register(meterRegistry)
                .increment();
        return decision;
    }

    public PeopleReadSourceDecision registrarDecisaoLeituraProfessorResumo() {
        PeopleReadSourceDecision decision = avaliarLeituraProfessorResumo();
        registrarMetricaDecisao(decision);
        Counter.builder("people.professor.read.routing.decisions")
                .tag("selected_source", decision.selectedSource())
                .tag("reason", decision.reason())
                .register(meterRegistry)
                .increment();
        return decision;
    }

    private void registrarMetricaDecisao(PeopleReadSourceDecision decision) {
        Counter.builder("people.read.routing.decisions")
                .tag("operation", decision.operation())
                .tag("selected_source", decision.selectedSource())
                .tag("reason", decision.reason())
                .register(meterRegistry)
                .increment();
    }

    public Map<String, PeopleReadSourceDecision> avaliarTodas() {
        Map<String, PeopleReadSourceDecision> decisions = new LinkedHashMap<>();
        for (ReadRouteDescriptor route : READ_ROUTES) {
            decisions.put(route.operation(), avaliar(route.operation()));
        }
        return decisions;
    }

    public PeopleReadSourceDecision avaliar(String operation) {
        ReadRouteDescriptor route = READ_ROUTES.stream()
                .filter(candidate -> candidate.operation().equals(operation))
                .findFirst()
                .orElse(new ReadRouteDescriptor(operation, "unknown", "unknown"));

        String reason = motivoInelegibilidade(route);
        boolean localReadEligible = isEligibleReason(reason);
        String selectedSource = selectedSource(localReadEligible, route.operation());
        return new PeopleReadSourceDecision(
                route.operation(),
                route.route(),
                route.candidateSource(),
                selectedSource,
                properties.localReadRoutingEnabled(),
                localReadEligible,
                properties.fallbackEnabled(),
                false,
                reason);
    }

    public PeopleReadSourceDecision avaliarLeituraEndereco() {
        String reason = motivoInelegibilidade(ADDRESS_READ_ROUTE);
        if ("local-read-adapter-not-configured".equals(reason)) {
            reason = "address-local-read-connection-disabled";
        }
        boolean localReadEligible = isEligibleReason(reason);
        return new PeopleReadSourceDecision(
                ADDRESS_READ_ROUTE.operation(),
                ADDRESS_READ_ROUTE.route(),
                ADDRESS_READ_ROUTE.candidateSource(),
                selectedSource(localReadEligible, ADDRESS_READ_ROUTE.operation()),
                properties.localReadRoutingEnabled(),
                localReadEligible,
                properties.fallbackEnabled(),
                false,
                reason);
    }

    public PeopleReadSourceDecision avaliarLeituraDocumentoMetadata() {
        String reason = motivoInelegibilidade(DOCUMENT_METADATA_READ_ROUTE);
        if ("local-read-adapter-not-configured".equals(reason)) {
            reason = "document-metadata-local-read-connection-disabled";
        }
        boolean localReadEligible = isEligibleReason(reason);
        return new PeopleReadSourceDecision(
                DOCUMENT_METADATA_READ_ROUTE.operation(),
                DOCUMENT_METADATA_READ_ROUTE.route(),
                DOCUMENT_METADATA_READ_ROUTE.candidateSource(),
                selectedSource(localReadEligible, DOCUMENT_METADATA_READ_ROUTE.operation()),
                properties.localReadRoutingEnabled(),
                localReadEligible,
                properties.fallbackEnabled(),
                false,
                reason);
    }

    public PeopleReadSourceDecision avaliarLeituraResponsavelVinculo() {
        String reason = motivoInelegibilidade(RESPONSAVEL_VINCULO_READ_ROUTE);
        if ("local-read-adapter-not-configured".equals(reason)) {
            reason = "responsible-link-local-read-connection-disabled";
        }
        boolean localReadEligible = isEligibleReason(reason);
        return new PeopleReadSourceDecision(
                RESPONSAVEL_VINCULO_READ_ROUTE.operation(),
                RESPONSAVEL_VINCULO_READ_ROUTE.route(),
                RESPONSAVEL_VINCULO_READ_ROUTE.candidateSource(),
                selectedSource(localReadEligible, RESPONSAVEL_VINCULO_READ_ROUTE.operation()),
                properties.localReadRoutingEnabled(),
                localReadEligible,
                properties.fallbackEnabled(),
                false,
                reason);
    }

    public PeopleReadSourceDecision avaliarLeituraFuncionarioResumo() {
        String reason = motivoInelegibilidade(FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE);
        if ("local-read-adapter-not-configured".equals(reason)) {
            reason = "funcionario-internal-summary-local-read-connection-disabled";
        }
        boolean localReadEligible = isEligibleReason(reason);
        return new PeopleReadSourceDecision(
                FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE.operation(),
                FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE.route(),
                FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE.candidateSource(),
                selectedSource(localReadEligible, FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE.operation()),
                properties.localReadRoutingEnabled(),
                localReadEligible,
                properties.fallbackEnabled(),
                false,
                reason);
    }

    public PeopleReadSourceDecision avaliarLeituraProfessorResumo() {
        String reason = motivoInelegibilidade(PROFESSOR_INTERNAL_SUMMARY_READ_ROUTE);
        if ("local-read-adapter-not-configured".equals(reason)) {
            reason = "professor-internal-summary-local-read-connection-disabled";
        }
        boolean localReadEligible = isEligibleReason(reason);
        return new PeopleReadSourceDecision(
                PROFESSOR_INTERNAL_SUMMARY_READ_ROUTE.operation(),
                PROFESSOR_INTERNAL_SUMMARY_READ_ROUTE.route(),
                PROFESSOR_INTERNAL_SUMMARY_READ_ROUTE.candidateSource(),
                selectedSource(localReadEligible, PROFESSOR_INTERNAL_SUMMARY_READ_ROUTE.operation()),
                properties.localReadRoutingEnabled(),
                localReadEligible,
                properties.fallbackEnabled(),
                false,
                reason);
    }

    private String motivoInelegibilidade(ReadRouteDescriptor route) {
        if (!properties.localReadRoutingEnabled()) {
            return "local-read-routing-disabled";
        }
        if (!properties.fallbackEnabled()) {
            return "fallback-required";
        }
        if (!properties.enabled()) {
            return "local-persistence-disabled";
        }
        if (!properties.backfillEnabled() || !properties.reconciliationEnabled()) {
            return "backfill-and-reconciliation-required";
        }
        if (totalContador("people.readmodel.sync.divergences") > 0.0d) {
            return "reconciliation-has-divergences";
        }
        if (totalContador("people.readmodel.sync.failures") > 0.0d) {
            return "local-persistence-has-failures";
        }
        PeopleReadModelSyncSummary lastReport = operationState.currentReport();
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
        if (RESPONSAVEL_VINCULO_READ_ROUTE.operation().equals(route.operation())) {
            return "local-responsible-link-read-eligible";
        }
        if (FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE.operation().equals(route.operation())) {
            return "local-funcionario-internal-summary-read-eligible";
        }
        if (PROFESSOR_INTERNAL_SUMMARY_READ_ROUTE.operation().equals(route.operation())) {
            return "local-professor-internal-summary-read-eligible";
        }
        return "local-read-adapter-not-configured";
    }

    private boolean isEligibleReason(String reason) {
        return "local-catalog-read-eligible".equals(reason)
                || "local-identity-read-eligible".equals(reason)
                || "local-student-responsible-read-eligible".equals(reason)
                || "local-address-read-eligible".equals(reason)
                || "local-document-metadata-read-eligible".equals(reason)
                || "local-responsible-link-read-eligible".equals(reason)
                || "local-funcionario-internal-summary-read-eligible".equals(reason)
                || "local-professor-internal-summary-read-eligible".equals(reason);
    }

    private String selectedSource(boolean localReadEligible, String operation) {
        if (!localReadEligible) {
            if (FUNCIONARIO_INTERNAL_SUMMARY_READ_ROUTE.operation().equals(operation)) {
                return MONOLITH_INTERNAL_RH_SOURCE;
            }
            if (PROFESSOR_INTERNAL_SUMMARY_READ_ROUTE.operation().equals(operation)) {
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
        if (RESPONSAVEL_VINCULO_READ_ROUTE.operation().equals(operation)) {
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
        if (PROFESSOR_INTERNAL_SUMMARY_READ_ROUTE.operation().equals(operation)) {
            return PROFESSOR_INTERNAL_SUMMARY_SOURCE;
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

    private record ReadRouteDescriptor(String operation, String route, String candidateSource) {
    }
}

