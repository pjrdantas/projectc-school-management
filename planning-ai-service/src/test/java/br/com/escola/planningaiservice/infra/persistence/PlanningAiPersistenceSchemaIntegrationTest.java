package br.com.escola.planningaiservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PedagogicalContentLibraryJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiContentVersionJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiGeneratedContentJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiInteractionJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PedagogicalContentLibraryJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanningAiContentVersionJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanningAiGeneratedContentJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanningAiInteractionJpaRepository;

@SpringBootTest
class PlanningAiPersistenceSchemaIntegrationTest {

    @Autowired
    private PlanningAiInteractionJpaRepository interactionRepository;

    @Autowired
    private PlanningAiGeneratedContentJpaRepository contentRepository;

    @Autowired
    private PlanningAiContentVersionJpaRepository versionRepository;

    @Autowired
    private PedagogicalContentLibraryJpaRepository libraryRepository;

    @Test
    void devePersistirBaseMinimaDoNovoBancoLocal() {
        UUID escolaId = UUID.randomUUID();
        UUID planejamentoId = UUID.randomUUID();

        PlanningAiInteractionJpaEntity interaction = new PlanningAiInteractionJpaEntity();
        interaction.setId(UUID.randomUUID());
        interaction.setEscolaId(escolaId);
        interaction.setPlanejamentoBimestralId(planejamentoId);
        interaction.setUsuarioId(UUID.randomUUID());
        interaction.setPromptProfessor("Monte uma atividade sobre fracoes");
        interaction.setRespostaIa("Sugestao de atividade");
        interaction.setModeloIa("gpt-4.1");
        interaction.setTokensEntrada(120);
        interaction.setTokensSaida(240);
        interaction.setCustoEstimado(new BigDecimal("1.2500"));
        interaction.setCreatedAt(LocalDateTime.now());
        interactionRepository.save(interaction);

        PlanningAiGeneratedContentJpaEntity content = new PlanningAiGeneratedContentJpaEntity();
        content.setId(UUID.randomUUID());
        content.setEscolaId(escolaId);
        content.setPlanejamentoBimestralId(planejamentoId);
        content.setInteracao(interaction);
        content.setTitulo("Sugestao - Fracoes");
        content.setConteudo("Conteudo gerado");
        content.setVersao(1);
        content.setHashConteudo("abc123");
        content.setAprovadoPeloProfessor(false);
        content.setReutilizavel(true);
        content.setAtivo(true);
        content.setStatus("GERADO");
        content.setTipoConteudo("ATIVIDADE");
        content.setCreatedAt(LocalDateTime.now());
        content.setUpdatedAt(LocalDateTime.now());
        contentRepository.save(content);

        PlanningAiContentVersionJpaEntity version = new PlanningAiContentVersionJpaEntity();
        version.setId(UUID.randomUUID());
        version.setEscolaId(escolaId);
        version.setConteudoGerado(content);
        version.setAlteradoPor(UUID.randomUUID());
        version.setNumeroVersao(1);
        version.setConteudo("Conteudo gerado");
        version.setMotivoAlteracao("Versao inicial");
        version.setCreatedAt(LocalDateTime.now());
        versionRepository.save(version);

        PedagogicalContentLibraryJpaEntity library = new PedagogicalContentLibraryJpaEntity();
        library.setId(UUID.randomUUID());
        library.setEscolaId(escolaId);
        library.setConteudoOrigem(content);
        library.setProfessorId(UUID.randomUUID());
        library.setDisciplinaId(UUID.randomUUID());
        library.setTipoConteudo("ATIVIDADE");
        library.setTitulo("Lista de fracoes");
        library.setTema("Fracoes");
        library.setConteudo("Conteudo publicado");
        library.setOrigem("PLANEJAMENTO_IA");
        library.setReutilizavel(true);
        library.setAtivo(true);
        library.setCreatedAt(LocalDateTime.now());
        library.setUpdatedAt(LocalDateTime.now());
        libraryRepository.save(library);

        assertThat(interactionRepository.findById(interaction.getId())).isPresent();
        assertThat(contentRepository.findById(content.getId())).isPresent();
        assertThat(versionRepository.findById(version.getId())).isPresent();
        assertThat(libraryRepository.findById(library.getId()))
                .get()
                .extracting(PedagogicalContentLibraryJpaEntity::getConteudoOrigem)
                .extracting(PlanningAiGeneratedContentJpaEntity::getId)
                .isEqualTo(content.getId());
    }
}
