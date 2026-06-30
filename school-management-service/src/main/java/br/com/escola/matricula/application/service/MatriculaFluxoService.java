package br.com.escola.matricula.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.documento.adapter.out.persistence.entity.DocumentoEntity;
import br.com.escola.documento.adapter.out.persistence.repository.DocumentoJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.entity.TurmaEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurmaJpaRepository;
import br.com.escola.historico.adapter.out.persistence.entity.BoletimEntity;
import br.com.escola.historico.adapter.out.persistence.entity.BoletimItemEntity;
import br.com.escola.historico.adapter.out.persistence.repository.BoletimItemJpaRepository;
import br.com.escola.historico.adapter.out.persistence.repository.BoletimJpaRepository;
import br.com.escola.historico.domain.exception.BoletimFechadoNaoEncontradoException;
import br.com.escola.matricula.adapter.in.web.MatriculaConclusaoAcademicaRequest;
import br.com.escola.matricula.adapter.in.web.MatriculaConclusaoAcademicaResponse;
import br.com.escola.matricula.adapter.in.web.MatriculaDocumentoEntregueRequest;
import br.com.escola.matricula.adapter.in.web.MatriculaDocumentoEntregueResponse;
import br.com.escola.matricula.adapter.in.web.MatriculaDocumentoExigidoResponse;
import br.com.escola.matricula.adapter.in.web.MatriculaRematriculaElegibilidadeResponse;
import br.com.escola.matricula.adapter.in.web.MatriculaRematriculaRequest;
import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaDocumentoEntregueEntity;
import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaDocumentoExigidoEntity;
import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEtapaEntity;
import br.com.escola.matricula.adapter.out.persistence.entity.StatusEtapaMatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.entity.StatusMatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaDocumentoEntregueJpaRepository;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaDocumentoExigidoJpaRepository;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaEtapaJpaRepository;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.matricula.adapter.out.persistence.repository.StatusEtapaMatriculaJpaRepository;
import br.com.escola.matricula.adapter.out.persistence.repository.StatusMatriculaJpaRepository;
import br.com.escola.matricula.application.dto.MatriculaEtapaOutput;
import br.com.escola.matricula.application.dto.MatriculaInput;
import br.com.escola.matricula.application.dto.MatriculaOutput;
import br.com.escola.matricula.application.dto.internal.AtualizarMatriculaEtapaStatusSolicitacao;
import br.com.escola.matricula.application.dto.internal.RegistrarMatriculaDocumentoEntregueSolicitacao;
import br.com.escola.matricula.application.port.internal.MatriculaDocumentoEntreguePort;
import br.com.escola.matricula.application.port.internal.MatriculaEtapaPort;
import br.com.escola.matricula.application.usecase.CriarMatriculaUseCase;
import br.com.escola.matricula.domain.exception.MatriculaDocumentoNaoEncontradoException;
import br.com.escola.matricula.domain.exception.MatriculaEtapaNaoEncontradaException;
import br.com.escola.matricula.domain.exception.MatriculaConclusaoAcademicaInvalidaException;
import br.com.escola.matricula.domain.exception.MatriculaNaoEncontradaException;
import br.com.escola.matricula.domain.exception.MatriculaStatusInvalidoException;
import br.com.escola.matricula.domain.exception.RematriculaNaoPermitidaException;
import br.com.escola.institucional.application.service.EscolaTenantService;

@Service
public class MatriculaFluxoService implements MatriculaEtapaPort, MatriculaDocumentoEntreguePort {

    private static final List<String> STATUS_NAO_OCUPAM_VAGA = List.of("CANCELADA", "INDEFERIDA", "TRANSFERIDO");

    private final MatriculaJpaRepository matriculaJpaRepository;
    private final TurmaJpaRepository turmaJpaRepository;
    private final MatriculaEtapaJpaRepository matriculaEtapaJpaRepository;
    private final StatusEtapaMatriculaJpaRepository statusEtapaMatriculaJpaRepository;
    private final StatusMatriculaJpaRepository statusMatriculaJpaRepository;
    private final DocumentoJpaRepository documentoJpaRepository;
    private final MatriculaDocumentoEntregueJpaRepository matriculaDocumentoEntregueJpaRepository;
    private final MatriculaDocumentoExigidoJpaRepository matriculaDocumentoExigidoJpaRepository;
    private final BoletimJpaRepository boletimJpaRepository;
    private final BoletimItemJpaRepository boletimItemJpaRepository;
    private final CriarMatriculaUseCase criarMatriculaUseCase;
    private final EscolaTenantService escolaTenantService;

