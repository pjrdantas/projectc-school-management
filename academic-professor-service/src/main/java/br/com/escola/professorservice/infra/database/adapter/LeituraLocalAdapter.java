package br.com.escola.professorservice.infra.database.adapter;

import static br.com.escola.professorservice.infra.database.mapper.AlocacaoReadMapper.toResponse;
import static br.com.escola.professorservice.infra.database.mapper.PersistenciaMapper.toResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.ResumoResponse;
import br.com.escola.professorservice.application.port.out.LeituraLocalPort;
import br.com.escola.professorservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.professorservice.infra.config.PersistenciaLocalProperties;
import br.com.escola.professorservice.infra.database.entity.AlocacaoJpaEntity;
import br.com.escola.professorservice.infra.database.entity.CadastroJpaEntity;
import br.com.escola.professorservice.infra.database.repository.AlocacaoJpaRepository;
import br.com.escola.professorservice.infra.database.repository.AlocacaoSyncStateJpaRepository;
import br.com.escola.professorservice.infra.database.repository.CadastroJpaRepository;
import br.com.escola.professorservice.infra.database.repository.CadastroSyncStateJpaRepository;
import br.com.escola.professorservice.infra.database.repository.TurmaSyncStateJpaRepository;
import io.micrometer.core.instrument.MeterRegistry;

@Repository
public class LeituraLocalAdapter implements LeituraLocalPort {

    private final CadastroJpaRepository professorRepository;
    private final AlocacaoJpaRepository alocacaoRepository;
    private final AlocacaoSyncStateJpaRepository alocacaoSyncStateRepository;
    private final CadastroSyncStateJpaRepository syncStateRepository;
    private final TurmaSyncStateJpaRepository turmaSyncStateRepository;
    private final PersistenciaLocalProperties properties;
    private final MeterRegistry meterRegistry;

    public LeituraLocalAdapter(
            CadastroJpaRepository professorRepository,
            AlocacaoJpaRepository alocacaoRepository,
            AlocacaoSyncStateJpaRepository alocacaoSyncStateRepository,
            CadastroSyncStateJpaRepository syncStateRepository,
            TurmaSyncStateJpaRepository turmaSyncStateRepository,
            PersistenciaLocalProperties properties,
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
    public ReadDecision decidirListarProfessores(InternalRequestContext context) {
        if (!properties.enabled()) {
            registrarDecisao("listar", "disabled", "feature_disabled");
            return new ReadDecision(false, false);
        }

        boolean supported = syncStateRepository.findById(context.escolaId())
                .map(state -> Boolean.TRUE.equals(state.getProfessoresCompletos()))
                .orElse(false);

        if (supported) {
            registrarDecisao("listar", "local", "sync_state_complete");
            return new ReadDecision(true, false);
        }

        if (properties.listarCutoverEnabled()) {
            registrarDecisao("listar", "local", "cutover_sync_state_incomplete");
            return new ReadDecision(false, true);
        }

        registrarDecisao("listar", "fallback", "sync_state_incomplete");
        return new ReadDecision(false, false);
    }

    @Override
    public List<ResumoResponse> listarProfessores(InternalRequestContext context) {
        return professorRepository.findAllByEscolaIdOrderByNomeCompletoAscIdAsc(context.escolaId()).stream()
                .map(professor -> toResponse(professor))
                .toList();
    }

    @Override
    public boolean supportsBuscarProfessorPorIdCutover(InternalRequestContext context) {
        return properties.enabled() && properties.buscarPorIdCutoverEnabled();
    }

    @Override
    public Optional<ResumoResponse> buscarProfessorPorId(InternalRequestContext context, UUID professorId) {
        if (!properties.enabled()) {
            registrarDecisao("buscarPorId", "disabled", "feature_disabled");
            return Optional.empty();
        }

        Optional<ResumoResponse> local = professorRepository.findById(professorId)
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
    public ReadDecision decidirListarAlocacoes(InternalRequestContext context, UUID professorId) {
        if (!properties.enabled()) {
            registrarDecisao("listarAlocacoes", "disabled", "feature_disabled");
            return new ReadDecision(false, false);
        }

        boolean supported = alocacaoSyncStateRepository.findById(professorId)
                .filter(state -> state.getEscolaId().equals(context.escolaId()))
                .map(state -> Boolean.TRUE.equals(state.getAlocacoesCompletas()))
                .orElse(false);

        if (supported) {
            registrarDecisao("listarAlocacoes", "local", "sync_state_complete");
            return new ReadDecision(true, false);
        }

        if (properties.listarAlocacoesCutoverEnabled()) {
            registrarDecisao("listarAlocacoes", "local", "cutover_sync_state_incomplete");
            return new ReadDecision(false, true);
        }

        registrarDecisao("listarAlocacoes", "fallback", "sync_state_incomplete");
        return new ReadDecision(false, false);
    }

    @Override
    public List<AlocacaoResponse> listarAlocacoes(InternalRequestContext context, UUID professorId) {
        CadastroJpaEntity professor = professorRepository.findById(professorId)
                .filter(entity -> entity.getEscolaId().equals(context.escolaId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor não encontrado"));

        return alocacaoRepository.findAllByProfessorIdOrderByCreatedAtAsc(professorId).stream()
                .map(entity -> toResponse(entity, professor))
                .toList();
    }

    @Override
    public ReadDecision decidirListarProfessoresPorTurma(InternalRequestContext context, UUID turmaId) {
        if (!properties.enabled()) {
            registrarDecisao("listarPorTurma", "disabled", "feature_disabled");
            return new ReadDecision(false, false);
        }

        boolean supported = turmaSyncStateRepository.findById(turmaId)
                .filter(state -> state.getEscolaId().equals(context.escolaId()))
                .map(state -> Boolean.TRUE.equals(state.getAlocacoesCompletas()))
                .orElse(false);

        if (supported) {
            registrarDecisao("listarPorTurma", "local", "sync_state_complete");
            return new ReadDecision(true, false);
        }

        if (properties.listarPorTurmaCutoverEnabled()) {
            registrarDecisao("listarPorTurma", "local", "cutover_sync_state_incomplete");
            return new ReadDecision(false, true);
        }

        registrarDecisao("listarPorTurma", "fallback", "sync_state_incomplete");
        return new ReadDecision(false, false);
    }

    @Override
    public List<AlocacaoResponse> listarProfessoresPorTurma(InternalRequestContext context, UUID turmaId) {
        return alocacaoRepository.findAllByTurmaIdOrderByCreatedAtAsc(turmaId).stream()
                .map(entity -> mapearSeProfessorDaMesmaEscola(context, entity))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private AlocacaoResponse mapearSeProfessorDaMesmaEscola(
            InternalRequestContext context,
            AlocacaoJpaEntity entity) {
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

