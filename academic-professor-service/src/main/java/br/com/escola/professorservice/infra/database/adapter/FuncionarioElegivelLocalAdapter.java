package br.com.escola.professorservice.infra.database.adapter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.FuncionarioElegivelResponse;
import br.com.escola.professorservice.application.port.out.FuncionarioElegivelPort;
import br.com.escola.professorservice.application.port.out.PessoaApoioPort;
import br.com.escola.professorservice.infra.database.repository.CadastroJpaRepository;

@Repository
public class FuncionarioElegivelLocalAdapter implements FuncionarioElegivelPort {

    private final PessoaApoioPort pessoaApoioPort;
    private final CadastroJpaRepository cadastroRepository;

    public FuncionarioElegivelLocalAdapter(
            PessoaApoioPort pessoaApoioPort,
            CadastroJpaRepository cadastroRepository) {
        this.pessoaApoioPort = pessoaApoioPort;
        this.cadastroRepository = cadastroRepository;
    }

    @Override
    public List<FuncionarioElegivelResponse> listarFuncionariosElegiveis(
            String authorization,
            InternalRequestContext context) {
        Set<UUID> pessoasJaVinculadas = cadastroRepository.findAllByEscolaIdOrderByNomeCompletoAscIdAsc(context.escolaId())
                .stream()
                .map(entity -> entity.getPessoaId())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());

        return pessoaApoioPort.listarFuncionariosAtivosPorEscola(authorization, context).stream()
                .filter(funcionario -> !pessoasJaVinculadas.contains(funcionario.pessoaId()))
                .map(funcionario -> toResponse(authorization, context, funcionario))
                .toList();
    }

    private FuncionarioElegivelResponse toResponse(
            String authorization,
            InternalRequestContext context,
            PessoaApoioPort.FuncionarioResumo funcionario) {
        var pessoa = pessoaApoioPort.buscarPessoaPorId(authorization, context, funcionario.pessoaId());
        return new FuncionarioElegivelResponse(
                funcionario.funcionarioId(),
                funcionario.pessoaId(),
                funcionario.nomeCompleto(),
                funcionario.escolaId(),
                pessoa.escolaNome(),
                funcionario.cargoDescricao(),
                funcionario.ativo(),
                true);
    }
}
