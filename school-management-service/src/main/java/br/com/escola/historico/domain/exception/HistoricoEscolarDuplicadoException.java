package br.com.escola.historico.domain.exception;

import java.io.Serial;
import java.util.UUID;

public class HistoricoEscolarDuplicadoException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public HistoricoEscolarDuplicadoException(UUID alunoId, UUID periodoLetivoId) {
        super("Já existe histórico escolar gerado para o aluno %s no período letivo %s"
                .formatted(alunoId, periodoLetivoId));
    }
}
