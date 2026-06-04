package br.com.escola.dashboard.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardIndicadorSnapshotRequest;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardIndicadorSnapshotResponse;
import br.com.escola.dashboard.adapter.out.persistence.entity.DashboardIndicadorSnapshotEntity;
import br.com.escola.dashboard.adapter.out.persistence.entity.PublicoDashboardEntity;
import br.com.escola.dashboard.adapter.out.persistence.repository.DashboardIndicadorSnapshotJpaRepository;
import br.com.escola.dashboard.adapter.out.persistence.repository.PublicoDashboardJpaRepository;

@Service
public class DashboardIndicadorSnapshotService {

    private final DashboardIndicadorSnapshotJpaRepository dashboardIndicadorSnapshotJpaRepository;
    private final PublicoDashboardJpaRepository publicoDashboardJpaRepository;

    public DashboardIndicadorSnapshotService(
            DashboardIndicadorSnapshotJpaRepository dashboardIndicadorSnapshotJpaRepository,
            PublicoDashboardJpaRepository publicoDashboardJpaRepository) {
        this.dashboardIndicadorSnapshotJpaRepository = dashboardIndicadorSnapshotJpaRepository;
        this.publicoDashboardJpaRepository = publicoDashboardJpaRepository;
    }

    @Transactional(readOnly = true)
    public List<DashboardIndicadorSnapshotResponse> listar(UUID publicoDashboardId, LocalDate referenciaData) {
        buscarPublico(publicoDashboardId);
        List<DashboardIndicadorSnapshotEntity> snapshots = referenciaData == null
                ? dashboardIndicadorSnapshotJpaRepository
                        .findByPublicoDashboardIdOrderByReferenciaDataDescCodigoIndicadorAsc(publicoDashboardId)
                : dashboardIndicadorSnapshotJpaRepository
                        .findByPublicoDashboardIdAndReferenciaDataOrderByCodigoIndicadorAsc(publicoDashboardId, referenciaData);
        return snapshots.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DashboardIndicadorSnapshotResponse> listarPorPublicoCodigo(String publicoCodigo, LocalDate referenciaData) {
        PublicoDashboardEntity publico = publicoDashboardJpaRepository.findByCodigo(normalizarCodigo(publicoCodigo))
                .orElseThrow(() -> notFound("Público de dashboard não encontrado para o código " + publicoCodigo));
        return listar(publico.getId(), referenciaData);
    }

    @Transactional
    public DashboardIndicadorSnapshotResponse salvar(DashboardIndicadorSnapshotRequest request) {
        PublicoDashboardEntity publico = buscarPublico(request.publicoDashboardId());
        String codigoIndicador = normalizarCodigo(request.codigoIndicador());

        DashboardIndicadorSnapshotEntity entity = dashboardIndicadorSnapshotJpaRepository
                .findByPublicoDashboardIdAndCodigoIndicadorAndReferenciaData(
                        publico.getId(),
                        codigoIndicador,
                        request.referenciaData())
                .orElseGet(() -> DashboardIndicadorSnapshotEntity.builder()
                        .publicoDashboard(publico)
                        .codigoIndicador(codigoIndicador)
                        .referenciaData(request.referenciaData())
                        .createdAt(LocalDateTime.now())
                        .build());

        entity.setPublicoDashboard(publico);
        entity.setCodigoIndicador(codigoIndicador);
        entity.setDescricao(request.descricao().trim());
        entity.setValorNumeric(request.valorNumeric());
        entity.setValorTexto(trimToNull(request.valorTexto()));
        entity.setReferenciaData(request.referenciaData());

        return toResponse(dashboardIndicadorSnapshotJpaRepository.save(entity));
    }

    @Transactional
    public void excluir(UUID id) {
        if (!dashboardIndicadorSnapshotJpaRepository.existsById(id)) {
            throw notFound("Snapshot de indicador não encontrado para o id " + id);
        }
        dashboardIndicadorSnapshotJpaRepository.deleteById(id);
    }

    private PublicoDashboardEntity buscarPublico(UUID publicoDashboardId) {
        return publicoDashboardJpaRepository.findById(publicoDashboardId)
                .orElseThrow(() -> notFound("Público de dashboard não encontrado para o id " + publicoDashboardId));
    }

    private DashboardIndicadorSnapshotResponse toResponse(DashboardIndicadorSnapshotEntity entity) {
        return new DashboardIndicadorSnapshotResponse(
                entity.getId(),
                entity.getPublicoDashboard().getId(),
                entity.getPublicoDashboard().getCodigo(),
                entity.getCodigoIndicador(),
                entity.getDescricao(),
                entity.getValorNumeric(),
                entity.getValorTexto(),
                entity.getReferenciaData());
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
