package br.com.escola.dashboard.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardConfiguracaoRequest;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardConfiguracaoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardPublicoRequest;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardPublicoResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardWidgetRequest;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardWidgetResponse;
import br.com.escola.dashboard.adapter.out.persistence.entity.DashboardEntity;
import br.com.escola.dashboard.adapter.out.persistence.entity.DashboardWidgetEntity;
import br.com.escola.dashboard.adapter.out.persistence.entity.PublicoDashboardEntity;
import br.com.escola.dashboard.adapter.out.persistence.repository.DashboardJpaRepository;
import br.com.escola.dashboard.adapter.out.persistence.repository.DashboardWidgetJpaRepository;
import br.com.escola.dashboard.adapter.out.persistence.repository.PublicoDashboardJpaRepository;

@Service
public class DashboardConfiguracaoAdminService {

    private final PublicoDashboardJpaRepository publicoDashboardJpaRepository;
    private final DashboardJpaRepository dashboardJpaRepository;
    private final DashboardWidgetJpaRepository dashboardWidgetJpaRepository;

    public DashboardConfiguracaoAdminService(
            PublicoDashboardJpaRepository publicoDashboardJpaRepository,
            DashboardJpaRepository dashboardJpaRepository,
            DashboardWidgetJpaRepository dashboardWidgetJpaRepository) {
        this.publicoDashboardJpaRepository = publicoDashboardJpaRepository;
        this.dashboardJpaRepository = dashboardJpaRepository;
        this.dashboardWidgetJpaRepository = dashboardWidgetJpaRepository;
    }

    @Transactional(readOnly = true)
    public List<DashboardPublicoResponse> listarPublicos() {
        return publicoDashboardJpaRepository.findAll().stream()
                .map(this::toPublicoResponse)
                .sorted((left, right) -> left.codigo().compareToIgnoreCase(right.codigo()))
                .toList();
    }

    @Transactional
    public DashboardPublicoResponse criarPublico(DashboardPublicoRequest request) {
        String codigo = normalizarCodigo(request.codigo());
        if (publicoDashboardJpaRepository.existsByCodigo(codigo)) {
            throw conflict("Público de dashboard já cadastrado para o código " + codigo);
        }

        PublicoDashboardEntity entity = PublicoDashboardEntity.builder()
                .codigo(codigo)
                .descricao(request.descricao().trim())
                .build();
        return toPublicoResponse(publicoDashboardJpaRepository.save(entity));
    }

