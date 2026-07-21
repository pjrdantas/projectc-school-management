package br.com.escola.peopleservice.application.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.infra.config.PeopleRuntimeProperties;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PeopleDataAccessPolicy {

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

    private final PeopleRuntimeProperties properties;
    private final MeterRegistry meterRegistry;

    public PeopleDataAccessPolicy(PeopleRuntimeProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    public PeopleDataAccessDecision registrarDecisao(String operation) {
        return registrar(avaliar(operation));
    }

    public PeopleDataAccessDecision registrarDecisaoLeituraEndereco() {
        return registrar(avaliar("endereco"));
    }

    public PeopleDataAccessDecision registrarDecisaoLeituraContato() {
        return registrar(avaliar("contato"));
    }

    public PeopleDataAccessDecision registrarDecisaoLeituraDocumentoMetadata() {
        return registrar(avaliar("documentoMetadata"));
    }

    public PeopleDataAccessDecision registrarDecisaoLeituraAlunoVinculo() {
        return registrar(avaliar("alunoVinculo"));
    }

    public PeopleDataAccessDecision registrarDecisaoLeituraResponsavelVinculo() {
        return registrar(avaliar("responsavelVinculo"));
    }

    public PeopleDataAccessDecision registrarDecisaoLeituraFuncionarioResumo() {
        return registrar(avaliar("funcionarioResumo"));
    }

    public PeopleDataAccessDecision registrarDecisaoLeituraProfessorResumo() {
        return registrar(avaliar("professorResumo"));
    }

    public PeopleDataAccessDecision avaliar(String operation) {
        String source = SOURCES.getOrDefault(operation, "people_read_model");
        boolean enabled = properties.enabled() && properties.localReadRoutingEnabled();
        return new PeopleDataAccessDecision(
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

    public PeopleDataAccessDecision avaliarLeituraEndereco() {
        return avaliar("endereco");
    }

    public PeopleDataAccessDecision avaliarLeituraContato() {
        return avaliar("contato");
    }

    public PeopleDataAccessDecision avaliarLeituraDocumentoMetadata() {
        return avaliar("documentoMetadata");
    }

    public PeopleDataAccessDecision avaliarLeituraAlunoVinculo() {
        return avaliar("alunoVinculo");
    }

    public PeopleDataAccessDecision avaliarLeituraResponsavelVinculo() {
        return avaliar("responsavelVinculo");
    }

    public PeopleDataAccessDecision avaliarLeituraFuncionarioResumo() {
        return avaliar("funcionarioResumo");
    }

    public PeopleDataAccessDecision avaliarLeituraProfessorResumo() {
        return avaliar("professorResumo");
    }

    public Map<String, PeopleDataAccessDecision> avaliarTodas() {
        Map<String, PeopleDataAccessDecision> decisions = new LinkedHashMap<>();
        SOURCES.keySet().stream().sorted().forEach(operation -> decisions.put(operation, avaliar(operation)));
        return decisions;
    }

    private PeopleDataAccessDecision registrar(PeopleDataAccessDecision decision) {
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
