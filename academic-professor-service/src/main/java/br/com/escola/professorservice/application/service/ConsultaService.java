package br.com.escola.professorservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.FuncionarioElegivelResponse;
import br.com.escola.professorservice.application.dto.ResumoResponse;
import br.com.escola.professorservice.application.port.in.ConsultaUseCase;
import br.com.escola.professorservice.application.port.out.FuncionarioElegivelPort;
import br.com.escola.professorservice.application.port.out.LeituraLocalPort;

@Service
public class ConsultaService implements ConsultaUseCase {

    private final FuncionarioElegivelPort funcionarioElegivelPort;
    private final LeituraLocalPort leituraLocalPort;

    public ConsultaService(
            FuncionarioElegivelPort funcionarioElegivelPort,
            LeituraLocalPort leituraLocalPort) {
        this.funcionarioElegivelPort = funcionarioElegivelPort;
        this.leituraLocalPort = leituraLocalPort;
    }

    @Override
    public List<ResumoResponse> listarProfessores(String authorization, InternalRequestContext context) {
        return leituraLocalPort.listarProfessores(context);
    }

    @Override
    public ResumoResponse buscarProfessorPorId(String authorization, InternalRequestContext context, UUID professorId) {
        return leituraLocalPort.buscarProfessorPorId(context, professorId);
    }

    @Override
    public List<AlocacaoResponse> listarAlocacoes(
            String authorization,
            InternalRequestContext context,
            UUID professorId) {
        return leituraLocalPort.listarAlocacoes(context, professorId);
    }

    @Override
    public List<AlocacaoResponse> listarProfessoresPorTurma(
            String authorization,
            InternalRequestContext context,
            UUID turmaId) {
        return leituraLocalPort.listarProfessoresPorTurma(context, turmaId);
    }

    @Override
    public List<FuncionarioElegivelResponse> listarFuncionariosElegiveis(
            String authorization,
            InternalRequestContext context) {
        return funcionarioElegivelPort.listarFuncionariosElegiveis(authorization, context);
    }
}

