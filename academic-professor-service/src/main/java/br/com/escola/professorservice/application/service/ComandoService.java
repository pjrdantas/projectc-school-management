package br.com.escola.professorservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.AllocateRequest;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.CreateRequest;
import br.com.escola.professorservice.application.dto.ResumoResponse;
import br.com.escola.professorservice.application.port.in.ComandoUseCase;
import br.com.escola.professorservice.application.port.out.CatalogoApoioPort;
import br.com.escola.professorservice.application.port.out.LeituraLocalPort;
import br.com.escola.professorservice.application.port.out.PessoaApoioPort;
import br.com.escola.professorservice.application.port.out.PersistenciaPort;

@Service
public class ComandoService implements ComandoUseCase {

    private final PessoaApoioPort pessoaApoioPort;
    private final CatalogoApoioPort catalogoApoioPort;
    private final LeituraLocalPort leituraLocalPort;
    private final PersistenciaPort persistenciaPort;

    public ComandoService(
            PessoaApoioPort pessoaApoioPort,
            CatalogoApoioPort catalogoApoioPort,
            LeituraLocalPort leituraLocalPort,
            PersistenciaPort persistenciaPort) {
        this.pessoaApoioPort = pessoaApoioPort;
        this.catalogoApoioPort = catalogoApoioPort;
        this.leituraLocalPort = leituraLocalPort;
        this.persistenciaPort = persistenciaPort;
    }

    @Override
    public ResumoResponse criarProfessor(
            String authorization,
            InternalRequestContext context,
            CreateRequest request) {
        var funcionario = pessoaApoioPort.buscarFuncionarioPorId(authorization, context, request.funcionarioId());
        var pessoa = pessoaApoioPort.buscarPessoaPorId(authorization, context, funcionario.pessoaId());
        return persistenciaPort.criarProfessor(context, funcionario, pessoa, request);
    }

    @Override
    public AlocacaoResponse alocarProfessorTurmaDisciplina(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            AllocateRequest request) {
        ResumoResponse professor = leituraLocalPort.buscarProfessorPorId(context, professorId);
        var turmaDisciplina =
                catalogoApoioPort.buscarTurmaDisciplina(authorization, context, request.turmaDisciplinaId());
        var turma = catalogoApoioPort.buscarTurma(authorization, context, turmaDisciplina.turmaId());
        return persistenciaPort.criarAlocacao(
                context,
                professor,
                turmaDisciplina,
                turma,
                request,
                Boolean.TRUE.equals(request.ativo()));
    }
}

