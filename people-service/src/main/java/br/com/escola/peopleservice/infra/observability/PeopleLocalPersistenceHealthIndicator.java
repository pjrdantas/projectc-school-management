package br.com.escola.peopleservice.infra.observability;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.application.dto.PeopleLocalReadRoutingDecision;
import br.com.escola.peopleservice.application.dto.PeopleCatalogReadModelSchemaPlan;
import br.com.escola.peopleservice.application.dto.PeopleAddressScopeClosurePlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentBackfillReconciliationPreparationPlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentLocalReadActivationEligibilityPlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentInternalUsageCandidatePlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentLocalReadCandidatePlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentMetadataLocalAdapterPreparationPlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentMetadataSchemaDiagnosticPlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentScopeDiagnosticPlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentInternalMetadataReadContractPlan;
import br.com.escola.peopleservice.application.dto.PeopleAddressWriteMonolithAdapterPlan;
import br.com.escola.peopleservice.application.dto.PeopleAddressWriteAuthorityPlan;
import br.com.escola.peopleservice.application.dto.PeopleFuncionarioInternalSummaryAdapterPreparationPlan;
import br.com.escola.peopleservice.application.dto.PeopleFuncionarioScopeDiagnosticPlan;
import br.com.escola.peopleservice.application.dto.PeopleFuncionarioInternalSummaryContractPlan;
import br.com.escola.peopleservice.application.dto.PeopleTransactionalReadModelExpansionPlan;
import br.com.escola.peopleservice.application.service.PeopleAddressScopeClosurePlanner;
import br.com.escola.peopleservice.application.service.PeopleAddressWriteAuthorityPlanner;
import br.com.escola.peopleservice.application.service.PeopleAddressWriteMonolithAdapterPlanner;
import br.com.escola.peopleservice.application.service.PeopleCatalogReadModelSchemaPlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentBackfillReconciliationPreparationPlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentLocalReadActivationEligibilityPlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentInternalUsageCandidatePlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentLocalReadCandidatePlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentMetadataLocalAdapterPreparationPlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentMetadataSchemaDiagnosticPlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentInternalMetadataReadContractPlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentScopeDiagnosticPlanner;
import br.com.escola.peopleservice.application.service.PeopleFuncionarioInternalSummaryAdapterPreparationPlanner;
import br.com.escola.peopleservice.application.service.PeopleFuncionarioInternalSummaryContractPlanner;
import br.com.escola.peopleservice.application.service.PeopleFuncionarioScopeDiagnosticPlanner;
import br.com.escola.peopleservice.application.service.PeopleLocalPersistenceOperationState;
import br.com.escola.peopleservice.application.service.PeopleLocalReadModelSchemaMigrationState;
import br.com.escola.peopleservice.application.service.PeopleLocalReadCutoverGuard;
import br.com.escola.peopleservice.application.service.PeopleTransactionalReadModelExpansionPlanner;
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
            "aluno",
            "responsavel",
            "aluno_responsavel",
            "endereco",
            "pessoa_endereco",
            "people_documento_read_model",
            "people_funcionario_read_model");

    private static final List<String> EXCLUDED_AUTHORITATIVE_TABLES = List.of(
            "funcionario",
            "professor",
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
                    "aluno,responsavel,aluno_responsavel"));

    private final PeopleLocalPersistenceProperties properties;
    private final MeterRegistry meterRegistry;
    private final PeopleLocalReadCutoverGuard readCutoverGuard;
    private final PeopleCatalogReadModelSchemaPlanner catalogSchemaPlanner;
    private final PeopleTransactionalReadModelExpansionPlanner transactionalExpansionPlanner;
    private final PeopleAddressWriteAuthorityPlanner addressWriteAuthorityPlanner;
    private final PeopleAddressWriteMonolithAdapterPlanner addressWriteMonolithAdapterPlanner =
            new PeopleAddressWriteMonolithAdapterPlanner();
    private final PeopleAddressScopeClosurePlanner addressScopeClosurePlanner =
            new PeopleAddressScopeClosurePlanner();
    private final PeopleDocumentScopeDiagnosticPlanner documentScopeDiagnosticPlanner =
            new PeopleDocumentScopeDiagnosticPlanner();
    private final PeopleFuncionarioScopeDiagnosticPlanner funcionarioScopeDiagnosticPlanner =
            new PeopleFuncionarioScopeDiagnosticPlanner();
    private final PeopleFuncionarioInternalSummaryContractPlanner funcionarioInternalSummaryContractPlanner =
            new PeopleFuncionarioInternalSummaryContractPlanner();
    private final PeopleFuncionarioInternalSummaryAdapterPreparationPlanner funcionarioInternalSummaryAdapterPreparationPlanner =
            new PeopleFuncionarioInternalSummaryAdapterPreparationPlanner();
    private final PeopleDocumentInternalMetadataReadContractPlanner documentInternalMetadataReadContractPlanner =
            new PeopleDocumentInternalMetadataReadContractPlanner();
    private final PeopleDocumentLocalReadCandidatePlanner documentLocalReadCandidatePlanner =
            new PeopleDocumentLocalReadCandidatePlanner();
    private final PeopleDocumentBackfillReconciliationPreparationPlanner documentBackfillReconciliationPreparationPlanner =
            new PeopleDocumentBackfillReconciliationPreparationPlanner();
    private final PeopleDocumentLocalReadActivationEligibilityPlanner documentLocalReadActivationEligibilityPlanner =
            new PeopleDocumentLocalReadActivationEligibilityPlanner();
    private final PeopleDocumentInternalUsageCandidatePlanner documentInternalUsageCandidatePlanner =
            new PeopleDocumentInternalUsageCandidatePlanner();
    private final PeopleDocumentMetadataLocalAdapterPreparationPlanner documentMetadataLocalAdapterPreparationPlanner =
            new PeopleDocumentMetadataLocalAdapterPreparationPlanner();
    private final PeopleDocumentMetadataSchemaDiagnosticPlanner documentMetadataSchemaDiagnosticPlanner =
            new PeopleDocumentMetadataSchemaDiagnosticPlanner();
    private final PeopleLocalReadModelSchemaMigrationState schemaMigrationState;
    private final PeopleLocalPersistenceOperationState operationState;

    public PeopleLocalPersistenceHealthIndicator(
            PeopleLocalPersistenceProperties properties,
            MeterRegistry meterRegistry,
            PeopleLocalReadCutoverGuard readCutoverGuard,
            PeopleCatalogReadModelSchemaPlanner catalogSchemaPlanner,
            PeopleTransactionalReadModelExpansionPlanner transactionalExpansionPlanner,
            PeopleAddressWriteAuthorityPlanner addressWriteAuthorityPlanner,
            PeopleLocalReadModelSchemaMigrationState schemaMigrationState,
            PeopleLocalPersistenceOperationState operationState) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        this.readCutoverGuard = readCutoverGuard;
        this.catalogSchemaPlanner = catalogSchemaPlanner;
        this.transactionalExpansionPlanner = transactionalExpansionPlanner;
        this.addressWriteAuthorityPlanner = addressWriteAuthorityPlanner;
        this.schemaMigrationState = schemaMigrationState;
        this.operationState = operationState;
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
        details.put("readModelFallbackEnabled", properties.readModelFallbackEnabled());
        details.put("mode", "read_only_shadow_foundation");
        details.put("authoritative", false);
        details.put("writeCutoverAllowed", false);
        details.put("readModelTables", READ_MODEL_TABLES);
        details.put("excludedAuthoritativeTables", EXCLUDED_AUTHORITATIVE_TABLES);
        details.put("shadowReadRoutes", diagnosticoRotasLeitura());
        details.put("backfillPlan", diagnosticoBackfill());
        details.put("readRoutingPlan", diagnosticoRoteamentoLeitura());
        details.put("guardedReadCutoverClosure", diagnosticoFechamentoCutoverLeitura());
        details.put("catalogReadModelSchemaPlan", diagnosticoSchemaCatalogo());
        details.put("transactionalReadModelExpansionPlan", diagnosticoExpansaoTransacional());
        details.put("nextBlockedSliceDiagnostic", diagnosticoProximaFatiaBloqueada());
        details.put("addressSchemaBackfillDiagnostic", diagnosticoSchemaBackfillEndereco());
        details.put("addressLocalReadContractDiagnostic", diagnosticoContratoLeituraLocalEndereco());
        details.put("addressLocalReadCutoverEligibilityDiagnostic", diagnosticoElegibilidadeCutoverEndereco());
        details.put("addressWriteAuthorityDiagnostic", diagnosticoAutoridadeEscritaEndereco());
        details.put("addressWriteMonolithAdapterDiagnostic", diagnosticoAdapterEscritaMonolito());
        details.put("peopleAddressScopeClosureDiagnostic", diagnosticoFechamentoEscopoPessoaEndereco());
        details.put("peopleDocumentScopeDiagnostic", diagnosticoEscopoPessoaDocumento());
        details.put("peopleDocumentInternalMetadataReadContractDiagnostic",
                diagnosticoContratoInternoLeituraMetadadosDocumento());
        details.put("peopleDocumentLocalReadCandidateDiagnostic",
                diagnosticoCandidatoLeituraLocalDocumento());
        details.put("peopleDocumentMetadataSchemaDiagnostic",
                diagnosticoSchemaMetadadosDocumento());
        details.put("peopleDocumentMetadataLocalAdapterPreparationDiagnostic",
                diagnosticoPreparacaoAdapterLocalMetadadosDocumento());
        details.put("peopleDocumentBackfillReconciliationDiagnostic",
                diagnosticoBackfillReconciliacaoDocumento());
        details.put("peopleDocumentLocalReadActivationEligibilityDiagnostic",
                diagnosticoElegibilidadeAtivacaoLeituraLocalDocumento());
        details.put("peopleDocumentInternalUsageCandidateDiagnostic",
                diagnosticoUsoInternoMinimoDocumento());
        details.put("peopleFuncionarioScopeDiagnostic", diagnosticoEscopoFuncionario());
        details.put("peopleFuncionarioInternalSummaryContractDiagnostic",
                diagnosticoContratoInternoResumoFuncionario());
        details.put("peopleFuncionarioInternalSummaryAdapterPreparationDiagnostic",
                diagnosticoPreparacaoAdapterResumoFuncionario());
        details.put("schemaMigration", schemaMigrationState.currentReport());
        details.put("localReadModelBackfill", operationState.currentReport());
        details.put("catalogBackfill", operationState.currentReport());
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
        details.put("readRoutingDecisionsTotal",
                totalContador("people.shadow.local.persistence.read.routing.decisions"));
        details.put("localCatalogReadsTotal",
                totalContador("people.shadow.local.persistence.catalog.reads"));
        details.put("localIdentityReadsTotal",
                totalContador("people.shadow.local.persistence.identity.reads"));
        details.put("localStudentResponsibleReadsTotal",
                totalContador("people.shadow.local.persistence.student.responsible.reads"));
        details.put("addressReadRoutingDecisionsTotal",
                totalContador("people.shadow.local.persistence.address.read.routing.decisions"));
        details.put("localAddressReadsTotal",
                totalContador("people.shadow.local.persistence.address.reads"));
        details.put("documentMetadataReadRoutingDecisionsTotal",
                totalContador("people.shadow.local.persistence.document.metadata.read.routing.decisions"));
        details.put("localDocumentMetadataReadsTotal",
                totalContador("people.shadow.local.persistence.document.metadata.reads"));
        details.put("localDocumentMetadataReadsTotal",
                totalContador("people.shadow.local.persistence.document.metadata.reads"));
        details.put("addressWriteShadowCommandsTotal",
                totalContador("people.shadow.local.persistence.address.write.shadow.commands"));
        details.put("monolithAddressWriteRequestsTotal",
                totalContador("people.shadow.monolith.address.write.requests"));
        details.put("monolithAddressWriteFailuresTotal",
                totalContador("people.shadow.monolith.address.write.failures"));
        details.put("schemaMigrationsTotal",
                totalContador("people.shadow.local.persistence.schema.migrations"));

        if (properties.readModelCutoverEnabled()) {
            String reason = primeiraInelegibilidadeRoteamento();
            details.put("reason", reason);
            if (!"read-model-cutover-eligible".equals(reason)) {
                return Health.outOfService().withDetails(details).build();
            }
            return Health.up().withDetails(details).build();
        }

        if (properties.enabled()) {
            details.put("reason", "local-persistence-foundation-only");
            return Health.outOfService().withDetails(details).build();
        }

        return Health.up().withDetails(details).build();
    }

    private Map<String, Object> diagnosticoFechamentoCutoverLeitura() {
        PeopleLocalReadRoutingDecision decision = readCutoverGuard.avaliar("consultarCadastro");
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("operation", "consultarCadastro");
        details.put("shadowRoute", decision.shadowRoute());
        details.put("status", decision.localReadEligible()
                ? "guarded_local_read_enabled"
                : "guarded_local_read_blocked");
        details.put("selectedSource", decision.selectedSource());
        details.put("localCandidateSource", "people_read_model_student_responsible");
        details.put("fallbackSource", "monolith_proxy");
        details.put("fallbackRequired", true);
        details.put("fallbackEnabled", decision.fallbackEnabled());
        details.put("writesEnabled", decision.writesEnabled());
        details.put("reason", decision.reason());
        details.put("greenCriteria", List.of(
                "people.shadow.local-persistence.read-model-cutover-enabled=true",
                "people.shadow.local-persistence.read-model-fallback-enabled=true",
                "people.shadow.local-persistence.enabled=true",
                "people.shadow.local-persistence.backfill-enabled=true",
                "people.shadow.local-persistence.reconciliation-enabled=true",
                "localReadModelBackfill.status=completed",
                "localReadModelBackfill.divergences=0",
                "people.shadow.local.persistence.reconciliation.divergences=0",
                "people.shadow.local.persistence.failures=0"));
        details.put("metrics", Map.of(
                "routingDecisions", "people.shadow.local.persistence.read.routing.decisions",
                "localReads", "people.shadow.local.persistence.student.responsible.reads",
                "reconciliationDivergences", "people.shadow.local.persistence.reconciliation.divergences",
                "localPersistenceFailures", "people.shadow.local.persistence.failures"));
        details.put("rollbackSteps", List.of(
                "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                "keep-people.shadow.local-persistence.read-model-fallback-enabled=true",
                "investigate-people.shadow.local.persistence.student.responsible.reads{result=fallback_error}",
                "rerun-student-responsible-backfill-and-reconciliation-before-reenable",
                "keep-consultarCadastro-on-monolith-proxy-when-guard-is-not-green"));
        details.put("nextSliceBlocked", "endereco");
        return details;
    }

    private Map<String, Object> diagnosticoSchemaCatalogo() {
        PeopleCatalogReadModelSchemaPlan plan = catalogSchemaPlanner.planejarSchemaCatalogo();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("migrationAllowedNow", plan.migrationAllowedNow());
        details.put("physicalSchemaRequiredNext", plan.physicalSchemaRequiredNext());
        details.put("localReadAdapterRequiredNext", plan.localReadAdapterRequiredNext());
        details.put("readCutoverAllowed", plan.readCutoverAllowed());
        details.put("writeCutoverAllowed", plan.writeCutoverAllowed());
        details.put("tables", plan.tables());
        details.put("excludedTables", plan.excludedTables());
        details.put("blockers", plan.blockers());
        details.put("rollbackSteps", plan.rollbackSteps());
        return details;
    }

    private Map<String, Object> diagnosticoExpansaoTransacional() {
        PeopleTransactionalReadModelExpansionPlan plan =
                transactionalExpansionPlanner.planejarProximaFatiaTransacional();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("migrationAllowedNow", plan.migrationAllowedNow());
        details.put("backfillAllowedNow", plan.backfillAllowedNow());
        details.put("localReadCutoverAllowedNow", plan.localReadCutoverAllowedNow());
        details.put("candidateTables", plan.candidateTables());
        details.put("requiredHardening", plan.requiredHardening());
        details.put("blockedTables", plan.blockedTables());
        details.put("rollbackSteps", plan.rollbackSteps());
        return details;
    }

    private Map<String, Object> diagnosticoAutoridadeEscritaEndereco() {
        PeopleAddressWriteAuthorityPlan plan = addressWriteAuthorityPlanner.planejarAutoridadeEscritaEndereco();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("writeCutoverAllowedNow", plan.writeCutoverAllowedNow());
        details.put("migrationAllowedNow", plan.migrationAllowedNow());
        details.put("backfillAllowedNow", plan.backfillAllowedNow());
        details.put("localReadPrerequisiteClosed", plan.localReadPrerequisiteClosed());
        details.put("preparedCommandArtifacts", Map.of(
                "port", "PeopleAddressWritePort",
                "writeCommand", "PessoaEnderecoWriteCommand",
                "cleanupCommand", "PessoaEnderecoCleanupCommand",
                "result", "PessoaEnderecoWriteResult",
                "shadowService", "PeopleAddressWriteShadowService",
                "adapterCreated", true,
                "routeCreated", false,
                "localPersistenceConnected", false));
        details.put("shadowCommandExecution", Map.of(
                "service", "PeopleAddressWriteShadowService",
                "metric", "people.shadow.local.persistence.address.write.shadow.commands",
                "selectedSource", "monolith_proxy",
                "persistedLocally", false,
                "fallbackRequired", true,
                "localWriteEnabled", false));
        details.put("candidateOperations", plan.candidateOperations());
        details.put("monolithWriteAuthorities", plan.monolithWriteAuthorities());
        details.put("requiredContracts", plan.requiredContracts());
        details.put("consistencyBlockers", plan.consistencyBlockers());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        return details;
    }

    private Map<String, Object> diagnosticoAdapterEscritaMonolito() {
        PeopleAddressWriteMonolithAdapterPlan plan =
                addressWriteMonolithAdapterPlanner.planejarAdapterEscritaMonolito();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("monolithHttpWriteContractAvailable", plan.monolithHttpWriteContractAvailable());
        details.put("adapterImplementationAllowedNow", plan.adapterImplementationAllowedNow());
        details.put("writeCutoverAllowedNow", plan.writeCutoverAllowedNow());
        details.put("localPersistenceAllowedNow", plan.localPersistenceAllowedNow());
        details.put("candidateOperations", plan.candidateOperations());
        details.put("requiredMonolithContracts", plan.requiredMonolithContracts());
        details.put("guardPreconditions", plan.guardPreconditions());
        details.put("consistencyBlockers", plan.consistencyBlockers());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        details.put("currentPeopleServiceState", Map.of(
                "shadowService", "PeopleAddressWriteShadowService",
                "writePort", "PeopleAddressWritePort",
                "monolithWriteClientCreated", true,
                "monolithWriteClient", "MonolithPessoaAddressWriteClient",
                "monolithWriteClientEnabledByDefault", false,
                "guardProperty", "people.shadow.monolith.address-write-adapter-enabled",
                "localPersistenceConnected", false,
                "routeCreated", false));
        return details;
    }

    private Map<String, Object> diagnosticoFechamentoEscopoPessoaEndereco() {
        PeopleAddressScopeClosurePlan plan =
                addressScopeClosurePlanner.planejarFechamentoEscopoPessoaEndereco();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("readScopeClosed", plan.readScopeClosed());
        details.put("writeScopePreparedWithoutCutover", plan.writeScopePreparedWithoutCutover());
        details.put("activationRequiredNow", plan.activationRequiredNow());
        details.put("safeToStartNextFamilyDiagnostic", plan.safeToStartNextFamilyDiagnostic());
        details.put("closedCapabilities", plan.closedCapabilities());
        details.put("remainingActivationBlockers", plan.remainingActivationBlockers());
        details.put("nextFamilyCandidates", plan.nextFamilyCandidates());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        details.put("currentRecommendation", Map.of(
                "keepAddressGuardDisabled", true,
                "keepAddressWritesOnMonolith", true,
                "nextPreferredFamily", "pessoa_documento",
                "reopenAddressInThisPhase", false));
        return details;
    }

    private Map<String, Object> diagnosticoEscopoPessoaDocumento() {
        PeopleDocumentScopeDiagnosticPlan plan =
                documentScopeDiagnosticPlanner.planejarDiagnosticoEscopoPessoaDocumento();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("diagnosticReadyNow", plan.diagnosticReadyNow());
        details.put("internalContractSeparationAllowedNow", plan.internalContractSeparationAllowedNow());
        details.put("localPersistenceAllowedNow", plan.localPersistenceAllowedNow());
        details.put("externalRouteChangeAllowedNow", plan.externalRouteChangeAllowedNow());
        details.put("fallbackToCurrentMonolithRequired", plan.fallbackToCurrentMonolithRequired());
        details.put("minimalReadCandidates", plan.minimalReadCandidates());
        details.put("minimalWriteCandidates", plan.minimalWriteCandidates());
        details.put("monolithDependencies", plan.monolithDependencies());
        details.put("consistencyImpacts", plan.consistencyImpacts());
        details.put("piiImpacts", plan.piiImpacts());
        details.put("minimalMigrationRequirements", plan.minimalMigrationRequirements());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("firstImplementationGuardrails", plan.firstImplementationGuardrails());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        details.put("currentRecommendation", Map.of(
                "preferFirstImplementation", "internal_metadata_read_only",
                "keepWritesOnMonolith", true,
                "keepCleanupOnMonolith", true,
                "prepareExternalRouteNow", false));
        return details;
    }

    private Map<String, Object> diagnosticoContratoInternoLeituraMetadadosDocumento() {
        PeopleDocumentInternalMetadataReadContractPlan plan =
                documentInternalMetadataReadContractPlanner.planejarContratoInternoDeLeituraDeMetadados();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("contractPrepared", plan.contractPrepared());
        details.put("internalServicePrepared", plan.internalServicePrepared());
        details.put("adapterCreated", plan.adapterCreated());
        details.put("localPersistenceConnected", plan.localPersistenceConnected());
        details.put("externalRouteCreated", plan.externalRouteCreated());
        details.put("bffFrontendChangeAllowedNow", plan.bffFrontendChangeAllowedNow());
        details.put("writeCutoverAllowedNow", plan.writeCutoverAllowedNow());
        details.put("candidateSource", plan.candidateSource());
        details.put("fallbackSource", plan.fallbackSource());
        details.put("fallbackRequired", plan.fallbackRequired());
        details.put("minimalInternalPayload", plan.minimalInternalPayload());
        details.put("firstConsumers", plan.firstConsumers());
        details.put("guardrails", plan.guardrails());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        details.put("preparedArtifacts", Map.of(
                "port", "PeopleDocumentMetadataLocalReadPort",
                "response", "PessoaDocumentoMetadataLocalReadResponse",
                "internalService", "PeopleDocumentMetadataLocalReadService",
                "adapterCreated", false,
                "routeCreated", false,
                "localPersistenceConnected", false));
        return details;
    }

    private Map<String, Object> diagnosticoCandidatoLeituraLocalDocumento() {
        PeopleDocumentLocalReadCandidatePlan plan =
                documentLocalReadCandidatePlanner.planejarCandidatoDeLeituraLocalDeDocumento();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("schemaDiagnosticAllowedNow", plan.schemaDiagnosticAllowedNow());
        details.put("adapterDiagnosticAllowedNow", plan.adapterDiagnosticAllowedNow());
        details.put("continueWithDocumentFamilyNow", plan.continueWithDocumentFamilyNow());
        details.put("switchToFuncionarioNow", plan.switchToFuncionarioNow());
        details.put("localReadCutoverAllowedNow", plan.localReadCutoverAllowedNow());
        details.put("sourceTables", plan.sourceTables());
        details.put("minimalCandidateColumns", plan.minimalCandidateColumns());
        details.put("reconciliationKey", plan.reconciliationKey());
        details.put("secondaryReconciliationChecks", plan.secondaryReconciliationChecks());
        details.put("ownershipRules", plan.ownershipRules());
        details.put("consistencyBlockers", plan.consistencyBlockers());
        details.put("migrationPrerequisites", plan.migrationPrerequisites());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        details.put("currentDecision", Map.of(
                "preferredNextFamily", "pessoa_documento",
                "continueWithSchemaDiagnostic", true,
                "switchToFuncionarioAfterThisDiagnostic", false,
                "prepareRouteNow", false));
        return details;
    }

    private Map<String, Object> diagnosticoSchemaMetadadosDocumento() {
        PeopleDocumentMetadataSchemaDiagnosticPlan plan =
                documentMetadataSchemaDiagnosticPlanner.planejarSchemaMinimoDeMetadados();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("migrationAllowedNow", plan.migrationAllowedNow());
        details.put("backfillAllowedNow", plan.backfillAllowedNow());
        details.put("localReadAdapterAllowedNow", plan.localReadAdapterAllowedNow());
        details.put("localReadAdapterPrepared", plan.localReadAdapterPrepared());
        details.put("localReadCutoverAllowedNow", plan.localReadCutoverAllowedNow());
        details.put("reconciliationKey", plan.reconciliationKey());
        details.put("nextImplementationSlice", plan.nextImplementationSlice());
        details.put("minimalColumns", plan.minimalColumns());
        details.put("schemaMigration", plan.schemaMigration());
        details.put("backfillReconciliation", plan.backfillReconciliation());
        details.put("preparedArtifacts", plan.preparedArtifacts());
        details.put("ownershipRule", plan.ownershipRule());
        details.put("caminhoArquivoPolicy", plan.caminhoArquivoPolicy());
        details.put("secondaryReconciliationChecks", plan.secondaryReconciliationChecks());
        details.put("blockersBeforeAdapter", plan.blockersBeforeAdapter());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        return details;
    }

    private Map<String, Object> diagnosticoPreparacaoAdapterLocalMetadadosDocumento() {
        PeopleDocumentMetadataLocalAdapterPreparationPlan plan =
                documentMetadataLocalAdapterPreparationPlanner.planejarPreparacaoDoAdapterLocal();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("adapterImplementationAllowedNow", plan.adapterImplementationAllowedNow());
        details.put("adapterPrepared", plan.adapterPrepared());
        details.put("internalServiceConnected", plan.internalServiceConnected());
        details.put("externalRouteCreated", plan.externalRouteCreated());
        details.put("localReadCutoverAllowedNow", plan.localReadCutoverAllowedNow());
        details.put("candidateSource", plan.candidateSource());
        details.put("fallbackSource", plan.fallbackSource());
        details.put("routingOperation", plan.routingOperation());
        details.put("schemaVersion", plan.schemaVersion());
        details.put("preparedArtifacts", plan.preparedArtifacts());
        details.put("blockerBeforeActivation", plan.blockerBeforeActivation());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        return details;
    }

    private Map<String, Object> diagnosticoBackfillReconciliacaoDocumento() {
        PeopleDocumentBackfillReconciliationPreparationPlan plan =
                documentBackfillReconciliationPreparationPlanner.planejarBackfillReconciliacao();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("migrationAllowedNow", plan.migrationAllowedNow());
        details.put("backfillAllowedNow", plan.backfillAllowedNow());
        details.put("reconciliationAllowedNow", plan.reconciliationAllowedNow());
        details.put("localReadCutoverAllowedNow", plan.localReadCutoverAllowedNow());
        details.put("source", plan.source());
        details.put("target", plan.target());
        details.put("reconciliationKey", plan.reconciliationKey());
        details.put("sourceTables", plan.sourceTables());
        details.put("consistencyBlockers", plan.consistencyBlockers());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        return details;
    }

    private Map<String, Object> diagnosticoElegibilidadeAtivacaoLeituraLocalDocumento() {
        PeopleDocumentLocalReadActivationEligibilityPlan plan =
                documentLocalReadActivationEligibilityPlanner.planejarElegibilidadeDeAtivacao();
        PeopleLocalReadRoutingDecision decision = readCutoverGuard.avaliarLeituraDocumentoMetadata();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("internalServiceConnected", plan.internalServiceConnected());
        details.put("localReadGuardPrepared", plan.localReadGuardPrepared());
        details.put("localReadCutoverAllowedNow", decision.localReadEligible());
        details.put("externalRouteCreated", plan.externalRouteCreated());
        details.put("fallbackRequired", plan.fallbackRequired());
        details.put("routingOperation", decision.operation());
        details.put("shadowRoute", decision.shadowRoute());
        details.put("candidateSource", decision.candidateSource());
        details.put("selectedSource", decision.selectedSource());
        details.put("fallbackSource", plan.fallbackSource());
        details.put("reason", decision.reason());
        details.put("guardPreconditions", plan.guardPreconditions());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        return details;
    }

    private Map<String, Object> diagnosticoUsoInternoMinimoDocumento() {
        PeopleDocumentInternalUsageCandidatePlan plan =
                documentInternalUsageCandidatePlanner.planejarUsoInternoMinimo();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("internalUsageCandidateFound", plan.internalUsageCandidateFound());
        details.put("safeToConnectNow", plan.safeToConnectNow());
        details.put("externalRouteChangeRequired", plan.externalRouteChangeRequired());
        details.put("fallbackRequired", plan.fallbackRequired());
        details.put("currentBlockers", plan.currentBlockers());
        details.put("preservedBoundaries", plan.preservedBoundaries());
        details.put("rollbackSteps", plan.rollbackSteps());
        return details;
    }

    private Map<String, Object> diagnosticoEscopoFuncionario() {
        PeopleFuncionarioScopeDiagnosticPlan plan =
                funcionarioScopeDiagnosticPlanner.planejarDiagnosticoEscopoFuncionario();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("diagnosticReadyNow", plan.diagnosticReadyNow());
        details.put("internalContractSeparationAllowedNow", plan.internalContractSeparationAllowedNow());
        details.put("localPersistenceAllowedNow", plan.localPersistenceAllowedNow());
        details.put("externalRouteChangeAllowedNow", plan.externalRouteChangeAllowedNow());
        details.put("fallbackToCurrentMonolithRequired", plan.fallbackToCurrentMonolithRequired());
        details.put("minimalReadCandidates", plan.minimalReadCandidates());
        details.put("minimalWriteCandidates", plan.minimalWriteCandidates());
        details.put("monolithDependencies", plan.monolithDependencies());
        details.put("consistencyImpacts", plan.consistencyImpacts());
        details.put("minimalMigrationRequirements", plan.minimalMigrationRequirements());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("firstImplementationGuardrails", plan.firstImplementationGuardrails());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        details.put("currentRecommendation", Map.of(
                "preferFirstImplementation", "funcionario_internal_summary_read_only",
                "keepProfessorAndAuthOnMonolith", true,
                "prepareExternalRouteNow", false,
                "advanceToPersistenceNow", false));
        return details;
    }

    private Map<String, Object> diagnosticoContratoInternoResumoFuncionario() {
        PeopleFuncionarioInternalSummaryContractPlan plan =
                funcionarioInternalSummaryContractPlanner.planejarContratoInternoResumoFuncionario();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("contractPrepared", plan.contractPrepared());
        details.put("internalServicePrepared", plan.internalServicePrepared());
        details.put("adapterCreated", plan.adapterCreated());
        details.put("localPersistenceConnected", plan.localPersistenceConnected());
        details.put("externalRouteCreated", plan.externalRouteCreated());
        details.put("bffFrontendChangeAllowedNow", plan.bffFrontendChangeAllowedNow());
        details.put("writeCutoverAllowedNow", plan.writeCutoverAllowedNow());
        details.put("candidateSource", plan.candidateSource());
        details.put("fallbackSource", plan.fallbackSource());
        details.put("fallbackRequired", plan.fallbackRequired());
        details.put("minimalInternalPayload", plan.minimalInternalPayload());
        details.put("firstConsumers", plan.firstConsumers());
        details.put("guardrails", plan.guardrails());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        details.put("preparedArtifacts", Map.of(
                "port", "PeopleFuncionarioInternalSummaryPort",
                "response", "PessoaFuncionarioInternalSummaryResponse",
                "internalService", "PeopleFuncionarioInternalSummaryService",
                "adapterCreated", false,
                "routeCreated", false,
                "localPersistenceConnected", false));
        return details;
    }

    private Map<String, Object> diagnosticoPreparacaoAdapterResumoFuncionario() {
        PeopleFuncionarioInternalSummaryAdapterPreparationPlan plan =
                funcionarioInternalSummaryAdapterPreparationPlanner.planejarPreparacaoDoAdapterLocal();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("adapterImplementationAllowedNow", plan.adapterImplementationAllowedNow());
        details.put("adapterPrepared", plan.adapterPrepared());
        details.put("internalServiceConnected", plan.internalServiceConnected());
        details.put("externalRouteCreated", plan.externalRouteCreated());
        details.put("localReadCutoverAllowedNow", plan.localReadCutoverAllowedNow());
        details.put("candidateSource", plan.candidateSource());
        details.put("fallbackSource", plan.fallbackSource());
        details.put("routingOperation", plan.routingOperation());
        details.put("schemaVersion", plan.schemaVersion());
        details.put("preparedArtifacts", plan.preparedArtifacts());
        details.put("blockerBeforeActivation", plan.blockerBeforeActivation());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        return details;
    }

    private Map<String, Object> diagnosticoProximaFatiaBloqueada() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("slice", "endereco");
        details.put("status", "address_local_read_internal_guard_connection_prepared_no_route");
        details.put("implementationAllowedNow", true);
        details.put("schemaAllowedNow", true);
        details.put("backfillAllowedNow", true);
        details.put("localReadCutoverAllowedNow", false);
        details.put("dependsOnClosedSlice", "consultarCadastro");
        details.put("tables", List.of("endereco", "pessoa_endereco"));
        details.put("firstSafeImplementationSlice", "address_adapter_connected_internal_guard_no_external_route");
        details.put("requiredContractDecisions", List.of(
                "define-internal-address-read-payload-before-adapter",
                "keep-address-read-independent-from-consultarCadastro-until-contract-is-explicit",
                "separate-external-cep-lookup-from-persisted-address-data",
                "define-person-address-principal-selection-and-multiple-address-behavior",
                "keep-read-cutover-blocked-until-address-reconciliation-is-green"));
        details.put("preparedInternalContract", Map.of(
                "port", "PeopleAddressLocalReadPort",
                "response", "PessoaEnderecoLocalReadResponse",
                "adapter", "JdbcPeopleAddressLocalReadAdapter",
                "internalService", "PeopleAddressLocalReadService",
                "routingOperation", "addressLocalRead",
                "operations", List.of(
                        "buscarEnderecoPrincipalPorPessoa",
                        "listarEnderecosPorPessoa"),
                "jpaEntityExposure", false));
        details.put("writeConsumers", List.of(
                "PessoaFoundationService.criarPessoaComTipoEEndereco",
                "PessoaFoundationService.atualizarPessoaEEndereco",
                "CriarAlunoUseCase",
                "AtualizarAlunoUseCase",
                "CriarResponsavelUseCase",
                "AtualizarResponsavelUseCase"));
        details.put("cleanupConsumers", List.of(
                "AlunoPersistenceGateway.removeById",
                "ResponsavelPersistenceGateway.removeById",
                "PessoaEnderecoJpaRepository.deleteByPessoaId",
                "PessoaEnderecoJpaRepository.countByEnderecoId"));
        details.put("cepLookupConsumers", List.of(
                "EnderecoCepController.GET /enderecos/cep/{cep}",
                "ViaCepService.consultar",
                "TransferenciaAlunoService.preencherEnderecoComViaCep"));
        details.put("monolithDependencies", List.of(
                "compartilhado.endereco.EnderecoEntity",
                "compartilhado.endereco.PessoaEnderecoEntity",
                "compartilhado.endereco.TipoEnderecoEntity",
                "compartilhado.pessoa.service.PessoaFoundationService",
                "aluno-responsavel-create-update-use-cases",
                "via-cep-lookup"));
        details.put("consistencyRisks", List.of(
                "address-is-written-together-with-student-or-responsible-person",
                "same-address-can-be-linked-or-cleaned-up-by-person-gateways",
                "principal-address-is-a-business-rule-not-just-a-table-copy",
                "cep-lookup-is-external-and-must-not-become-local-read-model-authority",
                "current-consultarCadastro-response-does-not-expose-address-fields"));
        details.put("greenCriteriaBeforeSchema", List.of(
                "internal-address-contract-defined-without-jpa-entities",
                "principal-address-rule-explicit",
                "orphan-address-cleanup-strategy-defined",
                "cep-lookup-kept-as-external-adapter",
                "no-current-external-route-depends-on-address-local-read"));
        details.put("rollbackSteps", List.of(
                "disable-people.shadow.local-persistence.migration-enabled",
                "disable-people.shadow.local-persistence.backfill-enabled",
                "disable-people.shadow.local-persistence.reconciliation-enabled",
                "keep-endereco-on-monolith-proxy",
                "keep-consultarCadastro-guarded-cutover-independent-from-address",
                "disable-people.shadow.local-persistence.enabled-if-address-diagnostic-finds-write-risk"));
        return details;
    }

    private Map<String, Object> diagnosticoElegibilidadeCutoverEndereco() {
        Map<String, Object> details = new LinkedHashMap<>();
        PeopleLocalReadRoutingDecision decision = readCutoverGuard.avaliarLeituraEndereco();
        details.put("slice", "endereco_read_cutover_eligibility");
        details.put("phase", "Fase 70");
        details.put("status", "adapter_connected_to_internal_guard_no_route");
        details.put("routingOperation", decision.operation());
        details.put("shadowRoute", decision.shadowRoute());
        details.put("candidateSource", decision.candidateSource());
        details.put("selectedSource", decision.selectedSource());
        details.put("localReadRequested", decision.localReadRequested());
        details.put("localReadEligible", decision.localReadEligible());
        details.put("reason", decision.reason());
        details.put("localReadCutoverAllowedNow", decision.localReadEligible());
        details.put("adapterPrepared", true);
        details.put("internalGuardedServiceConnected", true);
        details.put("queryServiceConnected", false);
        details.put("routeCreated", false);
        details.put("bffFrontendChangeAllowedNow", false);
        details.put("writeCutoverAllowedNow", false);
        details.put("candidateSource", "people_read_model_address");
        details.put("fallbackSource", "monolith_proxy");
        details.put("fallbackRequired", true);
        details.put("candidateOperations", List.of(
                "buscarEnderecoPrincipalPorPessoa",
                "listarEnderecosPorPessoa"));
        details.put("minimumGuardCriteria", List.of(
                "people.shadow.local-persistence.enabled=true",
                "people.shadow.local-persistence.migration-enabled=true",
                "people.shadow.local-persistence.backfill-enabled=true",
                "people.shadow.local-persistence.reconciliation-enabled=true",
                "people.shadow.local-persistence.read-model-fallback-enabled=true",
                "localReadModelBackfill.status=completed",
                "localReadModelBackfill.divergences=0",
                "people.shadow.local.persistence.reconciliation.divergences=0",
                "people.shadow.local.persistence.failures=0",
                "address-reconciliation-has-no-multiple-principal-addresses",
                "address-reconciliation-has-no-normalized-field-divergence",
                "address-reconciliation-has-no-missing-person-or-address-reference"));
        details.put("blockersBeforeExternalExposure", List.of(
                "no-address-external-route-contract-selected",
                "consultarCadastro-current-payload-does-not-expose-address",
                "address-write-authority-remains-on-monolith"));
        details.put("metrics", Map.of(
                "routingDecisions", "people.shadow.local.persistence.read.routing.decisions{operation=addressLocalRead}",
                "addressRoutingDecisions", "people.shadow.local.persistence.address.read.routing.decisions",
                "localReads", "people.shadow.local.persistence.address.reads",
                "reconciliationDivergences", "people.shadow.local.persistence.reconciliation.divergences",
                "localPersistenceFailures", "people.shadow.local.persistence.failures"));
        details.put("explicitlyOutOfScope", List.of(
                "create-new-address-rest-route",
                "change-consultarCadastro-payload",
                "bff-route-change",
                "frontend-change",
                "address-write-cutover"));
        details.put("rollbackSteps", List.of(
                "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                "keep-people.shadow.local-persistence.read-model-fallback-enabled=true",
                "keep-address-read-on-monolith-proxy",
                "disconnect-address-adapter-from-query-service-if-added-in-future-phase",
                "rerun-address-backfill-and-reconciliation-before-reactivation"));
        details.put("recommendedNextStep", "close_phase_70_and_plan_address_write_authority_diagnostic");
        return details;
    }

    private Map<String, Object> diagnosticoSchemaBackfillEndereco() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("slice", "endereco_pessoa_endereco");
        details.put("status", "backfill_reconciliation_prepared_read_cutover_still_blocked");
        details.put("migrationAllowedNow", true);
        details.put("backfillAllowedNow", true);
        details.put("localReadCutoverAllowedNow", false);
        details.put("schemaMigration", Map.of(
                "version", "V4__create_people_address_read_model.sql",
                "enabledByDefault", false,
                "optInFlag", "people.shadow.local-persistence.migration-enabled",
                "automaticBackfill", false));
        details.put("backfillReconciliation", Map.of(
                "enabledByDefault", false,
                "optInFlags", List.of(
                        "people.shadow.local-persistence.backfill-enabled",
                        "people.shadow.local-persistence.reconciliation-enabled"),
                "source", "monolith_jdbc",
                "target", "people_read_model_address",
                "idempotencyKeys", List.of("endereco.id_endereco", "pessoa_endereco.id_pessoa_endereco"),
                "greenBlockers", List.of(
                        "multiple-principal-addresses-per-person",
                        "normalized-address-field-divergence",
                        "missing-person-or-address-reference")));
        details.put("candidateTables", List.of("endereco", "pessoa_endereco"));
        details.put("referenceTables", List.of("pessoa", "tipo_endereco"));
        details.put("minimalColumns", Map.of(
                "endereco", List.of(
                        "id_endereco",
                        "cep",
                        "logradouro",
                        "numero",
                        "complemento",
                        "bairro",
                        "cidade",
                        "uf",
                        "created_at",
                        "updated_at"),
                "pessoa_endereco", List.of(
                        "id_pessoa_endereco",
                        "id_pessoa",
                        "id_endereco",
                        "id_tipo_endereco",
                        "principal",
                        "created_at")));
        details.put("reconciliationKey", "pessoa_endereco.id_pessoa_endereco");
        details.put("secondaryReconciliationChecks", List.of(
                "id_pessoa",
                "id_endereco",
                "id_tipo_endereco",
                "principal",
                "normalized_cep_logradouro_numero_bairro_cidade_uf"));
        details.put("principalAddressRule",
                "principal=true is the only address exposed by the internal contract; multiple principal records block green reconciliation");
        details.put("cepLookupPolicy",
                "ViaCEP remains an external lookup adapter and is not local read-model authority");
        details.put("rollbackSteps", List.of(
                "disable-people.shadow.local-persistence.migration-enabled",
                "disable-people.shadow.local-persistence.backfill-enabled",
                "disable-people.shadow.local-persistence.reconciliation-enabled",
                "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                "keep-endereco-on-monolith-proxy",
                "keep-consultarCadastro-independent-from-address-local-read"));
        details.put("nextImplementationSlice", "phase_68_closure_no_cutover");
        details.put("explicitlyOutOfScope", List.of(
                "address-local-read-cutover",
                "address-write-cutover",
                "bff-or-frontend-route-change"));
        return details;
    }

    private Map<String, Object> diagnosticoContratoLeituraLocalEndereco() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("slice", "endereco_local_read_contract");
        details.put("status", "adapter_connected_to_internal_guard_no_route");
        details.put("phase", "Fase 69");
        details.put("contractAllowedNow", true);
        details.put("localReadAdapterAllowedNow", false);
        details.put("localReadAdapterPrepared", true);
        details.put("localReadAdapterConnected", true);
        details.put("localReadCutoverAllowedNow", readCutoverGuard.avaliarLeituraEndereco().localReadEligible());
        details.put("externalRouteChangeAllowedNow", false);
        details.put("bffFrontendChangeAllowedNow", false);
        details.put("writeCutoverAllowedNow", false);
        details.put("candidateSource", "people_read_model_address");
        details.put("fallbackSource", "monolith_proxy");
        details.put("fallbackRequired", true);
        details.put("candidateOperations", List.of(
                "buscarEnderecoPrincipalPorPessoa",
                "listarEnderecosPorPessoa"));
        details.put("preparedArtifacts", Map.of(
                "port", "PeopleAddressLocalReadPort",
                "response", "PessoaEnderecoLocalReadResponse",
                "adapter", "JdbcPeopleAddressLocalReadAdapter",
                "internalService", "PeopleAddressLocalReadService",
                "routeCreated", false,
                "adapterCreated", true,
                "queryServiceConnected", false));
        details.put("minimalInternalPayload", List.of(
                "id_pessoa_endereco",
                "id_pessoa",
                "id_endereco",
                "id_tipo_endereco",
                "tipo_endereco_codigo",
                "tipo_endereco_descricao",
                "principal",
                "cep",
                "logradouro",
                "numero",
                "complemento",
                "bairro",
                "cidade",
                "uf"));
        details.put("guardPreconditions", List.of(
                "people.shadow.local-persistence.enabled=true",
                "people.shadow.local-persistence.migration-enabled=true",
                "people.shadow.local-persistence.backfill-enabled=true",
                "people.shadow.local-persistence.reconciliation-enabled=true",
                "people.shadow.local-persistence.read-model-fallback-enabled=true",
                "localReadModelBackfill.status=completed",
                "localReadModelBackfill.divergences=0",
                "address-reconciliation-has-no-multiple-principal-addresses",
                "address-reconciliation-has-no-normalized-field-divergence",
                "address-reconciliation-has-no-missing-person-or-address-reference"));
        details.put("consistencyRules", List.of(
                "only-principal-address-can-feed-single-address-contract",
                "multiple-principal-addresses-per-person-block-local-read",
                "cep-lookup-is-not-authority-for-persisted-address-read",
                "address-cleanup-and-orphan-removal-remain-on-monolith-until-write-authority"));
        details.put("outOfScope", List.of(
                "new-internal-rest-route",
                "bff-route-change",
                "frontend-change",
                "consultarCadastro-payload-change",
                "address-write-cutover",
                "using-local-address-read-without-fallback"));
        details.put("rollbackSteps", List.of(
                "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                "keep-people.shadow.local-persistence.read-model-fallback-enabled=true",
                "keep-address-read-on-monolith-proxy",
                "rerun-address-backfill-and-reconciliation-before-any-adapter-activation",
                "block-adapter-activation-when-address-principal-rule-is-violated"));
        details.put("nextImplementationSlice", "address_adapter_connected_internal_guard_no_external_route");
        return details;
    }

    private Map<String, Object> diagnosticoRoteamentoLeitura() {
        Map<String, Object> plan = new LinkedHashMap<>();
        readCutoverGuard.avaliarTodas().forEach((operation, decision) -> {
            Map<String, Object> detalhe = new LinkedHashMap<>();
            detalhe.put("shadowRoute", decision.shadowRoute());
            detalhe.put("candidateSource", decision.candidateSource());
            detalhe.put("selectedSource", decision.selectedSource());
            detalhe.put("localReadRequested", decision.localReadRequested());
            detalhe.put("localReadEligible", decision.localReadEligible());
            detalhe.put("fallbackEnabled", decision.fallbackEnabled());
            detalhe.put("writesEnabled", decision.writesEnabled());
            detalhe.put("reason", decision.reason());
            plan.put(operation, detalhe);
        });
        return plan;
    }

    private String primeiraInelegibilidadeRoteamento() {
        return readCutoverGuard.avaliarTodas().values().stream()
                .filter(decision -> !decision.localReadEligible())
                .map(PeopleLocalReadRoutingDecision::reason)
                .findFirst()
                .orElse("read-model-cutover-eligible");
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
            PeopleLocalReadRoutingDecision decision = readCutoverGuard.avaliar(route.operation());
            detalhe.put("shadowRoute", route.shadowRoute());
            detalhe.put("candidateSource", route.candidateSource());
            detalhe.put("currentSource", decision.selectedSource());
            detalhe.put("localReadEnabled", decision.localReadEligible());
            detalhe.put("fallbackRequired", decision.fallbackEnabled());
            detalhe.put("reason", decision.reason());
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
