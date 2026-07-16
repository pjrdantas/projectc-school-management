package br.com.escola.responsiblesservice.application.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import br.com.escola.responsiblesservice.application.context.InternalRequestContext;
import br.com.escola.responsiblesservice.application.port.in.ResponsavelQueryUseCase;
import br.com.escola.responsiblesservice.application.port.out.ResponsavelReadPort;

@Service
public class ResponsavelQueryService implements ResponsavelQueryUseCase {

    private final ResponsavelReadPort responsavelReadPort;

    public ResponsavelQueryService(ResponsavelReadPort responsavelReadPort) {
        this.responsavelReadPort = responsavelReadPort;
    }

    @Override
    public ResponseEntity<String> listarResponsaveis(
            String authorization,
            InternalRequestContext context,
            String nome,
            String cpf) {
        return responsavelReadPort.listarResponsaveis(authorization, context, nome, cpf);
    }

    @Override
    public ResponseEntity<String> buscarResponsavelPorId(
            String authorization,
            InternalRequestContext context,
            UUID responsavelId) {
        return responsavelReadPort.buscarResponsavelPorId(authorization, context, responsavelId);
    }

    @Override
    public ResponseEntity<String> listarResponsaveisPorAluno(
            String authorization,
            InternalRequestContext context,
            UUID alunoId) {
        return responsavelReadPort.listarResponsaveisPorAluno(authorization, context, alunoId);
    }
}
