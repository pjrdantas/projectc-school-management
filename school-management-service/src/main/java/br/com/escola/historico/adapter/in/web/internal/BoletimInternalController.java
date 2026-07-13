package br.com.escola.historico.adapter.in.web.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.historico.adapter.in.web.dto.BoletimResponse;
import br.com.escola.historico.application.service.BoletimService;

@RestController
@RequestMapping("/internal/boletins")
public class BoletimInternalController {

    private final BoletimService boletimService;

    public BoletimInternalController(BoletimService boletimService) {
        this.boletimService = boletimService;
    }

    @GetMapping("/matriculas/{matriculaId}")
    public BoletimResponse consultarPorMatricula(@PathVariable @NonNull UUID matriculaId) {
        return boletimService.consultarPorMatricula(matriculaId);
    }

    @GetMapping("/matriculas/{matriculaId}/fechamentos")
    public List<BoletimResponse> listarFechamentos(@PathVariable @NonNull UUID matriculaId) {
        return boletimService.listarFechamentos(matriculaId);
    }
}