    public MatriculaFluxoService(
            MatriculaJpaRepository matriculaJpaRepository,
            TurmaJpaRepository turmaJpaRepository,
            MatriculaEtapaJpaRepository matriculaEtapaJpaRepository,
            StatusEtapaMatriculaJpaRepository statusEtapaMatriculaJpaRepository,
            StatusMatriculaJpaRepository statusMatriculaJpaRepository,
            DocumentoJpaRepository documentoJpaRepository,
            MatriculaDocumentoEntregueJpaRepository matriculaDocumentoEntregueJpaRepository,
            MatriculaDocumentoExigidoJpaRepository matriculaDocumentoExigidoJpaRepository,
            BoletimJpaRepository boletimJpaRepository,
            BoletimItemJpaRepository boletimItemJpaRepository,
            CriarMatriculaUseCase criarMatriculaUseCase,
            EscolaTenantService escolaTenantService) {
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.turmaJpaRepository = turmaJpaRepository;
        this.matriculaEtapaJpaRepository = matriculaEtapaJpaRepository;
        this.statusEtapaMatriculaJpaRepository = statusEtapaMatriculaJpaRepository;
        this.statusMatriculaJpaRepository = statusMatriculaJpaRepository;
        this.documentoJpaRepository = documentoJpaRepository;
        this.matriculaDocumentoEntregueJpaRepository = matriculaDocumentoEntregueJpaRepository;
        this.matriculaDocumentoExigidoJpaRepository = matriculaDocumentoExigidoJpaRepository;
        this.boletimJpaRepository = boletimJpaRepository;
        this.boletimItemJpaRepository = boletimItemJpaRepository;
        this.criarMatriculaUseCase = criarMatriculaUseCase;
        this.escolaTenantService = escolaTenantService;
    }

    @Transactional(readOnly = true)
    public List<MatriculaEtapaOutput> listarEtapas(UUID matriculaId) {
        validarMatriculaExistente(matriculaId);
        return matriculaEtapaJpaRepository.findByMatricula_IdOrderByOrdem(matriculaId).stream()
                .map(this::toEtapaOutput)
                .toList();
    }

    @Transactional
    @Override
    public MatriculaEtapaOutput atualizarStatusEtapa(
            UUID matriculaId,
            UUID etapaId,
            AtualizarMatriculaEtapaStatusSolicitacao solicitacao) {
        validarMatriculaExistente(matriculaId);
        MatriculaEtapaEntity etapa = matriculaEtapaJpaRepository.findByIdAndMatricula_Id(etapaId, matriculaId)
                .orElseThrow(() -> new MatriculaEtapaNaoEncontradaException(etapaId));
        StatusEtapaMatriculaEntity status = statusEtapaMatriculaJpaRepository
                .findByCodigoIgnoreCase(solicitacao.status())
                .orElseThrow(() -> new MatriculaStatusInvalidoException(solicitacao.status()));

        etapa.setStatus(status);
        etapa.setObservacao(solicitacao.observacao());
        if ("CONCLUIDA".equalsIgnoreCase(status.getCodigo())) {
            etapa.setDataConclusao(LocalDateTime.now());
        } else {
            etapa.setDataConclusao(null);
        }

        return toEtapaOutput(matriculaEtapaJpaRepository.save(etapa));
    }

