package br.com.escola.rh.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.professor.adapter.out.persistence.repository.ProfessorJpaRepository;
import br.com.escola.rh.adapter.out.persistence.entity.FuncionarioEntity;
import br.com.escola.rh.adapter.out.persistence.repository.FuncionarioJpaRepository;
import br.com.escola.rh.application.dto.internal.FuncionarioProfessorResumo;
import br.com.escola.rh.application.port.internal.FuncionarioProfessorPort;

@Service
@Primary
public class FuncionarioProfessorService implements FuncionarioProfessorPort {

    private final FuncionarioJpaRepository funcionarioJpaRepository;
    private final ProfessorJpaRepository professorJpaRepository;

    public FuncionarioProfessorService(
            FuncionarioJpaRepository funcionarioJpaRepository,
            ProfessorJpaRepository professorJpaRepository) {
        this.funcionarioJpaRepository = funcionarioJpaRepository;
        this.professorJpaRepository = professorJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FuncionarioProfessorResumo> buscarFuncionarioParaProfessor(UUID escolaId, UUID funcionarioId) {
        return funcionarioJpaRepository.findByIdAndPessoa_Escola_Id(funcionarioId, escolaId)
                .map(funcionario -> toResumo(funcionario, escolaId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuncionarioProfessorResumo> listarFuncionariosElegiveisParaProfessor(UUID escolaId) {
        return funcionarioJpaRepository.findAllByPessoa_Escola_Id(escolaId).stream()
                .map(funcionario -> toResumo(funcionario, escolaId))
                .filter(resumo -> Boolean.TRUE.equals(resumo.elegivelProfessor()))
                .sorted((left, right) -> left.nomeCompleto().compareToIgnoreCase(right.nomeCompleto()))
                .toList();
    }

    private FuncionarioProfessorResumo toResumo(FuncionarioEntity entity, UUID escolaId) {
        boolean ativo = Boolean.TRUE.equals(entity.getAtivo());
        boolean jaCadastradoComoProfessor = professorJpaRepository.existsByPessoa_IdAndPessoa_Escola_Id(
                entity.getPessoa().getId(),
                escolaId);
        return new FuncionarioProfessorResumo(
                entity.getId(),
                entity.getPessoa().getId(),
                entity.getPessoa().getNomeCompleto(),
                entity.getPessoa().getEscola().getId(),
                entity.getPessoa().getEscola().getNome(),
                entity.getCargo() == null ? null : entity.getCargo().getDescricao(),
                entity.getAtivo(),
                ativo && !jaCadastradoComoProfessor);
    }
}
