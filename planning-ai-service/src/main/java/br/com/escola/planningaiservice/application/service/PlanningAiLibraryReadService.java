package br.com.escola.planningaiservice.application.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.planningaiservice.application.dto.BibliotecaConteudoPedagogicoResponse;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PedagogicalContentLibraryJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PedagogicalContentLibraryJpaRepository;

@Service
public class PlanningAiLibraryReadService {

    private final PedagogicalContentLibraryJpaRepository libraryRepository;
    private final PlanningAiDescriptorService descriptorService;

    public PlanningAiLibraryReadService(
            PedagogicalContentLibraryJpaRepository libraryRepository,
            PlanningAiDescriptorService descriptorService) {
        this.libraryRepository = libraryRepository;
        this.descriptorService = descriptorService;
    }

    public List<BibliotecaConteudoPedagogicoResponse> listar(
            UUID escolaId,
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema) {
        return libraryRepository.findByEscolaIdOrderByCreatedAtAsc(escolaId)
                .stream()
                .filter(entry -> professorId == null || professorId.equals(entry.getProfessorId()))
                .filter(entry -> disciplinaId == null || disciplinaId.equals(entry.getDisciplinaId()))
                .filter(entry -> tipoConteudo == null || tipoConteudo.equalsIgnoreCase(entry.getTipoConteudo()))
                .filter(entry -> tema == null || matchesTema(entry, tema))
                .map(this::toResponse)
                .toList();
    }

    private boolean matchesTema(PedagogicalContentLibraryJpaEntity entry, String tema) {
        if (entry.getTema() == null) {
            return false;
        }
        return entry.getTema().toLowerCase(Locale.ROOT).contains(tema.toLowerCase(Locale.ROOT));
    }

    private BibliotecaConteudoPedagogicoResponse toResponse(PedagogicalContentLibraryJpaEntity entity) {
        return new BibliotecaConteudoPedagogicoResponse(
                entity.getId(),
                entity.getEscolaId(),
                null,
                entity.getProfessorId(),
                null,
                entity.getDisciplinaId(),
                null,
                entity.getTipoConteudo(),
                descriptorService.tipoConteudoDescricao(entity.getTipoConteudo()),
                entity.getTitulo(),
                entity.getTema(),
                entity.getConteudo(),
                entity.getOrigem(),
                entity.isReutilizavel(),
                entity.isAtivo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
