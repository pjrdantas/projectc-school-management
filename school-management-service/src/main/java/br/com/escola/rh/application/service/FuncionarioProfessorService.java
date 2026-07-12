package br.com.escola.rh.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.compartilhado.pessoa.dto.internal.PessoaResumo;
import br.com.escola.compartilhado.pessoa.port.internal.PessoaConsultaPort;
import br.com.escola.professor.application.port.internal.ProfessorPessoaPort;
import br.com.escola.rh.adapter.out.persistence.entity.FuncionarioEntity;
import br.com.escola.rh.adapter.out.persistence.repository.FuncionarioJpaRepository;
import br.com.escola.rh.application.dto.internal.FuncionarioProfessorResumo;
import br.com.escola.rh.application.port.internal.FuncionarioProfessorPort;

@Service
@Primary
public class FuncionarioProfessorService implements FuncionarioProfessorPort {

    private final FuncionarioJpaRepository funcionarioJpaRepository;
    private final ProfessorPessoaPort professorPessoaPort;
    private final PessoaConsultaPort pessoaConsultaPort;

    public FuncionarioProfessorService(
            FuncionarioJpaRepository funcionarioJpaRepository,
            ProfessorPessoaPort professorPessoaPort,
            PessoaConsultaPort pessoaConsultaPort) {
        this.funcionarioJpaRepository = funcionarioJpaRepository;
        this.professorPessoaPort = professorPessoaPort;
        this.pessoaConsultaPort = pessoaConsultaPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FuncionarioProfessorResumo> buscarFuncionarioParaProfessor(UUID escolaId, UUID funcionarioId) {
        return funcionarioJpaRepository.findByIdAndPessoa_Escola_Id(funcionarioId, escolaId)
                .flatMap(funcionario -> toResumo(funcionario, escolaId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuncionarioProfessorResumo> listarFuncionariosElegiveisParaProfessor(UUID escolaId) {
        return funcionarioJpaRepository.findAllByPessoa_Escola_Id(escolaId).stream()
                .flatMap(funcionario -> toResumo(funcionario, escolaId).stream())
                .filter(resumo -> Boolean.TRUE.equals(resumo.elegivelProfessor()))
                .sorted((left, right) -> left.nomeCompleto().compareToIgnoreCase(right.nomeCompleto()))
                .toList();
    }

    private Optional<FuncionarioProfessorResumo> toResumo(FuncionarioEntity entity, UUID escolaId) {
        UUID pessoaId = entity.getPessoa() == null ? null : entity.getPessoa().getId();
        Optional<PessoaResumo> pessoa = pessoaConsultaPort.buscarPessoaPorIdEEscola(pessoaId, escolaId);
        if (pessoa.isEmpty()) {
            return Optional.empty();
        }

        boolean ativo = Boolean.TRUE.equals(entity.getAtivo());
        boolean jaCadastradoComoProfessor = professorPessoaPort
                .existeProfessorPorPessoa(escolaId, pessoa.get().id());
        return Optional.of(new FuncionarioProfessorResumo(
                entity.getId(),
                pessoa.get().id(),
                pessoa.get().nomeCompleto(),
                pessoa.get().escolaId(),
                pessoa.get().escolaNome(),
                entity.getCargo() == null ? null : entity.getCargo().getDescricao(),
                entity.getAtivo(),
                ativo && !jaCadastradoComoProfessor));
    }
}
