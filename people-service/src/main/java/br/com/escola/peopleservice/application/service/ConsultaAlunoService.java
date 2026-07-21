package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.AlunoFichaResponse;
import br.com.escola.peopleservice.application.dto.AlunoResponse;
import br.com.escola.peopleservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.peopleservice.application.model.AlunoConsulta;
import br.com.escola.peopleservice.application.port.in.ConsultarAlunoUseCase;
import br.com.escola.peopleservice.application.port.out.AlunoLeituraPort;
import br.com.escola.peopleservice.application.port.out.ResponsaveisAlunoConsultaPort;

@Service
public class ConsultaAlunoService implements ConsultarAlunoUseCase {

    private final AlunoLeituraPort alunoLeituraPort;
    private final ResponsaveisAlunoConsultaPort responsaveisAlunoConsultaPort;

    public ConsultaAlunoService(
            AlunoLeituraPort alunoLeituraPort,
            ResponsaveisAlunoConsultaPort responsaveisAlunoConsultaPort) {
        this.alunoLeituraPort = alunoLeituraPort;
        this.responsaveisAlunoConsultaPort = responsaveisAlunoConsultaPort;
    }

    @Override
    public List<AlunoResponse> listar(String nome, InternalRequestContext context) {
        return alunoLeituraPort.listar(nome, context.escolaId()).stream()
                .map(AlunoResponse::from)
                .toList();
    }

    @Override
    public AlunoResponse buscar(UUID alunoId, InternalRequestContext context) {
        return buscarAluno(alunoId, context).map(AlunoResponse::from).orElseThrow(
                () -> new RecursoNaoEncontradoException("Aluno nao encontrado"));
    }

    @Override
    public AlunoFichaResponse buscarFicha(
            UUID alunoId,
            String authorization,
            InternalRequestContext context) {
        AlunoResponse aluno = buscar(alunoId, context);
        return new AlunoFichaResponse(
                aluno,
                responsaveisAlunoConsultaPort.listarResponsaveisPorAluno(alunoId, authorization, context));
    }

    private Optional<AlunoConsulta> buscarAluno(
            UUID alunoId,
            InternalRequestContext context) {
        return alunoLeituraPort.buscar(alunoId, context.escolaId());
    }
}
