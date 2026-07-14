package br.com.escola.planningaiservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.BibliotecaConteudoPedagogicoResponse;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PedagogicalContentLibraryJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanningAiGeneratedContentJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PedagogicalContentLibraryJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanningAiGeneratedContentJpaRepository;

@Service
public class PlanningAiLibraryPublicationPersistenceService {

    private final PlanningAiGeneratedContentJpaRepository contentRepository;
    private final PedagogicalContentLibraryJpaRepository libraryRepository;

    public PlanningAiLibraryPublicationPersistenceService(
            PlanningAiGeneratedContentJpaRepository contentRepository,
            PedagogicalContentLibraryJpaRepository libraryRepository) {
        this.contentRepository = contentRepository;
        this.libraryRepository = libraryRepository;
    }

    @Transactional
    public BibliotecaConteudoPedagogicoResponse persistirPublicacao(
            InternalRequestContext context,
            UUID conteudoId,
            BibliotecaConteudoPedagogicoResponse response) {
        if (response == null) {
            return null;
        }

        PlanningAiGeneratedContentJpaEntity content = contentRepository.findByIdAndEscolaId(conteudoId, context.escolaId())
                .orElse(null);
        if (content == null) {
            return response;
        }

        PedagogicalContentLibraryJpaEntity library = libraryRepository.findById(response.id())
                .orElseGet(PedagogicalContentLibraryJpaEntity::new);
        library.setId(response.id());
        library.setEscolaId(context.escolaId());
        library.setEscolaNome(response.escolaNome());
        library.setConteudoOrigem(content);
        library.setProfessorId(response.professorId());
        library.setProfessorNome(response.professorNome());
        library.setDisciplinaId(response.disciplinaId());
        library.setDisciplinaNome(response.disciplinaNome());
        library.setTipoConteudo(response.tipoConteudo());
        library.setTitulo(response.titulo());
        library.setTema(response.tema());
        library.setConteudo(response.conteudo());
        library.setOrigem(response.origem());
        library.setReutilizavel(Boolean.TRUE.equals(response.reutilizavel()));
        library.setAtivo(Boolean.TRUE.equals(response.ativo()));
        library.setCreatedAt(response.createdAt());
        library.setUpdatedAt(response.updatedAt());
        libraryRepository.save(library);
        return response;
    }
}
