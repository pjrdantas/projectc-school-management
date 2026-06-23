package br.com.escola.professor.application.service;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import br.com.escola.institucional.application.service.EscolaTenantService;
import br.com.escola.professor.adapter.in.web.dto.ProfessorAlocacaoRequest;
import br.com.escola.professor.adapter.in.web.dto.ProfessorAlocacaoResponse;
import br.com.escola.professor.adapter.in.web.dto.ProfessorFuncionarioElegivelResponse;
import br.com.escola.professor.adapter.in.web.dto.ProfessorRequest;
import br.com.escola.professor.adapter.in.web.dto.ProfessorResponse;
import br.com.escola.professor.application.dto.internal.AlocarProfessorTurmaDisciplinaSolicitacao;
import br.com.escola.professor.application.dto.internal.CriarProfessorSolicitacao;
import br.com.escola.professor.application.dto.internal.ProfessorAlocacaoResumo;
import br.com.escola.professor.application.dto.internal.ProfessorResumo;
import br.com.escola.professor.application.port.internal.ProfessorAcademicoPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class ProfessorFluxoOrquestradorService {

    private final ProfessorService professorService;
    private final ProfessorAcademicoPort professorInternalApiClient;
    private final EscolaTenantService escolaTenantService;
    private final MeterRegistry meterRegistry;
    private final boolean internalClientEnabled;
    private final boolean fallbackLocalOnError;

    public ProfessorFluxoOrquestradorService(
            ProfessorService professorService,
            @Qualifier("professorInternalApiClient") ProfessorAcademicoPort professorInternalApiClient,
            EscolaTenantService escolaTenantService,
            MeterRegistry meterRegistry,
            @Value("${professor.internal-client.enabled:false}") boolean internalClientEnabled,
            @Value("${professor.internal-client.fallback-local-on-error:true}") boolean fallbackLocalOnError) {
        this.professorService = professorService;
        this.professorInternalApiClient = professorInternalApiClient;
        this.escolaTenantService = escolaTenantService;
        this.meterRegistry = meterRegistry;
        this.internalClientEnabled = internalClientEnabled;
        this.fallbackLocalOnError = fallbackLocalOnError;
    }

    public ProfessorResponse criar(ProfessorRequest request) {
        return executarComClienteInterno(
                "criar",
                () -> toProfessorResponse(professorInternalApiClient.criarProfessor(
                        escolaPadraoId(),
                        new CriarProfessorSolicitacao(
                                request.funcionarioId(),
                                request.registroProfissional(),
                                request.formacao(),
                                request.ativo()))),
                () -> professorService.criar(request));
    }

    public List<ProfessorResponse> listar() {
        return professorService.listar();
    }

    public List<ProfessorFuncionarioElegivelResponse> listarFuncionariosElegiveis() {
        return professorService.listarFuncionariosElegiveis();
    }

    public ProfessorResponse buscarPorId(UUID id) {
        return executarComClienteInterno(
                "buscarPorId",
                () -> professorInternalApiClient.buscarProfessor(escolaPadraoId(), id)
                        .map(this::toProfessorResponse)
                        .orElseThrow(br.com.escola.professor.domain.exception.ProfessorNaoEncontradoException::new),
                () -> professorService.buscarPorId(id));
    }

    public ProfessorAlocacaoResponse vincularTurmaDisciplina(UUID professorId, ProfessorAlocacaoRequest request) {
        return executarComClienteInterno(
                "vincularTurmaDisciplina",
                () -> toAlocacaoResponse(professorInternalApiClient.alocarProfessorTurmaDisciplina(
                        escolaPadraoId(),
                        professorId,
                        new AlocarProfessorTurmaDisciplinaSolicitacao(
                                request.turmaDisciplinaId(),
                                request.dataInicio(),
                                request.dataFim(),
                                request.ativo()))),
                () -> professorService.vincularTurmaDisciplina(professorId, request));
    }

    public List<ProfessorAlocacaoResponse> listarAlocacoes(UUID professorId) {
        return executarComClienteInterno(
                "listarAlocacoes",
                () -> professorInternalApiClient.listarAlocacoes(escolaPadraoId(), professorId).stream()
                        .map(this::toAlocacaoResponse)
                        .toList(),
                () -> professorService.listarAlocacoes(professorId));
    }

    private <T> T executarComClienteInterno(String operacao, Supplier<T> remoto, Supplier<T> local) {
        if (!internalClientEnabled) {
            registrarRequisicao(operacao, "local", "feature_disabled");
            return local.get();
        }

        try {
            T resultado = remoto.get();
            registrarRequisicao(operacao, "internal", "success");
            return resultado;
        } catch (RuntimeException exception) {
            registrarRequisicao(operacao, "internal", "error");

            if (!fallbackLocalOnError || !permiteFallback(exception)) {
                throw exception;
            }

            registrarFallback(operacao, exception);
            T resultado = local.get();
            registrarRequisicao(operacao, "local", "fallback");
            return resultado;
        }
    }

    private boolean permiteFallback(RuntimeException exception) {
        return exception instanceof RestClientException;
    }

    private void registrarRequisicao(String operacao, String destino, String resultado) {
        meterRegistry.counter(
                "professor.internal.client.requests",
                "operacao", operacao,
                "destino", destino,
                "resultado", resultado)
                .increment();
    }

    private void registrarFallback(String operacao, RuntimeException exception) {
        meterRegistry.counter(
                "professor.internal.client.fallbacks",
                "operacao", operacao,
                "causa", exception.getClass().getSimpleName())
                .increment();
    }

    private UUID escolaPadraoId() {
        return escolaTenantService.obterOuCriarEscolaPadrao().getId();
    }

    private ProfessorResponse toProfessorResponse(ProfessorResumo resumo) {
        return new ProfessorResponse(
                resumo.id(),
                resumo.pessoaId(),
                resumo.nomeCompleto(),
                resumo.escolaId(),
                resumo.escolaNome(),
                resumo.registroProfissional(),
                resumo.formacao(),
                resumo.ativo(),
                resumo.createdAt(),
                resumo.updatedAt());
    }

    private ProfessorAlocacaoResponse toAlocacaoResponse(ProfessorAlocacaoResumo resumo) {
        return new ProfessorAlocacaoResponse(
                resumo.id(),
                resumo.professorId(),
                resumo.professorNome(),
                resumo.turmaDisciplinaId(),
                resumo.turmaId(),
                resumo.turmaNome(),
                resumo.disciplinaId(),
                resumo.disciplinaNome(),
                resumo.dataInicio(),
                resumo.dataFim(),
                resumo.ativo(),
                resumo.createdAt());
    }
}
