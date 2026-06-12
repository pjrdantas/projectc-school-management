package br.com.escola.avaliacao.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.avaliacao.adapter.in.web.dto.AvaliacaoRequest;
import br.com.escola.avaliacao.adapter.in.web.dto.AvaliacaoResponse;
import br.com.escola.avaliacao.adapter.in.web.dto.NotaAlunoRequest;
import br.com.escola.avaliacao.adapter.in.web.dto.NotaAlunoResponse;
import br.com.escola.avaliacao.adapter.out.persistence.entity.AvaliacaoEntity;
import br.com.escola.avaliacao.adapter.out.persistence.entity.NotaAlunoEntity;
import br.com.escola.avaliacao.adapter.out.persistence.entity.TipoAvaliacaoEntity;
import br.com.escola.avaliacao.adapter.out.persistence.repository.AvaliacaoJpaRepository;
import br.com.escola.avaliacao.adapter.out.persistence.repository.NotaAlunoJpaRepository;
import br.com.escola.avaliacao.adapter.out.persistence.repository.TipoAvaliacaoJpaRepository;
import br.com.escola.avaliacao.domain.exception.AvaliacaoMatriculaTurmaInconsistenteException;
import br.com.escola.avaliacao.domain.exception.AvaliacaoNaoEncontradaException;
import br.com.escola.avaliacao.domain.exception.AvaliacaoNotaDuplicadaException;
import br.com.escola.avaliacao.domain.exception.AvaliacaoNotaInvalidaException;
import br.com.escola.avaliacao.domain.exception.TipoAvaliacaoNaoEncontradoException;
import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.professor.adapter.out.persistence.entity.ProfessorTurmaDisciplinaEntity;
import br.com.escola.professor.adapter.out.persistence.repository.ProfessorTurmaDisciplinaJpaRepository;
import br.com.escola.professor.domain.exception.AulaNaoEncontradaException;
import br.com.escola.professor.domain.exception.ProfessorTurmaDisciplinaNaoEncontradaException;
import br.com.escola.institucional.application.service.EscolaTenantService;

@Service
public class AvaliacaoService {

    private final AvaliacaoJpaRepository avaliacaoJpaRepository;
    private final NotaAlunoJpaRepository notaAlunoJpaRepository;
    private final TipoAvaliacaoJpaRepository tipoAvaliacaoJpaRepository;
    private final ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository;
    private final MatriculaJpaRepository matriculaJpaRepository;
    private final EscolaTenantService escolaTenantService;

    public AvaliacaoService(
            AvaliacaoJpaRepository avaliacaoJpaRepository,
            NotaAlunoJpaRepository notaAlunoJpaRepository,
            TipoAvaliacaoJpaRepository tipoAvaliacaoJpaRepository,
            ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository,
            MatriculaJpaRepository matriculaJpaRepository,
            EscolaTenantService escolaTenantService) {
        this.avaliacaoJpaRepository = avaliacaoJpaRepository;
        this.notaAlunoJpaRepository = notaAlunoJpaRepository;
        this.tipoAvaliacaoJpaRepository = tipoAvaliacaoJpaRepository;
        this.professorTurmaDisciplinaJpaRepository = professorTurmaDisciplinaJpaRepository;
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.escolaTenantService = escolaTenantService;
    }

