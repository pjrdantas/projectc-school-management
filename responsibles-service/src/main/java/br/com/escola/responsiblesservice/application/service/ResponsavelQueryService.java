package br.com.escola.responsiblesservice.application.service;

import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import br.com.escola.responsiblesservice.application.context.InternalRequestContext;
import br.com.escola.responsiblesservice.application.dto.ResponsavelReadModelResponse;
import br.com.escola.responsiblesservice.application.port.in.ResponsavelQueryUseCase;
import br.com.escola.responsiblesservice.application.port.out.ResponsavelLocalReadPort;
import br.com.escola.responsiblesservice.application.port.out.ResponsavelReadPort;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesReadModelProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ResponsavelQueryService implements ResponsavelQueryUseCase {

    private final ObjectProvider<ResponsavelLocalReadPort> responsavelLocalReadPortProvider;
    private final ResponsavelReadPort responsavelReadPort;
    private final ResponsiblesReadModelProperties readModelProperties;
    private final ObjectMapper objectMapper;

    public ResponsavelQueryService(
            ObjectProvider<ResponsavelLocalReadPort> responsavelLocalReadPortProvider,
            ResponsavelReadPort responsavelReadPort,
            ResponsiblesReadModelProperties readModelProperties,
            ObjectMapper objectMapper) {
        this.responsavelLocalReadPortProvider = responsavelLocalReadPortProvider;
        this.responsavelReadPort = responsavelReadPort;
        this.readModelProperties = readModelProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseEntity<String> listarResponsaveis(
            String authorization,
            InternalRequestContext context,
            String nome,
            String cpf) {
        if (readModelProperties.enabled() && readModelProperties.localReadRoutingEnabled()) {
            ResponsavelLocalReadPort localReadPort = responsavelLocalReadPortProvider.getIfAvailable();
            if (localReadPort != null) {
                try {
                    var localResponse = localReadPort.listarResponsaveis(context.escolaId(), nome, cpf);
                    if (localResponse.isPresent()) {
                        return json(localResponse.get());
                    }
                } catch (RuntimeException exception) {
                    if (!readModelProperties.fallbackEnabled()) {
                        throw exception;
                    }
                }
            }
        }
        return responsavelReadPort.listarResponsaveis(authorization, context, nome, cpf);
    }

    @Override
    public ResponseEntity<String> buscarResponsavelPorId(
            String authorization,
            InternalRequestContext context,
            UUID responsavelId) {
        if (readModelProperties.enabled() && readModelProperties.localReadRoutingEnabled()) {
            ResponsavelLocalReadPort localReadPort = responsavelLocalReadPortProvider.getIfAvailable();
            if (localReadPort != null) {
                try {
                    var localResponse = localReadPort.buscarResponsavelPorId(responsavelId, context.escolaId());
                    if (localResponse.isPresent()) {
                        return json(localResponse.get());
                    }
                } catch (RuntimeException exception) {
                    if (!readModelProperties.fallbackEnabled()) {
                        throw exception;
                    }
                }
            }
        }
        return responsavelReadPort.buscarResponsavelPorId(authorization, context, responsavelId);
    }

    @Override
    public ResponseEntity<String> listarResponsaveisPorAluno(
            String authorization,
            InternalRequestContext context,
            UUID alunoId) {
        if (readModelProperties.enabled() && readModelProperties.localReadRoutingEnabled()) {
            ResponsavelLocalReadPort localReadPort = responsavelLocalReadPortProvider.getIfAvailable();
            if (localReadPort != null) {
                try {
                    var localResponse = localReadPort.listarResponsaveisPorAluno(alunoId, context.escolaId());
                    if (localResponse.isPresent()) {
                        return json(localResponse.get());
                    }
                } catch (RuntimeException exception) {
                    if (!readModelProperties.fallbackEnabled()) {
                        throw exception;
                    }
                }
            }
        }
        return responsavelReadPort.listarResponsaveisPorAluno(authorization, context, alunoId);
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
