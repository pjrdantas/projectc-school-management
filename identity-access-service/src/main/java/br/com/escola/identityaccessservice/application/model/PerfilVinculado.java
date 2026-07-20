package br.com.escola.identityaccessservice.application.model;

import java.util.UUID;

public record PerfilVinculado(UUID id, String codigo, String nome) {
}
