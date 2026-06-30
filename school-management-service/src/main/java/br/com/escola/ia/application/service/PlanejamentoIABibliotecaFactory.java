package br.com.escola.ia.application.service;

import org.springframework.stereotype.Component;

import br.com.escola.catalogo.adapter.out.persistence.entity.DisciplinaEntity;
import br.com.escola.ia.adapter.out.persistence.entity.BibliotecaConteudoPedagogicoEntity;
import br.com.escola.ia.adapter.out.persistence.entity.PlanejamentoIAConteudoGeradoEntity;
import br.com.escola.ia.adapter.out.persistence.entity.TipoConteudoIAEntity;
import br.com.escola.ia.application.dto.internal.PlanejamentoIABibliotecaPublicacaoResumo;
import br.com.escola.professor.adapter.out.persistence.entity.ProfessorEntity;
import jakarta.persistence.EntityManager;

@Component
public class PlanejamentoIABibliotecaFactory {

    private final EntityManager entityManager;

    public PlanejamentoIABibliotecaFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public PlanejamentoIABibliotecaPublicacaoResumo extrairResumo(
            PlanejamentoIAConteudoGeradoEntity conteudo,
            String origem) {
        var planejamento = conteudo.getPlanejamentoBimestral();
        var alocacao = planejamento.getProfessorTurmaDisciplina();
        return new PlanejamentoIABibliotecaPublicacaoResumo(
                alocacao.getProfessor().getId(),
                alocacao.getTurmaDisciplina().getDisciplina().getId(),
                conteudo.getTipoConteudoIA().getId(),
                conteudo.getTitulo(),
                planejamento.getTemaPrincipal(),
                conteudo.getConteudo(),
                origem,
                conteudo.getReutilizavel());
    }

    public BibliotecaConteudoPedagogicoEntity criar(PlanejamentoIABibliotecaPublicacaoResumo resumo) {
        return BibliotecaConteudoPedagogicoEntity.builder()
                .professor(entityManager.getReference(ProfessorEntity.class, resumo.professorId()))
                .disciplina(entityManager.getReference(DisciplinaEntity.class, resumo.disciplinaId()))
                .tipoConteudoIA(entityManager.getReference(TipoConteudoIAEntity.class, resumo.tipoConteudoId()))
                .titulo(resumo.titulo())
                .tema(resumo.tema())
                .conteudo(resumo.conteudo())
                .origem(resumo.origem())
                .reutilizavel(resumo.reutilizavel())
                .ativo(Boolean.TRUE)
                .createdAt(java.time.LocalDateTime.now())
                .build();
    }
}
