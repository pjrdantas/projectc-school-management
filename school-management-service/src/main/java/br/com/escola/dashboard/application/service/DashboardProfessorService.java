package br.com.escola.dashboard.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.avaliacao.adapter.out.persistence.repository.AvaliacaoJpaRepository;
import br.com.escola.avaliacao.adapter.out.persistence.repository.NotaAlunoJpaRepository;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardProfessorResponse;
import br.com.escola.dashboard.adapter.in.web.dto.DashboardProfessorTurmaResponse;
import br.com.escola.dashboard.application.dto.internal.DashboardProfessorResumo;
import br.com.escola.dashboard.application.dto.internal.DashboardProfessorTurmaResumo;
import br.com.escola.dashboard.application.port.internal.DashboardProfessorPort;
import br.com.escola.frequencia.adapter.out.persistence.repository.FrequenciaProfessorJpaRepository;
import br.com.escola.institucional.application.dto.EscolaContexto;
import br.com.escola.institucional.application.port.EscolaContextoPort;
import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoBimestralEntity;
import br.com.escola.planejamento.adapter.out.persistence.repository.PlanejamentoBimestralJpaRepository;
import br.com.escola.professor.adapter.out.persistence.entity.ProfessorTurmaDisciplinaEntity;
import br.com.escola.professor.adapter.out.persistence.repository.AulaJpaRepository;
import br.com.escola.professor.adapter.out.persistence.repository.ProfessorJpaRepository;
import br.com.escola.professor.adapter.out.persistence.repository.ProfessorTurmaDisciplinaJpaRepository;
import br.com.escola.professor.domain.exception.ProfessorNaoEncontradoException;

@Service
public class DashboardProfessorService implements DashboardProfessorPort {

    private final ProfessorJpaRepository professorJpaRepository;
    private final ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository;
    private final AulaJpaRepository aulaJpaRepository;
    private final FrequenciaProfessorJpaRepository frequenciaProfessorJpaRepository;
    private final AvaliacaoJpaRepository avaliacaoJpaRepository;
    private final NotaAlunoJpaRepository notaAlunoJpaRepository;
    private final PlanejamentoBimestralJpaRepository planejamentoBimestralJpaRepository;
    private final EscolaContextoPort escolaContextoPort;

    public DashboardProfessorService(
            ProfessorJpaRepository professorJpaRepository,
            ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository,
            AulaJpaRepository aulaJpaRepository,
            FrequenciaProfessorJpaRepository frequenciaProfessorJpaRepository,
            AvaliacaoJpaRepository avaliacaoJpaRepository,
            NotaAlunoJpaRepository notaAlunoJpaRepository,
            PlanejamentoBimestralJpaRepository planejamentoBimestralJpaRepository,
            EscolaContextoPort escolaContextoPort) {
        this.professorJpaRepository = professorJpaRepository;
        this.professorTurmaDisciplinaJpaRepository = professorTurmaDisciplinaJpaRepository;
        this.aulaJpaRepository = aulaJpaRepository;
        this.frequenciaProfessorJpaRepository = frequenciaProfessorJpaRepository;
        this.avaliacaoJpaRepository = avaliacaoJpaRepository;
        this.notaAlunoJpaRepository = notaAlunoJpaRepository;
        this.planejamentoBimestralJpaRepository = planejamentoBimestralJpaRepository;
        this.escolaContextoPort = escolaContextoPort;
    }

