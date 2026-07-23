package br.com.escola.planningaiservice.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.BibliotecaConteudoPedagogicoResponse;
import br.com.escola.planningaiservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.BibliotecaConteudoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.ConteudoGeradoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.BibliotecaConteudoJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.ConteudoGeradoJpaRepository;

@Service
public class BibliotecaPublicacaoPersistenciaService {

    private final ConteudoGeradoJpaRepository contentRepository;
    private final BibliotecaConteudoJpaRepository libraryRepository;
    private final DescritorService descriptorService;

    public BibliotecaPublicacaoPersistenciaService(
            ConteudoGeradoJpaRepository contentRepository,
            BibliotecaConteudoJpaRepository libraryRepository,
            DescritorService descriptorService) {
        this.contentRepository = contentRepository;
        this.libraryRepository = libraryRepository;
        this.descriptorService = descriptorService;
    }

    @Transactional
    public BibliotecaConteudoPedagogicoResponse publicarBiblioteca(
            InternalRequestContext context,
            UUID conteudoId) {
        ConteudoGeradoJpaEntity content = contentRepository.findByIdAndEscolaId(conteudoId, context.escolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conteudo IA nao encontrado"));

        LocalDateTime now = LocalDateTime.now();
        BibliotecaConteudoJpaEntity library = libraryRepository.findByEscolaIdAndConteudoOrigem_Id(context.escolaId(), conteudoId)
                .orElseGet(BibliotecaConteudoJpaEntity::new);
        if (library.getId() == null) {
            library.setId(UUID.randomUUID());
            library.setCreatedAt(now);
        }
        library.setEscolaId(context.escolaId());
        library.setEscolaNome(content.getEscolaNome());
        library.setConteudoOrigem(content);
        library.setProfessorId(context.usuarioId());
        library.setProfessorNome(nomeProfessor(context.usuarioId()));
        library.setDisciplinaId(null);
        library.setDisciplinaNome(null);
        library.setTipoConteudo(content.getTipoConteudo());
        library.setTitulo(content.getTitulo());
        library.setTema(extrairTema(content.getTitulo()));
        library.setConteudo(content.getConteudo());
        library.setOrigem("PLANEJAMENTO_IA");
        library.setReutilizavel(content.isReutilizavel());
        library.setAtivo(content.isAtivo());
        library.setUpdatedAt(now);
        libraryRepository.save(library);
        return new BibliotecaConteudoPedagogicoResponse(
                library.getId(),
                library.getEscolaId(),
                library.getEscolaNome(),
                library.getProfessorId(),
                library.getProfessorNome(),
                library.getDisciplinaId(),
                library.getDisciplinaNome(),
                library.getTipoConteudo(),
                descriptorService.tipoConteudoDescricao(library.getTipoConteudo()),
                library.getTitulo(),
                library.getTema(),
                library.getConteudo(),
                library.getOrigem(),
                library.isReutilizavel(),
                library.isAtivo(),
                library.getCreatedAt(),
                library.getUpdatedAt());
    }

    private String nomeProfessor(UUID usuarioId) {
        return "Professor " + usuarioId.toString().substring(0, 8);
    }

    private String extrairTema(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return null;
        }
        int separator = titulo.indexOf('-');
        if (separator >= 0 && separator + 1 < titulo.length()) {
            return titulo.substring(separator + 1).trim();
        }
        return titulo.trim();
    }
}

