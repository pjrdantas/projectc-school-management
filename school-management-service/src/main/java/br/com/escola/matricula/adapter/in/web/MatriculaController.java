package br.com.escola.matricula.adapter.in.web;

import java.util.UUID;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.matricula.application.dto.MatriculaFiltro;
import br.com.escola.matricula.application.dto.MatriculaInput;
import br.com.escola.matricula.application.dto.MatriculaOutput;
import br.com.escola.matricula.application.service.MatriculaFluxoService;
import br.com.escola.matricula.application.usecase.AtualizarStatusMatriculaUseCase;
import br.com.escola.matricula.application.usecase.ConsultarMatriculasUseCase;
import br.com.escola.matricula.application.usecase.CriarMatriculaUseCase;
import br.com.escola.matricula.application.usecase.ExcluirMatriculaUseCase;
import br.com.escola.matricula.application.dto.MatriculaEtapaOutput;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    private final CriarMatriculaUseCase criarMatriculaUseCase;
    private final ConsultarMatriculasUseCase consultarMatriculasUseCase;
    private final AtualizarStatusMatriculaUseCase atualizarStatusMatriculaUseCase;
    private final ExcluirMatriculaUseCase excluirMatriculaUseCase;
    private final MatriculaFluxoService matriculaFluxoService;

    public MatriculaController(
            CriarMatriculaUseCase criarMatriculaUseCase,
            ConsultarMatriculasUseCase consultarMatriculasUseCase,
            AtualizarStatusMatriculaUseCase atualizarStatusMatriculaUseCase,
            ExcluirMatriculaUseCase excluirMatriculaUseCase,
            MatriculaFluxoService matriculaFluxoService) {
        this.criarMatriculaUseCase = criarMatriculaUseCase;
        this.consultarMatriculasUseCase = consultarMatriculasUseCase;
        this.atualizarStatusMatriculaUseCase = atualizarStatusMatriculaUseCase;
        this.excluirMatriculaUseCase = excluirMatriculaUseCase;
        this.matriculaFluxoService = matriculaFluxoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma matrícula")
    public MatriculaResponse criar(@Valid @RequestBody MatriculaRequest request) {
        MatriculaOutput output = criarMatriculaUseCase.executar(
                new MatriculaInput(
                        request.alunoId(),
                        request.turmaId(),
                        request.periodoLetivoId(),
                        request.tipoMatricula(),
                        request.observacao()));
        return toResponse(output);
    }

    @GetMapping
    @Operation(summary = "Consulta matrículas")
    public List<MatriculaResponse> consultar(
            @RequestParam(required = false) UUID alunoId,
            @RequestParam(required = false) UUID turmaId,
            @RequestParam(required = false) UUID periodoLetivoId,
            @RequestParam(required = false) String status) {
        MatriculaFiltro filtro = new MatriculaFiltro(alunoId, turmaId, periodoLetivoId, status);
        return consultarMatriculasUseCase.executar(filtro).stream().map(this::toResponse).toList();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualiza status da matrícula")
    public MatriculaResponse atualizarStatus(
            @PathVariable UUID id,
            @Valid @RequestBody MatriculaStatusRequest request) {
        return toResponse(atualizarStatusMatriculaUseCase.executar(id, request.status(), request.justificativa()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui uma matrícula")
    public void excluir(@PathVariable UUID id) {
        excluirMatriculaUseCase.executar(id);
    }

    @GetMapping("/{id}/etapas")
    @Operation(summary = "Lista etapas da matrícula")
    public List<MatriculaEtapaOutput> listarEtapas(@PathVariable UUID id) {
        return matriculaFluxoService.listarEtapas(id);
    }

    @PatchMapping("/{id}/etapas/{etapaId}/status")
    @Operation(summary = "Atualiza status de uma etapa da matrícula")
    public MatriculaEtapaOutput atualizarStatusEtapa(
            @PathVariable UUID id,
            @PathVariable UUID etapaId,
            @Valid @RequestBody MatriculaEtapaStatusRequest request) {
        return matriculaFluxoService.atualizarStatusEtapa(id, etapaId, request);
    }

    @GetMapping("/{id}/documentos-entregues")
    @Operation(summary = "Lista documentos entregues da matrícula")
    public List<MatriculaDocumentoEntregueResponse> listarDocumentosEntregues(@PathVariable UUID id) {
        return matriculaFluxoService.listarDocumentosEntregues(id);
    }

    @GetMapping("/{id}/documentos-exigidos")
    @Operation(summary = "Lista documentos exigidos da matrícula")
    public List<MatriculaDocumentoExigidoResponse> listarDocumentosExigidos(@PathVariable UUID id) {
        return matriculaFluxoService.listarDocumentosExigidos(id);
    }

    @PostMapping("/{id}/documentos-entregues")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra documento entregue da matrícula")
    public MatriculaDocumentoEntregueResponse registrarDocumentoEntregue(
            @PathVariable UUID id,
            @Valid @RequestBody MatriculaDocumentoEntregueRequest request) {
        return matriculaFluxoService.registrarDocumentoEntregue(id, request);
    }

    private MatriculaResponse toResponse(MatriculaOutput output) {
        return new MatriculaResponse(
                output.id(),
                output.alunoId(),
                output.turmaId(),
                output.serieId(),
                output.serieNome(),
                output.periodoLetivoId(),
                output.status(),
                output.tipoMatricula(),
                output.dataMatricula(),
                output.observacao(),
                output.createdAt(),
                output.etapas());
    }
}
