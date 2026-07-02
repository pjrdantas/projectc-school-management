package br.com.escola.historico.application.port.internal;

import java.util.List;

import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarTelaResponse;
import br.com.escola.historico.application.dto.internal.HistoricoEscolarPendenciaContexto;

public interface HistoricoEscolarPendenciaPort {

    List<HistoricoEscolarTelaResponse.Pendencia> calcularParaCadastro(
            Integer serieMatriculaAtual,
            Integer serieConcluidaOrigem);

    List<HistoricoEscolarTelaResponse.Pendencia> calcularParaEdicao(
            HistoricoEscolarPendenciaContexto contexto);
}
