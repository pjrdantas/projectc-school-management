package br.com.escola.professorservice.application.port.out;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.AllocateRequest;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.CreateRequest;
import br.com.escola.professorservice.application.dto.ResumoResponse;

public interface PersistenciaPort {

    ResumoResponse criarProfessor(
            InternalRequestContext context,
            PessoaApoioPort.FuncionarioResumo funcionario,
            PessoaApoioPort.PessoaResumo pessoa,
            CreateRequest request);

    AlocacaoResponse criarAlocacao(
            InternalRequestContext context,
            ResumoResponse professor,
            CatalogoApoioPort.TurmaDisciplinaResumo turmaDisciplina,
            CatalogoApoioPort.TurmaResumo turma,
            AllocateRequest request,
            boolean ativo);
}

