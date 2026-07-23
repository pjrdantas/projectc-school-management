package br.com.escola.identityaccessservice.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.identityaccessservice.application.dto.PermissaoRequest;
import br.com.escola.identityaccessservice.application.model.PermissaoAdministrada;
import br.com.escola.identityaccessservice.application.port.in.AdministrarPermissaoUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping({ "/internal/v1/permissoes", "/internal/permissoes" })
public class AdministracaoPermissaoController {

    private final AdministrarPermissaoUseCase administrarPermissaoUseCase;

    public AdministracaoPermissaoController(AdministrarPermissaoUseCase administrarPermissaoUseCase) {
        this.administrarPermissaoUseCase = administrarPermissaoUseCase;
    }

    @GetMapping
    public List<PermissaoAdministrada> listar() {
        return administrarPermissaoUseCase.listar();
    }

    @GetMapping("/{id}")
    public PermissaoAdministrada buscar(@PathVariable UUID id) {
        return administrarPermissaoUseCase.buscar(id);
    }

    @PostMapping
    public ResponseEntity<PermissaoAdministrada> criar(@Valid @RequestBody PermissaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(administrarPermissaoUseCase.criar(request));
    }

    @PutMapping("/{id}")
    public PermissaoAdministrada atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody PermissaoRequest request) {
        return administrarPermissaoUseCase.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        administrarPermissaoUseCase.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
