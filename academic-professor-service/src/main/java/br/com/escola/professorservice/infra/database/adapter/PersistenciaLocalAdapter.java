package br.com.escola.professorservice.infra.database.adapter;

import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.AllocateRequest;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.CreateRequest;
import br.com.escola.professorservice.application.dto.ResumoResponse;
import br.com.escola.professorservice.application.dto.UpdateRequest;
import br.com.escola.professorservice.application.dto.UpdateAllocateRequest;
import br.com.escola.professorservice.application.exception.ConflitoNegocioException;
import br.com.escola.professorservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.professorservice.application.port.out.CatalogoApoioPort;
import br.com.escola.professorservice.application.port.out.PessoaApoioPort;
import br.com.escola.professorservice.application.port.out.PersistenciaPort;
import br.com.escola.professorservice.infra.database.entity.AlocacaoJpaEntity;
import br.com.escola.professorservice.infra.database.entity.CadastroJpaEntity;
import br.com.escola.professorservice.infra.database.repository.AlocacaoJpaRepository;
import br.com.escola.professorservice.infra.database.repository.CadastroJpaRepository;
import br.com.escola.professorservice.infra.database.mapper.AlocacaoReadMapper;
import br.com.escola.professorservice.infra.database.mapper.PersistenciaMapper;
import io.micrometer.core.instrument.MeterRegistry;

@Repository
@Transactional
public class PersistenciaLocalAdapter implements PersistenciaPort {

    private final CadastroJpaRepository repository;
    private final AlocacaoJpaRepository alocacaoRepository;
    private final MeterRegistry meterRegistry;

