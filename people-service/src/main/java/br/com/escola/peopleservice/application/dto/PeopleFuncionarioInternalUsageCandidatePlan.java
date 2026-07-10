package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record PeopleFuncionarioInternalUsageCandidatePlan(
        String phase,
        String slice,
        String status,
        String recommendedNextStep,
        String minimalNextSlice,
        boolean internalUsageCandidateFound,
        boolean safeToConnectNow,
        boolean externalRouteChangeRequired,
        boolean fallbackRequired,
        List<String> currentBlockers,
        List<String> preservedBoundaries,
        List<String> rollbackSteps) {
}
