package br.com.escola.accesscontrol.adapter.in.web;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerfilRequest(@NotBlank @Size(max = 50) String codigo, @NotBlank @Size(max = 120) String nome, @Size(max = 255) String descricao, List<UUID> permissaoIds) {}
