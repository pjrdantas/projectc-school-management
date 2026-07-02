package br.com.escola.peopleservice.application.dto;

public record PeopleLocalReadRoutingDecision(
        String operation,
        String shadowRoute,
        String candidateSource,
        String selectedSource,
        boolean localReadRequested,
        boolean localReadEligible,
        boolean fallbackEnabled,
        boolean writesEnabled,
        String reason) {
}
