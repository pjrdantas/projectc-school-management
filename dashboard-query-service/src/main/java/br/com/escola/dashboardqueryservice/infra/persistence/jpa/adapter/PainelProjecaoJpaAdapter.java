package br.com.escola.dashboardqueryservice.infra.persistence.jpa.adapter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelAcademicoResponse;
import br.com.escola.dashboardqueryservice.application.dto.PainelAlertaResponse;
import br.com.escola.dashboardqueryservice.application.dto.PainelConfiguracaoResponse;
import br.com.escola.dashboardqueryservice.application.dto.PainelDiretorResponse;
import br.com.escola.dashboardqueryservice.application.dto.PainelFrontendResponse;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorHistoricoPontoResponse;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorHistoricoResponse;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorSnapshotResponse;
import br.com.escola.dashboardqueryservice.application.dto.PainelProfessorResponse;
import br.com.escola.dashboardqueryservice.application.dto.PainelProjecaoUpsertRequest;
import br.com.escola.dashboardqueryservice.application.dto.PainelPublicoResponse;
import br.com.escola.dashboardqueryservice.application.dto.PainelSecretariaResponse;
import br.com.escola.dashboardqueryservice.application.dto.TipoPainelProjecao;
import br.com.escola.dashboardqueryservice.application.exception.PainelQueryServiceResourceNotFoundException;
import br.com.escola.dashboardqueryservice.application.port.out.PainelAcademicoPort;
import br.com.escola.dashboardqueryservice.application.port.out.PainelAlertaPort;
import br.com.escola.dashboardqueryservice.application.port.out.PainelConfiguracaoPort;
import br.com.escola.dashboardqueryservice.application.port.out.PainelDiretorPort;
import br.com.escola.dashboardqueryservice.application.port.out.PainelFrontendPort;
import br.com.escola.dashboardqueryservice.application.port.out.PainelIndicadorHistoricoPort;
import br.com.escola.dashboardqueryservice.application.port.out.PainelIndicadorSnapshotPort;
import br.com.escola.dashboardqueryservice.application.port.out.PainelProfessorPort;
import br.com.escola.dashboardqueryservice.application.port.out.PainelProjecaoPort;
import br.com.escola.dashboardqueryservice.application.port.out.PainelPublicoPort;
import br.com.escola.dashboardqueryservice.application.port.out.PainelSecretariaPort;
import br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity.PainelProjecaoJpaEntity;
import br.com.escola.dashboardqueryservice.infra.persistence.jpa.repository.PainelProjecaoJpaRepository;

