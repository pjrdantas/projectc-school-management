package br.com.escola.peopleservice.application.service;

public record PeopleReadSourceDecision(
        String operation,
        String route,
        String candidateSource,
        String selectedSource,
        boolean localReadRequested,
        boolean localReadEligible,
        boolean fallbackEnabled,
        boolean writesEnabled,
        String reason) {
}

