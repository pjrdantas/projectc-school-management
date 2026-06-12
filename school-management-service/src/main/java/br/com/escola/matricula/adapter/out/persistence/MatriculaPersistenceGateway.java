package br.com.escola.matricula.adapter.out.persistence;

import java.util.UUID;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalogo.adapter.out.persistence.entity.PeriodoLetivoEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaEntity;
import br.com.escola.matricula.adapter.out.persistence.entity.EtapaMatriculaModeloEntity;
import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEtapaEntity;
import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.entity.StatusEtapaMatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.entity.StatusMatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.entity.TipoMatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.repository.EtapaMatriculaModeloJpaRepository;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaDocumentoEntregueJpaRepository;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaEtapaJpaRepository;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.matricula.adapter.out.persistence.repository.StatusEtapaMatriculaJpaRepository;
import br.com.escola.matricula.adapter.out.persistence.repository.StatusMatriculaJpaRepository;
import br.com.escola.matricula.adapter.out.persistence.repository.TipoMatriculaJpaRepository;
import br.com.escola.matricula.application.dto.MatriculaFiltro;
import br.com.escola.matricula.application.dto.MatriculaEtapaOutput;
import br.com.escola.matricula.application.dto.MatriculaHistoricoOutput;
import br.com.escola.matricula.application.dto.MatriculaOutput;
import br.com.escola.matricula.application.port.out.MatriculaGateway;
import br.com.escola.matricula.domain.MatriculaStatus;
import br.com.escola.matricula.domain.MatriculaTipo;
import br.com.escola.matricula.domain.exception.MatriculaAtivaDuplicadaException;
import br.com.escola.matricula.domain.exception.MatriculaNaoEncontradaException;
import br.com.escola.matricula.domain.exception.MatriculaStatusInvalidoException;
import br.com.escola.matricula.domain.exception.MatriculaTipoInvalidoException;
import br.com.escola.aluno.adapter.out.persistence.entity.AlunoEntity;
import br.com.escola.institucional.application.service.EscolaTenantService;
import jakarta.persistence.EntityManager;

@Component
public class MatriculaPersistenceGateway implements MatriculaGateway {

    private static final List<String> STATUS_NAO_OCUPAM_VAGA = List.of("CANCELADA", "INDEFERIDA", "TRANSFERIDO");

    private final MatriculaJpaRepository matriculaJpaRepository;
    private final MatriculaEtapaJpaRepository matriculaEtapaJpaRepository;
    private final MatriculaDocumentoEntregueJpaRepository matriculaDocumentoEntregueJpaRepository;
    private final TipoMatriculaJpaRepository tipoMatriculaJpaRepository;
    private final StatusMatriculaJpaRepository statusMatriculaJpaRepository;
    private final StatusEtapaMatriculaJpaRepository statusEtapaMatriculaJpaRepository;
    private final EtapaMatriculaModeloJpaRepository etapaMatriculaModeloJpaRepository;
    private final EntityManager entityManager;
    private final EscolaTenantService escolaTenantService;

    public MatriculaPersistenceGateway(
            MatriculaJpaRepository matriculaJpaRepository,
            MatriculaEtapaJpaRepository matriculaEtapaJpaRepository,
            MatriculaDocumentoEntregueJpaRepository matriculaDocumentoEntregueJpaRepository,
            TipoMatriculaJpaRepository tipoMatriculaJpaRepository,
            StatusMatriculaJpaRepository statusMatriculaJpaRepository,
            StatusEtapaMatriculaJpaRepository statusEtapaMatriculaJpaRepository,
            EtapaMatriculaModeloJpaRepository etapaMatriculaModeloJpaRepository,
            EntityManager entityManager,
            EscolaTenantService escolaTenantService) {
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.matriculaEtapaJpaRepository = matriculaEtapaJpaRepository;
        this.matriculaDocumentoEntregueJpaRepository = matriculaDocumentoEntregueJpaRepository;
        this.tipoMatriculaJpaRepository = tipoMatriculaJpaRepository;
        this.statusMatriculaJpaRepository = statusMatriculaJpaRepository;
        this.statusEtapaMatriculaJpaRepository = statusEtapaMatriculaJpaRepository;
        this.etapaMatriculaModeloJpaRepository = etapaMatriculaModeloJpaRepository;
        this.entityManager = entityManager;
        this.escolaTenantService = escolaTenantService;
    }

