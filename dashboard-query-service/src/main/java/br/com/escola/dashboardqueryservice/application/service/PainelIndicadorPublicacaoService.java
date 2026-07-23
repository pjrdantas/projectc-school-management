package br.com.escola.dashboardqueryservice.application.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.dashboardqueryservice.application.context.InternalRequestContext;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorPublicacaoCommand;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorSnapshotCommand;
import br.com.escola.dashboardqueryservice.application.dto.PainelIndicadorSnapshotResponse;
import br.com.escola.dashboardqueryservice.application.dto.PainelProjecaoUpsertRequest;
import br.com.escola.dashboardqueryservice.application.dto.TipoPainelProjecao;
import br.com.escola.dashboardqueryservice.application.exception.PainelQueryServiceResourceNotFoundException;
import br.com.escola.dashboardqueryservice.application.port.in.PainelIndicadorLocalUseCase;
import br.com.escola.dashboardqueryservice.application.port.in.PainelIndicadorPublicacaoUseCase;
import br.com.escola.dashboardqueryservice.application.port.in.PainelProjecaoUseCase;
import br.com.escola.dashboardqueryservice.application.port.out.PainelAdministracaoStorePort;
import br.com.escola.dashboardqueryservice.domain.model.PainelPublico;

@Service
@Transactional
public class PainelIndicadorPublicacaoService implements PainelIndicadorPublicacaoUseCase {

    private final PainelIndicadorLocalUseCase indicadorLocalUseCase;
    private final PainelProjecaoUseCase projecaoUseCase;
    private final PainelAdministracaoStorePort publicoStore;
    private final ObjectMapper objectMapper;

    public PainelIndicadorPublicacaoService(
            PainelIndicadorLocalUseCase indicadorLocalUseCase,
            PainelProjecaoUseCase projecaoUseCase,
            PainelAdministracaoStorePort publicoStore,
            ObjectMapper objectMapper) {
        this.indicadorLocalUseCase = indicadorLocalUseCase;
        this.projecaoUseCase = projecaoUseCase;
        this.publicoStore = publicoStore;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<PainelIndicadorSnapshotResponse> publicar(
            InternalRequestContext context, PainelIndicadorPublicacaoCommand command) {
        validarOrigem(command);
        PainelPublico publico = publicoStore.buscarPublico(context.escolaId(), command.publicoId())
                .orElseThrow(() -> new PainelQueryServiceResourceNotFoundException("Publico nao encontrado na escola"));
        List<PainelIndicadorSnapshotResponse> snapshots = command.indicadores().stream()
                .map(indicador -> indicadorLocalUseCase.salvar(context, new PainelIndicadorSnapshotCommand(
                        publico.id(), indicador.codigoIndicador(), indicador.descricao(), indicador.valorNumeric(),
                        indicador.valorTexto(), command.escolaNome(), command.referenciaData())))
                .toList();
        atualizarProjecoes(context, publico, command);
        return snapshots;
    }

    private void atualizarProjecoes(
            InternalRequestContext context, PainelPublico publico, PainelIndicadorPublicacaoCommand command) {
        projecaoUseCase.atualizar(context, new PainelProjecaoUpsertRequest(
                TipoPainelProjecao.SNAPSHOTS, publico.codigo(), null, null, command.referenciaData(),
                objectMapper.valueToTree(indicadorLocalUseCase.listar(context, publico.id(), command.referenciaData()))));
        projecaoUseCase.atualizar(context, new PainelProjecaoUpsertRequest(
                TipoPainelProjecao.HISTORICO, publico.codigo(), null, null, null,
                objectMapper.valueToTree(indicadorLocalUseCase.historico(context, publico.codigo(), null, null, null))));
    }

    private void validarOrigem(PainelIndicadorPublicacaoCommand command) {
        boolean invalido = command.indicadores().stream()
                .map(PainelIndicadorPublicacaoCommand.Indicador::codigoIndicador)
                .map(codigo -> codigo.trim().toUpperCase(Locale.ROOT))
                .anyMatch(codigo -> !command.origem().aceita(codigo));
        if (invalido) {
            throw new IllegalArgumentException("A origem informada nao pode publicar um ou mais codigos de indicador");
        }
    }
}
