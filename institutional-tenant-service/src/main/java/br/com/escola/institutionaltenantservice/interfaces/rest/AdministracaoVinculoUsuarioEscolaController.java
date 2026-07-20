package br.com.escola.institutionaltenantservice.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.institutionaltenantservice.application.dto.VinculoUsuarioEscolaRequest;
import br.com.escola.institutionaltenantservice.application.dto.VinculoUsuarioEscolaResponse;
import br.com.escola.institutionaltenantservice.application.model.ResultadoVinculoUsuarioEscola;
import br.com.escola.institutionaltenantservice.application.port.in.AdministrarVinculoUsuarioEscolaUseCase;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/v1/vinculos-usuario-escola")
public class AdministracaoVinculoUsuarioEscolaController {

    private final AdministrarVinculoUsuarioEscolaUseCase administrarVinculoUsuarioEscolaUseCase;

    public AdministracaoVinculoUsuarioEscolaController(
            AdministrarVinculoUsuarioEscolaUseCase administrarVinculoUsuarioEscolaUseCase) {
        this.administrarVinculoUsuarioEscolaUseCase = administrarVinculoUsuarioEscolaUseCase;
    }

    @GetMapping
    public List<VinculoUsuarioEscolaResponse> listar(
            @RequestParam(required = false) UUID usuarioId,
            @RequestParam(required = false) UUID escolaId) {
        return administrarVinculoUsuarioEscolaUseCase.listar(usuarioId, escolaId).stream()
                .map(VinculoUsuarioEscolaResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public VinculoUsuarioEscolaResponse buscar(@PathVariable UUID id) {
        return VinculoUsuarioEscolaResponse.from(administrarVinculoUsuarioEscolaUseCase.buscar(id));
    }

    @PostMapping
    public ResponseEntity<VinculoUsuarioEscolaResponse> vincular(
            @Valid @RequestBody VinculoUsuarioEscolaRequest request) {
        ResultadoVinculoUsuarioEscola result = administrarVinculoUsuarioEscolaUseCase.vincular(request);
        HttpStatus status = result.criado() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(VinculoUsuarioEscolaResponse.from(result.vinculo()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        administrarVinculoUsuarioEscolaUseCase.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
