package br.com.escola.professorservice.infra.database.adapter;

import static br.com.escola.professorservice.infra.database.mapper.ProfessorShadowPersistenceMapper.toEntity;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.ProfessorResumoResponse;
import br.com.escola.professorservice.application.exception.ProfessorShadowPersistenceDivergenceException;
import br.com.escola.professorservice.application.port.out.ProfessorShadowPersistencePort;
import br.com.escola.professorservice.infra.config.ProfessorShadowLocalPersistenceProperties;
import br.com.escola.professorservice.infra.database.entity.ProfessorShadowJpaEntity;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowJpaRepository;
import io.micrometer.core.instrument.MeterRegistry;

@Repository
@Transactional
public class LocalProfessorShadowPersistenceAdapter implements ProfessorShadowPersistencePort {

    private final ProfessorShadowJpaRepository repository;
    private final ProfessorShadowLocalPersistenceProperties properties;
    private final MeterRegistry meterRegistry;

    public LocalProfessorShadowPersistenceAdapter(
            ProfessorShadowJpaRepository repository,
            ProfessorShadowLocalPersistenceProperties properties,
            MeterRegistry meterRegistry) {
        this.repository = repository;
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

    private void validarMesmaIdentidade(ProfessorShadowJpaEntity entity, ProfessorResumoResponse response) {
        if (!Objects.equals(entity.getPessoaId(), response.pessoaId())
                || !Objects.equals(entity.getEscolaId(), response.escolaId())) {
            throw new ProfessorShadowPersistenceDivergenceException(
                    "Professor ja existe localmente com identidade diferente da retornada pelo monolito");
        }
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