    @Transactional(readOnly = true)
    public DashboardProfessorResponse consultar(UUID professorId) {
        DashboardProfessorResumo resumo = consultarResumo(professorId);
        return new DashboardProfessorResponse(
                resumo.escolaId(),
                resumo.escolaNome(),
                resumo.professorId(),
                resumo.turmasVinculadas(),
                resumo.alocacoesAtivas(),
                resumo.aulasPlanejadas(),
                resumo.aulasRealizadas(),
                resumo.frequenciasPendentes(),
                resumo.avaliacoesRegistradas(),
                resumo.avaliacoesComNotasPendentes(),
                resumo.planejamentosBimestrais(),
                resumo.planejamentosBimestraisPendentes(),
                resumo.turmas().stream()
                        .map(this::toTurmaResponse)
                        .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardProfessorResumo consultarResumo(UUID professorId) {
        EscolaContexto contexto = escolaContextoPort.obterContextoPadrao();
        UUID escolaId = contexto.escolaId();
        if (!professorJpaRepository.existsByIdAndPessoa_Escola_Id(professorId, escolaId)) {
            throw new ProfessorNaoEncontradoException();
        }

        List<ProfessorTurmaDisciplinaEntity> alocacoes = professorTurmaDisciplinaJpaRepository.findByProfessorId(professorId)
                .stream()
                .filter(alocacao -> escolaId.equals(alocacao.getTurmaDisciplina().getTurma().getEscola().getId()))
                .toList();
        List<ProfessorTurmaDisciplinaEntity> alocacoesAtivas = alocacoes.stream()
                .filter(alocacao -> !Boolean.FALSE.equals(alocacao.getAtivo()))
                .toList();

        return new DashboardProfessorResumo(
                contexto.escolaId(),
                contexto.escolaNome(),
                professorId,
                contarTurmasDistintas(alocacoesAtivas),
                alocacoesAtivas.size(),
                contarAulas(alocacoes, false),
                contarAulas(alocacoes, true),
                contarFrequenciasPendentes(professorId, alocacoes),
                contarAvaliacoes(alocacoes),
                contarAvaliacoesComNotasPendentes(alocacoes),
                contarPlanejamentosBimestrais(alocacoes),
                contarPlanejamentosBimestraisPendentes(alocacoes),
                listarTurmas(alocacoesAtivas));
    }

    private long contarTurmasDistintas(List<ProfessorTurmaDisciplinaEntity> alocacoes) {
        return alocacoes.stream()
                .map(alocacao -> alocacao.getTurmaDisciplina().getTurma().getId())
                .distinct()
                .count();
    }

    private long contarAulas(List<ProfessorTurmaDisciplinaEntity> alocacoes, boolean realizada) {
        return alocacoes.stream()
                .flatMap(alocacao -> aulaJpaRepository.findByProfessorTurmaDisciplinaId(alocacao.getId()).stream())
                .filter(aula -> Boolean.valueOf(realizada).equals(aula.getRealizada()))
                .count();
    }

    private long contarFrequenciasPendentes(UUID professorId, List<ProfessorTurmaDisciplinaEntity> alocacoes) {
        return alocacoes.stream()
                .flatMap(alocacao -> aulaJpaRepository.findByProfessorTurmaDisciplinaId(alocacao.getId()).stream())
                .filter(aula -> Boolean.TRUE.equals(aula.getRealizada()))
                .filter(aula -> frequenciaProfessorJpaRepository.findByAulaIdAndProfessorId(aula.getId(), professorId).isEmpty())
                .count();
    }

    private long contarAvaliacoes(List<ProfessorTurmaDisciplinaEntity> alocacoes) {
        return alocacoes.stream()
                .mapToLong(alocacao -> avaliacaoJpaRepository.findByProfessorTurmaDisciplinaId(alocacao.getId()).size())
                .sum();
    }

    private long contarAvaliacoesComNotasPendentes(List<ProfessorTurmaDisciplinaEntity> alocacoes) {
        return alocacoes.stream()
                .flatMap(alocacao -> avaliacaoJpaRepository.findByProfessorTurmaDisciplinaId(alocacao.getId()).stream())
                .filter(avaliacao -> notaAlunoJpaRepository.findByAvaliacaoId(avaliacao.getId()).isEmpty())
                .count();
    }

    private long contarPlanejamentosBimestrais(List<ProfessorTurmaDisciplinaEntity> alocacoes) {
        return alocacoes.stream()
                .mapToLong(alocacao -> planejamentoBimestralJpaRepository.findByProfessorTurmaDisciplinaId(alocacao.getId()).size())
                .sum();
    }

    private long contarPlanejamentosBimestraisPendentes(List<ProfessorTurmaDisciplinaEntity> alocacoes) {
        return alocacoes.stream()
                .flatMap(alocacao -> planejamentoBimestralJpaRepository.findByProfessorTurmaDisciplinaId(alocacao.getId()).stream())
                .filter(this::isPlanejamentoPendente)
                .count();
    }

    private boolean isPlanejamentoPendente(PlanejamentoBimestralEntity planejamento) {
        return !Boolean.TRUE.equals(planejamento.getAprovadoPeloProfessor());
    }

    private List<DashboardProfessorTurmaResumo> listarTurmas(List<ProfessorTurmaDisciplinaEntity> alocacoes) {
        return alocacoes.stream()
                .map(this::toTurmaResumo)
                .sorted(Comparator
                        .comparing(DashboardProfessorTurmaResumo::turmaNome)
                        .thenComparing(DashboardProfessorTurmaResumo::disciplinaNome))
                .toList();
    }

    private DashboardProfessorTurmaResumo toTurmaResumo(ProfessorTurmaDisciplinaEntity alocacao) {
        var turmaDisciplina = alocacao.getTurmaDisciplina();
        return new DashboardProfessorTurmaResumo(
                alocacao.getId(),
                turmaDisciplina.getTurma().getId(),
                turmaDisciplina.getTurma().getNome(),
                turmaDisciplina.getDisciplina().getId(),
                turmaDisciplina.getDisciplina().getNome());
    }

    private DashboardProfessorTurmaResponse toTurmaResponse(DashboardProfessorTurmaResumo turma) {
        return new DashboardProfessorTurmaResponse(
                turma.professorTurmaDisciplinaId(),
                turma.turmaId(),
                turma.turmaNome(),
                turma.disciplinaId(),
                turma.disciplinaNome());
    }
}
