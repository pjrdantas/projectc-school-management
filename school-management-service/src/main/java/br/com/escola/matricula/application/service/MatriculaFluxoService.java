package br.com.escola.matricula.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.documento.adapter.out.persistence.entity.DocumentoEntity;
import br.com.escola.documento.adapter.out.persistence.repository.DocumentoJpaRepository;
import br.com.escola.matricula.adapter.in.web.MatriculaDocumentoEntregueRequest;
import br.com.escola.matricula.adapter.in.web.MatriculaDocumentoEntregueResponse;
import br.com.escola.matricula.adapter.in.web.MatriculaDocumentoExigidoResponse;
import br.com.escola.matricula.adapter.in.web.MatriculaEtapaStatusRequest;
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
import br.com.escola.matricula.domain.exception.MatriculaDocumentoNaoEncontradoException;
import br.com.escola.matricula.domain.exception.MatriculaEtapaNaoEncontradaException;
import br.com.escola.matricula.domain.exception.MatriculaNaoEncontradaException;
import br.com.escola.matricula.domain.exception.MatriculaStatusInvalidoException;

@Service
public class MatriculaFluxoService {

    private final MatriculaJpaRepository matriculaJpaRepository;
    private final MatriculaEtapaJpaRepository matriculaEtapaJpaRepository;
    private final StatusEtapaMatriculaJpaRepository statusEtapaMatriculaJpaRepository;
    private final StatusMatriculaJpaRepository statusMatriculaJpaRepository;
    private final DocumentoJpaRepository documentoJpaRepository;
    private final MatriculaDocumentoEntregueJpaRepository matriculaDocumentoEntregueJpaRepository;
    private final MatriculaDocumentoExigidoJpaRepository matriculaDocumentoExigidoJpaRepository;

    public MatriculaFluxoService(
            MatriculaJpaRepository matriculaJpaRepository,
            MatriculaEtapaJpaRepository matriculaEtapaJpaRepository,
            StatusEtapaMatriculaJpaRepository statusEtapaMatriculaJpaRepository,
            StatusMatriculaJpaRepository statusMatriculaJpaRepository,
            DocumentoJpaRepository documentoJpaRepository,
            MatriculaDocumentoEntregueJpaRepository matriculaDocumentoEntregueJpaRepository,
            MatriculaDocumentoExigidoJpaRepository matriculaDocumentoExigidoJpaRepository) {
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.matriculaEtapaJpaRepository = matriculaEtapaJpaRepository;
        this.statusEtapaMatriculaJpaRepository = statusEtapaMatriculaJpaRepository;
        this.statusMatriculaJpaRepository = statusMatriculaJpaRepository;
        this.documentoJpaRepository = documentoJpaRepository;
        this.matriculaDocumentoEntregueJpaRepository = matriculaDocumentoEntregueJpaRepository;
        this.matriculaDocumentoExigidoJpaRepository = matriculaDocumentoExigidoJpaRepository;
    }

    @Transactional(readOnly = true)
    public List<MatriculaEtapaOutput> listarEtapas(UUID matriculaId) {
        validarMatriculaExistente(matriculaId);
        return matriculaEtapaJpaRepository.findByMatricula_IdOrderByOrdem(matriculaId).stream()
                .map(this::toEtapaOutput)
                .toList();
    }

    @Transactional
    public MatriculaEtapaOutput atualizarStatusEtapa(
            UUID matriculaId,
            UUID etapaId,
            MatriculaEtapaStatusRequest request) {
        validarMatriculaExistente(matriculaId);
        MatriculaEtapaEntity etapa = matriculaEtapaJpaRepository.findByIdAndMatricula_Id(etapaId, matriculaId)
                .orElseThrow(() -> new MatriculaEtapaNaoEncontradaException(etapaId));
        StatusEtapaMatriculaEntity status = statusEtapaMatriculaJpaRepository
                .findByCodigoIgnoreCase(request.status())
                .orElseThrow(() -> new MatriculaStatusInvalidoException(request.status()));

        etapa.setStatus(status);
        etapa.setObservacao(request.observacao());
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
        MatriculaEntity matricula = matriculaJpaRepository.findById(matriculaId)
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
    public MatriculaDocumentoEntregueResponse registrarDocumentoEntregue(
            UUID matriculaId,
            MatriculaDocumentoEntregueRequest request) {
        MatriculaEntity matricula = matriculaJpaRepository.findById(matriculaId)
                .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaId));
        DocumentoEntity documento = documentoJpaRepository.findById(request.documentoId())
                .orElseThrow(() -> new MatriculaDocumentoNaoEncontradoException(request.documentoId()));

        boolean conferido = Boolean.TRUE.equals(request.conferido());
        MatriculaDocumentoEntregueEntity entity = MatriculaDocumentoEntregueEntity.builder()
                .matricula(matricula)
                .documento(documento)
                .conferido(conferido)
                .conferidoPor(request.conferidoPor())
                .dataConferencia(conferido ? LocalDateTime.now() : null)
                .observacao(request.observacao())
                .createdAt(LocalDateTime.now())
                .build();

        MatriculaDocumentoEntregueEntity saved = matriculaDocumentoEntregueJpaRepository.save(entity);
        atualizarStatusSeDocumentosObrigatoriosCompletos(matricula);
        return toDocumentoEntregueResponse(saved);
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
        if (!matriculaJpaRepository.existsById(matriculaId)) {
            throw new MatriculaNaoEncontradaException(matriculaId);
        }
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
