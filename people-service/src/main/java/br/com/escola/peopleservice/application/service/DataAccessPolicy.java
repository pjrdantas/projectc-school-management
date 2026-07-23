package br.com.escola.peopleservice.application.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.infra.config.RuntimeProperties;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class DataAccessPolicy {

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

    private final RuntimeProperties properties;
    private final MeterRegistry meterRegistry;

    public DataAccessPolicy(RuntimeProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    public DataAccessDecision registrarDecisao(String operation) {
        return registrar(avaliar(operation));
    }

    public DataAccessDecision registrarDecisaoLeituraEndereco() {
        return registrar(avaliar("endereco"));
    }

    public DataAccessDecision registrarDecisaoLeituraContato() {
        return registrar(avaliar("contato"));
    }

    public DataAccessDecision registrarDecisaoLeituraDocumentoMetadata() {
        return registrar(avaliar("documentoMetadata"));
    }

    public DataAccessDecision registrarDecisaoLeituraAlunoVinculo() {
        return registrar(avaliar("alunoVinculo"));
    }

    public DataAccessDecision registrarDecisaoLeituraResponsavelVinculo() {
        return registrar(avaliar("responsavelVinculo"));
    }

    public DataAccessDecision registrarDecisaoLeituraFuncionarioResumo() {
        return registrar(avaliar("funcionarioResumo"));
    }

    public DataAccessDecision registrarDecisaoLeituraProfessorResumo() {
        return registrar(avaliar("professorResumo"));
    }

    public DataAccessDecision avaliar(String operation) {
        String source = SOURCES.getOrDefault(operation, "people_read_model");
        boolean enabled = properties.enabled() && properties.localReadRoutingEnabled();
        return new DataAccessDecision(
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

    public DataAccessDecision avaliarLeituraEndereco() {
        return avaliar("endereco");
    }

    public DataAccessDecision avaliarLeituraContato() {
        return avaliar("contato");
    }

    public DataAccessDecision avaliarLeituraDocumentoMetadata() {
        return avaliar("documentoMetadata");
    }

    public DataAccessDecision avaliarLeituraAlunoVinculo() {
        return avaliar("alunoVinculo");
    }

    public DataAccessDecision avaliarLeituraResponsavelVinculo() {
        return avaliar("responsavelVinculo");
    }

    public DataAccessDecision avaliarLeituraFuncionarioResumo() {
        return avaliar("funcionarioResumo");
    }

    public DataAccessDecision avaliarLeituraProfessorResumo() {
        return avaliar("professorResumo");
    }

    public Map<String, DataAccessDecision> avaliarTodas() {
        Map<String, DataAccessDecision> decisions = new LinkedHashMap<>();
        SOURCES.keySet().stream().sorted().forEach(operation -> decisions.put(operation, avaliar(operation)));
        return decisions;
    }

    private DataAccessDecision registrar(DataAccessDecision decision) {
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
