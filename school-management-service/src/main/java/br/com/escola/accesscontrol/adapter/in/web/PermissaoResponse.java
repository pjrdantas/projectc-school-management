package br.com.escola.accesscontrol.adapter.in.web;

import java.time.LocalDateTime;
import java.util.UUID;

public record PermissaoResponse(UUID id, String codigo, String descricao, LocalDateTime createdAt) {}
