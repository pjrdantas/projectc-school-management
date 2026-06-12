package br.com.escola.planejamento.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.avaliacao.adapter.out.persistence.entity.PeriodoAvaliativoEntity;
import br.com.escola.avaliacao.adapter.out.persistence.entity.TipoAvaliacaoEntity;
import br.com.escola.avaliacao.adapter.out.persistence.repository.PeriodoAvaliativoJpaRepository;
import br.com.escola.avaliacao.adapter.out.persistence.repository.TipoAvaliacaoJpaRepository;
import br.com.escola.avaliacao.domain.exception.TipoAvaliacaoNaoEncontradoException;
import br.com.escola.institucional.application.service.EscolaTenantService;
import br.com.escola.planejamento.adapter.in.web.dto.PlanejamentoBimestralAulaRequest;
import br.com.escola.planejamento.adapter.in.web.dto.PlanejamentoBimestralAulaResponse;
import br.com.escola.planejamento.adapter.in.web.dto.PlanejamentoBimestralAvaliacaoRequest;
import br.com.escola.planejamento.adapter.in.web.dto.PlanejamentoBimestralAvaliacaoResponse;
import br.com.escola.planejamento.adapter.in.web.dto.PlanejamentoBimestralRequest;
import br.com.escola.planejamento.adapter.in.web.dto.PlanejamentoBimestralResponse;
import br.com.escola.planejamento.adapter.in.web.dto.PlanejamentoBimestralStatusRequest;
import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoBimestralAulaEntity;
import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoBimestralAvaliacaoEntity;
import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoBimestralEntity;
import br.com.escola.planejamento.adapter.out.persistence.entity.StatusPlanejamentoEntity;
import br.com.escola.planejamento.adapter.out.persistence.repository.PlanejamentoBimestralAulaJpaRepository;
import br.com.escola.planejamento.adapter.out.persistence.repository.PlanejamentoBimestralAvaliacaoJpaRepository;
import br.com.escola.planejamento.adapter.out.persistence.repository.PlanejamentoBimestralJpaRepository;
import br.com.escola.planejamento.adapter.out.persistence.repository.StatusPlanejamentoJpaRepository;
import br.com.escola.planejamento.domain.exception.PlanejamentoAulaPrevistaDuplicadaException;
import br.com.escola.planejamento.domain.exception.PlanejamentoBimestralNaoEncontradoException;
import br.com.escola.planejamento.domain.exception.PlanejamentoPeriodoAvaliativoNaoEncontradoException;
import br.com.escola.planejamento.domain.exception.PlanejamentoStatusNaoEncontradoException;
import br.com.escola.professor.adapter.out.persistence.entity.ProfessorTurmaDisciplinaEntity;
import br.com.escola.professor.adapter.out.persistence.repository.ProfessorTurmaDisciplinaJpaRepository;
import br.com.escola.professor.domain.exception.ProfessorTurmaDisciplinaNaoEncontradaException;

@Service
public class PlanejamentoBimestralService {

    private static final String STATUS_RASCUNHO = "RASCUNHO";
    private static final String STATUS_APROVADO = "APROVADO";

    private final PlanejamentoBimestralJpaRepository planejamentoBimestralJpaRepository;
    private final PlanejamentoBimestralAulaJpaRepository planejamentoBimestralAulaJpaRepository;
    private final PlanejamentoBimestralAvaliacaoJpaRepository planejamentoBimestralAvaliacaoJpaRepository;
    private final ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository;
    private final PeriodoAvaliativoJpaRepository periodoAvaliativoJpaRepository;
    private final StatusPlanejamentoJpaRepository statusPlanejamentoJpaRepository;
    private final TipoAvaliacaoJpaRepository tipoAvaliacaoJpaRepository;
    private final EscolaTenantService escolaTenantService;

