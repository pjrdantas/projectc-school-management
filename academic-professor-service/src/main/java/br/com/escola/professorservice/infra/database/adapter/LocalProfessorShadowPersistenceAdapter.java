package br.com.escola.professorservice.infra.database.adapter;

import static br.com.escola.professorservice.infra.database.mapper.ProfessorAlocacaoShadowPersistenceMapper.toEntity;
import static br.com.escola.professorservice.infra.database.mapper.ProfessorShadowPersistenceMapper.toEntity;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.ProfessorAllocateRequest;
import br.com.escola.professorservice.application.dto.ProfessorAlocacaoResponse;
import br.com.escola.professorservice.application.dto.ProfessorResumoResponse;
import br.com.escola.professorservice.application.exception.ProfessorShadowPersistenceDivergenceException;
import br.com.escola.professorservice.application.port.out.ProfessorShadowPersistencePort;
import br.com.escola.professorservice.infra.config.ProfessorShadowLocalPersistenceProperties;
import br.com.escola.professorservice.infra.database.entity.ProfessorAlocacaoShadowJpaEntity;
import br.com.escola.professorservice.infra.database.entity.ProfessorAlocacaoShadowSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.entity.ProfessorShadowJpaEntity;
import br.com.escola.professorservice.infra.database.entity.ProfessorTurmaShadowSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.repository.ProfessorAlocacaoShadowJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorAlocacaoShadowSyncStateJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorTurmaShadowSyncStateJpaRepository;
import io.micrometer.core.instrument.MeterRegistry;

@Repository
@Transactional
public class LocalProfessorShadowPersistenceAdapter implements ProfessorShadowPersistencePort {

    private final ProfessorShadowJpaRepository repository;
    private final ProfessorAlocacaoShadowJpaRepository alocacaoRepository;
    private final ProfessorAlocacaoShadowSyncStateJpaRepository alocacaoSyncStateRepository;
    private final ProfessorTurmaShadowSyncStateJpaRepository turmaSyncStateRepository;
    private final ProfessorShadowLocalPersistenceProperties properties;
    private final MeterRegistry meterRegistry;

