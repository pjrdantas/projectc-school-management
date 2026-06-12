package br.com.escola.professor.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaDisciplinaEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurmaDisciplinaJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurmaJpaRepository;
import br.com.escola.catalogo.domain.exception.TurmaNaoEncontradaException;
import br.com.escola.professor.adapter.in.web.dto.ProfessorAlocacaoRequest;
import br.com.escola.professor.adapter.in.web.dto.ProfessorAlocacaoResponse;
import br.com.escola.professor.adapter.in.web.dto.ProfessorFuncionarioElegivelResponse;
import br.com.escola.professor.adapter.in.web.dto.ProfessorRequest;
import br.com.escola.professor.adapter.in.web.dto.ProfessorResponse;
import br.com.escola.professor.adapter.out.persistence.entity.ProfessorEntity;
import br.com.escola.professor.adapter.out.persistence.entity.ProfessorTurmaDisciplinaEntity;
import br.com.escola.professor.adapter.out.persistence.repository.ProfessorJpaRepository;
import br.com.escola.professor.adapter.out.persistence.repository.ProfessorTurmaDisciplinaJpaRepository;
import br.com.escola.professor.domain.exception.ProfessorFuncionarioInativoException;
import br.com.escola.professor.domain.exception.ProfessorJaCadastradoException;
import br.com.escola.professor.domain.exception.ProfessorNaoEncontradoException;
import br.com.escola.professor.domain.exception.ProfessorTurmaDisciplinaDuplicadaException;
import br.com.escola.professor.domain.exception.ProfessorTurmaDisciplinaNaoEncontradaException;
import br.com.escola.institucional.application.service.EscolaTenantService;
import br.com.escola.rh.adapter.out.persistence.entity.FuncionarioEntity;
import br.com.escola.rh.adapter.out.persistence.repository.FuncionarioJpaRepository;

@Service
public class ProfessorService {

    private final ProfessorJpaRepository professorJpaRepository;
    private final FuncionarioJpaRepository funcionarioJpaRepository;
    private final TurmaJpaRepository turmaJpaRepository;
    private final TurmaDisciplinaJpaRepository turmaDisciplinaJpaRepository;
    private final ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository;
    private final EscolaTenantService escolaTenantService;

    public ProfessorService(
            ProfessorJpaRepository professorJpaRepository,
            FuncionarioJpaRepository funcionarioJpaRepository,
            TurmaJpaRepository turmaJpaRepository,
            TurmaDisciplinaJpaRepository turmaDisciplinaJpaRepository,
            ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository,
            EscolaTenantService escolaTenantService) {
        this.professorJpaRepository = professorJpaRepository;
        this.funcionarioJpaRepository = funcionarioJpaRepository;
        this.turmaJpaRepository = turmaJpaRepository;
        this.turmaDisciplinaJpaRepository = turmaDisciplinaJpaRepository;
        this.professorTurmaDisciplinaJpaRepository = professorTurmaDisciplinaJpaRepository;
        this.escolaTenantService = escolaTenantService;
    }

    @Transactional
    public ProfessorResponse criar(ProfessorRequest request) {
        UUID escolaId = escolaPadraoId();
        FuncionarioEntity funcionario = funcionarioJpaRepository.findByIdAndPessoa_Escola_Id(request.funcionarioId(), escolaId)
                .orElseThrow(() -> new ProfessorNaoEncontradoException("Funcionário não encontrado."));

        if (Boolean.FALSE.equals(funcionario.getAtivo())) {
            throw new ProfessorFuncionarioInativoException();
        }

        professorJpaRepository.findByPessoa_IdAndPessoa_Escola_Id(funcionario.getPessoa().getId(), escolaId)
                .ifPresent(professor -> {
                    throw new ProfessorJaCadastradoException();
                });

        ProfessorEntity professor = ProfessorEntity.builder()
                .pessoa(funcionario.getPessoa())
                .registroProfissional(request.registroProfissional())
                .formacao(request.formacao())
                .ativo(request.ativo() == null ? Boolean.TRUE : request.ativo())
                .createdAt(LocalDateTime.now())
                .build();

        return toProfessorResponse(professorJpaRepository.save(professor));
    }

