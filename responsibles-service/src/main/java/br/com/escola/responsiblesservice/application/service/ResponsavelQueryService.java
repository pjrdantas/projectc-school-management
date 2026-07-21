package br.com.escola.responsiblesservice.application.service;

import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.responsiblesservice.application.context.InternalRequestContext;
import br.com.escola.responsiblesservice.application.port.in.ResponsavelQueryUseCase;
import br.com.escola.responsiblesservice.application.port.out.ResponsavelLocalReadPort;

@Service
public class ResponsavelQueryService implements ResponsavelQueryUseCase {

    private final ObjectProvider<ResponsavelLocalReadPort> responsavelLocalReadPortProvider;
    private final ObjectMapper objectMapper;

    public ResponsavelQueryService(
            ObjectProvider<ResponsavelLocalReadPort> responsavelLocalReadPortProvider,
            ObjectMapper objectMapper) {
        this.responsavelLocalReadPortProvider = responsavelLocalReadPortProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseEntity<String> listarResponsaveis(
            String authorization,
            InternalRequestContext context,
            String nome,
            String cpf) {
        ResponsavelLocalReadPort localReadPort = requireLocalReadPort();
        return json(localReadPort.listarResponsaveis(context.escolaId(), nome, cpf).orElseGet(java.util.List::of));
    }

    @Override
    public ResponseEntity<String> buscarResponsavelPorId(
            String authorization,
            InternalRequestContext context,
            UUID responsavelId) {
        return requireLocalReadPort().buscarResponsavelPorId(responsavelId, context.escolaId())
                .map(this::json)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<String> listarResponsaveisPorAluno(
            String authorization,
            InternalRequestContext context,
            UUID alunoId) {
        return requireLocalReadPort().listarResponsaveisPorAluno(alunoId, context.escolaId())
                .map(this::json)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponsavelLocalReadPort requireLocalReadPort() {
        ResponsavelLocalReadPort port = responsavelLocalReadPortProvider.getIfAvailable();
        if (port == null) {
            throw new IllegalStateException("responsibles-local-read-adapter-required");
        }
        return port;
    }

    private ResponseEntity<String> json(Object body) {
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(body));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("responsibles-local-read-serialization-failed", exception);
        }
    }
}