    public LocalProfessorShadowPersistenceAdapter(
            ProfessorShadowJpaRepository repository,
            ProfessorAlocacaoShadowJpaRepository alocacaoRepository,
            ProfessorAlocacaoShadowSyncStateJpaRepository alocacaoSyncStateRepository,
            ProfessorTurmaShadowSyncStateJpaRepository turmaSyncStateRepository,
            ProfessorShadowLocalPersistenceProperties properties,
            MeterRegistry meterRegistry) {
        this.repository = repository;
        this.alocacaoRepository = alocacaoRepository;
        this.alocacaoSyncStateRepository = alocacaoSyncStateRepository;
        this.turmaSyncStateRepository = turmaSyncStateRepository;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void registrarCriacaoShadow(InternalRequestContext context, ProfessorResumoResponse response) {
        if (!properties.enabled()) {
            registrarRequisicao("criar", "skipped_disabled");
            return;
        }

        try {
            validarDivergencia(context.escolaId(), response);
            repository.save(toEntity(response));
            registrarRequisicao("criar", "success");
        } catch (ProfessorShadowPersistenceDivergenceException exception) {
            registrarRequisicao("criar", "divergence");
            registrarFalha("criar", "divergence");
            if (properties.failOnError()) {
                throw exception;
            }
        } catch (RuntimeException exception) {
            registrarRequisicao("criar", "error");
            registrarFalha("criar", exception.getClass().getSimpleName());
            if (properties.failOnError()) {
                throw exception;
            }
        }
    }

    @Override
    public void registrarAlocacaoShadow(
            InternalRequestContext context,
            ProfessorAllocateRequest request,
            ProfessorAlocacaoResponse response) {
        if (!properties.enabled()) {
            registrarRequisicao("vincularTurmaDisciplina", "skipped_disabled");
            return;
        }

        try {
            validarAlocacaoDivergencia(context.escolaId(), request, response);
            alocacaoRepository.save(toEntity(response));
            atualizarSyncStateDeAlocacao(context, response);
            registrarRequisicao("vincularTurmaDisciplina", "success");
        } catch (ProfessorShadowPersistenceDivergenceException exception) {
            registrarRequisicao("vincularTurmaDisciplina", "divergence");
            registrarFalha("vincularTurmaDisciplina", "divergence");
            if (properties.failOnError()) {
                throw exception;
            }
        } catch (RuntimeException exception) {
            registrarRequisicao("vincularTurmaDisciplina", "error");
            registrarFalha("vincularTurmaDisciplina", exception.getClass().getSimpleName());
            if (properties.failOnError()) {
                throw exception;
            }
        }
    }

    private void validarDivergencia(UUID contextoEscolaId, ProfessorResumoResponse response) {
        if (!Objects.equals(contextoEscolaId, response.escolaId())) {
            throw new ProfessorShadowPersistenceDivergenceException(
                    "Resposta do monolito retornou escola divergente da requisicao shadow");
        }

        repository.findById(response.id())
                .ifPresent(entity -> validarMesmaIdentidade(entity, response));

        repository.findByPessoaIdAndEscolaId(response.pessoaId(), response.escolaId())
                .ifPresent(entity -> {
                    if (!entity.getId().equals(response.id())) {
                        throw new ProfessorShadowPersistenceDivergenceException(
                                "Pessoa ja vinculada a outro professor na copia shadow local");
                    }
                });
    }

    private void validarAlocacaoDivergencia(
            UUID contextoEscolaId,
            ProfessorAllocateRequest request,
            ProfessorAlocacaoResponse response) {
        if (!Objects.equals(request.turmaDisciplinaId(), response.turmaDisciplinaId())) {
            throw new ProfessorShadowPersistenceDivergenceException(
                    "Resposta do monolito retornou turmaDisciplina divergente da requisicao shadow");
        }

        ProfessorShadowJpaEntity professor = repository.findById(response.professorId())
                .orElseThrow(() -> new ProfessorShadowPersistenceDivergenceException(
                        "Professor da alocacao nao existe na copia shadow local"));

        if (!Objects.equals(professor.getEscolaId(), contextoEscolaId)) {
            throw new ProfessorShadowPersistenceDivergenceException(
                    "Professor da alocacao pertence a escola divergente da requisicao shadow");
        }

        alocacaoRepository.findById(response.id())
                .ifPresent(entity -> validarMesmaAlocacao(entity, response));

        alocacaoRepository.findByProfessorIdAndTurmaDisciplinaId(response.professorId(), response.turmaDisciplinaId())
                .ifPresent(entity -> {
                    if (!entity.getId().equals(response.id())) {
                        throw new ProfessorShadowPersistenceDivergenceException(
                                "Professor ja possui alocacao local para a turmaDisciplina com outro identificador");
                    }
                });
    }

    private void validarMesmaIdentidade(ProfessorShadowJpaEntity entity, ProfessorResumoResponse response) {
        if (!Objects.equals(entity.getPessoaId(), response.pessoaId())
                || !Objects.equals(entity.getEscolaId(), response.escolaId())) {
            throw new ProfessorShadowPersistenceDivergenceException(
                    "Professor ja existe localmente com identidade diferente da retornada pelo monolito");
        }
    }

    private void validarMesmaAlocacao(ProfessorAlocacaoShadowJpaEntity entity, ProfessorAlocacaoResponse response) {
        if (!Objects.equals(entity.getProfessorId(), response.professorId())
                || !Objects.equals(entity.getTurmaDisciplinaId(), response.turmaDisciplinaId())) {
            throw new ProfessorShadowPersistenceDivergenceException(
                    "Alocacao ja existe localmente com identidade diferente da retornada pelo monolito");
        }
    }

    private void atualizarSyncStateDeAlocacao(InternalRequestContext context, ProfessorAlocacaoResponse response) {
        long professorCount = alocacaoRepository.findAllByProfessorIdOrderByCreatedAtAsc(response.professorId()).size();
        long turmaCount = alocacaoRepository.findAllByTurmaIdOrderByCreatedAtAsc(response.turmaId()).size();

        alocacaoSyncStateRepository.save(new ProfessorAlocacaoShadowSyncStateJpaEntity(
                response.professorId(),
                context.escolaId(),
                true,
                professorCount,
                response.createdAt()));
        turmaSyncStateRepository.save(new ProfessorTurmaShadowSyncStateJpaEntity(
                response.turmaId(),
                context.escolaId(),
                true,
                turmaCount,
                response.createdAt()));
    }

    private void registrarRequisicao(String operacao, String resultado) {
        meterRegistry.counter(
                "professor.shadow.local.persistence.requests",
                "operacao", operacao,
                "resultado", resultado)
                .increment();
    }

    private void registrarFalha(String operacao, String causa) {
        meterRegistry.counter(
                "professor.shadow.local.persistence.failures",
                "operacao", operacao,
                "causa", causa)
                .increment();
    }
}
