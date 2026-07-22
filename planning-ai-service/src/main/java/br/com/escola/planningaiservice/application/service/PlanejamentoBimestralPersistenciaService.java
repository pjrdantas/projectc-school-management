package br.com.escola.planningaiservice.application.service;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.planningaiservice.application.context.InternalRequestContext;
import br.com.escola.planningaiservice.application.dto.PlanejamentoBimestralRequest;
import br.com.escola.planningaiservice.application.dto.PlanejamentoBimestralResponse;
import br.com.escola.planningaiservice.application.dto.PlanejamentoBimestralStatusRequest;
import br.com.escola.planningaiservice.application.dto.PlanejamentoBimestralAulaRequest;
import br.com.escola.planningaiservice.application.dto.PlanejamentoBimestralAulaResponse;
import br.com.escola.planningaiservice.application.dto.PlanejamentoBimestralAvaliacaoRequest;
import br.com.escola.planningaiservice.application.dto.PlanejamentoBimestralAvaliacaoResponse;
import br.com.escola.planningaiservice.application.exception.ConflitoNegocioException;
import br.com.escola.planningaiservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.planningaiservice.application.port.in.PlanejamentoBimestralUseCase;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanejamentoBimestralJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanejamentoBimestralAulaJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.entity.PlanejamentoBimestralAvaliacaoJpaEntity;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanejamentoBimestralJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanejamentoBimestralAulaJpaRepository;
import br.com.escola.planningaiservice.infra.persistence.jpa.repository.PlanejamentoBimestralAvaliacaoJpaRepository;

@Service
public class PlanejamentoBimestralPersistenciaService implements PlanejamentoBimestralUseCase {

    private static final String STATUS_RASCUNHO = "RASCUNHO";
    private static final String STATUS_EM_ANALISE = "EM_ANALISE";
    private static final String STATUS_APROVADO = "APROVADO";
    private static final String STATUS_REPROVADO = "REPROVADO";
    private static final Set<String> STATUS_VALIDOS = Set.of(
            STATUS_RASCUNHO, STATUS_EM_ANALISE, STATUS_APROVADO, STATUS_REPROVADO);

    private final PlanejamentoBimestralJpaRepository repository;
    private final PlanejamentoBimestralAulaJpaRepository aulaRepository;
    private final PlanejamentoBimestralAvaliacaoJpaRepository avaliacaoRepository;

