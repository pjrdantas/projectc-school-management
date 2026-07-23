package br.com.escola.responsiblesservice.interfaces.rest;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import br.com.escola.responsiblesservice.application.context.InternalHeaders;
import br.com.escola.responsiblesservice.application.context.InternalRequestContext;
import br.com.escola.responsiblesservice.application.dto.CadastrarResponsavelCommand;
import br.com.escola.responsiblesservice.application.dto.AtualizarResponsavelCommand;
import br.com.escola.responsiblesservice.application.dto.VincularAlunoResponsavelCommand;
import br.com.escola.responsiblesservice.application.port.in.AlunoResponsavelWriteUseCase;
import br.com.escola.responsiblesservice.application.port.in.ResponsavelQueryUseCase;
import br.com.escola.responsiblesservice.application.port.in.ResponsavelWriteUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class ResponsavelInternalController {

    private final ResponsavelQueryUseCase responsavelQueryUseCase;
    private final ResponsavelWriteUseCase responsavelWriteUseCase;
    private final AlunoResponsavelWriteUseCase alunoResponsavelWriteUseCase;

    public ResponsavelInternalController(
            ResponsavelQueryUseCase responsavelQueryUseCase,
            ResponsavelWriteUseCase responsavelWriteUseCase,
            AlunoResponsavelWriteUseCase alunoResponsavelWriteUseCase) {
        this.responsavelQueryUseCase = responsavelQueryUseCase;
        this.responsavelWriteUseCase = responsavelWriteUseCase;
        this.alunoResponsavelWriteUseCase = alunoResponsavelWriteUseCase;
    }

    @PostMapping("/responsaveis")
    public ResponseEntity<?> criarResponsavel(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestBody CadastrarResponsavelCommand command) {
        return ResponseEntity.status(201).body(responsavelWriteUseCase.criar(command, context));
    }

    @PutMapping("/responsaveis/{id}")
    public ResponseEntity<?> atualizarResponsavel(
            @PathVariable @NonNull UUID id,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestBody AtualizarResponsavelCommand command) {
        return ResponseEntity.ok(responsavelWriteUseCase.atualizar(id, command, context));
    }

    @DeleteMapping("/responsaveis/{id}")
    public ResponseEntity<Void> excluirResponsavel(
            @PathVariable @NonNull UUID id,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        responsavelWriteUseCase.excluir(id, context);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/alunos/{alunoId}/responsaveis")
    public ResponseEntity<Void> vincularAlunoResponsavel(
            @PathVariable @NonNull UUID alunoId,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestBody VincularAlunoResponsavelCommand command) {
        alunoResponsavelWriteUseCase.vincular(alunoId, command, context);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/alunos/{alunoId}/responsaveis/{responsavelId}")
    public ResponseEntity<Void> desvincularAlunoResponsavel(
            @PathVariable @NonNull UUID alunoId,
            @PathVariable @NonNull UUID responsavelId,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        alunoResponsavelWriteUseCase.desvincular(alunoId, responsavelId, context);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/responsaveis")
    public ResponseEntity<String> listarResponsaveis(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf) {
        return responsavelQueryUseCase.listarResponsaveis(authorization, context, nome, cpf);
    }

    @GetMapping("/responsaveis/{id}")
    public ResponseEntity<String> buscarResponsavelPorId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return responsavelQueryUseCase.buscarResponsavelPorId(authorization, context, id);
    }

    @GetMapping("/alunos/{alunoId}/responsaveis")
    public ResponseEntity<String> listarResponsaveisPorAluno(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID alunoId) {
        return responsavelQueryUseCase.listarResponsaveisPorAluno(authorization, context, alunoId);
    }
}
