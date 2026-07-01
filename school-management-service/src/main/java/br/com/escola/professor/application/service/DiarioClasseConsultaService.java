package br.com.escola.professor.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.avaliacao.adapter.out.persistence.entity.AvaliacaoEntity;
import br.com.escola.avaliacao.adapter.out.persistence.repository.AvaliacaoJpaRepository;
import br.com.escola.frequencia.adapter.out.persistence.entity.FrequenciaAlunoEntity;
import br.com.escola.frequencia.adapter.out.persistence.repository.FrequenciaAlunoJpaRepository;
import br.com.escola.institucional.application.port.EscolaContextoPort;
import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoBimestralAulaEntity;
import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoBimestralEntity;
import br.com.escola.planejamento.adapter.out.persistence.repository.PlanejamentoBimestralAulaJpaRepository;
import br.com.escola.planejamento.adapter.out.persistence.repository.PlanejamentoBimestralJpaRepository;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseAlunoResponse;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseAssinaturaResponse;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseAvaliacaoResponse;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseCabecalhoResponse;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseConteudoPlanejadoResponse;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseResponse;
import br.com.escola.professor.adapter.out.persistence.entity.ProfessorTurmaDisciplinaEntity;
import br.com.escola.professor.adapter.out.persistence.repository.AulaJpaRepository;
import br.com.escola.professor.adapter.out.persistence.repository.ProfessorTurmaDisciplinaJpaRepository;
import br.com.escola.professor.domain.exception.ProfessorTurmaDisciplinaNaoEncontradaException;

@Service
public class DiarioClasseConsultaService {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM");
    private static final List<String> STATUS_MATRICULA_EXCLUIDOS = List.of("CANCELADA", "INDEFERIDA", "TRANSFERIDO");

    private final ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository;
    private final AulaJpaRepository aulaJpaRepository;
    private final FrequenciaAlunoJpaRepository frequenciaAlunoJpaRepository;
    private final MatriculaJpaRepository matriculaJpaRepository;
    private final PlanejamentoBimestralJpaRepository planejamentoBimestralJpaRepository;
    private final PlanejamentoBimestralAulaJpaRepository planejamentoBimestralAulaJpaRepository;
    private final AvaliacaoJpaRepository avaliacaoJpaRepository;
    private final EscolaContextoPort escolaContextoPort;

    public DiarioClasseConsultaService(
            ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository,
            AulaJpaRepository aulaJpaRepository,
            FrequenciaAlunoJpaRepository frequenciaAlunoJpaRepository,
            MatriculaJpaRepository matriculaJpaRepository,
            PlanejamentoBimestralJpaRepository planejamentoBimestralJpaRepository,
            PlanejamentoBimestralAulaJpaRepository planejamentoBimestralAulaJpaRepository,
            AvaliacaoJpaRepository avaliacaoJpaRepository,
            EscolaContextoPort escolaContextoPort) {
        this.professorTurmaDisciplinaJpaRepository = professorTurmaDisciplinaJpaRepository;
        this.aulaJpaRepository = aulaJpaRepository;
        this.frequenciaAlunoJpaRepository = frequenciaAlunoJpaRepository;
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.planejamentoBimestralJpaRepository = planejamentoBimestralJpaRepository;
        this.planejamentoBimestralAulaJpaRepository = planejamentoBimestralAulaJpaRepository;
        this.avaliacaoJpaRepository = avaliacaoJpaRepository;
        this.escolaContextoPort = escolaContextoPort;
    }

