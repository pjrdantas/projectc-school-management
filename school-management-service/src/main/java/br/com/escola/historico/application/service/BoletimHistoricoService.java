package br.com.escola.historico.application.service;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.historico.adapter.out.persistence.repository.BoletimItemJpaRepository;
import br.com.escola.historico.adapter.out.persistence.repository.BoletimJpaRepository;
import br.com.escola.historico.application.dto.internal.BoletimHistoricoItemResumo;
import br.com.escola.historico.application.dto.internal.BoletimHistoricoResumo;
import br.com.escola.historico.application.port.internal.BoletimHistoricoPort;

@Service
public class BoletimHistoricoService implements BoletimHistoricoPort {

    private final BoletimJpaRepository boletimJpaRepository;
    private final BoletimItemJpaRepository boletimItemJpaRepository;

    public BoletimHistoricoService(
            BoletimJpaRepository boletimJpaRepository,
            BoletimItemJpaRepository boletimItemJpaRepository) {
        this.boletimJpaRepository = boletimJpaRepository;
        this.boletimItemJpaRepository = boletimItemJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BoletimHistoricoResumo> buscarParaGeracao(UUID boletimId, UUID escolaId) {
        return boletimJpaRepository.findByIdAndMatricula_Turma_Escola_Id(boletimId, escolaId)
                .map(boletim -> new BoletimHistoricoResumo(
                        boletim.getId(),
                        boletim.getMatricula().getId(),
                        boletim.getMatricula().getAluno().getId(),
                        boletim.getMatricula().getPeriodoLetivo().getId(),
                        boletim.getMatricula().getAluno().getNomeCompleto(),
                        boletim.getMatricula().getAluno().getRg(),
                        boletim.getMatricula().getAluno().getRa(),
                        boletim.getMatricula().getAluno().getRm(),
                        boletim.getMatricula().getAluno().getDataNascimento(),
                        boletim.getMatricula().getAluno().getNaturalidade(),
                        boletim.getMatricula().getAluno().getNacionalidade(),
                        boletim.getMatricula().getPeriodoLetivo().getAno(),
                        boletim.getPeriodoReferencia(),
                        boletimItemJpaRepository.findByBoletimId(boletim.getId()).stream()
                                .sorted(Comparator.comparing(item -> item.getDisciplina().getNome(), String.CASE_INSENSITIVE_ORDER))
                                .map(item -> new BoletimHistoricoItemResumo(
                                        boletim.getMatricula().getPeriodoLetivo(),
                                        boletim.getMatricula().getTurma().getSerie(),
                                        item.getDisciplina(),
                                        item.getDisciplina().getNome(),
                                        boletim.getMatricula().getPeriodoLetivo().getAno(),
                                        boletim.getMatricula().getTurma().getSerie().getNome(),
                                        item.getMedia(),
                                        item.getFrequenciaPercentual(),
                                        item.getCargaHoraria(),
                                        item.getDisciplina().getCargaHoraria(),
                                        item.getResultado()))
                                .toList()));
    }
}
