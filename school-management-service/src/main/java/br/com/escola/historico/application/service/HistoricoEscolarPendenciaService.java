package br.com.escola.historico.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarTelaResponse;
import br.com.escola.historico.application.dto.internal.HistoricoEscolarPendenciaContexto;
import br.com.escola.historico.application.port.internal.HistoricoEscolarPendenciaPort;

@Service
public class HistoricoEscolarPendenciaService implements HistoricoEscolarPendenciaPort {

    private static final Pattern PRIMEIRO_NUMERO = Pattern.compile("(\\d+)");

    @Override
    public List<HistoricoEscolarTelaResponse.Pendencia> calcularParaCadastro(
            Integer serieMatriculaAtual,
            Integer serieConcluidaOrigem) {
        List<HistoricoEscolarTelaResponse.Pendencia> pendencias = new ArrayList<>();
        if (serieMatriculaAtual != null && serieMatriculaAtual > 1) {
            pendencias.add(new HistoricoEscolarTelaResponse.Pendencia(
                    "HISTORICO_SERIE_ANTERIOR_INCOMPLETO",
                    "AVISO",
                    "ANOS_COMPONENTES",
                    "Aluno matriculado em série posterior. O histórico precisa ser preenchido até a série anterior à matrícula."));
        }
        pendencias.add(new HistoricoEscolarTelaResponse.Pendencia(
                "ANOS_COMPONENTES_PENDENTES",
                "AVISO",
                "ANOS_COMPONENTES",
                "Há anos letivos e componentes curriculares pendentes para preenchimento."));
        pendencias.add(new HistoricoEscolarTelaResponse.Pendencia(
                "ESTUDOS_REALIZADOS_PENDENTE",
                "AVISO",
                "ESTUDOS_REALIZADOS",
                "Há estudos realizados pendentes para completar o histórico."));
        if (serieConcluidaOrigem == null) {
            pendencias.add(new HistoricoEscolarTelaResponse.Pendencia(
                    "CERTIFICADO_PENDENTE",
                    "AVISO",
                    "OBSERVACOES_CERTIFICADO",
                    "A série concluída na escola de origem ainda precisa ser informada."));
        }
        return pendencias;
    }

    @Override
    public List<HistoricoEscolarTelaResponse.Pendencia> calcularParaEdicao(
            HistoricoEscolarPendenciaContexto contexto) {
        List<HistoricoEscolarTelaResponse.Pendencia> pendencias = new ArrayList<>();
        List<String> seriesComponentes = contexto.seriesComponentes() == null ? List.of() : contexto.seriesComponentes();
        if (seriesComponentes.isEmpty()) {
            pendencias.add(new HistoricoEscolarTelaResponse.Pendencia(
                    "ANOS_COMPONENTES_PENDENTES",
                    "AVISO",
                    "ANOS_COMPONENTES",
                    "Há componentes curriculares ou anos letivos pendentes até a série anterior à matrícula."));
        }
        if (contexto.serieMatriculaAtual() != null
                && contexto.serieMatriculaAtual() > 1
                && !cobreSerieAnterior(seriesComponentes, contexto.serieMatriculaAtual() - 1)) {
            pendencias.add(new HistoricoEscolarTelaResponse.Pendencia(
                    "HISTORICO_SERIE_ANTERIOR_INCOMPLETO",
                    "AVISO",
                    "ANOS_COMPONENTES",
                    "Aluno matriculado em série posterior. O histórico precisa estar preenchido até a série anterior à matrícula."));
        }
        if (contexto.serieConcluidaOrigem() == null) {
            pendencias.add(new HistoricoEscolarTelaResponse.Pendencia(
                    "CERTIFICADO_PENDENTE",
                    "AVISO",
                    "OBSERVACOES_CERTIFICADO",
                    "A série concluída na escola de origem ainda precisa ser informada."));
        }
        return pendencias;
    }

    private boolean cobreSerieAnterior(List<String> seriesComponentes, int serieObrigatoriaAte) {
        return seriesComponentes.stream()
                .map(HistoricoEscolarPendenciaService::serieOrdem)
                .filter(java.util.Objects::nonNull)
                .anyMatch(ordem -> ordem <= serieObrigatoriaAte);
    }

    private static Integer serieOrdem(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        Matcher matcher = PRIMEIRO_NUMERO.matcher(value);
        if (!matcher.find()) {
            return null;
        }
        return Integer.valueOf(matcher.group(1));
    }
}