    public PlanejamentoBimestralPersistenciaService(
            PlanejamentoBimestralJpaRepository repository,
            PlanejamentoBimestralAulaJpaRepository aulaRepository,
            PlanejamentoBimestralAvaliacaoJpaRepository avaliacaoRepository) {
        this.repository = repository;
        this.aulaRepository = aulaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    @Override
    @Transactional
    public PlanejamentoBimestralResponse criar(InternalRequestContext context, PlanejamentoBimestralRequest request) {
        PlanejamentoBimestralJpaEntity entity = new PlanejamentoBimestralJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setEscolaId(context.escolaId());
        entity.setProfessorTurmaDisciplinaId(request.professorTurmaDisciplinaId());
        entity.setPeriodoAvaliativoId(request.periodoAvaliativoId());
        entity.setStatus(STATUS_RASCUNHO);
        entity.setTitulo(request.titulo());
        entity.setTemaPrincipal(request.temaPrincipal());
        entity.setDescricaoInicial(request.descricaoInicial());
        entity.setObjetivoGeral(request.objetivoGeral());
        entity.setObservacaoProfessor(request.observacaoProfessor());
        entity.setConteudoFinalAprovado(request.conteudoFinalAprovado());
        entity.setReutilizavel(Boolean.TRUE.equals(request.reutilizavel()));
        entity.setCriadoComAuxilioIa(Boolean.TRUE.equals(request.criadoComAuxilioIa()));
        entity.setAprovadoPeloProfessor(false);
        entity.setCreatedAt(LocalDateTime.now());
        return response(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanejamentoBimestralResponse> listar(
            InternalRequestContext context,
            UUID professorTurmaDisciplinaId,
            UUID periodoAvaliativoId) {
        return findAll(context.escolaId(), professorTurmaDisciplinaId, periodoAvaliativoId).stream()
                .map(this::response)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanejamentoBimestralResponse buscarPorId(InternalRequestContext context, UUID planejamentoId) {
        return response(repository.findByIdAndEscolaId(planejamentoId, context.escolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Planejamento bimestral nao encontrado")));
    }

    @Override
    @Transactional
    public PlanejamentoBimestralResponse atualizar(
            InternalRequestContext context,
            UUID planejamentoId,
            PlanejamentoBimestralRequest request) {
        PlanejamentoBimestralJpaEntity entity = find(context.escolaId(), planejamentoId);
        entity.setProfessorTurmaDisciplinaId(request.professorTurmaDisciplinaId());
        entity.setPeriodoAvaliativoId(request.periodoAvaliativoId());
        entity.setTitulo(request.titulo());
        entity.setTemaPrincipal(request.temaPrincipal());
        entity.setDescricaoInicial(request.descricaoInicial());
        entity.setObjetivoGeral(request.objetivoGeral());
        entity.setObservacaoProfessor(request.observacaoProfessor());
        entity.setConteudoFinalAprovado(request.conteudoFinalAprovado());
        entity.setReutilizavel(Boolean.TRUE.equals(request.reutilizavel()));
        entity.setCriadoComAuxilioIa(Boolean.TRUE.equals(request.criadoComAuxilioIa()));
        entity.setUpdatedAt(LocalDateTime.now());
        return response(repository.save(entity));
    }

    @Override
    @Transactional
    public PlanejamentoBimestralResponse alterarStatus(
            InternalRequestContext context,
            UUID planejamentoId,
            PlanejamentoBimestralStatusRequest request) {
        PlanejamentoBimestralJpaEntity entity = find(context.escolaId(), planejamentoId);
        String target = request.status().trim().toUpperCase();
        if (!STATUS_VALIDOS.contains(target)) {
            throw new IllegalArgumentException("Status de planejamento invalido");
        }
        if (!entity.getStatus().equals(target) && !canTransition(entity.getStatus(), target)) {
            throw new ConflitoNegocioException("Transicao de status de planejamento invalida");
        }
        if (!entity.getStatus().equals(target)) {
            entity.setStatus(target);
            entity.setAprovadoPeloProfessor(STATUS_APROVADO.equals(target));
            entity.setDataAprovacao(STATUS_APROVADO.equals(target) ? LocalDateTime.now() : null);
            entity.setUpdatedAt(LocalDateTime.now());
        }
        return response(repository.save(entity));
    }

    @Override
    @Transactional
    public PlanejamentoBimestralAulaResponse adicionarAula(
            InternalRequestContext context,
            UUID planejamentoId,
            PlanejamentoBimestralAulaRequest request) {
        find(context.escolaId(), planejamentoId);
        return aulaRepository.findByPlanejamentoBimestralIdAndNumeroAula(planejamentoId, request.numeroAula())
                .map(existing -> sameAula(existing, request)
                        ? aulaResponse(existing)
                        : throwConflitoAula())
                .orElseGet(() -> {
                    PlanejamentoBimestralAulaJpaEntity entity = new PlanejamentoBimestralAulaJpaEntity();
                    entity.setId(UUID.randomUUID());
                    entity.setPlanejamentoBimestralId(planejamentoId);
                    entity.setNumeroAula(request.numeroAula());
                    entity.setTemaAula(request.temaAula());
                    entity.setObjetivoAula(request.objetivoAula());
                    entity.setConteudoPrevisto(request.conteudoPrevisto());
                    entity.setMetodologia(request.metodologia());
                    entity.setRecursos(request.recursos());
                    entity.setAtividadePrevista(request.atividadePrevista());
                    entity.setObservacao(request.observacao());
                    entity.setCreatedAt(LocalDateTime.now());
                    return aulaResponse(aulaRepository.save(entity));
                });
    }

    @Override
    @Transactional
    public PlanejamentoBimestralAvaliacaoResponse adicionarAvaliacao(
            InternalRequestContext context,
            UUID planejamentoId,
            PlanejamentoBimestralAvaliacaoRequest request) {
        find(context.escolaId(), planejamentoId);
        String key = assessmentKey(request);
        return avaliacaoRepository.findByPlanejamentoBimestralIdAndChaveIdempotencia(planejamentoId, key)
                .map(this::avaliacaoResponse)
                .orElseGet(() -> {
                    PlanejamentoBimestralAvaliacaoJpaEntity entity = new PlanejamentoBimestralAvaliacaoJpaEntity();
                    entity.setId(UUID.randomUUID());
                    entity.setPlanejamentoBimestralId(planejamentoId);
                    entity.setTitulo(request.titulo());
                    entity.setDescricao(request.descricao());
                    entity.setDataPrevista(request.dataPrevista());
                    entity.setPeso(request.peso());
                    entity.setValorMaximo(request.valorMaximo());
                    entity.setTipoAvaliacao(request.tipoAvaliacao().trim().toUpperCase());
                    entity.setConteudoCobrado(request.conteudoCobrado());
                    entity.setOrientacaoAplicacao(request.orientacaoAplicacao());
                    entity.setChaveIdempotencia(key);
                    entity.setCreatedAt(LocalDateTime.now());
                    return avaliacaoResponse(avaliacaoRepository.save(entity));
                });
    }

    private List<PlanejamentoBimestralJpaEntity> findAll(UUID escolaId, UUID professorTurmaDisciplinaId, UUID periodoAvaliativoId) {
        if (professorTurmaDisciplinaId != null && periodoAvaliativoId != null) {
            return repository.findByEscolaIdAndProfessorTurmaDisciplinaIdAndPeriodoAvaliativoIdOrderByCreatedAtDesc(
                    escolaId, professorTurmaDisciplinaId, periodoAvaliativoId);
        }
        if (professorTurmaDisciplinaId != null) {
            return repository.findByEscolaIdAndProfessorTurmaDisciplinaIdOrderByCreatedAtDesc(escolaId, professorTurmaDisciplinaId);
        }
        if (periodoAvaliativoId != null) {
            return repository.findByEscolaIdAndPeriodoAvaliativoIdOrderByCreatedAtDesc(escolaId, periodoAvaliativoId);
        }
        return repository.findByEscolaIdOrderByCreatedAtDesc(escolaId);
    }

    private PlanejamentoBimestralJpaEntity find(UUID escolaId, UUID planejamentoId) {
        return repository.findByIdAndEscolaId(planejamentoId, escolaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Planejamento bimestral nao encontrado"));
    }

    private boolean canTransition(String source, String target) {
        return (STATUS_RASCUNHO.equals(source) && STATUS_EM_ANALISE.equals(target))
                || (STATUS_EM_ANALISE.equals(source)
                        && (STATUS_APROVADO.equals(target) || STATUS_REPROVADO.equals(target)))
                || (STATUS_REPROVADO.equals(source) && STATUS_RASCUNHO.equals(target));
    }

    private PlanejamentoBimestralAulaResponse throwConflitoAula() {
        throw new ConflitoNegocioException("Numero de aula ja utilizado neste planejamento");
    }

    private boolean sameAula(PlanejamentoBimestralAulaJpaEntity entity, PlanejamentoBimestralAulaRequest request) {
        return java.util.Objects.equals(entity.getTemaAula(), request.temaAula())
                && java.util.Objects.equals(entity.getObjetivoAula(), request.objetivoAula())
                && java.util.Objects.equals(entity.getConteudoPrevisto(), request.conteudoPrevisto())
                && java.util.Objects.equals(entity.getMetodologia(), request.metodologia())
                && java.util.Objects.equals(entity.getRecursos(), request.recursos())
                && java.util.Objects.equals(entity.getAtividadePrevista(), request.atividadePrevista())
                && java.util.Objects.equals(entity.getObservacao(), request.observacao());
    }

    private String assessmentKey(PlanejamentoBimestralAvaliacaoRequest request) {
        String value = String.join("|", request.titulo(), String.valueOf(request.dataPrevista()),
                request.peso().stripTrailingZeros().toPlainString(), String.valueOf(request.valorMaximo()),
                request.tipoAvaliacao().trim().toUpperCase(), String.valueOf(request.descricao()),
                String.valueOf(request.conteudoCobrado()), String.valueOf(request.orientacaoAplicacao()));
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponivel", exception);
        }
    }

    private PlanejamentoBimestralAulaResponse aulaResponse(PlanejamentoBimestralAulaJpaEntity entity) {
        return new PlanejamentoBimestralAulaResponse(entity.getId(), entity.getPlanejamentoBimestralId(),
                entity.getNumeroAula(), entity.getTemaAula(), entity.getCreatedAt());
    }

    private PlanejamentoBimestralAvaliacaoResponse avaliacaoResponse(PlanejamentoBimestralAvaliacaoJpaEntity entity) {
        return new PlanejamentoBimestralAvaliacaoResponse(entity.getId(), entity.getPlanejamentoBimestralId(),
                entity.getTitulo(), entity.getDataPrevista(), entity.getPeso(), entity.getValorMaximo(),
                entity.getTipoAvaliacao(), entity.getCreatedAt());
    }

    private PlanejamentoBimestralResponse response(PlanejamentoBimestralJpaEntity entity) {
        return new PlanejamentoBimestralResponse(
                entity.getId(), entity.getProfessorTurmaDisciplinaId(), entity.getEscolaId(), entity.getPeriodoAvaliativoId(),
                entity.getStatus(), entity.getTitulo(), entity.getTemaPrincipal(), entity.getDescricaoInicial(),
                entity.getObjetivoGeral(), entity.getObservacaoProfessor(), entity.getConteudoFinalAprovado(),
                entity.isReutilizavel(), entity.isCriadoComAuxilioIa(), entity.isAprovadoPeloProfessor(),
                entity.getDataAprovacao(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
