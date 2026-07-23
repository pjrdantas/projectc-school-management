package br.com.escola.peopleservice.application.model;

import java.util.UUID;

public record EscolaPessoa(
        UUID id,
        String nome,
        boolean ativa) {
}
