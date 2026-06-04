package br.com.escola.matricula.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.matricula.application.service.MatriculaDocumentoExigidoAdminService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/matriculas/catalogos/documentos-exigidos")
public class MatriculaDocumentoExigidoAdminController {

    private final MatriculaDocumentoExigidoAdminService service;

    public MatriculaDocumentoExigidoAdminController(MatriculaDocumentoExigidoAdminService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista documentos exigidos por tipo de matrícula")
    public List<MatriculaDocumentoExigidoResponse> listar(
            @RequestParam @NonNull UUID tipoMatriculaId) {
        return service.listarPorTipoMatricula(tipoMatriculaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Configura documento exigido por tipo de matrícula")
    public MatriculaDocumentoExigidoResponse criar(@Valid @RequestBody MatriculaDocumentoExigidoRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza documento exigido por tipo de matrícula")
    public MatriculaDocumentoExigidoResponse atualizar(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody MatriculaDocumentoExigidoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove documento exigido por tipo de matrícula")
    public void excluir(@PathVariable @NonNull UUID id) {
        service.excluir(id);
    }
}
