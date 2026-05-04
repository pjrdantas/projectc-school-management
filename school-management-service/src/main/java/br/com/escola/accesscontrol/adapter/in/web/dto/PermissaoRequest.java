package br.com.escola.accesscontrol.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PermissaoRequest(
		@NotBlank @Size(max = 80) String codigo, 
		@Size(max = 255) String descricao
) {}