    @Transactional(readOnly = true)
    public DiarioClasseResponse carregar(
            UUID professorId,
            UUID turmaId,
            UUID disciplinaId,
            Integer anoLetivo,
            Integer mes,
            LocalDate dataReferencia) {
        ProfessorTurmaDisciplinaEntity alocacao = professorTurmaDisciplinaJpaRepository
                .findFirstByProfessor_IdAndTurmaDisciplina_Turma_IdAndTurmaDisciplina_Disciplina_IdAndTurmaDisciplina_Turma_PeriodoLetivo_AnoAndTurmaDisciplina_Turma_Escola_Id(
                        professorId,
                        turmaId,
                        disciplinaId,
                        anoLetivo,
                        escolaId())
                .orElseThrow(ProfessorTurmaDisciplinaNaoEncontradaException::new);

        LocalDate dataInicial = LocalDate.of(anoLetivo, mes, 1);
        LocalDate dataFinal = dataInicial.withDayOfMonth(dataInicial.lengthOfMonth());

        List<MatriculaEntity> matriculas = matriculaJpaRepository
                .findByTurma_IdAndPeriodoLetivo_AnoAndTurma_Escola_IdAndStatus_CodigoNotInOrderByAluno_Pessoa_NomeCompletoAsc(
                        turmaId,
                        anoLetivo,
                        escolaId(),
                        STATUS_MATRICULA_EXCLUIDOS);
        List<FrequenciaAlunoEntity> frequencias = frequenciaAlunoJpaRepository
                .findByAula_ProfessorTurmaDisciplina_IdAndAula_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_IdAndAula_DataAulaBetween(
                        alocacao.getId(),
                        escolaId(),
                        dataInicial,
                        dataFinal);
        List<DiarioClasseAlunoResponse> alunos = montarAlunos(matriculas, frequencias);

        PlanejamentoBimestralEntity planejamento = selecionarPlanejamento(alocacao.getId(), dataReferencia);
        List<DiarioClasseConteudoPlanejadoResponse> conteudosPlanejados = planejamento == null
                ? List.of()
                : planejamentoBimestralAulaJpaRepository.findByPlanejamentoBimestralId(planejamento.getId()).stream()
                        .sorted(Comparator.comparing(PlanejamentoBimestralAulaEntity::getNumeroAula))
                        .map(this::toConteudoPlanejado)
                        .toList();

        List<DiarioClasseAvaliacaoResponse> avaliacoes = avaliacaoJpaRepository
                .findByProfessorTurmaDisciplina_IdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                        alocacao.getId(),
                        escolaId())
                .stream()
                .filter(avaliacao -> avaliacao.getDataAplicacao() != null)
                .filter(avaliacao -> avaliacao.getDataAplicacao().getYear() == anoLetivo
                        && avaliacao.getDataAplicacao().getMonthValue() == mes)
                .sorted(Comparator.comparing(AvaliacaoEntity::getDataAplicacao))
                .map(avaliacao -> new DiarioClasseAvaliacaoResponse(
                        avaliacao.getId().toString(),
                        avaliacao.getDataAplicacao().format(DATA_BR),
                        avaliacao.getTitulo(),
                        alocacao.getTurmaDisciplina().getTurma().getNome(),
                        formatarFaixaValor(avaliacao.getValorMaximo())))
                .toList();

        List<String> observacoes = aulaJpaRepository
                .findByProfessorTurmaDisciplina_IdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_IdAndDataAulaBetween(
                        alocacao.getId(),
                        escolaId(),
                        dataInicial,
                        dataFinal)
                .stream()
                .map(aula -> aula.getObservacao())
                .filter(observacao -> observacao != null && !observacao.isBlank())
                .distinct()
                .toList();

