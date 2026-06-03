package br.com.escola.seguranca.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PermissaoResponse(
		UUID id, 
		String codigo, 
		String descricao, 
		LocalDateTime createdAt
) {}
