package br.com.escola.identityaccessservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.identityaccessservice.application.context.InternalRequestContext;
import br.com.escola.identityaccessservice.application.dto.AuthContextResponse;
import br.com.escola.identityaccessservice.application.dto.EscolaSessaoResponse;
import br.com.escola.identityaccessservice.application.port.in.SessaoAutenticadaUseCase;
import br.com.escola.identityaccessservice.application.port.out.ContextoAutenticadoPort;
import br.com.escola.identityaccessservice.application.port.out.SessaoAutenticadaPort;

@Service
public class SessaoAutenticadaService implements SessaoAutenticadaUseCase {

    private final ContextoAutenticadoPort contextoAutenticadoPort;
    private final SessaoAutenticadaPort identityAccessPort;

    public SessaoAutenticadaService(
            ContextoAutenticadoPort contextoAutenticadoPort,
            SessaoAutenticadaPort identityAccessPort) {
        this.contextoAutenticadoPort = contextoAutenticadoPort;
        this.identityAccessPort = identityAccessPort;
    }

    @Override
    public AuthContextResponse consultarContextoAtual(
            String authorization,
            InternalRequestContext context) {
        return contextoAutenticadoPort.consultarContextoAtual(extrairBearerToken(authorization));
    }

    @Override
    public List<EscolaSessaoResponse> listarEscolasDisponiveis(
            String authorization,
            InternalRequestContext context) {
        return identityAccessPort.listarEscolasDisponiveis(extrairBearerToken(authorization), context);
    }

    @Override
    public AuthContextResponse selecionarEscolaAtiva(
            String authorization,
            InternalRequestContext context,
            UUID escolaId) {
        return identityAccessPort.selecionarEscolaAtiva(extrairBearerToken(authorization), context, escolaId);
    }

    private String extrairBearerToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new IllegalArgumentException("Authorization bearer obrigatorio");
        }
        if (!authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization bearer invalido");
        }
        String token = authorization.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            throw new IllegalArgumentException("Authorization bearer invalido");
        }
        return token;
    }
}