    public PlanejamentoBimestralService(
            PlanejamentoBimestralJpaRepository planejamentoBimestralJpaRepository,
            PlanejamentoBimestralAulaJpaRepository planejamentoBimestralAulaJpaRepository,
            PlanejamentoBimestralAvaliacaoJpaRepository planejamentoBimestralAvaliacaoJpaRepository,
            ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository,
            PeriodoAvaliativoJpaRepository periodoAvaliativoJpaRepository,
            StatusPlanejamentoJpaRepository statusPlanejamentoJpaRepository,
            TipoAvaliacaoJpaRepository tipoAvaliacaoJpaRepository,
            EscolaTenantService escolaTenantService) {
        this.planejamentoBimestralJpaRepository = planejamentoBimestralJpaRepository;
        this.planejamentoBimestralAulaJpaRepository = planejamentoBimestralAulaJpaRepository;
        this.planejamentoBimestralAvaliacaoJpaRepository = planejamentoBimestralAvaliacaoJpaRepository;
        this.professorTurmaDisciplinaJpaRepository = professorTurmaDisciplinaJpaRepository;
        this.periodoAvaliativoJpaRepository = periodoAvaliativoJpaRepository;
        this.statusPlanejamentoJpaRepository = statusPlanejamentoJpaRepository;
        this.tipoAvaliacaoJpaRepository = tipoAvaliacaoJpaRepository;
        this.escolaTenantService = escolaTenantService;
    }

