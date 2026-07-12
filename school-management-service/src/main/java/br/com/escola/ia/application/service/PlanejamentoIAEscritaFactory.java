package br.com.escola.ia.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.escola.ia.adapter.out.persistence.entity.PlanejamentoIAConteudoGeradoEntity;
import br.com.escola.ia.adapter.out.persistence.entity.PlanejamentoIAConteudoVersaoEntity;
import br.com.escola.ia.adapter.out.persistence.entity.PlanejamentoIAInteracaoEntity;
import br.com.escola.ia.adapter.out.persistence.entity.StatusConteudoIAEntity;
import br.com.escola.ia.adapter.out.persistence.entity.TipoConteudoIAEntity;
import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoBimestralEntity;
import jakarta.persistence.EntityManager;

@Component
public class PlanejamentoIAEscritaFactory {

    private final EntityManager entityManager;

    public PlanejamentoIAEscritaFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public PlanejamentoIAInteracaoEntity criarInteracao(
            UUID planejamentoId,
            String promptProfessor,
            GeracaoConteudoPedagogicoResultado resultado) {
        return PlanejamentoIAInteracaoEntity.builder()
                .planejamentoBimestral(referenciaPlanejamento(planejamentoId))
                .promptProfessor(promptProfessor)
                .respostaIA(resultado.conteudo())
                .modeloIA(resultado.modelo())
                .tokensEntrada(resultado.tokensEntrada())
                .tokensSaida(resultado.tokensSaida())
                .custoEstimado(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public PlanejamentoIAConteudoGeradoEntity criarConteudoGerado(
            UUID planejamentoId,
            PlanejamentoIAInteracaoEntity interacao,
            TipoConteudoIAEntity tipoConteudo,
            StatusConteudoIAEntity statusGerado,
            String titulo,
            String conteudo,
            String hashConteudo,
            Boolean reutilizavel) {
        return PlanejamentoIAConteudoGeradoEntity.builder()
                .planejamentoBimestral(referenciaPlanejamento(planejamentoId))
                .planejamentoIAInteracao(interacao)
                .tipoConteudoIA(tipoConteudo)
                .statusConteudoIA(statusGerado)
                .titulo(titulo)
                .conteudo(conteudo)
                .versao(1)
                .hashConteudo(hashConteudo)
                .aprovadoPeloProfessor(Boolean.FALSE)
                .reutilizavel(reutilizavel)
                .ativo(Boolean.TRUE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public PlanejamentoIAConteudoVersaoEntity criarVersaoInicial(
            PlanejamentoIAConteudoGeradoEntity conteudo,
            String conteudoGerado) {
        return PlanejamentoIAConteudoVersaoEntity.builder()
                .planejamentoIAConteudoGerado(conteudo)
                .numeroVersao(1)
                .conteudo(conteudoGerado)
                .motivoAlteracao("Versão inicial gerada em modo simulado.")
                .createdAt(LocalDateTime.now())
                .build();
    }

    public PlanejamentoIAConteudoVersaoEntity criarNovaVersao(
            PlanejamentoIAConteudoGeradoEntity conteudo,
            int numeroVersao,
            String conteudoVersao,
            String motivoAlteracao) {
        return PlanejamentoIAConteudoVersaoEntity.builder()
                .planejamentoIAConteudoGerado(conteudo)
                .numeroVersao(numeroVersao)
                .conteudo(conteudoVersao)
                .motivoAlteracao(motivoAlteracao)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private PlanejamentoBimestralEntity referenciaPlanejamento(UUID planejamentoId) {
        return entityManager.getReference(PlanejamentoBimestralEntity.class, planejamentoId);
    }
}
