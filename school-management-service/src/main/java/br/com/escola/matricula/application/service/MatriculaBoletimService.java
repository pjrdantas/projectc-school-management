package br.com.escola.matricula.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.matricula.application.dto.internal.MatriculaBoletimResumo;
import br.com.escola.matricula.application.port.internal.MatriculaBoletimPort;

@Service
public class MatriculaBoletimService implements MatriculaBoletimPort {

    private final MatriculaJpaRepository matriculaJpaRepository;

    public MatriculaBoletimService(MatriculaJpaRepository matriculaJpaRepository) {
        this.matriculaJpaRepository = matriculaJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MatriculaBoletimResumo> buscarResumoPorIdEEscola(UUID matriculaId, UUID escolaId) {
        return matriculaJpaRepository.findByIdAndTurma_Escola_Id(matriculaId, escolaId)
                .map(matricula -> new MatriculaBoletimResumo(
                        matricula.getId(),
                        matricula.getAluno().getId(),
                        matricula.getAluno().getPessoa().getNomeCompleto(),
                        matricula.getTurma().getId(),
                        matricula.getTurma().getNome(),
                        matricula.getPeriodoLetivo().getId(),
                        matricula.getPeriodoLetivo().getNome(),
                        matricula.getTurma().getEscola().getId(),
                        matricula.getTurma().getEscola().getNome()));
    }
}
