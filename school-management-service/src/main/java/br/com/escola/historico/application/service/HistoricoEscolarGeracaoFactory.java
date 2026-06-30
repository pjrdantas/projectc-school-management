package br.com.escola.historico.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import br.com.escola.aluno.adapter.out.persistence.entity.AlunoEntity;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarGeracaoRequest;
import br.com.escola.historico.adapter.out.persistence.entity.HistoricoEscolar;
import br.com.escola.historico.adapter.out.persistence.entity.HistoricoEscolarItem;
import br.com.escola.historico.application.dto.internal.BoletimHistoricoItemResumo;
import br.com.escola.historico.application.dto.internal.BoletimHistoricoResumo;

@Component
public class HistoricoEscolarGeracaoFactory {

    public HistoricoEscolar criar(
            HistoricoEscolarGeracaoRequest request,
            BoletimHistoricoResumo boletim,
            AlunoEntity aluno) {
        HistoricoEscolar historico = HistoricoEscolar.builder()
                .alunoId(boletim.alunoId())
                .origem("INTERNO")
                .nomeAluno(boletim.nomeAluno())
                .rgRen(boletim.rg())
                .ra(boletim.ra())
                .rm(boletim.rm())
                .dataNascimento(boletim.dataNascimento())
                .municipioNascimento(boletim.naturalidade())
                .paisNascimento(boletim.nacionalidade())
                .anoConclusao(boletim.anoConclusao())
                .ensinoConcluido(trimToNull(request.ensinoConcluido()))
                .dataEmissao(LocalDate.now())
                .observacoes(observacoesGeracao(request, boletim))
                .build();
        historico.setAluno(aluno);

        boletim.itens().stream()
                .map(this::toHistoricoItem)
                .forEach(historico::addComponenteCurricular);

        return historico;
    }

    private HistoricoEscolarItem toHistoricoItem(BoletimHistoricoItemResumo item) {
        return HistoricoEscolarItem.builder()
                .periodoLetivo(item.periodoLetivo())
                .serieEntity(item.serieEntity())
                .disciplina(item.disciplina())
                .componenteCurricular(item.componenteCurricular())
                .anoLetivo(item.anoLetivo())
                .serie(item.serie())
                .notaConceito(toNotaConceito(item.media()))
                .frequenciaPercentual(item.frequenciaPercentual())
                .totalAulas(item.totalAulas())
                .cargaHoraria(item.cargaHoraria())
                .resultado(item.resultado())
                .build();
    }

    private String toNotaConceito(BigDecimal media) {
        if (media == null) {
            return null;
        }
        return media.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String observacoesGeracao(HistoricoEscolarGeracaoRequest request, BoletimHistoricoResumo boletim) {
        String observacoes = trimToNull(request.observacoes());
        if (observacoes != null) {
            return observacoes;
        }
        return "Historico gerado a partir do boletim fechado %s, periodo %s"
                .formatted(boletim.boletimId(), boletim.periodoReferencia());
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
