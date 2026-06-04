package br.com.escola.dashboard.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.dashboard.adapter.in.web.dto.DashboardUsuarioConfiguracaoRequest;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardUsuarioConfiguracaoResponse;
import br.com.escola.dashboard.adapter.out.persistence.entity.DashboardUsuarioConfiguracaoEntity;
import br.com.escola.dashboard.adapter.out.persistence.entity.DashboardWidgetEntity;
import br.com.escola.dashboard.adapter.out.persistence.repository.DashboardUsuarioConfiguracaoJpaRepository;
import br.com.escola.dashboard.adapter.out.persistence.repository.DashboardWidgetJpaRepository;
import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;
import br.com.escola.seguranca.adapter.out.persistence.repository.SpringUsuarioJpaRepository;

@Service
public class DashboardUsuarioConfiguracaoService {

    private final DashboardUsuarioConfiguracaoJpaRepository dashboardUsuarioConfiguracaoJpaRepository;
    private final DashboardWidgetJpaRepository dashboardWidgetJpaRepository;
    private final SpringUsuarioJpaRepository usuarioJpaRepository;
    private final ObjectMapper objectMapper;

    public DashboardUsuarioConfiguracaoService(
            DashboardUsuarioConfiguracaoJpaRepository dashboardUsuarioConfiguracaoJpaRepository,
            DashboardWidgetJpaRepository dashboardWidgetJpaRepository,
            SpringUsuarioJpaRepository usuarioJpaRepository,
            ObjectMapper objectMapper) {
        this.dashboardUsuarioConfiguracaoJpaRepository = dashboardUsuarioConfiguracaoJpaRepository;
        this.dashboardWidgetJpaRepository = dashboardWidgetJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<DashboardUsuarioConfiguracaoResponse> listar(UUID usuarioId, UUID dashboardId) {
        buscarUsuario(usuarioId);

        return dashboardUsuarioConfiguracaoJpaRepository.findByUsuarioIdOrderByOrdemAscDashboardWidgetTituloAsc(usuarioId).stream()
                .filter(configuracao -> dashboardId == null
                        || configuracao.getDashboardWidget().getDashboard().getId().equals(dashboardId))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DashboardUsuarioConfiguracaoResponse salvar(
            UUID usuarioId,
            UUID dashboardWidgetId,
            DashboardUsuarioConfiguracaoRequest request) {
        UsuarioEntity usuario = buscarUsuario(usuarioId);
        DashboardWidgetEntity widget = buscarWidget(dashboardWidgetId);
        LocalDateTime now = LocalDateTime.now();

        DashboardUsuarioConfiguracaoEntity entity = dashboardUsuarioConfiguracaoJpaRepository
                .findByUsuarioIdAndDashboardWidgetId(usuarioId, dashboardWidgetId)
                .orElseGet(() -> DashboardUsuarioConfiguracaoEntity.builder()
                        .usuario(usuario)
                        .dashboardWidget(widget)
                        .createdAt(now)
                        .build());

        entity.setUsuario(usuario);
        entity.setDashboardWidget(widget);
        entity.setVisivel(request.visivel() == null || request.visivel());
        entity.setOrdem(request.ordem());
        entity.setConfiguracaoJson(normalizarConfiguracaoJson(request.configuracaoJson()));
        entity.setUpdatedAt(now);

        return toResponse(dashboardUsuarioConfiguracaoJpaRepository.save(entity));
    }

    @Transactional
    public void excluir(UUID usuarioId, UUID dashboardWidgetId) {
        buscarUsuario(usuarioId);
        buscarWidget(dashboardWidgetId);

        DashboardUsuarioConfiguracaoEntity entity = dashboardUsuarioConfiguracaoJpaRepository
                .findByUsuarioIdAndDashboardWidgetId(usuarioId, dashboardWidgetId)
                .orElseThrow(() -> notFound("Configuração de dashboard não encontrada para o usuário e widget informados"));
        dashboardUsuarioConfiguracaoJpaRepository.delete(entity);
    }

    private UsuarioEntity buscarUsuario(UUID usuarioId) {
        return usuarioJpaRepository.findById(usuarioId)
                .orElseThrow(() -> notFound("Usuário não encontrado para o id " + usuarioId));
    }

    private DashboardWidgetEntity buscarWidget(UUID dashboardWidgetId) {
        return dashboardWidgetJpaRepository.findById(dashboardWidgetId)
                .orElseThrow(() -> notFound("Widget de dashboard não encontrado para o id " + dashboardWidgetId));
    }

    private DashboardUsuarioConfiguracaoResponse toResponse(DashboardUsuarioConfiguracaoEntity entity) {
        DashboardWidgetEntity widget = entity.getDashboardWidget();
        return new DashboardUsuarioConfiguracaoResponse(
                entity.getId(),
                entity.getUsuario().getId(),
                widget.getId(),
                widget.getCodigo(),
                widget.getTitulo(),
                widget.getDashboard().getId(),
                widget.getDashboard().getCodigo(),
                entity.getVisivel(),
                entity.getOrdem(),
                entity.getConfiguracaoJson());
    }

    private String normalizarConfiguracaoJson(String configuracaoJson) {
        if (configuracaoJson == null || configuracaoJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(objectMapper.readTree(configuracaoJson));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "configuracaoJson deve conter um JSON válido");
        }
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
