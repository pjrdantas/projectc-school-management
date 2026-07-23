package br.com.escola.identityaccessservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.identityaccessservice.application.context.InternalRequestContext;
import br.com.escola.identityaccessservice.application.dto.AuthContextResponse;
import br.com.escola.identityaccessservice.application.dto.EscolaSessaoResponse;
import br.com.escola.identityaccessservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.identityaccessservice.application.model.ContextoSessaoAutenticada;
import br.com.escola.identityaccessservice.application.model.EscolaDisponivel;
import br.com.escola.identityaccessservice.application.port.in.SessaoAutenticadaUseCase;
import br.com.escola.identityaccessservice.application.port.out.EscolaSessaoPort;
import br.com.escola.identityaccessservice.application.port.out.SessaoAutenticadaPort;

@Service
public class SessaoAutenticadaService implements SessaoAutenticadaUseCase {

    private final SessaoAutenticadaPort sessaoAutenticadaPort;
    private final EscolaSessaoPort escolaSessaoPort;

    public SessaoAutenticadaService(
            SessaoAutenticadaPort sessaoAutenticadaPort,
            EscolaSessaoPort escolaSessaoPort) {
        this.sessaoAutenticadaPort = sessaoAutenticadaPort;
        this.escolaSessaoPort = escolaSessaoPort;
    }

    @Override
    public AuthContextResponse consultarContextoAtual(
            String authorization,
            InternalRequestContext context) {
        ContextoSessaoAutenticada sessao = consultarSessao(authorization);
        EscolaDisponivel escola = buscarEscolaAtual(authorization, context, sessao);
        return respostaContexto(sessao, escola);
    }

    @Override
    public List<EscolaSessaoResponse> listarEscolasDisponiveis(
            String authorization,
            InternalRequestContext context) {
        ContextoSessaoAutenticada sessao = consultarSessao(authorization);
        return listarEscolas(authorization, context, sessao).stream()
                .map(escola -> new EscolaSessaoResponse(escola.id(), escola.nome(), escola.ativa()))
                .toList();
    }

    @Override
    public AuthContextResponse selecionarEscolaAtiva(
            String authorization,
            InternalRequestContext context,
            UUID escolaId) {
        ContextoSessaoAutenticada sessao = consultarSessao(authorization);
        EscolaDisponivel escola = listarEscolas(authorization, context, sessao).stream()
                .filter(disponivel -> disponivel.id().equals(escolaId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Vinculo do usuario com a escola nao encontrado"));
        sessaoAutenticadaPort.atualizarEscolaAtiva(sessao.sessaoId(), escola.id());
        return respostaContexto(sessao, escola);
    }

    private ContextoSessaoAutenticada consultarSessao(String authorization) {
        return sessaoAutenticadaPort.consultar(extrairBearerToken(authorization));
    }

    private EscolaDisponivel buscarEscolaAtual(
            String authorization,
            InternalRequestContext requestContext,
            ContextoSessaoAutenticada sessao) {
        return listarEscolas(authorization, requestContext, sessao).stream()
                .filter(escola -> escola.id().equals(sessao.escolaId()))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Escola ativa da sessao nao encontrada"));
    }

    private List<EscolaDisponivel> listarEscolas(
            String authorization,
            InternalRequestContext requestContext,
            ContextoSessaoAutenticada sessao) {
        InternalRequestContext trustedContext = new InternalRequestContext(
                requestContext.correlationId(),
                sessao.usuarioId(),
                sessao.escolaId());
        return escolaSessaoPort.listarDisponiveis(authorization, trustedContext);
    }

    private AuthContextResponse respostaContexto(
            ContextoSessaoAutenticada sessao,
            EscolaDisponivel escola) {
        return new AuthContextResponse(
                sessao.usuarioId(),
                escola.id(),
                escola.nome(),
                sessao.username());
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

