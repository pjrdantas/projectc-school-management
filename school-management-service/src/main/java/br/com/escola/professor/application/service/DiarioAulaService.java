package br.com.escola.professor.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.frequencia.adapter.out.persistence.entity.FrequenciaAlunoEntity;
import br.com.escola.frequencia.adapter.out.persistence.entity.FrequenciaProfessorEntity;
import br.com.escola.frequencia.adapter.out.persistence.entity.SituacaoFrequenciaEntity;
import br.com.escola.frequencia.adapter.out.persistence.repository.FrequenciaAlunoJpaRepository;
import br.com.escola.frequencia.adapter.out.persistence.repository.FrequenciaProfessorJpaRepository;
import br.com.escola.frequencia.adapter.out.persistence.repository.SituacaoFrequenciaJpaRepository;
import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.professor.adapter.in.web.dto.AulaRequest;
import br.com.escola.professor.adapter.in.web.dto.AulaResponse;
import br.com.escola.professor.adapter.in.web.dto.FrequenciaAlunoRequest;
import br.com.escola.professor.adapter.in.web.dto.FrequenciaAlunoResponse;
import br.com.escola.professor.adapter.in.web.dto.FrequenciaProfessorRequest;
import br.com.escola.professor.adapter.in.web.dto.FrequenciaProfessorResponse;
import br.com.escola.professor.adapter.out.persistence.entity.AulaEntity;
import br.com.escola.professor.adapter.out.persistence.entity.ProfessorTurmaDisciplinaEntity;
import br.com.escola.professor.adapter.out.persistence.repository.AulaJpaRepository;
import br.com.escola.professor.adapter.out.persistence.repository.ProfessorTurmaDisciplinaJpaRepository;
import br.com.escola.professor.domain.exception.AulaFrequenciaAlunoDuplicadaException;
import br.com.escola.professor.domain.exception.AulaFrequenciaProfessorDuplicadaException;
import br.com.escola.professor.domain.exception.AulaMatriculaTurmaInconsistenteException;
import br.com.escola.professor.domain.exception.AulaNaoEncontradaException;
import br.com.escola.professor.domain.exception.SituacaoFrequenciaNaoEncontradaException;
import br.com.escola.professor.domain.exception.ProfessorTurmaDisciplinaNaoEncontradaException;
import br.com.escola.institucional.application.service.EscolaTenantService;

@Service
public class DiarioAulaService {

    private final AulaJpaRepository aulaJpaRepository;
    private final ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository;
    private final FrequenciaProfessorJpaRepository frequenciaProfessorJpaRepository;
    private final FrequenciaAlunoJpaRepository frequenciaAlunoJpaRepository;
    private final SituacaoFrequenciaJpaRepository situacaoFrequenciaJpaRepository;
    private final MatriculaJpaRepository matriculaJpaRepository;
    private final EscolaTenantService escolaTenantService;

    public DiarioAulaService(
            AulaJpaRepository aulaJpaRepository,
            ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository,
            FrequenciaProfessorJpaRepository frequenciaProfessorJpaRepository,
            FrequenciaAlunoJpaRepository frequenciaAlunoJpaRepository,
            SituacaoFrequenciaJpaRepository situacaoFrequenciaJpaRepository,
            MatriculaJpaRepository matriculaJpaRepository,
            EscolaTenantService escolaTenantService) {
        this.aulaJpaRepository = aulaJpaRepository;
        this.professorTurmaDisciplinaJpaRepository = professorTurmaDisciplinaJpaRepository;
        this.frequenciaProfessorJpaRepository = frequenciaProfessorJpaRepository;
        this.frequenciaAlunoJpaRepository = frequenciaAlunoJpaRepository;
        this.situacaoFrequenciaJpaRepository = situacaoFrequenciaJpaRepository;
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.escolaTenantService = escolaTenantService;
    }

