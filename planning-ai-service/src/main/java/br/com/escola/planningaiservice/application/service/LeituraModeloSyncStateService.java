package br.com.escola.planningaiservice.application.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.planningaiservice.infra.persistence.jpa.entity.LeituraModeloSyncStateJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.LeituraModeloSyncStateJpaRepository;

@Service
public class LeituraModeloSyncStateService {

    private static final String SCOPE_LIBRARY = "LIBRARY_QUERY";
    private static final String SCOPE_INTERACTIONS = "INTERACTIONS_PLANNING";
    private static final String SCOPE_CONTENTS = "CONTENTS_PLANNING";
    private static final String SCOPE_CONTENT_NOT_FOUND = "CONTENT_NOT_FOUND";
    private static final String SCOPE_CONTENT_VERSIONS_NOT_FOUND = "CONTENT_VERSIONS_NOT_FOUND";
    private static final String SCOPE_VERSIONS = "VERSIONS_CONTENT";

    private final LeituraModeloSyncStateJpaRepository repository;

    public LeituraModeloSyncStateService(LeituraModeloSyncStateJpaRepository repository) {
        this.repository = repository;
    }

    public boolean librarySynced(
            UUID escolaId,
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema) {
        return repository.existsById(syncId(SCOPE_LIBRARY, escolaId, null, libraryQueryKey(
                professorId,
                disciplinaId,
                tipoConteudo,
                tema)));
    }

    public boolean interactionsSynced(UUID escolaId, UUID planejamentoId) {
        return repository.existsById(syncId(SCOPE_INTERACTIONS, escolaId, planejamentoId, ""));
    }

    public boolean contentsSynced(UUID escolaId, UUID planejamentoId) {
        return repository.existsById(syncId(SCOPE_CONTENTS, escolaId, planejamentoId, ""));
    }

    public boolean contentNotFound(UUID escolaId, UUID conteudoId) {
        return repository.existsById(syncId(SCOPE_CONTENT_NOT_FOUND, escolaId, conteudoId, ""));
    }

    public boolean versionsSynced(UUID escolaId, UUID conteudoId) {
        return repository.existsById(syncId(SCOPE_VERSIONS, escolaId, conteudoId, ""));
    }

    public boolean contentVersionsNotFound(UUID escolaId, UUID conteudoId) {
        return repository.existsById(syncId(SCOPE_CONTENT_VERSIONS_NOT_FOUND, escolaId, conteudoId, ""));
    }

    @Transactional
    public void markLibrarySynced(
            UUID escolaId,
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema) {
        save(SCOPE_LIBRARY, escolaId, null, libraryQueryKey(professorId, disciplinaId, tipoConteudo, tema));
    }

    @Transactional
    public void markInteractionsSynced(UUID escolaId, UUID planejamentoId) {
        save(SCOPE_INTERACTIONS, escolaId, planejamentoId, "");
    }

    @Transactional
    public void markContentsSynced(UUID escolaId, UUID planejamentoId) {
        save(SCOPE_CONTENTS, escolaId, planejamentoId, "");
    }

    @Transactional
    public void markContentNotFound(UUID escolaId, UUID conteudoId) {
        save(SCOPE_CONTENT_NOT_FOUND, escolaId, conteudoId, "");
    }

    @Transactional
    public void markVersionsSynced(UUID escolaId, UUID conteudoId) {
        save(SCOPE_VERSIONS, escolaId, conteudoId, "");
    }

    @Transactional
    public void markContentVersionsNotFound(UUID escolaId, UUID conteudoId) {
        save(SCOPE_CONTENT_VERSIONS_NOT_FOUND, escolaId, conteudoId, "");
    }

    private void save(String scope, UUID escolaId, UUID referenciaId, String queryKey) {
        String id = syncId(scope, escolaId, referenciaId, queryKey);
        LeituraModeloSyncStateJpaEntity state = repository.findById(id)
                .orElseGet(LeituraModeloSyncStateJpaEntity::new);
        state.setId(id);
        state.setScope(scope);
        state.setEscolaId(escolaId);
        state.setReferenciaId(referenciaId);
        state.setQueryKey(queryKey);
        state.setSyncedAt(LocalDateTime.now());
        repository.save(state);
    }

    private String libraryQueryKey(
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema) {
        return String.join("|",
                normalizeUuid(professorId),
                normalizeUuid(disciplinaId),
                normalizeString(tipoConteudo),
                normalizeString(tema));
    }

    private String syncId(String scope, UUID escolaId, UUID referenciaId, String queryKey) {
        return String.join("|",
                scope,
                normalizeUuid(escolaId),
                normalizeUuid(referenciaId),
                queryKey == null ? "" : queryKey);
    }

    private String normalizeUuid(UUID value) {
        return value == null ? "" : value.toString();
    }

    private String normalizeString(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}

