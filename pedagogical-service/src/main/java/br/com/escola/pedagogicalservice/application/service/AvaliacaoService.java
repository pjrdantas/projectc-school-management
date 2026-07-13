package br.com.escola.pedagogicalservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.AvaliacaoResponse;
import br.com.escola.pedagogicalservice.application.port.in.AvaliacaoUseCase;
import br.com.escola.pedagogicalservice.application.port.out.AvaliacaoPort;

@Service
public class AvaliacaoService implements AvaliacaoUseCase {

    private final AvaliacaoPort avaliacaoPort;

    public AvaliacaoService(AvaliacaoPort avaliacaoPort) {
        this.avaliacaoPort = avaliacaoPort;
    }

    @Override
    public AvaliacaoResponse criar(String authorization, InternalRequestContext context, String requestBody) {
        return avaliacaoPort.criar(authorization, context, requestBody);
    }

    @Override
    public List<AvaliacaoResponse> listar(
            String authorization,
            InternalRequestContext context,
            UUID professorTurmaDisciplinaId,
            UUID turmaId) {
        return avaliacaoPort.listar(authorization, context, professorTurmaDisciplinaId, turmaId);
    }

    @Override
    public AvaliacaoResponse buscarPorId(String authorization, InternalRequestContext context, UUID avaliacaoId) {
        return avaliacaoPort.buscarPorId(authorization, context, avaliacaoId);
    }
}