    @Transactional
    public AulaResponse criarAula(AulaRequest request) {
        ProfessorTurmaDisciplinaEntity alocacao = professorTurmaDisciplinaJpaRepository
                .findByIdAndTurmaDisciplina_Turma_Escola_Id(request.professorTurmaDisciplinaId(), escolaId())
                .orElseThrow(ProfessorTurmaDisciplinaNaoEncontradaException::new);

        AulaEntity aula = AulaEntity.builder()
                .professorTurmaDisciplina(alocacao)
                .dataAula(request.dataAula())
                .horarioInicio(request.horarioInicio())
                .horarioFim(request.horarioFim())
                .conteudoMinistrado(request.conteudoMinistrado())
                .observacao(request.observacao())
                .realizada(request.realizada() == null ? Boolean.TRUE : request.realizada())
                .createdAt(LocalDateTime.now())
                .build();

        return toAulaResponse(aulaJpaRepository.save(aula));
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> listarAulas(UUID professorTurmaDisciplinaId, UUID turmaId) {
        if (professorTurmaDisciplinaId != null) {
            return aulaJpaRepository
                    .findByProfessorTurmaDisciplina_IdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                            professorTurmaDisciplinaId,
                            escolaId())
                    .stream()
                    .map(this::toAulaResponse)
                    .toList();
        }
        if (turmaId != null) {
            return aulaJpaRepository
                    .findByProfessorTurmaDisciplina_TurmaDisciplina_Turma_IdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                            turmaId,
                            escolaId())
                    .stream()
                    .map(this::toAulaResponse)
                    .toList();
        }
        return aulaJpaRepository.findAllByProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(escolaId()).stream()
                .map(this::toAulaResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AulaResponse buscarAulaPorId(UUID id) {
        return toAulaResponse(findAula(id));
    }

    @Transactional
    public FrequenciaProfessorResponse registrarFrequenciaProfessor(UUID aulaId, FrequenciaProfessorRequest request) {
        AulaEntity aula = findAula(aulaId);
        UUID professorId = aula.getProfessorTurmaDisciplina().getProfessor().getId();

        frequenciaProfessorJpaRepository
                .findByAula_IdAndAula_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_IdAndProfessor_Id(
                        aulaId,
                        escolaId(),
                        professorId)
                .ifPresent(frequencia -> {
                    throw new AulaFrequenciaProfessorDuplicadaException();
                });

        FrequenciaProfessorEntity entity = FrequenciaProfessorEntity.builder()
                .aula(aula)
                .professor(aula.getProfessorTurmaDisciplina().getProfessor())
                .presente(request.presente() == null ? Boolean.TRUE : request.presente())
                .justificativa(request.justificativa())
                .createdAt(LocalDateTime.now())
                .build();

        return toFrequenciaProfessorResponse(frequenciaProfessorJpaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<FrequenciaProfessorResponse> listarFrequenciaProfessor(UUID aulaId) {
        if (!aulaJpaRepository.existsByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(aulaId, escolaId())) {
            throw new AulaNaoEncontradaException();
        }
        return frequenciaProfessorJpaRepository
                .findByAula_IdAndAula_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(aulaId, escolaId())
                .stream()
                .map(this::toFrequenciaProfessorResponse)
                .toList();
    }

    @Transactional
    public FrequenciaAlunoResponse registrarFrequenciaAluno(UUID aulaId, FrequenciaAlunoRequest request) {
        AulaEntity aula = findAula(aulaId);
        MatriculaEntity matricula = matriculaJpaRepository.findByIdAndTurma_Escola_Id(request.matriculaId(), escolaId())
                .orElseThrow(() -> new AulaNaoEncontradaException("Matrícula não encontrada."));

        UUID turmaAulaId = aula.getProfessorTurmaDisciplina().getTurmaDisciplina().getTurma().getId();
        UUID turmaMatriculaId = matricula.getTurma().getId();
        if (!turmaAulaId.equals(turmaMatriculaId)) {
            throw new AulaMatriculaTurmaInconsistenteException();
        }

        frequenciaAlunoJpaRepository
                .findByAula_IdAndAula_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_IdAndMatricula_Id(
                        aulaId,
                        escolaId(),
                        request.matriculaId())
                .ifPresent(frequencia -> {
                    throw new AulaFrequenciaAlunoDuplicadaException();
                });

        SituacaoFrequenciaEntity situacao = situacaoFrequenciaJpaRepository.findByCodigo(request.situacao().toUpperCase())
                .orElseThrow(SituacaoFrequenciaNaoEncontradaException::new);

        FrequenciaAlunoEntity entity = FrequenciaAlunoEntity.builder()
                .aula(aula)
                .matricula(matricula)
                .situacaoFrequencia(situacao)
                .justificativa(request.justificativa())
                .createdAt(LocalDateTime.now())
                .build();

        return toFrequenciaAlunoResponse(frequenciaAlunoJpaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<FrequenciaAlunoResponse> listarFrequenciasAlunos(UUID aulaId) {
        if (!aulaJpaRepository.existsByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(aulaId, escolaId())) {
            throw new AulaNaoEncontradaException();
        }
        return frequenciaAlunoJpaRepository
                .findByAula_IdAndAula_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(aulaId, escolaId())
                .stream()
                .map(this::toFrequenciaAlunoResponse)
                .toList();
    }

    private AulaEntity findAula(UUID id) {
        return aulaJpaRepository.findByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(id, escolaId())
                .orElseThrow(AulaNaoEncontradaException::new);
    }

    private AulaResponse toAulaResponse(AulaEntity entity) {
        ProfessorTurmaDisciplinaEntity alocacao = entity.getProfessorTurmaDisciplina();
        return new AulaResponse(
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
                entity.getDataAula(),
                entity.getHorarioInicio(),
                entity.getHorarioFim(),
                entity.getConteudoMinistrado(),
                entity.getObservacao(),
                entity.getRealizada(),
                entity.getCreatedAt());
    }

    private FrequenciaProfessorResponse toFrequenciaProfessorResponse(FrequenciaProfessorEntity entity) {
        return new FrequenciaProfessorResponse(
                entity.getId(),
                entity.getAula().getId(),
                entity.getProfessor().getId(),
                entity.getProfessor().getPessoa().getNomeCompleto(),
                entity.getAula().getProfessorTurmaDisciplina().getTurmaDisciplina().getTurma().getEscola().getId(),
                entity.getAula().getProfessorTurmaDisciplina().getTurmaDisciplina().getTurma().getEscola().getNome(),
                entity.getPresente(),
                entity.getJustificativa(),
                entity.getCreatedAt());
    }

    private FrequenciaAlunoResponse toFrequenciaAlunoResponse(FrequenciaAlunoEntity entity) {
        return new FrequenciaAlunoResponse(
                entity.getId(),
                entity.getAula().getId(),
                entity.getMatricula().getId(),
                entity.getMatricula().getAluno().getId(),
                entity.getMatricula().getAluno().getPessoa().getNomeCompleto(),
                entity.getMatricula().getTurma().getEscola().getId(),
                entity.getMatricula().getTurma().getEscola().getNome(),
                entity.getSituacaoFrequencia().getCodigo(),
                entity.getJustificativa(),
                entity.getCreatedAt());
    }

    private UUID escolaId() {
        return escolaTenantService.obterOuCriarEscolaPadrao().getId();
    }
}
