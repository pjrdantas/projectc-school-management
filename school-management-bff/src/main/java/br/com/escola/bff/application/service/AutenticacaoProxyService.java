package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.port.out.SessaoAutenticadaPort;
import br.com.escola.bff.application.usecase.GerenciarAutenticacaoUseCase;
import reactor.core.publisher.Mono;

public class AutenticacaoProxyService implements GerenciarAutenticacaoUseCase {

    private final SessaoAutenticadaPort sessaoAutenticadaPort;

    public AutenticacaoProxyService(SessaoAutenticadaPort sessaoAutenticadaPort) {
        this.sessaoAutenticadaPort = sessaoAutenticadaPort;
    }

    @Override
    public Mono<ResponseEntity<String>> login(String requestBody, String correlationId) {
        return sessaoAutenticadaPort.login(requestBody, correlationId);
    }

    @Override
    public Mono<ResponseEntity<String>> refresh(String requestBody, String correlationId) {
        return sessaoAutenticadaPort.refresh(requestBody, correlationId);
    }

    @Override
    public Mono<ResponseEntity<String>> logout(String requestBody, String correlationId) {
        return sessaoAutenticadaPort.logout(requestBody, correlationId);
    }
}
