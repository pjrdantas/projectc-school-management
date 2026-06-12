package br.com.escola.dashboard.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardIndicadorHistoricoPontoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardIndicadorHistoricoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardIndicadorSnapshotRequest;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardIndicadorSnapshotResponse;
import br.com.escola.dashboard.adapter.out.persistence.entity.DashboardIndicadorSnapshotEntity;
import br.com.escola.dashboard.adapter.out.persistence.entity.PublicoDashboardEntity;
import br.com.escola.dashboard.adapter.out.persistence.repository.DashboardIndicadorSnapshotJpaRepository;
import br.com.escola.dashboard.adapter.out.persistence.repository.PublicoDashboardJpaRepository;
import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.institucional.application.service.EscolaTenantService;

@Service
public class DashboardIndicadorSnapshotService {

    private final DashboardIndicadorSnapshotJpaRepository dashboardIndicadorSnapshotJpaRepository;
    private final PublicoDashboardJpaRepository publicoDashboardJpaRepository;
    private final EscolaTenantService escolaTenantService;

    public DashboardIndicadorSnapshotService(
            DashboardIndicadorSnapshotJpaRepository dashboardIndicadorSnapshotJpaRepository,
            PublicoDashboardJpaRepository publicoDashboardJpaRepository,
            EscolaTenantService escolaTenantService) {
        this.dashboardIndicadorSnapshotJpaRepository = dashboardIndicadorSnapshotJpaRepository;
        this.publicoDashboardJpaRepository = publicoDashboardJpaRepository;
        this.escolaTenantService = escolaTenantService;
    }

    @Transactional(readOnly = true)
    public List<DashboardIndicadorSnapshotResponse> listar(UUID publicoDashboardId, LocalDate referenciaData) {
        buscarPublico(publicoDashboardId);
        UUID escolaId = escolaTenantService.obterOuCriarEscolaPadrao().getId();
        List<DashboardIndicadorSnapshotEntity> snapshots = referenciaData == null
                ? dashboardIndicadorSnapshotJpaRepository
                        .findByPublicoDashboardIdAndEscola_IdOrderByReferenciaDataDescCodigoIndicadorAsc(publicoDashboardId, escolaId)
                : dashboardIndicadorSnapshotJpaRepository
                        .findByPublicoDashboardIdAndEscola_IdAndReferenciaDataOrderByCodigoIndicadorAsc(
                                publicoDashboardId,
                                escolaId,
                                referenciaData);
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

    @Transactional(readOnly = true)
    public List<DashboardIndicadorHistoricoResponse> consultarHistorico(
            String publicoCodigo,
            String codigoIndicador,
            LocalDate dataInicio,
            LocalDate dataFim,
            UUID professorId) {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataInicio não pode ser posterior a dataFim");
        }

        PublicoDashboardEntity publico = publicoDashboardJpaRepository.findByCodigo(normalizarCodigo(publicoCodigo))
                .orElseThrow(() -> notFound("Público de dashboard não encontrado para o código " + publicoCodigo));
        String codigoFiltro = normalizarCodigoFiltro(codigoIndicador, professorId);
        UUID escolaId = escolaTenantService.obterOuCriarEscolaPadrao().getId();

        List<DashboardIndicadorSnapshotEntity> snapshots = dashboardIndicadorSnapshotJpaRepository
                .findByPublicoDashboardIdAndEscola_IdOrderByReferenciaDataDescCodigoIndicadorAsc(publico.getId(), escolaId)
                .stream()
                .filter(snapshot -> filtrarPorData(snapshot, dataInicio, dataFim))
                .filter(snapshot -> codigoFiltro == null || snapshot.getCodigoIndicador().equals(codigoFiltro))
                .filter(snapshot -> professorId == null || professorId.toString().equals(snapshot.getValorTexto()))
                .sorted(Comparator
                        .comparing(DashboardIndicadorSnapshotEntity::getCodigoIndicador)
                        .thenComparing(DashboardIndicadorSnapshotEntity::getReferenciaData))
                .toList();

        Map<String, List<DashboardIndicadorSnapshotEntity>> porIndicador = snapshots.stream()
                .collect(Collectors.groupingBy(
                        DashboardIndicadorSnapshotEntity::getCodigoIndicador,
                        LinkedHashMap::new,
                        Collectors.toList()));

        return porIndicador.entrySet().stream()
                .map(entry -> toHistoricoResponse(publico, entry.getKey(), entry.getValue()))
                .toList();
    }

