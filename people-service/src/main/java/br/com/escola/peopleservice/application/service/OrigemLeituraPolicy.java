package br.com.escola.peopleservice.application.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.infra.config.LeituraModeloProperties;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class OrigemLeituraPolicy {

    private static final Map<String, String> SOURCES = Map.ofEntries(
            Map.entry("listarTiposPessoa", "tipo_pessoa"),
            Map.entry("listarTiposEndereco", "tipo_endereco"),
            Map.entry("listarStatusAluno", "status_aluno"),
            Map.entry("listarParentescos", "parentesco"),
            Map.entry("buscarPorId", "people_read_model_identity"),
            Map.entry("consultarCadastro", "people_read_model_student_responsible"),
            Map.entry("listarResponsaveisPorAluno", "people_read_model_student_responsible"),
            Map.entry("endereco", "people_read_model_address"),
            Map.entry("contato", "people_read_model_identity"),
            Map.entry("documentoMetadata", "people_documento_read_model"),
            Map.entry("alunoVinculo", "people_read_model_student_responsible"),
            Map.entry("responsavelVinculo", "people_read_model_student_responsible"),
            Map.entry("funcionarioResumo", "people_funcionario_read_model"),
            Map.entry("professorResumo", "people_professor_read_model"));

    private final LeituraModeloProperties properties;
    private final MeterRegistry meterRegistry;

    public OrigemLeituraPolicy(LeituraModeloProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    public OrigemLeituraDecision registrarDecisao(String operation) {
        return registrar(avaliar(operation));
    }

    public OrigemLeituraDecision registrarDecisaoLeituraEndereco() {
        return registrar(avaliar("endereco"));
    }

    public OrigemLeituraDecision registrarDecisaoLeituraContato() {
        return registrar(avaliar("contato"));
    }

    public OrigemLeituraDecision registrarDecisaoLeituraDocumentoMetadata() {
        return registrar(avaliar("documentoMetadata"));
    }

    public OrigemLeituraDecision registrarDecisaoLeituraAlunoVinculo() {
        return registrar(avaliar("alunoVinculo"));
    }

    public OrigemLeituraDecision registrarDecisaoLeituraResponsavelVinculo() {
        return registrar(avaliar("responsavelVinculo"));
    }

    public OrigemLeituraDecision registrarDecisaoLeituraFuncionarioResumo() {
        return registrar(avaliar("funcionarioResumo"));
    }

    public OrigemLeituraDecision registrarDecisaoLeituraProfessorResumo() {
        return registrar(avaliar("professorResumo"));
    }

    public OrigemLeituraDecision avaliar(String operation) {
        String source = SOURCES.getOrDefault(operation, "people_read_model");
        boolean enabled = properties.enabled() && properties.localReadRoutingEnabled();
        return new OrigemLeituraDecision(
                operation,
                route(operation),
                source,
                source,
                properties.localReadRoutingEnabled(),
                enabled,
                false,
                false,
                enabled ? "local-read-required" : "local-read-disabled");
    }

    public OrigemLeituraDecision avaliarLeituraEndereco() {
        return avaliar("endereco");
    }

    public OrigemLeituraDecision avaliarLeituraContato() {
        return avaliar("contato");
    }

    public OrigemLeituraDecision avaliarLeituraDocumentoMetadata() {
        return avaliar("documentoMetadata");
    }

    public OrigemLeituraDecision avaliarLeituraAlunoVinculo() {
        return avaliar("alunoVinculo");
    }

    public OrigemLeituraDecision avaliarLeituraResponsavelVinculo() {
        return avaliar("responsavelVinculo");
    }

    public OrigemLeituraDecision avaliarLeituraFuncionarioResumo() {
        return avaliar("funcionarioResumo");
    }

    public OrigemLeituraDecision avaliarLeituraProfessorResumo() {
        return avaliar("professorResumo");
    }

    public Map<String, OrigemLeituraDecision> avaliarTodas() {
        Map<String, OrigemLeituraDecision> decisions = new LinkedHashMap<>();
        SOURCES.keySet().stream().sorted().forEach(operation -> decisions.put(operation, avaliar(operation)));
        return decisions;
    }

    private OrigemLeituraDecision registrar(OrigemLeituraDecision decision) {
        meterRegistry.counter(
                "people.read.routing.decisions",
                "operation", decision.operation(),
                "selectedSource", decision.selectedSource(),
                "eligible", Boolean.toString(decision.localReadEligible()))
                .increment();
        return decision;
    }

    private String route(String operation) {
        return switch (operation) {
            case "listarTiposPessoa" -> "GET /internal/v1/pessoas/catalogos/tipos-pessoa";
            case "listarTiposEndereco" -> "GET /internal/v1/pessoas/catalogos/tipos-endereco";
            case "buscarPorId" -> "GET /internal/v1/pessoas/{id}";
            case "consultarCadastro" -> "GET /internal/v1/pessoas/consulta-cadastral";
            case "listarResponsaveisPorAluno" -> "GET /internal/v1/alunos/{id}/responsaveis";
            default -> "internal-operation:" + operation;
        };
    }
}
