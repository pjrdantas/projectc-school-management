package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PessoaDocumentoMetadataResponse;
import br.com.escola.peopleservice.application.port.out.PessoaDocumentoMetadataPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PessoaDocumentoMetadataService {

    private final ObjectProvider<PessoaDocumentoMetadataPort> documentoPortProvider;
    private final PeopleReadSourcePolicy readRoutingPolicy;
    private final MeterRegistry meterRegistry;

    public PessoaDocumentoMetadataService(
            ObjectProvider<PessoaDocumentoMetadataPort> documentoPortProvider,
            PeopleReadSourcePolicy readRoutingPolicy,
            MeterRegistry meterRegistry) {
        this.documentoPortProvider = documentoPortProvider;
        this.readRoutingPolicy = readRoutingPolicy;
        this.meterRegistry = meterRegistry;
    }

    public Optional<PessoaDocumentoMetadataResponse> buscarDocumentoPorId(UUID documentoId, UUID escolaId) {
        var decision = readRoutingPolicy.registrarDecisaoLeituraDocumentoMetadata();
        if (!decision.localReadEligible()) {
            registrarLeituraDocumentoLocal("buscarDocumentoPorId", "fallback_guard_blocked");
            return Optional.empty();
        }
        PessoaDocumentoMetadataPort port = documentoPortProvider.getIfAvailable();
        if (port == null) {
            registrarLeituraDocumentoLocal("buscarDocumentoPorId", "fallback_adapter_missing");
            return Optional.empty();
        }
        try {
            Optional<PessoaDocumentoMetadataResponse> response = port.buscarDocumentoPorId(documentoId, escolaId);
            registrarLeituraDocumentoLocal(
                    "buscarDocumentoPorId",
                    response.isPresent() ? "success" : "fallback_not_found");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraDocumentoLocal("buscarDocumentoPorId", "fallback_error");
            return Optional.empty();
        }
    }

    public List<PessoaDocumentoMetadataResponse> listarDocumentosPorPessoa(UUID pessoaId, UUID escolaId) {
        var decision = readRoutingPolicy.registrarDecisaoLeituraDocumentoMetadata();
        if (!decision.localReadEligible()) {
            registrarLeituraDocumentoLocal("listarDocumentosPorPessoa", "fallback_guard_blocked");
            return List.of();
        }
        PessoaDocumentoMetadataPort port = documentoPortProvider.getIfAvailable();
        if (port == null) {
            registrarLeituraDocumentoLocal("listarDocumentosPorPessoa", "fallback_adapter_missing");
            return List.of();
        }
        try {
            List<PessoaDocumentoMetadataResponse> response = port.listarDocumentosPorPessoa(pessoaId, escolaId);
            registrarLeituraDocumentoLocal("listarDocumentosPorPessoa", "success");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraDocumentoLocal("listarDocumentosPorPessoa", "fallback_error");
            return List.of();
        }
    }

    private void registrarLeituraDocumentoLocal(String operation, String result) {
        meterRegistry.counter(
                "people.document.reads",
                "operation", operation,
                "result", result)
                .increment();
    }
}