    @Transactional
    public PlanejamentoBimestralResponse criar(PlanejamentoBimestralRequest request) {
        ProfessorTurmaDisciplinaEntity alocacao = findAlocacao(request.professorTurmaDisciplinaId());
        PeriodoAvaliativoEntity periodoAvaliativo = findPeriodoAvaliativo(request.periodoAvaliativoId());
        StatusPlanejamentoEntity statusRascunho = findStatus(STATUS_RASCUNHO);

        PlanejamentoBimestralEntity entity = PlanejamentoBimestralEntity.builder()
                .professorTurmaDisciplina(alocacao)
                .periodoAvaliativo(periodoAvaliativo)
                .statusPlanejamento(statusRascunho)
                .titulo(request.titulo())
                .temaPrincipal(request.temaPrincipal())
                .descricaoInicial(request.descricaoInicial())
                .objetivoGeral(request.objetivoGeral())
                .observacaoProfessor(request.observacaoProfessor())
                .conteudoFinalAprovado(request.conteudoFinalAprovado())
                .reutilizavel(Boolean.TRUE.equals(request.reutilizavel()))
                .criadoComAuxilioIA(Boolean.TRUE.equals(request.criadoComAuxilioIA()))
                .aprovadoPeloProfessor(Boolean.FALSE)
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(planejamentoBimestralJpaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<PlanejamentoBimestralResponse> listar(
            UUID professorId,
            UUID turmaId,
            UUID disciplinaId,
            UUID periodoAvaliativoId) {
        return planejamentoBimestralJpaRepository
                .filtrar(professorId, turmaId, disciplinaId, periodoAvaliativoId, escolaId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanejamentoBimestralResponse buscarPorId(UUID id) {
        return toResponse(findPlanejamento(id));
    }

    @Transactional
    public PlanejamentoBimestralResponse atualizar(UUID id, PlanejamentoBimestralRequest request) {
        PlanejamentoBimestralEntity planejamento = findPlanejamento(id);

        planejamento.setProfessorTurmaDisciplina(findAlocacao(request.professorTurmaDisciplinaId()));
        planejamento.setPeriodoAvaliativo(findPeriodoAvaliativo(request.periodoAvaliativoId()));
        planejamento.setTitulo(request.titulo());
        planejamento.setTemaPrincipal(request.temaPrincipal());
        planejamento.setDescricaoInicial(request.descricaoInicial());
        planejamento.setObjetivoGeral(request.objetivoGeral());
        planejamento.setObservacaoProfessor(request.observacaoProfessor());
        planejamento.setConteudoFinalAprovado(request.conteudoFinalAprovado());
        planejamento.setReutilizavel(Boolean.TRUE.equals(request.reutilizavel()));
        planejamento.setCriadoComAuxilioIA(Boolean.TRUE.equals(request.criadoComAuxilioIA()));
        planejamento.setUpdatedAt(LocalDateTime.now());

        return toResponse(planejamentoBimestralJpaRepository.save(planejamento));
    }

    @Transactional
    public PlanejamentoBimestralAulaResponse adicionarAulaPrevista(UUID planejamentoId, PlanejamentoBimestralAulaRequest request) {
        PlanejamentoBimestralEntity planejamento = findPlanejamento(planejamentoId);

        planejamentoBimestralAulaJpaRepository
                .findByPlanejamentoBimestralIdAndNumeroAula(planejamentoId, request.numeroAula())
                .ifPresent(aula -> {
                    throw new PlanejamentoAulaPrevistaDuplicadaException();
                });

        PlanejamentoBimestralAulaEntity aula = PlanejamentoBimestralAulaEntity.builder()
                .planejamentoBimestral(planejamento)
                .numeroAula(request.numeroAula())
                .temaAula(request.temaAula())
                .objetivoAula(request.objetivoAula())
                .conteudoPrevisto(request.conteudoPrevisto())
                .metodologia(request.metodologia())
                .recursos(request.recursos())
                .atividadePrevista(request.atividadePrevista())
                .observacao(request.observacao())
                .createdAt(LocalDateTime.now())
                .build();

        return toAulaResponse(planejamentoBimestralAulaJpaRepository.save(aula));
    }

    @Transactional
    public PlanejamentoBimestralAvaliacaoResponse adicionarAvaliacaoPrevista(
            UUID planejamentoId,
            PlanejamentoBimestralAvaliacaoRequest request) {
        PlanejamentoBimestralEntity planejamento = findPlanejamento(planejamentoId);
        TipoAvaliacaoEntity tipoAvaliacao = tipoAvaliacaoJpaRepository.findByCodigo(request.tipoAvaliacao().toUpperCase())
                .orElseThrow(TipoAvaliacaoNaoEncontradoException::new);

        PlanejamentoBimestralAvaliacaoEntity avaliacao = PlanejamentoBimestralAvaliacaoEntity.builder()
                .planejamentoBimestral(planejamento)
                .tipoAvaliacao(tipoAvaliacao)
                .titulo(request.titulo())
                .descricao(request.descricao())
                .dataPrevista(request.dataPrevista())
                .peso(request.peso())
                .valorMaximo(request.valorMaximo())
                .conteudoCobrado(request.conteudoCobrado())
                .orientacaoAplicacao(request.orientacaoAplicacao())
                .createdAt(LocalDateTime.now())
                .build();

        return toAvaliacaoResponse(planejamentoBimestralAvaliacaoJpaRepository.save(avaliacao));
    }

    @Transactional
    public PlanejamentoBimestralResponse alterarStatus(UUID id, PlanejamentoBimestralStatusRequest request) {
        PlanejamentoBimestralEntity planejamento = findPlanejamento(id);
        String codigoStatus = request.status().toUpperCase();
        StatusPlanejamentoEntity status = findStatus(codigoStatus);

        planejamento.setStatusPlanejamento(status);
        planejamento.setUpdatedAt(LocalDateTime.now());
        planejamento.setAprovadoPeloProfessor(STATUS_APROVADO.equals(codigoStatus));
        planejamento.setDataAprovacao(STATUS_APROVADO.equals(codigoStatus) ? LocalDateTime.now() : null);

        return toResponse(planejamentoBimestralJpaRepository.save(planejamento));
    }

    private PlanejamentoBimestralEntity findPlanejamento(UUID id) {
        return planejamentoBimestralJpaRepository
                .findByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(id, escolaId())
                .orElseThrow(PlanejamentoBimestralNaoEncontradoException::new);
    }

    private ProfessorTurmaDisciplinaEntity findAlocacao(UUID id) {
        return professorTurmaDisciplinaJpaRepository.findByIdAndTurmaDisciplina_Turma_Escola_Id(id, escolaId())
                .orElseThrow(ProfessorTurmaDisciplinaNaoEncontradaException::new);
    }

    private PeriodoAvaliativoEntity findPeriodoAvaliativo(UUID id) {
        if (id == null) {
            return null;
        }
        return periodoAvaliativoJpaRepository.findByIdAndPeriodoLetivo_Escola_Id(id, escolaId())
                .orElseThrow(PlanejamentoPeriodoAvaliativoNaoEncontradoException::new);
    }

    private StatusPlanejamentoEntity findStatus(String codigo) {
        return statusPlanejamentoJpaRepository.findByCodigo(codigo)
                .orElseThrow(PlanejamentoStatusNaoEncontradoException::new);
    }

    private PlanejamentoBimestralResponse toResponse(PlanejamentoBimestralEntity entity) {
        ProfessorTurmaDisciplinaEntity alocacao = entity.getProfessorTurmaDisciplina();
        PeriodoAvaliativoEntity periodoAvaliativo = entity.getPeriodoAvaliativo();
        StatusPlanejamentoEntity status = entity.getStatusPlanejamento();

        return new PlanejamentoBimestralResponse(
                entity.getId(),
                alocacao.getId(),
                alocacao.getProfessor().getId(),
                alocacao.getProfessor().getPessoa().getNomeCompleto(),
                alocacao.getTurmaDisciplina().getTurma().getId(),
                alocacao.getTurmaDisciplina().getTurma().getNome(),
                alocacao.getTurmaDisciplina().getDisciplina().getId(),
                alocacao.getTurmaDisciplina().getDisciplina().getNome(),
                alocacao.getTurmaDisciplina().getTurma().getEscola().getId(),
                alocacao.getTurmaDisciplina().getTurma().getEscola().getNome(),
                periodoAvaliativo == null ? null : periodoAvaliativo.getId(),
                periodoAvaliativo == null ? null : periodoAvaliativo.getNome(),
                status == null ? null : status.getCodigo(),
                status == null ? null : status.getDescricao(),
                entity.getTitulo(),
                entity.getTemaPrincipal(),
                entity.getDescricaoInicial(),
                entity.getObjetivoGeral(),
                entity.getObservacaoProfessor(),
                entity.getConteudoFinalAprovado(),
                entity.getReutilizavel(),
                entity.getCriadoComAuxilioIA(),
                entity.getAprovadoPeloProfessor(),
                entity.getDataAprovacao(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                planejamentoBimestralAulaJpaRepository.findByPlanejamentoBimestralId(entity.getId()).stream()
                        .map(this::toAulaResponse)
                        .toList(),
                planejamentoBimestralAvaliacaoJpaRepository.findByPlanejamentoBimestralId(entity.getId()).stream()
                        .map(this::toAvaliacaoResponse)
                        .toList());
    }

    private UUID escolaId() {
        return escolaTenantService.obterOuCriarEscolaPadrao().getId();
    }

    private PlanejamentoBimestralAulaResponse toAulaResponse(PlanejamentoBimestralAulaEntity entity) {
        return new PlanejamentoBimestralAulaResponse(
                entity.getId(),
                entity.getPlanejamentoBimestral().getId(),
                entity.getNumeroAula(),
                entity.getTemaAula(),
                entity.getObjetivoAula(),
                entity.getConteudoPrevisto(),
                entity.getMetodologia(),
                entity.getRecursos(),
                entity.getAtividadePrevista(),
                entity.getObservacao(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private PlanejamentoBimestralAvaliacaoResponse toAvaliacaoResponse(PlanejamentoBimestralAvaliacaoEntity entity) {
        return new PlanejamentoBimestralAvaliacaoResponse(
                entity.getId(),
                entity.getPlanejamentoBimestral().getId(),
                entity.getTitulo(),
                entity.getDescricao(),
                entity.getDataPrevista(),
                entity.getPeso(),
                entity.getValorMaximo(),
                entity.getTipoAvaliacao().getCodigo(),
                entity.getConteudoCobrado(),
                entity.getOrientacaoAplicacao(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
