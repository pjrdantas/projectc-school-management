package br.com.escola.peopleservice.infra.observability;

import java.util.LinkedHashMap;
import java.util.Map;

import br.com.escola.peopleservice.application.dto.PeopleDocumentAlunoConsumerConnectionStrategyPlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentAlunoConsumerContractPlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentBackfillReconciliationPreparationPlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentInternalMetadataReadContractPlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentInternalUsageCandidatePlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentLocalReadActivationEligibilityPlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentLocalReadCandidatePlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentMetadataLocalAdapterPreparationPlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentMetadataSchemaDiagnosticPlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentResponsavelConsumerConnectionStrategyPlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentResponsavelConsumerContractPlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentScopeClosurePlan;
import br.com.escola.peopleservice.application.dto.PeopleDocumentScopeDiagnosticPlan;
import br.com.escola.peopleservice.application.dto.PeopleLocalReadRoutingDecision;
import br.com.escola.peopleservice.application.service.PeopleDocumentAlunoConsumerConnectionStrategyPlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentAlunoConsumerContractPlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentBackfillReconciliationPreparationPlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentInternalMetadataReadContractPlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentInternalUsageCandidatePlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentLocalReadActivationEligibilityPlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentLocalReadCandidatePlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentMetadataLocalAdapterPreparationPlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentMetadataSchemaDiagnosticPlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentResponsavelConsumerConnectionStrategyPlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentResponsavelConsumerContractPlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentScopeClosurePlanner;
import br.com.escola.peopleservice.application.service.PeopleDocumentScopeDiagnosticPlanner;
import br.com.escola.peopleservice.application.service.PeopleLocalReadCutoverGuard;

final class PeopleLocalPersistenceDocumentDiagnostics {

    private final PeopleDocumentAlunoConsumerContractPlanner documentAlunoConsumerContractPlanner =
            new PeopleDocumentAlunoConsumerContractPlanner();
    private final PeopleDocumentAlunoConsumerConnectionStrategyPlanner documentAlunoConsumerConnectionStrategyPlanner =
            new PeopleDocumentAlunoConsumerConnectionStrategyPlanner();
    private final PeopleDocumentResponsavelConsumerContractPlanner documentResponsavelConsumerContractPlanner =
            new PeopleDocumentResponsavelConsumerContractPlanner();
    private final PeopleDocumentResponsavelConsumerConnectionStrategyPlanner documentResponsavelConsumerConnectionStrategyPlanner =
            new PeopleDocumentResponsavelConsumerConnectionStrategyPlanner();
    private final PeopleDocumentScopeClosurePlanner documentScopeClosurePlanner =
            new PeopleDocumentScopeClosurePlanner();
    private final PeopleDocumentScopeDiagnosticPlanner documentScopeDiagnosticPlanner =
            new PeopleDocumentScopeDiagnosticPlanner();
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

    private final PeopleLocalReadCutoverGuard readCutoverGuard;

    PeopleLocalPersistenceDocumentDiagnostics(PeopleLocalReadCutoverGuard readCutoverGuard) {
        this.readCutoverGuard = readCutoverGuard;
    }

