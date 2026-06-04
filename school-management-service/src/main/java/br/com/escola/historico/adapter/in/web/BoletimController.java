package br.com.escola.historico.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.historico.adapter.in.web.dto.BoletimFechamentoRequest;
import br.com.escola.historico.adapter.in.web.dto.BoletimResponse;
import br.com.escola.historico.application.service.BoletimService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/matriculas/{matriculaId}/boletim")
@Validated
public class BoletimController {

    private final BoletimService boletimService;

    public BoletimController(BoletimService boletimService) {
        this.boletimService = boletimService;
    }

    @GetMapping
    @Operation(summary = "Consulta boletim da matrícula")
    public BoletimResponse consultar(@PathVariable @NonNull UUID matriculaId) {
        return boletimService.consultarPorMatricula(matriculaId);
    }

    @PostMapping("/fechamento")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Fecha e persiste boletim da matrícula")
    public BoletimResponse fechar(
            @PathVariable @NonNull UUID matriculaId,
            @Valid @RequestBody BoletimFechamentoRequest request) {
        return boletimService.fecharBoletim(matriculaId, request);
    }

    @GetMapping("/fechamentos")
    @Operation(summary = "Lista boletins fechados da matrícula")
    public List<BoletimResponse> listarFechamentos(@PathVariable @NonNull UUID matriculaId) {
        return boletimService.listarFechamentos(matriculaId);
    }
}
