package br.com.escola.professorservice.infra.database.adapter;

import static br.com.escola.professorservice.infra.database.mapper.ProfessorAlocacaoShadowReadMapper.toResponse;
import static br.com.escola.professorservice.infra.database.mapper.ProfessorShadowPersistenceMapper.toResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.ProfessorAlocacaoResponse;
import br.com.escola.professorservice.application.dto.ProfessorResumoResponse;
import br.com.escola.professorservice.application.port.out.ProfessorShadowLocalReadPort;
import br.com.escola.professorservice.infra.config.ProfessorShadowLocalPersistenceProperties;
import br.com.escola.professorservice.infra.database.entity.ProfessorAlocacaoShadowJpaEntity;
import br.com.escola.professorservice.infra.database.entity.ProfessorShadowJpaEntity;
import br.com.escola.professorservice.infra.database.repository.ProfessorAlocacaoShadowJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorAlocacaoShadowSyncStateJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowSyncStateJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorTurmaShadowSyncStateJpaRepository;
import io.micrometer.core.instrument.MeterRegistry;

@Repository
public class LocalProfessorShadowReadAdapter implements ProfessorShadowLocalReadPort {

    private final ProfessorShadowJpaRepository professorRepository;
    private final ProfessorAlocacaoShadowJpaRepository alocacaoRepository;
    private final ProfessorAlocacaoShadowSyncStateJpaRepository alocacaoSyncStateRepository;
    private final ProfessorShadowSyncStateJpaRepository syncStateRepository;
    private final ProfessorTurmaShadowSyncStateJpaRepository turmaSyncStateRepository;
    private final ProfessorShadowLocalPersistenceProperties properties;
    private final MeterRegistry meterRegistry;

    public LocalProfessorShadowReadAdapter(
            ProfessorShadowJpaRepository professorRepository,
            ProfessorAlocacaoShadowJpaRepository alocacaoRepository,
            ProfessorAlocacaoShadowSyncStateJpaRepository alocacaoSyncStateRepository,
            ProfessorShadowSyncStateJpaRepository syncStateRepository,
            ProfessorTurmaShadowSyncStateJpaRepository turmaSyncStateRepository,
            ProfessorShadowLocalPersistenceProperties properties,
            MeterRegistry meterRegistry) {
        this.professorRepository = professorRepository;
        this.alocacaoRepository = alocacaoRepository;
        this.alocacaoSyncStateRepository = alocacaoSyncStateRepository;
        this.syncStateRepository = syncStateRepository;
        this.turmaSyncStateRepository = turmaSyncStateRepository;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public boolean supportsListarProfessores(InternalRequestContext context) {
        if (!properties.enabled()) {
            registrarDecisao("listar", "disabled", "feature_disabled");
            return false;
        }

        boolean supported = syncStateRepository.findById(context.escolaId())
                .map(state -> Boolean.TRUE.equals(state.getProfessoresCompletos()))
                .orElse(false);

        registrarDecisao("listar", supported ? "local" : "fallback", supported ? "sync_state_complete" : "sync_state_incomplete");
        return supported;
    }

    @Override
    public List<ProfessorResumoResponse> listarProfessores(InternalRequestContext context) {
        return professorRepository.findAllByEscolaIdOrderByNomeCompletoAscIdAsc(context.escolaId()).stream()
                .map(professor -> toResponse(professor))
                .toList();
    }

    @Override
    public boolean supportsBuscarProfessorPorIdCutover(InternalRequestContext context) {
        return properties.enabled() && properties.buscarPorIdCutoverEnabled();
    }

    @Override
    public Optional<ProfessorResumoResponse> buscarProfessorPorId(InternalRequestContext context, UUID professorId) {
        if (!properties.enabled()) {
            registrarDecisao("buscarPorId", "disabled", "feature_disabled");
            return Optional.empty();
        }

        Optional<ProfessorResumoResponse> local = professorRepository.findById(professorId)
                .filter(professor -> professor.getEscolaId().equals(context.escolaId()))
                .map(professor -> {
                    registrarDecisao("buscarPorId", "local", "local_record_present");
                    return toResponse(professor);
                });

        if (local.isEmpty()) {
            registrarDecisao(
                    "buscarPorId",
                    properties.buscarPorIdCutoverEnabled() ? "local" : "fallback",
                    properties.buscarPorIdCutoverEnabled() ? "cutover_local_not_found" : "local_record_missing");
        }
        return local;
    }

    @Override
    public boolean supportsListarAlocacoes(InternalRequestContext context, UUID professorId) {
        if (!properties.enabled()) {
            registrarDecisao("listarAlocacoes", "disabled", "feature_disabled");
            return false;
        }

        boolean supported = alocacaoSyncStateRepository.findById(professorId)
                .filter(state -> state.getEscolaId().equals(context.escolaId()))
                .map(state -> Boolean.TRUE.equals(state.getAlocacoesCompletas()))
                .orElse(false);

        registrarDecisao(
                "listarAlocacoes",
                supported ? "local" : "fallback",
                supported ? "sync_state_complete" : "sync_state_incomplete");
        return supported;
    }

    @Override
    public List<ProfessorAlocacaoResponse> listarAlocacoes(InternalRequestContext context, UUID professorId) {
        ProfessorShadowJpaEntity professor = professorRepository.findById(professorId)
                .filter(entity -> entity.getEscolaId().equals(context.escolaId()))
                .orElseThrow();

        return alocacaoRepository.findAllByProfessorIdOrderByCreatedAtAsc(professorId).stream()
                .map(entity -> toResponse(entity, professor))
                .toList();
    }

    @Override
    public boolean supportsListarProfessoresPorTurma(InternalRequestContext context, UUID turmaId) {
        if (!properties.enabled()) {
            registrarDecisao("listarPorTurma", "disabled", "feature_disabled");
            return false;
        }

        boolean supported = turmaSyncStateRepository.findById(turmaId)
                .filter(state -> state.getEscolaId().equals(context.escolaId()))
                .map(state -> Boolean.TRUE.equals(state.getAlocacoesCompletas()))
                .orElse(false);

        registrarDecisao(
                "listarPorTurma",
                supported ? "local" : "fallback",
                supported ? "sync_state_complete" : "sync_state_incomplete");
        return supported;
    }

    @Override
    public List<ProfessorAlocacaoResponse> listarProfessoresPorTurma(InternalRequestContext context, UUID turmaId) {
        return alocacaoRepository.findAllByTurmaIdOrderByCreatedAtAsc(turmaId).stream()
                .map(entity -> mapearSeProfessorDaMesmaEscola(context, entity))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private ProfessorAlocacaoResponse mapearSeProfessorDaMesmaEscola(
            InternalRequestContext context,
            ProfessorAlocacaoShadowJpaEntity entity) {
        return professorRepository.findById(entity.getProfessorId())
                .filter(professor -> professor.getEscolaId().equals(context.escolaId()))
                .map(professor -> toResponse(entity, professor))
                .orElse(null);
    }

    private void registrarDecisao(String operacao, String origem, String motivo) {
        meterRegistry.counter(
                "professor.shadow.local.read.requests",
                "operacao", operacao,
                "origem", origem,
                "motivo", motivo)
                .increment();
    }
}