    @Transactional(readOnly = true)
    public List<ProfessorResponse> listar() {
        return professorJpaRepository.findAllByPessoa_Escola_Id(escolaPadraoId()).stream()
                .map(this::toProfessorResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProfessorFuncionarioElegivelResponse> listarFuncionariosElegiveis() {
        UUID escolaId = escolaPadraoId();
        return funcionarioJpaRepository.findAllByPessoa_Escola_Id(escolaId).stream()
                .filter(funcionario -> Boolean.TRUE.equals(funcionario.getAtivo()))
                .filter(funcionario -> !professorJpaRepository.existsByPessoa_IdAndPessoa_Escola_Id(
                        funcionario.getPessoa().getId(), escolaId))
                .sorted((left, right) -> left.getPessoa().getNomeCompleto()
                        .compareToIgnoreCase(right.getPessoa().getNomeCompleto()))
                .map(this::toFuncionarioElegivelResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfessorResponse buscarPorId(UUID id) {
        return toProfessorResponse(findProfessor(id));
    }

    @Transactional
    public ProfessorAlocacaoResponse vincularTurmaDisciplina(UUID professorId, ProfessorAlocacaoRequest request) {
        ProfessorEntity professor = findProfessor(professorId);
        TurmaDisciplinaEntity turmaDisciplina = turmaDisciplinaJpaRepository
                .findByIdAndTurma_Escola_Id(request.turmaDisciplinaId(), escolaPadraoId())
                .orElseThrow(ProfessorTurmaDisciplinaNaoEncontradaException::new);

        professorTurmaDisciplinaJpaRepository.findByProfessorIdAndTurmaDisciplinaId(professorId, request.turmaDisciplinaId())
                .ifPresent(vinculo -> {
                    throw new ProfessorTurmaDisciplinaDuplicadaException();
                });

        ProfessorTurmaDisciplinaEntity entity = ProfessorTurmaDisciplinaEntity.builder()
                .professor(professor)
                .turmaDisciplina(turmaDisciplina)
                .dataInicio(request.dataInicio())
                .dataFim(request.dataFim())
                .ativo(request.ativo() == null ? Boolean.TRUE : request.ativo())
                .createdAt(LocalDateTime.now())
                .build();

        return toAlocacaoResponse(professorTurmaDisciplinaJpaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<ProfessorAlocacaoResponse> listarAlocacoes(UUID professorId) {
        if (!professorJpaRepository.existsByIdAndPessoa_Escola_Id(professorId, escolaPadraoId())) {
            throw new ProfessorNaoEncontradoException();
        }
        return professorTurmaDisciplinaJpaRepository.findByProfessorId(professorId).stream()
                .map(this::toAlocacaoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProfessorAlocacaoResponse> listarPorTurma(UUID turmaId) {
        if (!turmaJpaRepository.existsByIdAndEscola_Id(turmaId, escolaPadraoId())) {
            throw new TurmaNaoEncontradaException(turmaId);
        }
        return professorTurmaDisciplinaJpaRepository.findByTurmaDisciplinaTurmaId(turmaId).stream()
                .map(this::toAlocacaoResponse)
                .toList();
    }

    private ProfessorEntity findProfessor(UUID id) {
        return professorJpaRepository.findByIdAndPessoa_Escola_Id(id, escolaPadraoId())
                .orElseThrow(ProfessorNaoEncontradoException::new);
    }

    private ProfessorResponse toProfessorResponse(ProfessorEntity entity) {
        return new ProfessorResponse(
                entity.getId(),
                entity.getPessoa().getId(),
                entity.getPessoa().getNomeCompleto(),
                entity.getPessoa().getEscola().getId(),
                entity.getPessoa().getEscola().getNome(),
                entity.getRegistroProfissional(),
                entity.getFormacao(),
                entity.getAtivo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private ProfessorFuncionarioElegivelResponse toFuncionarioElegivelResponse(FuncionarioEntity entity) {
        return new ProfessorFuncionarioElegivelResponse(
                entity.getId(),
                entity.getPessoa().getNomeCompleto(),
                entity.getPessoa().getEscola().getId(),
                entity.getPessoa().getEscola().getNome(),
                entity.getCargo() == null ? null : entity.getCargo().getDescricao(),
                entity.getAtivo());
    }

    private ProfessorAlocacaoResponse toAlocacaoResponse(ProfessorTurmaDisciplinaEntity entity) {
        TurmaDisciplinaEntity turmaDisciplina = entity.getTurmaDisciplina();
        return new ProfessorAlocacaoResponse(
                entity.getId(),
                entity.getProfessor().getId(),
                entity.getProfessor().getPessoa().getNomeCompleto(),
                turmaDisciplina.getId(),
                turmaDisciplina.getTurma().getId(),
                turmaDisciplina.getTurma().getNome(),
                turmaDisciplina.getDisciplina().getId(),
                turmaDisciplina.getDisciplina().getNome(),
                entity.getDataInicio(),
                entity.getDataFim(),
                entity.getAtivo(),
                entity.getCreatedAt());
    }

    private UUID escolaPadraoId() {
        return escolaTenantService.obterOuCriarEscolaPadrao().getId();
    }
}
