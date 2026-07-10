package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PessoaDocumentoMetadataLocalReadResponse;
import br.com.escola.peopleservice.application.port.out.PeopleDocumentMetadataLocalReadPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PeopleDocumentMetadataLocalReadService {

    private final ObjectProvider<PeopleDocumentMetadataLocalReadPort> documentLocalReadPortProvider;
    private final MeterRegistry meterRegistry;

    public PeopleDocumentMetadataLocalReadService(
            ObjectProvider<PeopleDocumentMetadataLocalReadPort> documentLocalReadPortProvider,
            MeterRegistry meterRegistry) {
        this.documentLocalReadPortProvider = documentLocalReadPortProvider;
        this.meterRegistry = meterRegistry;
    }

    public Optional<PessoaDocumentoMetadataLocalReadResponse> buscarDocumentoPorId(UUID documentoId, UUID escolaId) {
        PeopleDocumentMetadataLocalReadPort port = documentLocalReadPortProvider.getIfAvailable();
        if (port == null) {
            registrarLeituraDocumentoLocal("buscarDocumentoPorId", "fallback_adapter_missing");
            return Optional.empty();
        }
        try {
            Optional<PessoaDocumentoMetadataLocalReadResponse> response = port.buscarDocumentoPorId(documentoId, escolaId);
            registrarLeituraDocumentoLocal(
                    "buscarDocumentoPorId",
                    response.isPresent() ? "success" : "fallback_not_found");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraDocumentoLocal("buscarDocumentoPorId", "fallback_error");
            return Optional.empty();
        }
    }

    public List<PessoaDocumentoMetadataLocalReadResponse> listarDocumentosPorPessoa(UUID pessoaId, UUID escolaId) {
        PeopleDocumentMetadataLocalReadPort port = documentLocalReadPortProvider.getIfAvailable();
        if (port == null) {
            registrarLeituraDocumentoLocal("listarDocumentosPorPessoa", "fallback_adapter_missing");
            return List.of();
        }
        try {
            List<PessoaDocumentoMetadataLocalReadResponse> response = port.listarDocumentosPorPessoa(pessoaId, escolaId);
            registrarLeituraDocumentoLocal("listarDocumentosPorPessoa", "success");
            return response;
        } catch (RuntimeException ex) {
            registrarLeituraDocumentoLocal("listarDocumentosPorPessoa", "fallback_error");
            return List.of();
        }
    }

    private void registrarLeituraDocumentoLocal(String operation, String result) {
        meterRegistry.counter(
                "people.shadow.local.persistence.document.metadata.reads",
                "operation", operation,
                "result", result)
                .increment();
    }
}
