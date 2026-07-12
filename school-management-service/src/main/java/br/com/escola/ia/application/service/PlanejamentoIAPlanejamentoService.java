package br.com.escola.ia.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.ia.application.dto.internal.PlanejamentoIAResumo;
import br.com.escola.ia.application.port.internal.PlanejamentoIAPort;
import br.com.escola.planejamento.adapter.out.persistence.repository.PlanejamentoBimestralJpaRepository;

@Service
public class PlanejamentoIAPlanejamentoService implements PlanejamentoIAPort {

    private final PlanejamentoBimestralJpaRepository planejamentoBimestralJpaRepository;

    public PlanejamentoIAPlanejamentoService(PlanejamentoBimestralJpaRepository planejamentoBimestralJpaRepository) {
        this.planejamentoBimestralJpaRepository = planejamentoBimestralJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlanejamentoIAResumo> buscarResumo(UUID planejamentoId, UUID escolaId) {
        return planejamentoBimestralJpaRepository
                .findByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(planejamentoId, escolaId)
                .map(planejamento -> new PlanejamentoIAResumo(
                        planejamento.getId(),
                        planejamento.getTitulo(),
                        planejamento.getTemaPrincipal(),
                        planejamento.getDescricaoInicial(),
                        planejamento.getObjetivoGeral(),
                        planejamento.getProfessorTurmaDisciplina().getTurmaDisciplina().getTurma().getNome(),
                        planejamento.getProfessorTurmaDisciplina().getTurmaDisciplina().getDisciplina().getNome()));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePlanejamento(UUID planejamentoId, UUID escolaId) {
        return planejamentoBimestralJpaRepository
                .existsByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(planejamentoId, escolaId);
    }
}
