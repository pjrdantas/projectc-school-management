package br.com.escola.dashboardqueryservice.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorHistoricoPontoResponse;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorHistoricoResponse;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorSnapshotCommand;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorSnapshotResponse;
import br.com.escola.dashboardqueryservice.application.exception.PainelQueryServiceResourceNotFoundException;
import br.com.escola.dashboardqueryservice.application.port.in.PainelIndicadorLocalUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelAdministracaoStorePort;
import br.com.escola.dashboardqueryservice.application.port.out.PainelIndicadorSnapshotStorePort;
import br.com.escola.dashboardqueryservice.domain.model.PainelIndicadorSnapshot;
import br.com.escola.dashboardqueryservice.domain.model.PainelPublico;

@Service
@Transactional
public class PainelIndicadorLocalService implements PainelIndicadorLocalUseCase {

    private final PainelAdministracaoStorePort publicoStore;
    private final PainelIndicadorSnapshotStorePort snapshotStore;

    public PainelIndicadorLocalService(
            PainelAdministracaoStorePort publicoStore,
            PainelIndicadorSnapshotStorePort snapshotStore) {
        this.publicoStore = publicoStore;
        this.snapshotStore = snapshotStore;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PainelIndicadorSnapshotResponse> listar(InternalRequestContext context, UUID publicoId, LocalDate referenciaData) {
        PainelPublico publico = publico(context.escolaId(), publicoId);
        return snapshotStore.listar(context.escolaId(), publico.id(), referenciaData).stream()
                .map(snapshot -> resposta(publico, snapshot)).toList();
    }

    @Override
    public PainelIndicadorSnapshotResponse salvar(InternalRequestContext context, PainelIndicadorSnapshotCommand command) {
        PainelPublico publico = publico(context.escolaId(), command.publicoId());
        String codigo = codigo(command.codigoIndicador());
        PainelIndicadorSnapshot atual = snapshotStore.buscarPorChave(
                context.escolaId(), publico.id(), codigo, command.referenciaData()).orElse(null);
        PainelIndicadorSnapshot salvo = snapshotStore.salvar(new PainelIndicadorSnapshot(
                atual == null ? UUID.randomUUID() : atual.id(), context.escolaId(), publico.id(), codigo,
                texto(command.descricao()), command.valorNumeric(), textoOpcional(command.valorTexto()),
                textoOpcional(command.escolaNome()), command.referenciaData()));
        return resposta(publico, salvo);
    }

    @Override
    public void excluir(InternalRequestContext context, UUID snapshotId) {
        PainelIndicadorSnapshot snapshot = snapshotStore.buscar(context.escolaId(), snapshotId)
                .orElseThrow(() -> naoEncontrado("Snapshot de indicador nao encontrado na escola"));
        snapshotStore.excluir(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PainelIndicadorHistoricoResponse> historico(
            InternalRequestContext context,
            String publicoCodigo,
            String codigoIndicador,
            LocalDate dataInicio,
            LocalDate dataFim) {
        PainelPublico publico = publicoStore.buscarPublicoPorCodigo(context.escolaId(), codigo(publicoCodigo))
                .orElseThrow(() -> naoEncontrado("Publico nao encontrado na escola"));
        return snapshotStore.listarHistorico(context.escolaId(), publico.id()).stream()
                .filter(snapshot -> codigoIndicador == null || codigo(snapshot.codigoIndicador()).equals(codigo(codigoIndicador)))
                .filter(snapshot -> dataInicio == null || !snapshot.referenciaData().isBefore(dataInicio))
                .filter(snapshot -> dataFim == null || !snapshot.referenciaData().isAfter(dataFim))
                .collect(Collectors.groupingBy(PainelIndicadorSnapshot::codigoIndicador))
                .entrySet().stream()
                .map(entry -> historico(publico.codigo(), entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(PainelIndicadorHistoricoResponse::codigoIndicador))
                .toList();
    }

    private PainelIndicadorHistoricoResponse historico(
            String publicoCodigo, String codigoIndicador, List<PainelIndicadorSnapshot> snapshots) {
        List<PainelIndicadorSnapshot> ordenados = snapshots.stream()
                .sorted(Comparator.comparing(PainelIndicadorSnapshot::referenciaData)).toList();
        PainelIndicadorSnapshot atual = ordenados.getLast();
        PainelIndicadorSnapshot anterior = ordenados.size() > 1 ? ordenados.get(ordenados.size() - 2) : null;
        return new PainelIndicadorHistoricoResponse(publicoCodigo, codigoIndicador, atual.descricao(),
                atual.valorNumeric(), anterior == null ? null : anterior.valorNumeric(),
                variacao(atual.valorNumeric(), anterior == null ? null : anterior.valorNumeric()),
                ordenados.stream().map(snapshot -> new PainelIndicadorHistoricoPontoResponse(
                        snapshot.referenciaData(), snapshot.valorNumeric(), snapshot.valorTexto())).toList());
    }

    private BigDecimal variacao(BigDecimal atual, BigDecimal anterior) {
        if (atual == null || anterior == null || BigDecimal.ZERO.compareTo(anterior) == 0) {
            return null;
        }
        return atual.subtract(anterior).multiply(BigDecimal.valueOf(100))
                .divide(anterior.abs(), 2, RoundingMode.HALF_UP);
    }

    private PainelIndicadorSnapshotResponse resposta(PainelPublico publico, PainelIndicadorSnapshot snapshot) {
        return new PainelIndicadorSnapshotResponse(snapshot.id(), publico.id(), publico.codigo(), snapshot.escolaId(),
                snapshot.escolaNome(), snapshot.codigoIndicador(), snapshot.descricao(), snapshot.valorNumeric(),
                snapshot.valorTexto(), snapshot.referenciaData());
    }

    private PainelPublico publico(UUID escolaId, UUID publicoId) {
        return publicoStore.buscarPublico(escolaId, publicoId)
                .orElseThrow(() -> naoEncontrado("Publico nao encontrado na escola"));
    }

    private String codigo(String value) {
        return texto(value).toUpperCase(Locale.ROOT);
    }

    private String texto(String value) {
        return value.trim();
    }

    private String textoOpcional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private PainelQueryServiceResourceNotFoundException naoEncontrado(String message) {
        return new PainelQueryServiceResourceNotFoundException(message);
    }
}
