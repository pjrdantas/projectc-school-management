package br.com.projeto.piloto.adapter.in.web.dto;

import java.time.LocalDateTime;

public record AplicativosResponseDTO(
	    Long id,
	    String nome,
	    String descricao,
	    String url,	    
	    String moduleName,
	    String exposedModule,
	    String routePath,
	    String ativo,
	    LocalDateTime criadoEm,
	    LocalDateTime atualizadoEm
	) {}
