package br.com.escola.ia.application.port.internal;

import java.util.List;
import java.util.UUID;

import br.com.escola.ia.application.dto.internal.PlanejamentoIABibliotecaPublicacaoResumo;
import br.com.escola.ia.application.dto.internal.PlanejamentoIABibliotecaResumo;

public interface PlanejamentoIABibliotecaPort {

    PlanejamentoIABibliotecaResumo publicar(PlanejamentoIABibliotecaPublicacaoResumo resumo);

    List<PlanejamentoIABibliotecaResumo> listar(
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema,
            UUID escolaId);
}
