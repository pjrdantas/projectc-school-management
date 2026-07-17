package br.com.escola.professorservice.infra.database.adapter;

import static br.com.escola.professorservice.infra.database.mapper.AlocacaoPersistenciaMapper.toEntity;
import static br.com.escola.professorservice.infra.database.mapper.PersistenciaMapper.toEntity;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.AllocateRequest;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.ResumoResponse;
import br.com.escola.professorservice.application.exception.PersistenciaDivergenciaException;
import br.com.escola.professorservice.application.port.out.PersistenciaPort;
import br.com.escola.professorservice.infra.config.PersistenciaLocalProperties;
import br.com.escola.professorservice.infra.database.entity.AlocacaoJpaEntity;
import br.com.escola.professorservice.infra.database.entity.AlocacaoSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.entity.CadastroJpaEntity;
import br.com.escola.professorservice.infra.database.entity.TurmaSyncStateJpaEntity;
import br.com.escola.professorservice.infra.database.repository.AlocacaoJpaRepository;
import br.com.escola.professorservice.infra.database.repository.AlocacaoSyncStateJpaRepository;
import br.com.escola.professorservice.infra.database.repository.CadastroJpaRepository;
import br.com.escola.professorservice.infra.database.repository.TurmaSyncStateJpaRepository;
import io.micrometer.core.instrument.MeterRegistry;

@Repository
@Transactional
public class PersistenciaLocalAdapter implements PersistenciaPort {

    private final CadastroJpaRepository repository;
    private final AlocacaoJpaRepository alocacaoRepository;
    private final AlocacaoSyncStateJpaRepository alocacaoSyncStateRepository;
    private final TurmaSyncStateJpaRepository turmaSyncStateRepository;
    private final PersistenciaLocalProperties properties;
    private final MeterRegistry meterRegistry;

    public PersistenciaLocalAdapter(
            CadastroJpaRepository repository,
            AlocacaoJpaRepository alocacaoRepository,
            AlocacaoSyncStateJpaRepository alocacaoSyncStateRepository,
            TurmaSyncStateJpaRepository turmaSyncStateRepository,
            PersistenciaLocalProperties properties,
            MeterRegistry meterRegistry) {
        this.repository = repository;
        this.alocacaoRepository = alocacaoRepository;
        this.alocacaoSyncStateRepository = alocacaoSyncStateRepository;
        this.turmaSyncStateRepository = turmaSyncStateRepository;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void registrarCriacaoShadow(InternalRequestContext context, ResumoResponse response) {
        if (!properties.enabled()) {
            registrarRequisicao("criar", "skipped_disabled");
            return;
        }

        try {
            validarDivergencia(context.escolaId(), response);
            repository.save(toEntity(response));
            registrarRequisicao("criar", "success");
        } catch (PersistenciaDivergenciaException exception) {
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
            AllocateRequest request,
            AlocacaoResponse response) {
        if (!properties.enabled()) {
            registrarRequisicao("vincularTurmaDisciplina", "skipped_disabled");
            return;
        }

        try {
            validarAlocacaoDivergencia(context.escolaId(), request, response);
            alocacaoRepository.save(toEntity(response));
            atualizarSyncStateDeAlocacao(context, response);
            registrarRequisicao("vincularTurmaDisciplina", "success");
        } catch (PersistenciaDivergenciaException exception) {
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

    private void validarDivergencia(UUID contextoEscolaId, ResumoResponse response) {
        if (!Objects.equals(contextoEscolaId, response.escolaId())) {
            throw new PersistenciaDivergenciaException(
                    "Resposta do monolito retornou escola divergente da requisicao shadow");
        }

        repository.findById(response.id())
                .ifPresent(entity -> validarMesmaIdentidade(entity, response));

        repository.findByPessoaIdAndEscolaId(response.pessoaId(), response.escolaId())
                .ifPresent(entity -> {
                    if (!entity.getId().equals(response.id())) {
                        throw new PersistenciaDivergenciaException(
                                "Pessoa ja vinculada a outro professor na copia shadow local");
                    }
                });
    }

    private void validarAlocacaoDivergencia(
            UUID contextoEscolaId,
            AllocateRequest request,
            AlocacaoResponse response) {
        if (!Objects.equals(request.turmaDisciplinaId(), response.turmaDisciplinaId())) {
            throw new PersistenciaDivergenciaException(
                    "Resposta do monolito retornou turmaDisciplina divergente da requisicao shadow");
        }

        CadastroJpaEntity professor = repository.findById(response.professorId())
                .orElseThrow(() -> new PersistenciaDivergenciaException(
                        "Professor da alocacao nao existe na copia shadow local"));

        if (!Objects.equals(professor.getEscolaId(), contextoEscolaId)) {
            throw new PersistenciaDivergenciaException(
                    "Professor da alocacao pertence a escola divergente da requisicao shadow");
        }

        alocacaoRepository.findById(response.id())
                .ifPresent(entity -> validarMesmaAlocacao(entity, response));

        alocacaoRepository.findByProfessorIdAndTurmaDisciplinaId(response.professorId(), response.turmaDisciplinaId())
                .ifPresent(entity -> {
                    if (!entity.getId().equals(response.id())) {
                        throw new PersistenciaDivergenciaException(
                                "Professor ja possui alocacao local para a turmaDisciplina com outro identificador");
                    }
                });
    }

    private void validarMesmaIdentidade(CadastroJpaEntity entity, ResumoResponse response) {
        if (!Objects.equals(entity.getPessoaId(), response.pessoaId())
                || !Objects.equals(entity.getEscolaId(), response.escolaId())) {
            throw new PersistenciaDivergenciaException(
                    "Professor ja existe localmente com identidade diferente da retornada pelo monolito");
        }
    }

    private void validarMesmaAlocacao(AlocacaoJpaEntity entity, AlocacaoResponse response) {
        if (!Objects.equals(entity.getProfessorId(), response.professorId())
                || !Objects.equals(entity.getTurmaDisciplinaId(), response.turmaDisciplinaId())) {
            throw new PersistenciaDivergenciaException(
                    "Alocacao ja existe localmente com identidade diferente da retornada pelo monolito");
        }
    }

    private void atualizarSyncStateDeAlocacao(InternalRequestContext context, AlocacaoResponse response) {
        long professorCount = alocacaoRepository.findAllByProfessorIdOrderByCreatedAtAsc(response.professorId()).size();
        long turmaCount = alocacaoRepository.findAllByTurmaIdOrderByCreatedAtAsc(response.turmaId()).size();

        alocacaoSyncStateRepository.save(new AlocacaoSyncStateJpaEntity(
                response.professorId(),
                context.escolaId(),
                true,
                professorCount,
                response.createdAt()));
        turmaSyncStateRepository.save(new TurmaSyncStateJpaEntity(
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

