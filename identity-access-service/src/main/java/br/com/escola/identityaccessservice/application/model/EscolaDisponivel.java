package br.com.escola.identityaccessservice.application.model;

import java.util.UUID;

public record EscolaDisponivel(
        UUID id,
        String nome,
        boolean ativa) {
}
