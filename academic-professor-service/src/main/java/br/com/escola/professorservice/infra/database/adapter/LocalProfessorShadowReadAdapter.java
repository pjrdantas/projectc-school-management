package br.com.escola.professorservice.infra.database.adapter;

import static br.com.escola.professorservice.infra.database.mapper.ProfessorAlocacaoShadowReadMapper.toResponse;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.ProfessorAlocacaoResponse;
import br.com.escola.professorservice.application.port.out.ProfessorShadowLocalReadPort;
import br.com.escola.professorservice.infra.config.ProfessorShadowLocalPersistenceProperties;
import br.com.escola.professorservice.infra.database.entity.ProfessorAlocacaoShadowJpaEntity;
import br.com.escola.professorservice.infra.database.entity.ProfessorShadowJpaEntity;
import br.com.escola.professorservice.infra.database.repository.ProfessorAlocacaoShadowJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowJpaRepository;
import io.micrometer.core.instrument.MeterRegistry;

@Repository
public class LocalProfessorShadowReadAdapter implements ProfessorShadowLocalReadPort {

    private final ProfessorShadowJpaRepository professorRepository;
    private final ProfessorAlocacaoShadowJpaRepository alocacaoRepository;
    private final ProfessorShadowLocalPersistenceProperties properties;
    private final MeterRegistry meterRegistry;

    public LocalProfessorShadowReadAdapter(
            ProfessorShadowJpaRepository professorRepository,
            ProfessorAlocacaoShadowJpaRepository alocacaoRepository,
            ProfessorShadowLocalPersistenceProperties properties,
            MeterRegistry meterRegistry) {
        this.professorRepository = professorRepository;
        this.alocacaoRepository = alocacaoRepository;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public boolean supportsListarAlocacoes(InternalRequestContext context, UUID professorId) {
        if (!properties.enabled()) {
            registrarDecisao("listarAlocacoes", "disabled");
            return false;
        }

        boolean supported = professorRepository.findById(professorId)
                .map(professor -> professor.getEscolaId().equals(context.escolaId()))
                .orElse(false);

        registrarDecisao("listarAlocacoes", supported ? "local" : "fallback");
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
            registrarDecisao("listarPorTurma", "disabled");
            return false;
        }

        boolean supported = alocacaoRepository.findAllByTurmaIdOrderByCreatedAtAsc(turmaId).stream()
                .map(ProfessorAlocacaoShadowJpaEntity::getProfessorId)
                .map(professorRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .anyMatch(professor -> professor.getEscolaId().equals(context.escolaId()));

        registrarDecisao("listarPorTurma", supported ? "local" : "fallback");
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

    private void registrarDecisao(String operacao, String origem) {
        meterRegistry.counter(
                "professor.shadow.local.read.requests",
                "operacao", operacao,
                "origem", origem)
                .increment();
    }
}
