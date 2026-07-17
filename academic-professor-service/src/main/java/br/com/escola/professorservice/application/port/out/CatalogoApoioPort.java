package br.com.escola.professorservice.application.port.out;

import java.util.UUID;

import br.com.escola.professorservice.application.context.InternalRequestContext;

public interface CatalogoApoioPort {

    TurmaDisciplinaResumo buscarTurmaDisciplina(String authorization, InternalRequestContext context, UUID turmaDisciplinaId);

    TurmaResumo buscarTurma(String authorization, InternalRequestContext context, UUID turmaId);

    record TurmaDisciplinaResumo(
            UUID id,
            UUID turmaId,
            UUID disciplinaId,
            String disciplinaNome,
            Integer cargaHoraria,
            UUID escolaId) {
    }

    record TurmaResumo(
            UUID id,
            String codigo,
            String nome,
            UUID escolaId) {
    }
}
