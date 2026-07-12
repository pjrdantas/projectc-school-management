package br.com.escola.professor.adapter.in.web.dto;

import jakarta.validation.constraints.Size;

public record DiarioClasseChecagemRequest(
        @Size(max = 500) String observacao
) {}
