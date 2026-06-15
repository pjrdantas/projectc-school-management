package br.com.escola.catalogo.application.port.internal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.catalogo.application.dto.internal.TurmaDisciplinaResumo;
import br.com.escola.catalogo.application.dto.internal.TurmaResumo;

public interface EstruturaTurmaPort {

    Optional<TurmaResumo> obterTurma(UUID escolaId, UUID turmaId);

    List<TurmaDisciplinaResumo> listarDisciplinasDaTurma(UUID escolaId, UUID turmaId);

    boolean turmaPossuiDisciplina(UUID escolaId, UUID turmaId, UUID disciplinaId);

    boolean turmaPertenceAoPeriodo(UUID escolaId, UUID turmaId, UUID periodoLetivoId);
}