    Map<String, Object> diagnosticoEscopoPessoaDocumento() {
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

    Map<String, Object> diagnosticoContratoInternoLeituraMetadadosDocumento() {
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

    Map<String, Object> diagnosticoCandidatoLeituraLocalDocumento() {
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

    Map<String, Object> diagnosticoSchemaMetadadosDocumento() {
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

    Map<String, Object> diagnosticoPreparacaoAdapterLocalMetadadosDocumento() {
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

    Map<String, Object> diagnosticoBackfillReconciliacaoDocumento() {
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

    Map<String, Object> diagnosticoElegibilidadeAtivacaoLeituraLocalDocumento() {
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

    Map<String, Object> diagnosticoUsoInternoMinimoDocumento() {
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

    Map<String, Object> diagnosticoFechamentoEscopoPessoaDocumento() {
        PeopleDocumentScopeClosurePlan plan =
                documentScopeClosurePlanner.planejarFechamentoEscopoPessoaDocumento();
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
                "keepDocumentGuardPreparedButUnused", true,
                "keepDocumentAuthorityOnMonolith", true,
                "nextPreferredFamily", "next_backend_family",
                "reopenPessoaDocumentoInThisPhase", false));
        return details;
    }

    Map<String, Object> diagnosticoContratoConsumidorAlunoDocumento() {
        PeopleDocumentAlunoConsumerContractPlan plan =
                documentAlunoConsumerContractPlanner.planejarContratoDoConsumidorAluno();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("firstFutureConsumer", plan.firstFutureConsumer());
        details.put("consumerOperation", plan.consumerOperation());
        details.put("candidateSource", plan.candidateSource());
        details.put("fallbackSource", plan.fallbackSource());
        details.put("contractPrepared", plan.contractPrepared());
        details.put("localReadServiceReusable", plan.localReadServiceReusable());
        details.put("safeToConnectNow", plan.safeToConnectNow());
        details.put("routeChangeRequiredNow", plan.routeChangeRequiredNow());
        details.put("monolithChangeRequiredNow", plan.monolithChangeRequiredNow());
        details.put("fallbackRequired", plan.fallbackRequired());
        details.put("consumerInputKeys", plan.consumerInputKeys());
        details.put("consumerOutputExpectations", plan.consumerOutputExpectations());
        details.put("preservedBoundaries", plan.preservedBoundaries());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        details.put("preparedConsumerArtifacts", Map.of(
                "port", "PeopleDocumentMetadataLocalReadPort",
                "response", "PessoaDocumentoMetadataLocalReadResponse",
                "internalService", "PeopleDocumentMetadataLocalReadService",
                "firstFutureConsumer", "documento_aluno_listar_por_aluno",
                "routeCreated", false,
                "legacyChangeRequiredNow", false));
        details.put("currentRecommendation", Map.of(
                "keepConnectionInsidePeopleServicePlanning", true,
                "keepLegacyUntouched", true,
                "nextPreferredFamily", "people_document_aluno_consumer_connection_strategy",
                "connectConsumerNow", false));
        return details;
    }

    Map<String, Object> diagnosticoEstrategiaConexaoConsumidorAlunoDocumento() {
        PeopleDocumentAlunoConsumerConnectionStrategyPlan plan =
                documentAlunoConsumerConnectionStrategyPlanner.planejarEstrategiaDeConexao();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("alunoPessoaLookupContractPrepared", plan.alunoPessoaLookupContractPrepared());
        details.put("alunoPessoaLookupAdapterPrepared", plan.alunoPessoaLookupAdapterPrepared());
        details.put("localResolutionReadyForConnection", plan.localResolutionReadyForConnection());
        details.put("realConsumerConnected", plan.realConsumerConnected());
        details.put("routeChangeRequiredNow", plan.routeChangeRequiredNow());
        details.put("legacyChangeRequiredNow", plan.legacyChangeRequiredNow());
        details.put("fallbackRequired", plan.fallbackRequired());
        details.put("preparedArtifacts", plan.preparedArtifacts());
        details.put("resolutionFlow", plan.resolutionFlow());
        details.put("guardrails", plan.guardrails());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        details.put("preparedLookupArtifacts", Map.of(
                "port", "PeopleStudentPessoaLocalReadPort",
                "response", "PessoaAlunoVinculoResponse",
                "internalService", "PeopleStudentPessoaLocalReadService",
                "adapter", "JdbcPeopleStudentPessoaLocalReadAdapter",
                "routeCreated", false,
                "legacyChangeRequiredNow", false));
        details.put("currentRecommendation", Map.of(
                "closeCurrentMacroPhaseAfterThisStep", true,
                "keepConsumerUnconnected", true,
                "nextPreferredFamily", "people_document_aluno_consumer_connection_closure",
                "realFlowConnectionAllowedNow", false));
        return details;
    }

    Map<String, Object> diagnosticoContratoConsumidorResponsavelDocumento() {
        PeopleDocumentResponsavelConsumerContractPlan plan =
                documentResponsavelConsumerContractPlanner.planejarContratoDoConsumidorResponsavel();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("firstFutureConsumer", plan.firstFutureConsumer());
        details.put("consumerOperation", plan.consumerOperation());
        details.put("candidateSource", plan.candidateSource());
        details.put("fallbackSource", plan.fallbackSource());
        details.put("contractPrepared", plan.contractPrepared());
        details.put("localReadServiceReusable", plan.localReadServiceReusable());
        details.put("safeToConnectNow", plan.safeToConnectNow());
        details.put("routeChangeRequiredNow", plan.routeChangeRequiredNow());
        details.put("legacyChangeRequiredNow", plan.legacyChangeRequiredNow());
        details.put("fallbackRequired", plan.fallbackRequired());
        details.put("consumerInputKeys", plan.consumerInputKeys());
        details.put("consumerOutputExpectations", plan.consumerOutputExpectations());
        details.put("preservedBoundaries", plan.preservedBoundaries());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        details.put("preparedConsumerArtifacts", Map.of(
                "port", "PeopleDocumentMetadataLocalReadPort",
                "response", "PessoaDocumentoMetadataLocalReadResponse",
                "internalService", "PeopleDocumentMetadataLocalReadService",
                "firstFutureConsumer", "documento_responsavel_listar_por_responsavel",
                "routeCreated", false,
                "legacyChangeRequiredNow", false));
        details.put("currentRecommendation", Map.of(
                "keepConnectionInsidePeopleServicePlanning", true,
                "keepLegacyUntouched", true,
                "nextPreferredFamily", "people_document_responsavel_consumer_connection_strategy",
                "connectConsumerNow", false));
        return details;
    }

    Map<String, Object> diagnosticoEstrategiaConexaoConsumidorResponsavelDocumento() {
        PeopleDocumentResponsavelConsumerConnectionStrategyPlan plan =
                documentResponsavelConsumerConnectionStrategyPlanner.planejarEstrategiaDeConexao();
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("phase", plan.phase());
        details.put("slice", plan.slice());
        details.put("status", plan.status());
        details.put("recommendedNextStep", plan.recommendedNextStep());
        details.put("minimalNextSlice", plan.minimalNextSlice());
        details.put("responsavelPessoaLookupContractPrepared", plan.responsavelPessoaLookupContractPrepared());
        details.put("responsavelPessoaLookupAdapterPrepared", plan.responsavelPessoaLookupAdapterPrepared());
        details.put("localResolutionReadyForConnection", plan.localResolutionReadyForConnection());
        details.put("realConsumerConnected", plan.realConsumerConnected());
        details.put("routeChangeRequiredNow", plan.routeChangeRequiredNow());
        details.put("legacyChangeRequiredNow", plan.legacyChangeRequiredNow());
        details.put("fallbackRequired", plan.fallbackRequired());
        details.put("preparedArtifacts", plan.preparedArtifacts());
        details.put("resolutionFlow", plan.resolutionFlow());
        details.put("guardrails", plan.guardrails());
        details.put("rollbackSteps", plan.rollbackSteps());
        details.put("explicitlyOutOfScope", plan.explicitlyOutOfScope());
        details.put("preparedLookupArtifacts", Map.of(
                "port", "PeopleResponsiblePessoaLocalReadPort",
                "response", "PessoaResponsavelVinculoResponse",
                "internalService", "PeopleResponsiblePessoaLocalReadService",
                "adapter", "JdbcPeopleResponsiblePessoaLocalReadAdapter",
                "routeCreated", false,
                "legacyChangeRequiredNow", false));
        details.put("currentRecommendation", Map.of(
                "closeCurrentMacroPhaseAfterThisStep", true,
                "keepConsumerUnconnected", true,
                "nextPreferredFamily", "people_document_responsavel_consumer_connection_closure",
                "realFlowConnectionAllowedNow", false));
        return details;
    }
}