        return new DiarioClasseResponse(
                montarCabecalho(alocacao, anoLetivo, mes, dataReferencia),
                alunos,
                conteudosPlanejados,
                observacoes,
                avaliacoes,
                new DiarioClasseAssinaturaResponse("", ""),
                Boolean.FALSE);
    }

    private DiarioClasseCabecalhoResponse montarCabecalho(
            ProfessorTurmaDisciplinaEntity alocacao,
            Integer anoLetivo,
            Integer mes,
            LocalDate dataReferencia) {
        var turma = alocacao.getTurmaDisciplina().getTurma();
        var disciplina = alocacao.getTurmaDisciplina().getDisciplina();
        var escola = turma.getEscola();
        var professor = alocacao.getProfessor();

        return new DiarioClasseCabecalhoResponse(
                gerarIdDiarioClasse(alocacao.getId(), anoLetivo, mes),
                escola.getId().toString(),
                nuloParaVazio(escola.getNome()),
                "",
                nuloParaVazio(escola.getCidade()),
                anoLetivo,
                mes,
                dataReferencia.toString(),
                turma.getId().toString(),
                turma.getNome(),
                nuloParaVazio(turma.getTurno()),
                disciplina.getId().toString(),
                disciplina.getNome(),
                professor.getId().toString(),
                professor.getPessoa().getNomeCompleto());
    }

    private List<DiarioClasseAlunoResponse> montarAlunos(
            List<MatriculaEntity> matriculas,
            List<FrequenciaAlunoEntity> frequencias) {
        Map<UUID, Map<String, String>> frequenciasPorMatricula = new LinkedHashMap<>();
        for (FrequenciaAlunoEntity frequencia : frequencias) {
            frequenciasPorMatricula
                    .computeIfAbsent(frequencia.getMatricula().getId(), ignored -> new LinkedHashMap<>())
                    .put(String.valueOf(frequencia.getAula().getDataAula().getDayOfMonth()), mapearSituacaoDiario(frequencia));
        }

        List<DiarioClasseAlunoResponse> alunos = new ArrayList<>();
        int numeroChamada = 1;
        for (MatriculaEntity matricula : matriculas) {
            alunos.add(new DiarioClasseAlunoResponse(
                    matricula.getAluno().getId().toString(),
                    numeroChamada++,
                    matricula.getAluno().getPessoa().getNomeCompleto(),
                    new LinkedHashMap<>(frequenciasPorMatricula.getOrDefault(matricula.getId(), Map.of()))));
        }
        return alunos;
    }

    private PlanejamentoBimestralEntity selecionarPlanejamento(UUID professorTurmaDisciplinaId, LocalDate dataReferencia) {
        return planejamentoBimestralJpaRepository.findByProfessorTurmaDisciplinaId(professorTurmaDisciplinaId).stream()
                .filter(planejamento -> planejamento.getPeriodoAvaliativo() != null)
                .filter(planejamento -> periodoContemData(planejamento, dataReferencia))
                .max(Comparator.comparing(PlanejamentoBimestralEntity::getCreatedAt))
                .orElseGet(() -> planejamentoBimestralJpaRepository.findByProfessorTurmaDisciplinaId(professorTurmaDisciplinaId)
                        .stream()
                        .max(Comparator.comparing(PlanejamentoBimestralEntity::getCreatedAt))
                        .orElse(null));
    }

    private boolean periodoContemData(PlanejamentoBimestralEntity planejamento, LocalDate dataReferencia) {
        LocalDate dataInicio = planejamento.getPeriodoAvaliativo().getDataInicio();
        LocalDate dataFim = planejamento.getPeriodoAvaliativo().getDataFim();
        if (dataInicio == null || dataFim == null) {
            return false;
        }
        return !dataReferencia.isBefore(dataInicio) && !dataReferencia.isAfter(dataFim);
    }

    private DiarioClasseConteudoPlanejadoResponse toConteudoPlanejado(PlanejamentoBimestralAulaEntity aula) {
        String descricao = aula.getConteudoPrevisto();
        if (descricao == null || descricao.isBlank()) {
            descricao = aula.getTemaAula();
        }
        return new DiarioClasseConteudoPlanejadoResponse(
                aula.getId().toString(),
                "Aula " + aula.getNumeroAula(),
                nuloParaVazio(descricao));
    }

    private String mapearSituacaoDiario(FrequenciaAlunoEntity frequencia) {
        String codigo = frequencia.getSituacaoFrequencia().getCodigo();
        if (codigo == null) {
            return "";
        }
        return switch (codigo.toUpperCase(Locale.ROOT)) {
            case "PRESENTE" -> "P";
            case "FALTA_JUSTIFICADA" -> ".";
            case "FALTA" -> "F";
            default -> "";
        };
    }

    private String formatarFaixaValor(BigDecimal valorMaximo) {
        if (valorMaximo == null) {
            return "";
        }
        return "0 a " + valorMaximo.stripTrailingZeros().toPlainString();
    }

    private String gerarIdDiarioClasse(UUID alocacaoId, Integer anoLetivo, Integer mes) {
        return "diario-" + anoLetivo + "-" + String.format("%02d", mes) + "-" + alocacaoId;
    }

    private String nuloParaVazio(String valor) {
        return valor == null ? "" : valor;
    }

    private UUID escolaId() {
        return escolaContextoPort.obterContextoPadrao().escolaId();
    }
}