@Component
@Transactional(readOnly = true)
public class PainelProjecaoJpaAdapter implements
        PainelAcademicoPort,
        PainelAlertaPort,
        PainelConfiguracaoPort,
        PainelDiretorPort,
        PainelFrontendPort,
        PainelIndicadorHistoricoPort,
        PainelIndicadorSnapshotPort,
        PainelProfessorPort,
        PainelProjecaoPort,
        PainelPublicoPort,
        PainelSecretariaPort {

    private final PainelProjecaoJpaRepository repository;
    private final ObjectMapper objectMapper;

    public PainelProjecaoJpaAdapter(PainelProjecaoJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public PainelAcademicoResponse consultarAcademico(String authorization, InternalRequestContext context) {
        return lerUnico(context.escolaId(), TipoPainelProjecao.ACADEMICO, projection -> true,
                PainelAcademicoResponse.class, "Projecao do painel academico nao encontrada");
    }

    @Override
    public PainelSecretariaResponse consultarSecretaria(String authorization, InternalRequestContext context) {
        return lerUnico(context.escolaId(), TipoPainelProjecao.SECRETARIA, projection -> true,
                PainelSecretariaResponse.class, "Projecao do painel da secretaria nao encontrada");
    }

    @Override
    public PainelDiretorResponse consultarDiretor(String authorization, InternalRequestContext context) {
        return lerUnico(context.escolaId(), TipoPainelProjecao.DIRETOR, projection -> true,
                PainelDiretorResponse.class, "Projecao do painel do diretor nao encontrada");
    }

    @Override
    public PainelProfessorResponse consultar(
            String authorization,
            InternalRequestContext context,
            UUID professorId) {
        return lerUnico(context.escolaId(), TipoPainelProjecao.PROFESSOR,
                projection -> professorId.equals(projection.getProfessorId()),
                PainelProfessorResponse.class, "Projecao do painel do professor nao encontrada");
    }

    @Override
    public List<PainelAlertaResponse> consultar(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            UUID professorId) {
        return lerListaOpcional(context.escolaId(), TipoPainelProjecao.ALERTAS,
                projection -> codigoIgual(publicoCodigo, projection.getPublicoCodigo())
                        && Objects.equals(professorId, projection.getProfessorId()),
                PainelAlertaResponse.class);
    }

    @Override
    public PainelFrontendResponse consultar(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            UUID usuarioId,
            UUID professorId) {
        return lerUnico(context.escolaId(), TipoPainelProjecao.FRONTEND,
                projection -> codigoIgual(publicoCodigo, projection.getPublicoCodigo())
                        && Objects.equals(usuarioId, projection.getUsuarioId())
                        && Objects.equals(professorId, projection.getProfessorId()),
                PainelFrontendResponse.class, "Projecao do painel frontend nao encontrada");
    }

    @Override
    public List<PainelIndicadorSnapshotResponse> listarPorPublicoCodigo(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            LocalDate referenciaData) {
        return lerListaOpcional(context.escolaId(), TipoPainelProjecao.SNAPSHOTS,
                projection -> codigoIgual(publicoCodigo, projection.getPublicoCodigo())
                        && (referenciaData == null || referenciaData.equals(projection.getReferenciaData())),
                PainelIndicadorSnapshotResponse.class);
    }

    @Override
    public List<PainelIndicadorHistoricoResponse> consultarHistorico(
            String authorization,
            InternalRequestContext context,
            String publicoCodigo,
            String codigoIndicador,
            LocalDate dataInicio,
            LocalDate dataFim,
            UUID professorId) {
        return lerListaOpcional(context.escolaId(), TipoPainelProjecao.HISTORICO,
                projection -> codigoIgual(publicoCodigo, projection.getPublicoCodigo())
                        && Objects.equals(professorId, projection.getProfessorId()),
                PainelIndicadorHistoricoResponse.class).stream()
                .filter(item -> codigoIndicador == null || codigoIgual(codigoIndicador, item.codigoIndicador()))
                .map(item -> filtrarPontos(item, dataInicio, dataFim))
                .toList();
    }

    @Override
    public List<PainelPublicoResponse> listarPublicos(
            String authorization,
            InternalRequestContext context) {
        return lerListaOpcional(context.escolaId(), TipoPainelProjecao.PUBLICOS,
                projection -> true, PainelPublicoResponse.class);
    }

    @Override
    public List<PainelConfiguracaoResponse> listarPainels(
            String authorization,
            InternalRequestContext context,
            UUID publicoPainelId,
            String publicoCodigo) {
        return lerListaOpcional(context.escolaId(), TipoPainelProjecao.PAINEIS,
                projection -> true, PainelConfiguracaoResponse.class).stream()
                .filter(item -> publicoPainelId == null || publicoPainelId.equals(item.publicoPainelId()))
                .filter(item -> publicoCodigo == null || codigoIgual(publicoCodigo, item.publicoCodigo()))
                .toList();
    }

    @Override
    @Transactional
    public void salvar(UUID escolaId, PainelProjecaoUpsertRequest request) {
        validarMetadados(request);
        validarPayload(request);

        String chave = chave(request);
        PainelProjecaoJpaEntity entity = repository.findByEscolaIdAndChaveProjecao(escolaId, chave)
                .orElseGet(() -> new PainelProjecaoJpaEntity(UUID.randomUUID(), escolaId, request.tipo(), chave));
        entity.atualizar(
                normalizarCodigo(request.publicoCodigo()),
                request.professorId(),
                request.usuarioId(),
                request.referenciaData(),
                request.payload().toString(),
                LocalDateTime.now());
        repository.save(entity);
    }

    private PainelIndicadorHistoricoResponse filtrarPontos(
            PainelIndicadorHistoricoResponse item,
            LocalDate dataInicio,
            LocalDate dataFim) {
        List<PainelIndicadorHistoricoPontoResponse> pontos = item.pontos().stream()
                .filter(ponto -> dataInicio == null || !ponto.referenciaData().isBefore(dataInicio))
                .filter(ponto -> dataFim == null || !ponto.referenciaData().isAfter(dataFim))
                .toList();
        return new PainelIndicadorHistoricoResponse(
                item.publicoCodigo(),
                item.codigoIndicador(),
                item.descricao(),
                item.valorAtual(),
                item.valorAnterior(),
                item.variacaoPercentual(),
                pontos);
    }

    private <T> T lerUnico(
            UUID escolaId,
            TipoPainelProjecao tipo,
            Predicate<PainelProjecaoJpaEntity> filtro,
            Class<T> responseType,
            String mensagemAusencia) {
        return projecoes(escolaId, tipo).stream()
                .filter(filtro)
                .findFirst()
                .map(projection -> desserializar(projection.getPayload(), responseType))
                .orElseThrow(() -> new PainelQueryServiceResourceNotFoundException(mensagemAusencia));
    }

    private <T> List<T> lerListaOpcional(
            UUID escolaId,
            TipoPainelProjecao tipo,
            Predicate<PainelProjecaoJpaEntity> filtro,
            Class<T> itemType) {
        return projecoes(escolaId, tipo).stream()
                .filter(filtro)
                .findFirst()
                .map(projection -> desserializarLista(projection.getPayload(), itemType))
                .orElseGet(List::of);
    }

    private List<PainelProjecaoJpaEntity> projecoes(UUID escolaId, TipoPainelProjecao tipo) {
        return repository.findByEscolaIdAndTipoOrderByUpdatedAtDesc(escolaId, tipo);
    }

    private void validarMetadados(PainelProjecaoUpsertRequest request) {
        switch (request.tipo()) {
            case PROFESSOR -> obrigatorio(request.professorId(), "professorId");
            case ALERTAS, FRONTEND, SNAPSHOTS, HISTORICO ->
                    obrigatorio(request.publicoCodigo(), "publicoCodigo");
            default -> {
            }
        }
    }

    private void validarPayload(PainelProjecaoUpsertRequest request) {
        switch (request.tipo()) {
            case ACADEMICO -> converter(request, PainelAcademicoResponse.class);
            case SECRETARIA -> converter(request, PainelSecretariaResponse.class);
            case DIRETOR -> converter(request, PainelDiretorResponse.class);
            case PROFESSOR -> converter(request, PainelProfessorResponse.class);
            case FRONTEND -> converter(request, PainelFrontendResponse.class);
            case ALERTAS -> converterLista(request, PainelAlertaResponse.class);
            case SNAPSHOTS -> converterLista(request, PainelIndicadorSnapshotResponse.class);
            case HISTORICO -> converterLista(request, PainelIndicadorHistoricoResponse.class);
            case PUBLICOS -> converterLista(request, PainelPublicoResponse.class);
            case PAINEIS -> converterLista(request, PainelConfiguracaoResponse.class);
        }
    }

    private void converter(PainelProjecaoUpsertRequest request, Class<?> responseType) {
        try {
            objectMapper.treeToValue(request.payload(), responseType);
        } catch (JacksonException exception) {
            throw payloadInvalido(request, exception);
        }
    }

    private void converterLista(PainelProjecaoUpsertRequest request, Class<?> itemType) {
        try {
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, itemType);
            objectMapper.convertValue(request.payload(), type);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Payload invalido para a projecao " + request.tipo(), exception);
        }
    }

    private IllegalArgumentException payloadInvalido(
            PainelProjecaoUpsertRequest request,
            JacksonException exception) {
        return new IllegalArgumentException("Payload invalido para a projecao " + request.tipo(), exception);
    }

    private <T> T desserializar(String payload, Class<T> responseType) {
        try {
            return objectMapper.readValue(payload, responseType);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Projecao local invalida para " + responseType.getSimpleName(), exception);
        }
    }

    private <T> List<T> desserializarLista(String payload, Class<T> itemType) {
        try {
            JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, itemType);
            return objectMapper.readValue(payload, type);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Projecao local invalida para " + itemType.getSimpleName(), exception);
        }
    }

    private String chave(PainelProjecaoUpsertRequest request) {
        return String.join("|",
                request.tipo().name(),
                valor(normalizarCodigo(request.publicoCodigo())),
                valor(request.professorId()),
                valor(request.usuarioId()),
                valor(request.referenciaData()));
    }

    private String normalizarCodigo(String codigo) {
        return codigo == null || codigo.isBlank() ? null : codigo.trim().toUpperCase(Locale.ROOT);
    }

    private boolean codigoIgual(String expected, String actual) {
        return expected != null && actual != null && expected.trim().equalsIgnoreCase(actual.trim());
    }

    private String valor(Object value) {
        return value == null ? "-" : value.toString();
    }

    private void obrigatorio(Object value, String campo) {
        if (value == null || value instanceof String text && text.isBlank()) {
            throw new IllegalArgumentException("Campo obrigatorio para o tipo de projecao: " + campo);
        }
    }
}