    public PersistenciaLocalAdapter(
            CadastroJpaRepository repository,
            AlocacaoJpaRepository alocacaoRepository,
            MeterRegistry meterRegistry) {
        this.repository = repository;
        this.alocacaoRepository = alocacaoRepository;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public ResumoResponse criarProfessor(
            InternalRequestContext context,
            PessoaApoioPort.FuncionarioResumo funcionario,
            PessoaApoioPort.PessoaResumo pessoa,
            CreateRequest request) {
        repository.findByPessoaIdAndEscolaId(funcionario.pessoaId(), context.escolaId()).ifPresent(existing -> {
            throw new ConflitoNegocioException("Pessoa informada ja esta vinculada a um professor nesta escola");
        });

        var now = java.time.LocalDateTime.now();
        CadastroJpaEntity entity = new CadastroJpaEntity(
                UUID.randomUUID(),
                funcionario.pessoaId(),
                pessoa.nomeCompleto(),
                context.escolaId(),
                pessoa.escolaNome(),
                request.registroProfissional(),
                request.formacao(),
                request.ativo() == null ? Boolean.TRUE : request.ativo(),
                now,
                now,
                context.usuarioId());
        repository.save(entity);
        registrarRequisicao("criar", "success");
        return PersistenciaMapper.toResponse(entity);
    }

    @Override
    public AlocacaoResponse criarAlocacao(
            InternalRequestContext context,
            ResumoResponse professor,
            CatalogoApoioPort.TurmaDisciplinaResumo turmaDisciplina,
            CatalogoApoioPort.TurmaResumo turma,
            AllocateRequest request,
            boolean ativo) {
        if (!context.escolaId().equals(professor.escolaId())) {
            throw new ConflitoNegocioException("Professor informado pertence a outra escola");
        }

        if (!context.escolaId().equals(turmaDisciplina.escolaId()) || !context.escolaId().equals(turma.escolaId())) {
            throw new ConflitoNegocioException("Turma disciplina informada pertence a outra escola");
        }

        if (ativo && alocacaoRepository.existsByProfessorIdAndTurmaDisciplinaIdAndAtivoTrue(
                professor.id(), turmaDisciplina.id())) {
            throw new ConflitoNegocioException("Professor informado ja esta vinculado a esta turma disciplina");
        }

        var now = java.time.LocalDateTime.now();
        AlocacaoJpaEntity entity = new AlocacaoJpaEntity(
                UUID.randomUUID(),
                professor.id(),
                turmaDisciplina.id(),
                turmaDisciplina.turmaId(),
                turma.nome(),
                turmaDisciplina.disciplinaId(),
                turmaDisciplina.disciplinaNome(),
                request.dataInicio(),
                request.dataFim(),
                ativo,
                now);
        alocacaoRepository.save(entity);
        registrarRequisicao("vincularTurmaDisciplina", "success");
        return AlocacaoReadMapper.toResponse(entity, repository.findById(professor.id()).orElseThrow());
    }

    @Override
    public ResumoResponse atualizarProfessor(
            InternalRequestContext context,
            UUID professorId,
            UpdateRequest request) {
        CadastroJpaEntity entity = repository.findById(professorId)
                .filter(professor -> professor.getEscolaId().equals(context.escolaId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor não encontrado"));

        if (!request.ativo() && alocacaoRepository.existsByProfessorIdAndAtivoTrue(professorId)) {
            throw new ConflitoNegocioException(
                    "Professor possui alocacao academica ativa e nao pode ser inativado");
        }

        entity.atualizarDados(
                request.registroProfissional(),
                request.formacao(),
                request.ativo(),
                java.time.LocalDateTime.now(),
                context.usuarioId());
        CadastroJpaEntity saved = repository.save(entity);
        registrarRequisicao("atualizar", "success");
        return PersistenciaMapper.toResponse(saved);
    }

    @Override
    public AlocacaoResponse atualizarAlocacao(
            InternalRequestContext context,
            ResumoResponse professor,
            UUID alocacaoId,
            CatalogoApoioPort.TurmaDisciplinaResumo turmaDisciplina,
            CatalogoApoioPort.TurmaResumo turma,
            UpdateAllocateRequest request) {
        if (!context.escolaId().equals(professor.escolaId())) {
            throw new ConflitoNegocioException("Professor informado pertence a outra escola");
        }
        if (!context.escolaId().equals(turmaDisciplina.escolaId())
                || !context.escolaId().equals(turma.escolaId())) {
            throw new ConflitoNegocioException("Turma disciplina informada pertence a outra escola");
        }

        AlocacaoJpaEntity entity = alocacaoRepository.findById(alocacaoId)
                .filter(alocacao -> alocacao.getProfessorId().equals(professor.id()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alocacao de professor nao encontrada"));

        if (request.ativo()) {
            alocacaoRepository.findByProfessorIdAndTurmaDisciplinaIdAndAtivoTrue(
                    professor.id(), turmaDisciplina.id())
                    .filter(existing -> !existing.getId().equals(alocacaoId))
                    .ifPresent(existing -> {
                        throw new ConflitoNegocioException("Professor informado ja esta vinculado a esta turma disciplina");
                    });
        }

        entity.atualizarDados(
                turmaDisciplina.id(),
                turmaDisciplina.turmaId(),
                turma.nome(),
                turmaDisciplina.disciplinaId(),
                turmaDisciplina.disciplinaNome(),
                request.dataInicio(),
                request.dataFim(),
                request.ativo());
        AlocacaoJpaEntity saved = alocacaoRepository.save(entity);
        registrarRequisicao("atualizarAlocacao", "success");
        return AlocacaoReadMapper.toResponse(saved, repository.findById(professor.id()).orElseThrow());
    }

    @Override
    public void encerrarAlocacao(
            InternalRequestContext context,
            UUID professorId,
            UUID alocacaoId) {
        AlocacaoJpaEntity entity = alocacaoRepository.findById(alocacaoId)
                .filter(alocacao -> alocacao.getProfessorId().equals(professorId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alocacao de professor nao encontrada"));
        entity.encerrar(java.time.LocalDate.now());
        alocacaoRepository.save(entity);
        registrarRequisicao("encerrarAlocacao", "success");
    }

    private void registrarRequisicao(String operacao, String resultado) {
        meterRegistry.counter(
                "professor.local.persistence.requests",
                "operacao", operacao,
                "resultado", resultado)
                .increment();
    }

    @SuppressWarnings("unused")
	private void registrarFalha(String operacao, String causa) {
        meterRegistry.counter(
                "professor.local.persistence.failures",
                "operacao", operacao,
                "causa", causa)
                .increment();
    }
}

