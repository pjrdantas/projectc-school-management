package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PessoaCatalogoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaCatalogoPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PessoaAlunoResponsavelCatalogoService {

    private final ObjectProvider<PessoaCatalogoPort> catalogoPortProvider;
    private final DataAccessPolicy readRoutingPolicy;
    private final MeterRegistry meterRegistry;

    public PessoaAlunoResponsavelCatalogoService(
            ObjectProvider<PessoaCatalogoPort> catalogoPortProvider,
            DataAccessPolicy readRoutingPolicy,
            MeterRegistry meterRegistry) {
        this.catalogoPortProvider = catalogoPortProvider;
        this.readRoutingPolicy = readRoutingPolicy;
        this.meterRegistry = meterRegistry;
    }

    public List<PessoaCatalogoResponse> listarStatusAluno() {
        return listarCatalogo("listarStatusAluno");
    }

    public List<PessoaCatalogoResponse> listarParentescos() {
        return listarCatalogo("listarParentescos");
    }

    private List<PessoaCatalogoResponse> listarCatalogo(String operation) {
        var decision = readRoutingPolicy.registrarDecisao(operation);
        if (!decision.localReadEligible()) {
            registrarLeituraCatalogo(operation, "fallback_guard_blocked");
            return List.of();
        }

        PessoaCatalogoPort port = catalogoPortProvider.getIfAvailable();
        if (port == null) {
            registrarLeituraCatalogo(operation, "fallback_adapter_missing");
            return List.of();
        }

        try {
            List<PessoaCatalogoResponse> response = switch (operation) {
                case "listarStatusAluno" -> port.listarStatusAluno();
                case "listarParentescos" -> port.listarParentescos();
                default -> List.of();
            };
            registrarLeituraCatalogo(operation, "success");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraCatalogo(operation, "fallback_error");
            return List.of();
        }
    }

    private void registrarLeituraCatalogo(String operation, String result) {
        meterRegistry.counter(
                "people.catalog.reads",
                "operation", operation,
                "result", result)
                .increment();
    }
}

