package br.com.escola.professor.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Primary;
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
import br.com.escola.professor.application.dto.internal.AlocarProfessorTurmaDisciplinaSolicitacao;
import br.com.escola.professor.application.dto.internal.CriarProfessorSolicitacao;
import br.com.escola.professor.application.dto.internal.ProfessorAlocacaoResumo;
import br.com.escola.professor.application.dto.internal.ProfessorResumo;
import br.com.escola.professor.application.port.internal.ProfessorAcademicoPort;
import br.com.escola.professor.domain.exception.ProfessorFuncionarioInativoException;
import br.com.escola.professor.domain.exception.ProfessorJaCadastradoException;
import br.com.escola.professor.domain.exception.ProfessorNaoEncontradoException;
import br.com.escola.professor.domain.exception.ProfessorTurmaDisciplinaDuplicadaException;
import br.com.escola.professor.domain.exception.ProfessorTurmaDisciplinaNaoEncontradaException;
import br.com.escola.institucional.application.service.EscolaTenantService;
import br.com.escola.rh.adapter.out.persistence.entity.FuncionarioEntity;
import br.com.escola.rh.adapter.out.persistence.repository.FuncionarioJpaRepository;

@Service
@Primary
public class ProfessorService implements ProfessorAcademicoPort {

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
        return toProfessorResponse(criarProfessor(null, new CriarProfessorSolicitacao(
                request.funcionarioId(),
                request.registroProfissional(),
                request.formacao(),
                request.ativo())));
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
        return buscarProfessor(null, id)
                .map(this::toProfessorResponse)
                .orElseThrow(ProfessorNaoEncontradoException::new);
    }

    @Transactional
    public ProfessorAlocacaoResponse vincularTurmaDisciplina(UUID professorId, ProfessorAlocacaoRequest request) {
        return toAlocacaoResponse(alocarProfessorTurmaDisciplina(
                null,
                professorId,
                new AlocarProfessorTurmaDisciplinaSolicitacao(
                        request.turmaDisciplinaId(),
                        request.dataInicio(),
                        request.dataFim(),
                        request.ativo())));
    }

    @Transactional(readOnly = true)
    public List<ProfessorAlocacaoResponse> listarAlocacoes(UUID professorId) {
        return listarAlocacoes(null, professorId).stream()
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

    @Override
    @Transactional
    public ProfessorResumo criarProfessor(UUID escolaId, CriarProfessorSolicitacao solicitacao) {
        UUID escolaResolvidaId = resolverEscolaId(escolaId);
        FuncionarioEntity funcionario = funcionarioJpaRepository
                .findByIdAndPessoa_Escola_Id(solicitacao.funcionarioId(), escolaResolvidaId)
                .orElseThrow(() -> new ProfessorNaoEncontradoException("Funcionário não encontrado."));

        if (Boolean.FALSE.equals(funcionario.getAtivo())) {
            throw new ProfessorFuncionarioInativoException();
        }

        professorJpaRepository.findByPessoa_IdAndPessoa_Escola_Id(funcionario.getPessoa().getId(), escolaResolvidaId)
                .ifPresent(professor -> {
                    throw new ProfessorJaCadastradoException();
                });

        ProfessorEntity professor = ProfessorEntity.builder()
                .pessoa(funcionario.getPessoa())
                .registroProfissional(solicitacao.registroProfissional())
                .formacao(solicitacao.formacao())
                .ativo(solicitacao.ativo() == null ? Boolean.TRUE : solicitacao.ativo())
                .createdAt(LocalDateTime.now())
                .build();

        return toProfessorResumo(professorJpaRepository.save(professor));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProfessorResumo> buscarProfessor(UUID escolaId, UUID professorId) {
        return professorJpaRepository.findByIdAndPessoa_Escola_Id(professorId, resolverEscolaId(escolaId))
                .map(this::toProfessorResumo);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeProfessor(UUID escolaId, UUID professorId) {
        return professorJpaRepository.existsByIdAndPessoa_Escola_Id(professorId, resolverEscolaId(escolaId));
    }

    @Override
    @Transactional
    public ProfessorAlocacaoResumo alocarProfessorTurmaDisciplina(
            UUID escolaId,
            UUID professorId,
            AlocarProfessorTurmaDisciplinaSolicitacao solicitacao) {
        UUID escolaResolvidaId = resolverEscolaId(escolaId);
        ProfessorEntity professor = findProfessor(professorId, escolaResolvidaId);
        TurmaDisciplinaEntity turmaDisciplina = turmaDisciplinaJpaRepository
                .findByIdAndTurma_Escola_Id(solicitacao.turmaDisciplinaId(), escolaResolvidaId)
                .orElseThrow(ProfessorTurmaDisciplinaNaoEncontradaException::new);

        professorTurmaDisciplinaJpaRepository
                .findByProfessorIdAndTurmaDisciplinaId(professorId, solicitacao.turmaDisciplinaId())
                .ifPresent(vinculo -> {
                    throw new ProfessorTurmaDisciplinaDuplicadaException();
                });

        ProfessorTurmaDisciplinaEntity entity = ProfessorTurmaDisciplinaEntity.builder()
                .professor(professor)
                .turmaDisciplina(turmaDisciplina)
                .dataInicio(solicitacao.dataInicio())
                .dataFim(solicitacao.dataFim())
                .ativo(solicitacao.ativo() == null ? Boolean.TRUE : solicitacao.ativo())
                .createdAt(LocalDateTime.now())
                .build();

        return toAlocacaoResumo(professorTurmaDisciplinaJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfessorAlocacaoResumo> listarAlocacoes(UUID escolaId, UUID professorId) {
        if (!existeProfessor(escolaId, professorId)) {
            throw new ProfessorNaoEncontradoException();
        }
        return professorTurmaDisciplinaJpaRepository.findByProfessorId(professorId).stream()
                .map(this::toAlocacaoResumo)
                .toList();
    }

    private ProfessorEntity findProfessor(UUID id, UUID escolaId) {
        return professorJpaRepository.findByIdAndPessoa_Escola_Id(id, escolaId)
                .orElseThrow(ProfessorNaoEncontradoException::new);
    }

    private ProfessorResponse toProfessorResponse(ProfessorEntity entity) {
        return toProfessorResponse(toProfessorResumo(entity));
    }

    private ProfessorResponse toProfessorResponse(ProfessorResumo resumo) {
        return new ProfessorResponse(
                resumo.id(),
                resumo.pessoaId(),
                resumo.nomeCompleto(),
                resumo.escolaId(),
                resumo.escolaNome(),
                resumo.registroProfissional(),
                resumo.formacao(),
                resumo.ativo(),
                resumo.createdAt(),
                resumo.updatedAt());
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
        return toAlocacaoResponse(toAlocacaoResumo(entity));
    }

    private ProfessorAlocacaoResponse toAlocacaoResponse(ProfessorAlocacaoResumo resumo) {
        return new ProfessorAlocacaoResponse(
                resumo.id(),
                resumo.professorId(),
                resumo.professorNome(),
                resumo.turmaDisciplinaId(),
                resumo.turmaId(),
                resumo.turmaNome(),
                resumo.disciplinaId(),
                resumo.disciplinaNome(),
                resumo.dataInicio(),
                resumo.dataFim(),
                resumo.ativo(),
                resumo.createdAt());
    }

    private ProfessorResumo toProfessorResumo(ProfessorEntity entity) {
        return new ProfessorResumo(
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

    private ProfessorAlocacaoResumo toAlocacaoResumo(ProfessorTurmaDisciplinaEntity entity) {
        TurmaDisciplinaEntity turmaDisciplina = entity.getTurmaDisciplina();
        return new ProfessorAlocacaoResumo(
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

    private UUID resolverEscolaId(UUID escolaId) {
        return escolaId == null ? escolaPadraoId() : escolaId;
    }

    private UUID escolaPadraoId() {
        return escolaTenantService.obterOuCriarEscolaPadrao().getId();
    }
}