    @Transactional
    public DashboardIndicadorSnapshotResponse salvar(DashboardIndicadorSnapshotRequest request) {
        PublicoDashboardEntity publico = buscarPublico(request.publicoDashboardId());
        EscolaEntity escola = escolaTenantService.obterOuCriarEscolaPadrao();
        String codigoIndicador = normalizarCodigo(request.codigoIndicador());

        DashboardIndicadorSnapshotEntity entity = dashboardIndicadorSnapshotJpaRepository
                .findByPublicoDashboardIdAndEscola_IdAndCodigoIndicadorAndReferenciaData(
                        publico.getId(),
                        escola.getId(),
                        codigoIndicador,
                        request.referenciaData())
                .orElseGet(() -> DashboardIndicadorSnapshotEntity.builder()
                        .publicoDashboard(publico)
                        .escola(escola)
                        .codigoIndicador(codigoIndicador)
                        .referenciaData(request.referenciaData())
                        .createdAt(LocalDateTime.now())
                        .build());

        entity.setPublicoDashboard(publico);
        entity.setEscola(escola);
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
                entity.getEscola().getId(),
                entity.getEscola().getNome(),
                entity.getCodigoIndicador(),
                entity.getDescricao(),
                entity.getValorNumeric(),
                entity.getValorTexto(),
                entity.getReferenciaData());
    }

    private DashboardIndicadorHistoricoResponse toHistoricoResponse(
            PublicoDashboardEntity publico,
            String codigoIndicador,
            List<DashboardIndicadorSnapshotEntity> snapshots) {
        DashboardIndicadorSnapshotEntity atual = snapshots.get(snapshots.size() - 1);
        DashboardIndicadorSnapshotEntity anterior = snapshots.size() > 1 ? snapshots.get(snapshots.size() - 2) : null;

        return new DashboardIndicadorHistoricoResponse(
                publico.getCodigo(),
                codigoIndicador,
                atual.getDescricao(),
                atual.getValorNumeric(),
                anterior == null ? null : anterior.getValorNumeric(),
                calcularVariacaoPercentual(atual.getValorNumeric(), anterior == null ? null : anterior.getValorNumeric()),
                snapshots.stream()
                        .map(snapshot -> new DashboardIndicadorHistoricoPontoResponse(
                                snapshot.getReferenciaData(),
                                snapshot.getValorNumeric(),
                                snapshot.getValorTexto()))
                        .toList());
    }

    private BigDecimal calcularVariacaoPercentual(BigDecimal atual, BigDecimal anterior) {
        if (atual == null || anterior == null || BigDecimal.ZERO.compareTo(anterior) == 0) {
            return null;
        }
        return atual.subtract(anterior)
                .multiply(BigDecimal.valueOf(100))
                .divide(anterior, 2, RoundingMode.HALF_UP);
    }

    private boolean filtrarPorData(DashboardIndicadorSnapshotEntity snapshot, LocalDate dataInicio, LocalDate dataFim) {
        LocalDate referenciaData = snapshot.getReferenciaData();
        return (dataInicio == null || !referenciaData.isBefore(dataInicio))
                && (dataFim == null || !referenciaData.isAfter(dataFim));
    }

    private String normalizarCodigoFiltro(String codigoIndicador, UUID professorId) {
        if (codigoIndicador == null || codigoIndicador.isBlank()) {
            return null;
        }
        String codigo = normalizarCodigo(codigoIndicador);
        if (professorId == null || codigo.startsWith("PROFESSOR_")) {
            return codigo;
        }
        return "PROFESSOR_" + professorId.toString().replace("-", "").toUpperCase(Locale.ROOT) + "_" + codigo;
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
