package br.com.escola.professor.application.service;

import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.professor.adapter.out.persistence.repository.ProfessorJpaRepository;
import br.com.escola.professor.application.port.internal.ProfessorPessoaPort;

@Service
@Primary
public class ProfessorPessoaService implements ProfessorPessoaPort {

    private final ProfessorJpaRepository professorJpaRepository;

    public ProfessorPessoaService(ProfessorJpaRepository professorJpaRepository) {
        this.professorJpaRepository = professorJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeProfessorPorPessoa(UUID escolaId, UUID pessoaId) {
        if (escolaId == null || pessoaId == null) {
            return false;
        }
        return professorJpaRepository.existsByPessoa_IdAndPessoa_Escola_Id(pessoaId, escolaId);
    }
}
