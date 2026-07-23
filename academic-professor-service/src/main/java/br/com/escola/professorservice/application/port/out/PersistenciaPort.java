package br.com.escola.professorservice.application.port.out;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.AllocateRequest;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.CreateRequest;
import br.com.escola.professorservice.application.dto.ResumoResponse;
import br.com.escola.professorservice.application.dto.UpdateRequest;
import br.com.escola.professorservice.application.dto.UpdateAllocateRequest;

public interface PersistenciaPort {

    ResumoResponse criarProfessor(
            InternalRequestContext context,
            PessoaApoioPort.FuncionarioResumo funcionario,
            PessoaApoioPort.PessoaResumo pessoa,
            CreateRequest request);

    ResumoResponse atualizarProfessor(
            InternalRequestContext context,
            java.util.UUID professorId,
            UpdateRequest request);

    AlocacaoResponse criarAlocacao(
            InternalRequestContext context,
            ResumoResponse professor,
            CatalogoApoioPort.TurmaDisciplinaResumo turmaDisciplina,
            CatalogoApoioPort.TurmaResumo turma,
            AllocateRequest request,
            boolean ativo);

    AlocacaoResponse atualizarAlocacao(
            InternalRequestContext context,
            ResumoResponse professor,
            java.util.UUID alocacaoId,
            CatalogoApoioPort.TurmaDisciplinaResumo turmaDisciplina,
            CatalogoApoioPort.TurmaResumo turma,
            UpdateAllocateRequest request);

    void encerrarAlocacao(
            InternalRequestContext context,
            java.util.UUID professorId,
            java.util.UUID alocacaoId);
}

