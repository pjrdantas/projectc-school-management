package br.com.escola.professor.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DiarioClasseChecagemResponse(
        UUID idLancamento,
        UUID escolaId,
        String status,
        UUID checadoCoordenacaoPorFuncionario,
        LocalDateTime checadoCoordenacaoEm,
        UUID checadoDirecaoPorFuncionario,
        LocalDateTime checadoDirecaoEm,
        Boolean bloqueado
) {}