    @Transactional
    public DashboardPublicoResponse atualizarPublico(UUID id, DashboardPublicoRequest request) {
        PublicoDashboardEntity entity = buscarPublico(id);
        String codigo = normalizarCodigo(request.codigo());
        publicoDashboardJpaRepository.findByCodigo(codigo)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw conflict("Público de dashboard já cadastrado para o código " + codigo);
                });

        entity.setCodigo(codigo);
        entity.setDescricao(request.descricao().trim());
        return toPublicoResponse(publicoDashboardJpaRepository.save(entity));
    }

    @Transactional
    public void excluirPublico(UUID id) {
        if (!publicoDashboardJpaRepository.existsById(id)) {
            throw notFound("Público de dashboard não encontrado para o id " + id);
        }
        publicoDashboardJpaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<DashboardConfiguracaoResponse> listarDashboards(UUID publicoDashboardId) {
        List<DashboardEntity> dashboards = publicoDashboardId == null
                ? dashboardJpaRepository.findAllByOrderByNomeAsc()
                : dashboardJpaRepository.findByPublicoDashboardIdOrderByNomeAsc(publicoDashboardId);
        return dashboards.stream()
                .map(this::toDashboardResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DashboardConfiguracaoResponse> listarDashboardsPorPublicoCodigo(String publicoCodigo) {
        PublicoDashboardEntity publico = publicoDashboardJpaRepository.findByCodigo(normalizarCodigo(publicoCodigo))
                .orElseThrow(() -> notFound("Público de dashboard não encontrado para o código " + publicoCodigo));
        return listarDashboards(publico.getId());
    }

    @Transactional
    public DashboardConfiguracaoResponse criarDashboard(DashboardConfiguracaoRequest request) {
        PublicoDashboardEntity publico = buscarPublico(request.publicoDashboardId());
        String codigo = normalizarCodigo(request.codigo());
        if (dashboardJpaRepository.existsByCodigo(codigo)) {
            throw conflict("Dashboard já cadastrado para o código " + codigo);
        }

        DashboardEntity entity = DashboardEntity.builder()
                .publicoDashboard(publico)
                .codigo(codigo)
                .nome(request.nome().trim())
                .descricao(trimToNull(request.descricao()))
                .ativo(request.ativo() == null || request.ativo())
                .createdAt(LocalDateTime.now())
                .build();
        return toDashboardResponse(dashboardJpaRepository.save(entity));
    }

    @Transactional
    public DashboardConfiguracaoResponse atualizarDashboard(UUID id, DashboardConfiguracaoRequest request) {
        DashboardEntity entity = buscarDashboard(id);
        PublicoDashboardEntity publico = buscarPublico(request.publicoDashboardId());
        String codigo = normalizarCodigo(request.codigo());
        dashboardJpaRepository.findByCodigo(codigo)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw conflict("Dashboard já cadastrado para o código " + codigo);
                });

        entity.setPublicoDashboard(publico);
        entity.setCodigo(codigo);
        entity.setNome(request.nome().trim());
        entity.setDescricao(trimToNull(request.descricao()));
        entity.setAtivo(request.ativo() == null || request.ativo());
        return toDashboardResponse(dashboardJpaRepository.save(entity));
    }

    @Transactional
    public void excluirDashboard(UUID id) {
        if (!dashboardJpaRepository.existsById(id)) {
            throw notFound("Dashboard não encontrado para o id " + id);
        }
        dashboardJpaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<DashboardWidgetResponse> listarWidgets(UUID dashboardId) {
        buscarDashboard(dashboardId);
        return dashboardWidgetJpaRepository.findByDashboardIdOrderByOrdemAscTituloAsc(dashboardId).stream()
                .map(this::toWidgetResponse)
                .toList();
    }

    @Transactional
    public DashboardWidgetResponse criarWidget(DashboardWidgetRequest request) {
        DashboardEntity dashboard = buscarDashboard(request.dashboardId());
        String codigo = normalizarCodigo(request.codigo());
        if (dashboardWidgetJpaRepository.existsByDashboardIdAndCodigo(dashboard.getId(), codigo)) {
            throw conflict("Widget já cadastrado para este dashboard e código " + codigo);
        }

        DashboardWidgetEntity entity = DashboardWidgetEntity.builder()
                .dashboard(dashboard)
                .codigo(codigo)
                .titulo(request.titulo().trim())
                .descricao(trimToNull(request.descricao()))
                .tipoWidget(normalizarCodigo(request.tipoWidget()))
                .ordem(request.ordem() == null ? 0 : request.ordem())
                .queryReferencia(trimToNull(request.queryReferencia()))
                .ativo(request.ativo() == null || request.ativo())
                .createdAt(LocalDateTime.now())
                .build();
        return toWidgetResponse(dashboardWidgetJpaRepository.save(entity));
    }

    @Transactional
    public DashboardWidgetResponse atualizarWidget(UUID id, DashboardWidgetRequest request) {
        DashboardWidgetEntity entity = buscarWidget(id);
        DashboardEntity dashboard = buscarDashboard(request.dashboardId());
        String codigo = normalizarCodigo(request.codigo());
        dashboardWidgetJpaRepository.findByDashboardIdAndCodigo(dashboard.getId(), codigo)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw conflict("Widget já cadastrado para este dashboard e código " + codigo);
                });

        entity.setDashboard(dashboard);
        entity.setCodigo(codigo);
        entity.setTitulo(request.titulo().trim());
        entity.setDescricao(trimToNull(request.descricao()));
        entity.setTipoWidget(normalizarCodigo(request.tipoWidget()));
        entity.setOrdem(request.ordem() == null ? 0 : request.ordem());
        entity.setQueryReferencia(trimToNull(request.queryReferencia()));
        entity.setAtivo(request.ativo() == null || request.ativo());
        return toWidgetResponse(dashboardWidgetJpaRepository.save(entity));
    }

    @Transactional
    public void excluirWidget(UUID id) {
        if (!dashboardWidgetJpaRepository.existsById(id)) {
            throw notFound("Widget de dashboard não encontrado para o id " + id);
        }
        dashboardWidgetJpaRepository.deleteById(id);
    }

    private PublicoDashboardEntity buscarPublico(UUID id) {
        return publicoDashboardJpaRepository.findById(id)
                .orElseThrow(() -> notFound("Público de dashboard não encontrado para o id " + id));
    }

    private DashboardEntity buscarDashboard(UUID id) {
        return dashboardJpaRepository.findById(id)
                .orElseThrow(() -> notFound("Dashboard não encontrado para o id " + id));
    }

    private DashboardWidgetEntity buscarWidget(UUID id) {
        return dashboardWidgetJpaRepository.findById(id)
                .orElseThrow(() -> notFound("Widget de dashboard não encontrado para o id " + id));
    }

    private DashboardPublicoResponse toPublicoResponse(PublicoDashboardEntity entity) {
        return new DashboardPublicoResponse(entity.getId(), entity.getCodigo(), entity.getDescricao());
    }

    private DashboardConfiguracaoResponse toDashboardResponse(DashboardEntity entity) {
        return new DashboardConfiguracaoResponse(
                entity.getId(),
                entity.getPublicoDashboard().getId(),
                entity.getPublicoDashboard().getCodigo(),
                entity.getCodigo(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getAtivo());
    }

    private DashboardWidgetResponse toWidgetResponse(DashboardWidgetEntity entity) {
        return new DashboardWidgetResponse(
                entity.getId(),
                entity.getDashboard().getId(),
                entity.getDashboard().getCodigo(),
                entity.getCodigo(),
                entity.getTitulo(),
                entity.getDescricao(),
                entity.getTipoWidget(),
                entity.getOrdem(),
                entity.getQueryReferencia(),
                entity.getAtivo());
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

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
