package br.com.escola.pedagogicalservice.interfaces.rest;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.pedagogicalservice.application.context.InternalHeaders;
import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.BoletimResponse;
import br.com.escola.pedagogicalservice.application.port.in.BoletimQueryUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class PedagogicalInternalController {

    private final BoletimQueryUseCase boletimQueryUseCase;

    public PedagogicalInternalController(BoletimQueryUseCase boletimQueryUseCase) {
        this.boletimQueryUseCase = boletimQueryUseCase;
    }

    @GetMapping("/matriculas/{matriculaId}/boletim")
    public BoletimResponse consultarBoletimPorMatricula(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID matriculaId) {
        return boletimQueryUseCase.consultarBoletimPorMatricula(authorization, context, matriculaId);
    }
}