    @Transactional(readOnly = true)
    public List<MatriculaDocumentoEntregueResponse> listarDocumentosEntregues(UUID matriculaId) {
        validarMatriculaExistente(matriculaId);
        return matriculaDocumentoEntregueJpaRepository.findByMatricula_Id(matriculaId).stream()
                .map(this::toDocumentoEntregueResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MatriculaDocumentoExigidoResponse> listarDocumentosExigidos(UUID matriculaId) {
        MatriculaEntity matricula = matriculaJpaRepository.findByIdAndTurma_Escola_Id(matriculaId, escolaId())
                .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaId));
        List<MatriculaDocumentoEntregueEntity> entregues =
                matriculaDocumentoEntregueJpaRepository.findByMatricula_Id(matriculaId);

        return matriculaDocumentoExigidoJpaRepository
                .findByTipoMatricula_IdOrderByOrdemAsc(matricula.getTipoMatricula().getId())
                .stream()
                .map(exigido -> toDocumentoExigidoResponse(exigido, entregues))
                .toList();
    }

    @Transactional
    @Override
    public MatriculaDocumentoEntregueResponse registrarDocumentoEntregue(
            UUID matriculaId,
            RegistrarMatriculaDocumentoEntregueSolicitacao solicitacao) {
        MatriculaEntity matricula = matriculaJpaRepository.findByIdAndTurma_Escola_Id(matriculaId, escolaId())
                .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaId));
        DocumentoEntity documento = documentoJpaRepository.findByIdAndEscolaId(solicitacao.documentoId(), escolaId())
                .orElseThrow(() -> new MatriculaDocumentoNaoEncontradoException(solicitacao.documentoId()));

        boolean conferido = Boolean.TRUE.equals(solicitacao.conferido());
        MatriculaDocumentoEntregueEntity entity = MatriculaDocumentoEntregueEntity.builder()
                .matricula(matricula)
                .documento(documento)
                .conferido(conferido)
                .conferidoPor(solicitacao.conferidoPor())
                .dataConferencia(conferido ? LocalDateTime.now() : null)
                .observacao(solicitacao.observacao())
                .createdAt(LocalDateTime.now())
                .build();

        MatriculaDocumentoEntregueEntity saved = matriculaDocumentoEntregueJpaRepository.save(entity);
        atualizarStatusSeDocumentosObrigatoriosCompletos(matricula);
        return toDocumentoEntregueResponse(saved);
    }

    @Transactional
    public MatriculaConclusaoAcademicaResponse concluirAcademicamente(
            UUID matriculaId,
            MatriculaConclusaoAcademicaRequest request) {
        MatriculaEntity matricula = matriculaJpaRepository.findByIdAndTurma_Escola_Id(matriculaId, escolaId())
                .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaId));
        BoletimEntity boletim = boletimJpaRepository.findByIdAndMatricula_Turma_Escola_Id(request.boletimId(), escolaId())
                .orElseThrow(() -> new BoletimFechadoNaoEncontradoException(request.boletimId()));
        if (!boletim.getMatricula().getId().equals(matriculaId)) {
            throw new MatriculaConclusaoAcademicaInvalidaException(
                    "Boletim fechado não pertence à matrícula informada");
        }

        List<BoletimItemEntity> itens = boletimItemJpaRepository.findByBoletimId(boletim.getId());
        if (itens.isEmpty()) {
            throw new MatriculaConclusaoAcademicaInvalidaException(
                    "Boletim fechado não possui itens para conclusão acadêmica");
        }
        if (itens.stream().anyMatch(item -> "PENDENTE".equalsIgnoreCase(item.getResultado()))) {
            throw new MatriculaConclusaoAcademicaInvalidaException(
                    "Boletim fechado possui componentes pendentes");
        }

        String resultadoFinal = itens.stream().anyMatch(item -> "REPROVADO".equalsIgnoreCase(item.getResultado()))
                ? "REPROVADO"
                : "APROVADO";
        String statusFinal = "APROVADO".equals(resultadoFinal) ? "CONCLUIDA" : "EFETIVADA";

        StatusMatriculaEntity status = statusMatriculaJpaRepository.findByCodigoIgnoreCase(statusFinal)
                .orElseThrow(() -> new MatriculaStatusInvalidoException(statusFinal));
        matricula.setStatus(status);

        String evento = "Conclusão acadêmica " + resultadoFinal + " pelo boletim " + boletim.getId();
        if (request.observacao() != null && !request.observacao().isBlank()) {
            evento += ": " + request.observacao().trim();
        }
        String observacaoAtual = matricula.getObservacao();
        matricula.setObservacao(observacaoAtual == null || observacaoAtual.isBlank()
                ? evento
                : observacaoAtual + System.lineSeparator() + evento);
        matriculaJpaRepository.save(matricula);

        return new MatriculaConclusaoAcademicaResponse(
                matricula.getId(),
                boletim.getId(),
                resultadoFinal,
                statusFinal,
                matricula.getObservacao());
    }

    @Transactional
    public MatriculaOutput rematricular(UUID matriculaAnteriorId, MatriculaRematriculaRequest request) {
        MatriculaEntity matriculaAnterior = matriculaJpaRepository.findByIdAndTurma_Escola_Id(matriculaAnteriorId, escolaId())
                .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaAnteriorId));
        if (!"CONCLUIDA".equalsIgnoreCase(matriculaAnterior.getStatus().getCodigo())) {
            throw new RematriculaNaoPermitidaException("matrícula base deve estar concluída para renovação");
        }

        return criarMatriculaUseCase.executar(new MatriculaInput(
                matriculaAnterior.getAluno().getId(),
                request.turmaId(),
                request.periodoLetivoId(),
                "RENOVACAO",
                request.observacao()));
    }

    @Transactional(readOnly = true)
    public MatriculaRematriculaElegibilidadeResponse consultarElegibilidadeRematricula(
            UUID matriculaAnteriorId,
            UUID turmaDestinoId,
            UUID periodoLetivoDestinoId) {
        MatriculaEntity matriculaAnterior = matriculaJpaRepository.findByIdAndTurma_Escola_Id(matriculaAnteriorId, escolaId())
                .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaAnteriorId));
        List<String> motivos = new ArrayList<>();

        if (!"CONCLUIDA".equalsIgnoreCase(matriculaAnterior.getStatus().getCodigo())) {
            motivos.add("Matrícula base deve estar concluída para renovação");
        }

        TurmaEntity turmaDestino = null;
        if (turmaDestinoId != null) {
            turmaDestino = turmaJpaRepository.findByIdAndEscola_Id(turmaDestinoId, escolaId()).orElse(null);
            if (turmaDestino == null) {
                motivos.add("Turma de destino não encontrada");
            }
        }

        if (turmaDestino != null && periodoLetivoDestinoId != null
                && !turmaDestino.getPeriodoLetivo().getId().equals(periodoLetivoDestinoId)) {
            motivos.add("Turma de destino não pertence ao período letivo informado");
        }

        if (periodoLetivoDestinoId != null
                && matriculaJpaRepository.existsByAluno_IdAndAluno_Pessoa_Escola_IdAndPeriodoLetivo_Id(
                        matriculaAnterior.getAluno().getId(),
                        escolaId(),
                        periodoLetivoDestinoId)) {
            motivos.add("Aluno já possui matrícula no período letivo de destino");
        }

        if (turmaDestino != null) {
            Integer serieOrigem = matriculaAnterior.getTurma().getSerie().getOrdem();
            Integer serieDestino = turmaDestino.getSerie().getOrdem();
            if (serieOrigem == null || serieDestino == null || !serieDestino.equals(serieOrigem + 1)) {
                motivos.add("Turma de destino deve ser da série imediatamente posterior");
            }

            long matriculasQueOcupamVaga =
                    matriculaJpaRepository.countByTurma_IdAndTurma_Escola_IdAndStatus_CodigoNotIn(
                            turmaDestinoId,
                            escolaId(),
                            STATUS_NAO_OCUPAM_VAGA);
            if (matriculasQueOcupamVaga >= turmaDestino.getCapacidade()) {
                motivos.add("Turma de destino não possui vaga disponível");
            }
        }

        return new MatriculaRematriculaElegibilidadeResponse(
                matriculaAnterior.getId(),
                matriculaAnterior.getAluno().getId(),
                matriculaAnterior.getStatus().getCodigo(),
                matriculaAnterior.getTurma().getId(),
                matriculaAnterior.getTurma().getSerie().getId(),
                matriculaAnterior.getTurma().getSerie().getNome(),
                matriculaAnterior.getTurma().getSerie().getOrdem(),
                turmaDestino == null ? turmaDestinoId : turmaDestino.getId(),
                periodoLetivoDestinoId,
                turmaDestino == null ? null : turmaDestino.getSerie().getId(),
                turmaDestino == null ? null : turmaDestino.getSerie().getNome(),
                turmaDestino == null ? null : turmaDestino.getSerie().getOrdem(),
                motivos.isEmpty(),
                motivos);
    }

    private void atualizarStatusSeDocumentosObrigatoriosCompletos(MatriculaEntity matricula) {
        if (!"AGUARDANDO_DOCUMENTOS".equalsIgnoreCase(matricula.getStatus().getCodigo())) {
            return;
        }

        List<MatriculaDocumentoExigidoEntity> obrigatorios = matriculaDocumentoExigidoJpaRepository
                .findByTipoMatricula_IdOrderByOrdemAsc(matricula.getTipoMatricula().getId())
                .stream()
                .filter(exigido -> Boolean.TRUE.equals(exigido.getObrigatorio()))
                .toList();
        if (obrigatorios.isEmpty()) {
            return;
        }

        List<MatriculaDocumentoEntregueEntity> entregues =
                matriculaDocumentoEntregueJpaRepository.findByMatricula_Id(matricula.getId());
        boolean todosObrigatoriosEntregues = obrigatorios.stream()
                .allMatch(exigido -> entregues.stream()
                        .anyMatch(entregue -> entregue.getDocumento().getTipoDocumentoId()
                                .equals(exigido.getTipoDocumento().getId())));
        if (!todosObrigatoriosEntregues) {
            return;
        }

        StatusMatriculaEntity emAndamento = statusMatriculaJpaRepository.findByCodigoIgnoreCase("EM_ANDAMENTO")
                .orElseThrow(() -> new MatriculaStatusInvalidoException("EM_ANDAMENTO"));
        matricula.setStatus(emAndamento);
        matriculaJpaRepository.save(matricula);
    }

    private void validarMatriculaExistente(UUID matriculaId) {
        if (!matriculaJpaRepository.existsByIdAndTurma_Escola_Id(matriculaId, escolaId())) {
            throw new MatriculaNaoEncontradaException(matriculaId);
        }
    }

    private UUID escolaId() {
        return escolaTenantService.obterOuCriarEscolaPadrao().getId();
    }

    private MatriculaEtapaOutput toEtapaOutput(MatriculaEtapaEntity etapa) {
        return new MatriculaEtapaOutput(
                etapa.getId(),
                etapa.getDescricao(),
                etapa.getOrdem(),
                etapa.getStatus().getCodigo(),
                etapa.getDataInicio(),
                etapa.getDataConclusao(),
                etapa.getObservacao());
    }

    private MatriculaDocumentoEntregueResponse toDocumentoEntregueResponse(MatriculaDocumentoEntregueEntity entity) {
        DocumentoEntity documento = entity.getDocumento();
        return new MatriculaDocumentoEntregueResponse(
                entity.getId(),
                entity.getMatricula().getId(),
                documento.getId(),
                documento.getTipoDocumentoId(),
                documento.getNumeroDocumento(),
                documento.getCaminhoArquivo(),
                entity.getConferido(),
                entity.getConferidoPor(),
                entity.getDataConferencia(),
                entity.getObservacao(),
                entity.getCreatedAt());
    }

    private MatriculaDocumentoExigidoResponse toDocumentoExigidoResponse(
            MatriculaDocumentoExigidoEntity exigido,
            List<MatriculaDocumentoEntregueEntity> entregues) {
        MatriculaDocumentoEntregueEntity entregue = entregues.stream()
                .filter(item -> item.getDocumento().getTipoDocumentoId().equals(exigido.getTipoDocumento().getId()))
                .findFirst()
                .orElse(null);

        return new MatriculaDocumentoExigidoResponse(
                exigido.getId(),
                exigido.getTipoMatricula().getId(),
                exigido.getTipoMatricula().getCodigo(),
                exigido.getTipoDocumento().getId(),
                exigido.getTipoDocumento().getCodigo(),
                exigido.getTipoDocumento().getDescricao(),
                exigido.getObrigatorio(),
                exigido.getOrdem(),
                entregue != null,
                entregue == null ? null : entregue.getId(),
                entregue == null ? null : entregue.getDocumento().getId());
    }
}
