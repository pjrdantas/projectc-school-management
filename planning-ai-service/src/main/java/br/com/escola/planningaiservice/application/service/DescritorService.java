package br.com.escola.planningaiservice.application.service;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class DescritorService {

    private static final Map<String, String> STATUS_DESCRIPTIONS = Map.of(
            "GERADO", "Gerado",
            "EM_EDICAO", "Em edicao",
            "APROVADO", "Aprovado");

    private static final Map<String, String> TYPE_DESCRIPTIONS = Map.of(
            "ATIVIDADE", "Atividade",
            "PLANO_AULA", "Plano de aula",
            "AVALIACAO", "Avaliacao",
            "RESUMO", "Resumo");

    public String statusDescricao(String status) {
        if (status == null) {
            return null;
        }
        return STATUS_DESCRIPTIONS.getOrDefault(status.toUpperCase(Locale.ROOT), status);
    }

    public String tipoConteudoDescricao(String tipoConteudo) {
        if (tipoConteudo == null) {
            return null;
        }
        return TYPE_DESCRIPTIONS.getOrDefault(tipoConteudo.toUpperCase(Locale.ROOT), tipoConteudo);
    }
}

