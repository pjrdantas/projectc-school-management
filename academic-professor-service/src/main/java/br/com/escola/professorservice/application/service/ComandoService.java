package br.com.escola.professorservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.AllocateRequest;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.CreateRequest;
import br.com.escola.professorservice.application.dto.ResumoResponse;
import br.com.escola.professorservice.application.dto.UpdateRequest;
import br.com.escola.professorservice.application.dto.UpdateAllocateRequest;
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
    public ResumoResponse atualizarProfessor(
            InternalRequestContext context,
            UUID professorId,
            UpdateRequest request) {
        leituraLocalPort.buscarProfessorPorId(context, professorId);
        return persistenciaPort.atualizarProfessor(context, professorId, request);
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

    @Override
    public AlocacaoResponse atualizarAlocacaoProfessorTurmaDisciplina(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            UUID alocacaoId,
            UpdateAllocateRequest request) {
        if (request.dataInicio() != null && request.dataFim() != null
                && request.dataFim().isBefore(request.dataInicio())) {
            throw new IllegalArgumentException("dataFim nao pode ser anterior a dataInicio");
        }

        ResumoResponse professor = leituraLocalPort.buscarProfessorPorId(context, professorId);
        var turmaDisciplina = catalogoApoioPort.buscarTurmaDisciplina(
                authorization, context, request.turmaDisciplinaId());
        var turma = catalogoApoioPort.buscarTurma(authorization, context, turmaDisciplina.turmaId());
        return persistenciaPort.atualizarAlocacao(
                context, professor, alocacaoId, turmaDisciplina, turma, request);
    }

    @Override
    public void encerrarAlocacaoProfessorTurmaDisciplina(
            InternalRequestContext context,
            UUID professorId,
            UUID alocacaoId) {
        leituraLocalPort.buscarProfessorPorId(context, professorId);
        persistenciaPort.encerrarAlocacao(context, professorId, alocacaoId);
    }
}

