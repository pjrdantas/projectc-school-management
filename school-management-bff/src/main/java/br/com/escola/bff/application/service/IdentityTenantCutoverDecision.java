package br.com.escola.bff.application.service;

public record IdentityTenantCutoverDecision(
        IdentityTenantRoute route,
        boolean useNewService,
        String reason
) {}