    @Transactional
    public AvaliacaoResponse criar(AvaliacaoRequest request) {
        ProfessorTurmaDisciplinaEntity alocacao = professorTurmaDisciplinaJpaRepository
                .findByIdAndTurmaDisciplina_Turma_Escola_Id(request.professorTurmaDisciplinaId(), escolaId())
                .orElseThrow(ProfessorTurmaDisciplinaNaoEncontradaException::new);
        TipoAvaliacaoEntity tipo = tipoAvaliacaoJpaRepository.findByCodigo(request.tipoAvaliacao().toUpperCase())
                .orElseThrow(TipoAvaliacaoNaoEncontradoException::new);

        AvaliacaoEntity avaliacao = AvaliacaoEntity.builder()
                .professorTurmaDisciplina(alocacao)
                .tipoAvaliacao(tipo)
                .titulo(request.titulo())
                .descricao(request.descricao())
                .dataAplicacao(request.dataAplicacao())
                .valorMaximo(request.valorMaximo())
                .peso(request.peso())
                .createdAt(LocalDateTime.now())
                .build();

        return toAvaliacaoResponse(avaliacaoJpaRepository.save(avaliacao));
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoResponse> listar(UUID professorTurmaDisciplinaId, UUID turmaId) {
        if (professorTurmaDisciplinaId != null) {
            return avaliacaoJpaRepository
                    .findByProfessorTurmaDisciplina_IdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                            professorTurmaDisciplinaId,
                            escolaId())
                    .stream()
                    .map(this::toAvaliacaoResponse)
                    .toList();
        }
        if (turmaId != null) {
            return avaliacaoJpaRepository
                    .findByProfessorTurmaDisciplina_TurmaDisciplina_Turma_IdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                            turmaId,
                            escolaId())
                    .stream()
                    .map(this::toAvaliacaoResponse)
                    .toList();
        }
        return avaliacaoJpaRepository.findAllByProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(escolaId()).stream()
                .map(this::toAvaliacaoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AvaliacaoResponse buscarPorId(UUID id) {
        return toAvaliacaoResponse(findAvaliacao(id));
    }

    @Transactional
    public NotaAlunoResponse lancarNota(UUID avaliacaoId, NotaAlunoRequest request) {
        AvaliacaoEntity avaliacao = findAvaliacao(avaliacaoId);
        MatriculaEntity matricula = matriculaJpaRepository.findByIdAndTurma_Escola_Id(request.matriculaId(), escolaId())
                .orElseThrow(() -> new AulaNaoEncontradaException("Matrícula não encontrada."));

        UUID turmaAvaliacaoId = avaliacao.getProfessorTurmaDisciplina().getTurmaDisciplina().getTurma().getId();
        UUID turmaMatriculaId = matricula.getTurma().getId();
        if (!turmaAvaliacaoId.equals(turmaMatriculaId)) {
            throw new AvaliacaoMatriculaTurmaInconsistenteException();
        }

        if (request.nota().compareTo(BigDecimal.ZERO) < 0 || request.nota().compareTo(avaliacao.getValorMaximo()) > 0) {
            throw new AvaliacaoNotaInvalidaException();
        }

        notaAlunoJpaRepository
                .findByAvaliacao_IdAndAvaliacao_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_IdAndMatricula_Id(
                        avaliacaoId,
                        escolaId(),
                        request.matriculaId())
                .ifPresent(nota -> {
                    throw new AvaliacaoNotaDuplicadaException();
                });

        NotaAlunoEntity entity = NotaAlunoEntity.builder()
                .avaliacao(avaliacao)
                .matricula(matricula)
                .nota(request.nota())
                .observacao(request.observacao())
                .createdAt(LocalDateTime.now())
                .build();

        return toNotaResponse(notaAlunoJpaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<NotaAlunoResponse> listarNotasPorAvaliacao(UUID avaliacaoId) {
        if (!avaliacaoJpaRepository.existsByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(avaliacaoId, escolaId())) {
            throw new AvaliacaoNaoEncontradaException();
        }
        return notaAlunoJpaRepository
                .findByAvaliacao_IdAndAvaliacao_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                        avaliacaoId,
                        escolaId())
                .stream()
                .map(this::toNotaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotaAlunoResponse> listarNotasPorMatricula(UUID matriculaId) {
        if (!matriculaJpaRepository.existsByIdAndTurma_Escola_Id(matriculaId, escolaId())) {
            throw new AulaNaoEncontradaException("Matrícula não encontrada.");
        }
        return notaAlunoJpaRepository.findByMatricula_IdAndMatricula_Turma_Escola_Id(matriculaId, escolaId()).stream()
                .map(this::toNotaResponse)
                .toList();
    }

    private AvaliacaoEntity findAvaliacao(UUID id) {
        return avaliacaoJpaRepository.findByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(id, escolaId())
                .orElseThrow(AvaliacaoNaoEncontradaException::new);
    }

    private AvaliacaoResponse toAvaliacaoResponse(AvaliacaoEntity entity) {
        ProfessorTurmaDisciplinaEntity alocacao = entity.getProfessorTurmaDisciplina();
        return new AvaliacaoResponse(
                entity.getId(),
                alocacao.getId(),
                alocacao.getProfessor().getId(),
                alocacao.getProfessor().getPessoa().getNomeCompleto(),
                alocacao.getTurmaDisciplina().getTurma().getId(),
                alocacao.getTurmaDisciplina().getTurma().getNome(),
                alocacao.getTurmaDisciplina().getTurma().getEscola().getId(),
                alocacao.getTurmaDisciplina().getTurma().getEscola().getNome(),
                alocacao.getTurmaDisciplina().getDisciplina().getId(),
                alocacao.getTurmaDisciplina().getDisciplina().getNome(),
                entity.getTitulo(),
                entity.getDescricao(),
                entity.getDataAplicacao(),
                entity.getValorMaximo(),
                entity.getPeso(),
                entity.getTipoAvaliacao().getCodigo(),
                entity.getCreatedAt());
    }

    private NotaAlunoResponse toNotaResponse(NotaAlunoEntity entity) {
        return new NotaAlunoResponse(
                entity.getId(),
                entity.getAvaliacao().getId(),
                entity.getAvaliacao().getTitulo(),
                entity.getMatricula().getId(),
                entity.getMatricula().getAluno().getId(),
                entity.getMatricula().getAluno().getPessoa().getNomeCompleto(),
                entity.getMatricula().getTurma().getEscola().getId(),
                entity.getMatricula().getTurma().getEscola().getNome(),
                entity.getNota(),
                entity.getObservacao(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private UUID escolaId() {
        return escolaTenantService.obterOuCriarEscolaPadrao().getId();
    }
}