    @Override
    @Transactional
    public MatriculaOutput save(
            UUID alunoId,
            UUID turmaId,
            UUID periodoLetivoId,
            MatriculaStatus status,
            MatriculaTipo tipoMatricula,
            String observacao) {
        AlunoEntity aluno = entityManager.getReference(AlunoEntity.class, alunoId);
        TurmaEntity turma = entityManager.getReference(TurmaEntity.class, turmaId);
        PeriodoLetivoEntity periodoLetivo = entityManager.getReference(PeriodoLetivoEntity.class, periodoLetivoId);
        TipoMatriculaEntity tipo = resolveTipo(tipoMatricula);
        StatusMatriculaEntity statusEntity = resolveStatus(status);

        MatriculaEntity matriculaEntity = new MatriculaEntity();
        matriculaEntity.setAluno(aluno);
        matriculaEntity.setTurma(turma);
        matriculaEntity.setPeriodoLetivo(periodoLetivo);
        matriculaEntity.setStatus(statusEntity);
        matriculaEntity.setTipoMatricula(tipo);
        matriculaEntity.setDataSolicitacao(LocalDate.now());
        matriculaEntity.setObservacao(observacao);
        matriculaEntity.setCreatedAt(LocalDateTime.now());

        MatriculaEntity saved = matriculaJpaRepository.save(matriculaEntity);
        criarEtapasIniciais(saved, tipo);
        return toOutput(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaOutput> findByFiltro(MatriculaFiltro filtro, MatriculaStatus status) {
        UUID escolaId = escolaId();
        Specification<MatriculaEntity> spec = (root, query, cb) ->
                cb.equal(root.get("turma").get("escola").get("id"), escolaId);

        if (filtro.alunoId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("aluno").get("id"), filtro.alunoId()));
        }
        if (filtro.turmaId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("turma").get("id"), filtro.turmaId()));
        }
        if (filtro.periodoLetivoId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("periodoLetivo").get("id"), filtro.periodoLetivoId()));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status").get("codigo"), status.name()));
        }

        return matriculaJpaRepository.findAll(spec).stream().map(this::toOutput).toList();
    }

    @Override
    public long countMatriculasQueOcupamVagaByTurmaId(UUID turmaId) {
        return matriculaJpaRepository.countByTurma_IdAndTurma_Escola_IdAndStatus_CodigoNotIn(
                turmaId,
                escolaId(),
                STATUS_NAO_OCUPAM_VAGA);
    }

    @Override
    public boolean existsByAlunoIdAndPeriodoLetivoId(UUID alunoId, UUID periodoLetivoId) {
        return matriculaJpaRepository.existsByAluno_IdAndAluno_Pessoa_Escola_IdAndPeriodoLetivo_Id(
                alunoId,
                escolaId(),
                periodoLetivoId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MatriculaHistoricoOutput> findHistoricoAnteriorMaisRecente(UUID alunoId, UUID periodoLetivoId) {
        return matriculaJpaRepository
                .findFirstByAluno_IdAndAluno_Pessoa_Escola_IdAndPeriodoLetivo_IdNotOrderByDataSolicitacaoDescCreatedAtDesc(
                        alunoId,
                        escolaId(),
                        periodoLetivoId)
                .map(entity -> new MatriculaHistoricoOutput(
                        entity.getId(),
                        entity.getTurma().getId(),
                        entity.getPeriodoLetivo().getId(),
                        entity.getTurma().getSerie().getOrdem(),
                        entity.getStatus().getCodigo()));
    }

    @Override
    @Transactional
    public MatriculaOutput updateStatus(UUID id, MatriculaStatus status) {
        return updateStatus(id, status, null);
    }

    @Override
    @Transactional
    public MatriculaOutput updateStatus(UUID id, MatriculaStatus status, String justificativa) {
        MatriculaEntity matricula = matriculaJpaRepository.findByIdAndTurma_Escola_Id(id, escolaId())
                .orElseThrow(() -> new MatriculaNaoEncontradaException(id));
        if (matriculaJpaRepository.existsByAluno_IdAndAluno_Pessoa_Escola_IdAndPeriodoLetivo_IdAndIdNot(
                matricula.getAluno().getId(),
                escolaId(),
                matricula.getPeriodoLetivo().getId(),
                matricula.getId())) {
            throw new MatriculaAtivaDuplicadaException(
                    matricula.getAluno().getId(),
                    matricula.getPeriodoLetivo().getId());
        }
        matricula.setStatus(resolveStatus(status));
        if (status == MatriculaStatus.EFETIVADA && matricula.getDataEfetivacao() == null) {
            matricula.setDataEfetivacao(LocalDate.now());
        }
        if (justificativa != null && !justificativa.isBlank()) {
            String observacaoAtual = matricula.getObservacao();
            String evento = "Status " + status.name() + ": " + justificativa.trim();
            matricula.setObservacao(observacaoAtual == null || observacaoAtual.isBlank()
                    ? evento
                    : observacaoAtual + System.lineSeparator() + evento);
        }
        return toOutput(matriculaJpaRepository.save(matricula));
    }

    @Override
    public boolean existsById(UUID id) {
        return matriculaJpaRepository.existsByIdAndTurma_Escola_Id(id, escolaId());
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        matriculaDocumentoEntregueJpaRepository.deleteByMatricula_Id(id);
        matriculaEtapaJpaRepository.deleteByMatricula_Id(id);
        matriculaJpaRepository.deleteById(id);
    }

    private MatriculaOutput toOutput(MatriculaEntity entity) {
        return new MatriculaOutput(
                entity.getId(),
                entity.getAluno().getId(),
                entity.getTurma().getId(),
                entity.getTurma().getEscola().getId(),
                entity.getTurma().getEscola().getNome(),
                entity.getTurma().getSerie().getId(),
                entity.getTurma().getSerie().getNome(),
                entity.getPeriodoLetivo().getId(),
                entity.getStatus().getCodigo(),
                entity.getTipoMatricula() == null ? null : entity.getTipoMatricula().getCodigo(),
                entity.getDataMatricula(),
                entity.getObservacao(),
                entity.getCreatedAt(),
                etapasToOutput(entity.getId()));
    }

    private TipoMatriculaEntity resolveTipo(MatriculaTipo tipoMatricula) {
        return tipoMatriculaJpaRepository.findByCodigoIgnoreCase(tipoMatricula.name())
                .orElseThrow(() -> new MatriculaTipoInvalidoException(tipoMatricula.name()));
    }

    private StatusMatriculaEntity resolveStatus(MatriculaStatus status) {
        return statusMatriculaJpaRepository.findByCodigoIgnoreCase(status.name())
                .orElseThrow(() -> new MatriculaStatusInvalidoException(status.name()));
    }

    private StatusEtapaMatriculaEntity resolveStatusEtapa(String codigo) {
        return statusEtapaMatriculaJpaRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new MatriculaStatusInvalidoException(codigo));
    }

    private void criarEtapasIniciais(MatriculaEntity matricula, TipoMatriculaEntity tipo) {
        StatusEtapaMatriculaEntity pendente = resolveStatusEtapa("PENDENTE");
        List<EtapaMatriculaModeloEntity> modelos = etapaMatriculaModeloJpaRepository.findByTipoMatriculaOrderByOrdem(tipo);

        if (modelos.isEmpty()) {
            MatriculaEtapaEntity etapa = novaEtapa(matricula, null, pendente, "Solicitação de matrícula", 1);
            matriculaEtapaJpaRepository.save(etapa);
            return;
        }

        modelos.stream()
                .map(modelo -> novaEtapa(matricula, modelo, pendente, modelo.getDescricao(), modelo.getOrdem()))
                .forEach(matriculaEtapaJpaRepository::save);
    }

    private MatriculaEtapaEntity novaEtapa(
            MatriculaEntity matricula,
            EtapaMatriculaModeloEntity modelo,
            StatusEtapaMatriculaEntity status,
            String descricao,
            Integer ordem) {
        MatriculaEtapaEntity etapa = new MatriculaEtapaEntity();
        etapa.setMatricula(matricula);
        etapa.setModelo(modelo);
        etapa.setStatus(status);
        etapa.setDescricao(descricao);
        etapa.setOrdem(ordem);
        etapa.setDataInicio(LocalDateTime.now());
        return etapa;
    }

    private List<MatriculaEtapaOutput> etapasToOutput(UUID matriculaId) {
        return matriculaEtapaJpaRepository.findByMatricula_IdOrderByOrdem(matriculaId).stream()
                .map(etapa -> new MatriculaEtapaOutput(
                        etapa.getId(),
                        etapa.getDescricao(),
                        etapa.getOrdem(),
                        etapa.getStatus().getCodigo(),
                        etapa.getDataInicio(),
                        etapa.getDataConclusao(),
                        etapa.getObservacao()))
                .toList();
    }

    private UUID escolaId() {
        return escolaTenantService.obterOuCriarEscolaPadrao().getId();
    }
}
