package br.com.escola.professorservice.application.port.out;

import java.util.List;
import java.util.UUID;

import br.com.escola.professorservice.application.context.InternalRequestContext;

public interface PessoaApoioPort {

    FuncionarioResumo buscarFuncionarioPorId(String authorization, InternalRequestContext context, UUID funcionarioId);

    List<FuncionarioResumo> listarFuncionariosAtivosPorEscola(String authorization, InternalRequestContext context);

    PessoaResumo buscarPessoaPorId(String authorization, InternalRequestContext context, UUID pessoaId);

    record FuncionarioResumo(
            UUID funcionarioId,
            UUID pessoaId,
            UUID escolaId,
            String nomeCompleto,
            String cargoDescricao,
            boolean ativo) {
    }

    record PessoaResumo(
            UUID id,
            String nomeCompleto,
            UUID escolaId,
            String escolaNome,
            Boolean ativo) {
    }
}
