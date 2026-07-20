package br.com.escola.institutionaltenantservice.interfaces.rest;

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

import br.com.escola.institutionaltenantservice.application.dto.EscolaRequest;
import br.com.escola.institutionaltenantservice.application.dto.EscolaResponse;
import br.com.escola.institutionaltenantservice.application.port.in.AdministrarEscolaUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/v1/escolas")
public class AdministracaoEscolaController {

    private final AdministrarEscolaUseCase administrarEscolaUseCase;

    public AdministracaoEscolaController(AdministrarEscolaUseCase administrarEscolaUseCase) {
        this.administrarEscolaUseCase = administrarEscolaUseCase;
    }

    @GetMapping
    public List<EscolaResponse> listar() {
        return administrarEscolaUseCase.listar().stream().map(EscolaResponse::from).toList();
    }

    @GetMapping("/{id}")
    public EscolaResponse buscar(@PathVariable UUID id) {
        return EscolaResponse.from(administrarEscolaUseCase.buscar(id));
    }

    @PostMapping
    public ResponseEntity<EscolaResponse> criar(@Valid @RequestBody EscolaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EscolaResponse.from(administrarEscolaUseCase.criar(request)));
    }

    @PutMapping("/{id}")
    public EscolaResponse atualizar(@PathVariable UUID id, @Valid @RequestBody EscolaRequest request) {
        return EscolaResponse.from(administrarEscolaUseCase.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        administrarEscolaUseCase.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
