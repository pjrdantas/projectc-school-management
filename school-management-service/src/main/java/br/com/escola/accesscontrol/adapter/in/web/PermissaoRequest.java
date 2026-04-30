package br.com.escola.accesscontrol.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PermissaoRequest(@NotBlank @Size(max = 80) String codigo, @Size(max = 255) String descricao) {}
