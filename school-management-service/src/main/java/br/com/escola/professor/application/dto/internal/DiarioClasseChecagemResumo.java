package br.com.escola.professor.application.dto.internal;

import java.time.LocalDateTime;
import java.util.UUID;

public record DiarioClasseChecagemResumo(
        UUID diarioClasseLancamentoId,
        UUID escolaId,
        String status,
        UUID checadoCoordenacaoPorFuncionario,
        LocalDateTime checadoCoordenacaoEm,
        UUID checadoDirecaoPorFuncionario,
        LocalDateTime checadoDirecaoEm,
        Boolean bloqueado
) {}
