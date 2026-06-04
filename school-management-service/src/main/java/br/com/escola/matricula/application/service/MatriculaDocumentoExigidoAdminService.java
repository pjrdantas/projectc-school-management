package br.com.escola.matricula.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.documento.adapter.out.persistence.entity.TipoDocumentoEntity;
import br.com.escola.documento.adapter.out.persistence.repository.TipoDocumentoJpaRepository;
import br.com.escola.matricula.adapter.in.web.MatriculaDocumentoExigidoRequest;
import br.com.escola.matricula.adapter.in.web.MatriculaDocumentoExigidoResponse;
import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaDocumentoExigidoEntity;
import br.com.escola.matricula.adapter.out.persistence.entity.TipoMatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaDocumentoExigidoJpaRepository;
import br.com.escola.matricula.adapter.out.persistence.repository.TipoMatriculaJpaRepository;
import br.com.escola.matricula.domain.exception.MatriculaDocumentoExigidoDuplicadoException;
import br.com.escola.matricula.domain.exception.MatriculaDocumentoExigidoNaoEncontradoException;
import br.com.escola.matricula.domain.exception.MatriculaTipoDocumentoNaoEncontradoException;
import br.com.escola.matricula.domain.exception.MatriculaTipoNaoEncontradoException;

@Service
public class MatriculaDocumentoExigidoAdminService {

    private final MatriculaDocumentoExigidoJpaRepository matriculaDocumentoExigidoJpaRepository;
    private final TipoMatriculaJpaRepository tipoMatriculaJpaRepository;
    private final TipoDocumentoJpaRepository tipoDocumentoJpaRepository;

    public MatriculaDocumentoExigidoAdminService(
            MatriculaDocumentoExigidoJpaRepository matriculaDocumentoExigidoJpaRepository,
            TipoMatriculaJpaRepository tipoMatriculaJpaRepository,
            TipoDocumentoJpaRepository tipoDocumentoJpaRepository) {
        this.matriculaDocumentoExigidoJpaRepository = matriculaDocumentoExigidoJpaRepository;
        this.tipoMatriculaJpaRepository = tipoMatriculaJpaRepository;
        this.tipoDocumentoJpaRepository = tipoDocumentoJpaRepository;
    }

    @Transactional(readOnly = true)
    public List<MatriculaDocumentoExigidoResponse> listarPorTipoMatricula(UUID tipoMatriculaId) {
        return matriculaDocumentoExigidoJpaRepository
                .findByTipoMatricula_IdOrderByOrdemAsc(tipoMatriculaId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MatriculaDocumentoExigidoResponse criar(MatriculaDocumentoExigidoRequest request) {
        TipoMatriculaEntity tipoMatricula = buscarTipoMatricula(request.tipoMatriculaId());
        TipoDocumentoEntity tipoDocumento = buscarTipoDocumento(request.tipoDocumentoId());

        if (matriculaDocumentoExigidoJpaRepository.existsByTipoMatricula_IdAndTipoDocumento_Id(
                tipoMatricula.getId(),
                tipoDocumento.getId())) {
            throw new MatriculaDocumentoExigidoDuplicadoException(tipoMatricula.getId(), tipoDocumento.getId());
        }

        MatriculaDocumentoExigidoEntity entity = MatriculaDocumentoExigidoEntity.builder()
                .tipoMatricula(tipoMatricula)
                .tipoDocumento(tipoDocumento)
                .obrigatorio(request.obrigatorio() == null || request.obrigatorio())
                .ordem(request.ordem())
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(matriculaDocumentoExigidoJpaRepository.save(entity));
    }

    @Transactional
    public MatriculaDocumentoExigidoResponse atualizar(UUID id, MatriculaDocumentoExigidoRequest request) {
        MatriculaDocumentoExigidoEntity entity = matriculaDocumentoExigidoJpaRepository.findById(id)
                .orElseThrow(() -> new MatriculaDocumentoExigidoNaoEncontradoException(id));
        TipoMatriculaEntity tipoMatricula = buscarTipoMatricula(request.tipoMatriculaId());
        TipoDocumentoEntity tipoDocumento = buscarTipoDocumento(request.tipoDocumentoId());

        matriculaDocumentoExigidoJpaRepository
                .findByTipoMatricula_IdAndTipoDocumento_Id(tipoMatricula.getId(), tipoDocumento.getId())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new MatriculaDocumentoExigidoDuplicadoException(tipoMatricula.getId(), tipoDocumento.getId());
                });

        entity.setTipoMatricula(tipoMatricula);
        entity.setTipoDocumento(tipoDocumento);
        entity.setObrigatorio(request.obrigatorio() == null || request.obrigatorio());
        entity.setOrdem(request.ordem());

        return toResponse(matriculaDocumentoExigidoJpaRepository.save(entity));
    }

    @Transactional
    public void excluir(UUID id) {
        if (!matriculaDocumentoExigidoJpaRepository.existsById(id)) {
            throw new MatriculaDocumentoExigidoNaoEncontradoException(id);
        }
        matriculaDocumentoExigidoJpaRepository.deleteById(id);
    }

    private TipoMatriculaEntity buscarTipoMatricula(UUID id) {
        return tipoMatriculaJpaRepository.findById(id)
                .orElseThrow(() -> new MatriculaTipoNaoEncontradoException(id));
    }

    private TipoDocumentoEntity buscarTipoDocumento(UUID id) {
        return tipoDocumentoJpaRepository.findById(id)
                .orElseThrow(() -> new MatriculaTipoDocumentoNaoEncontradoException(id));
    }

    private MatriculaDocumentoExigidoResponse toResponse(MatriculaDocumentoExigidoEntity entity) {
        return new MatriculaDocumentoExigidoResponse(
                entity.getId(),
                entity.getTipoMatricula().getId(),
                entity.getTipoMatricula().getCodigo(),
                entity.getTipoDocumento().getId(),
                entity.getTipoDocumento().getCodigo(),
                entity.getTipoDocumento().getDescricao(),
                entity.getObrigatorio(),
                entity.getOrdem(),
                null,
                null,
                null);
    }
}
