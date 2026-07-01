package br.com.escola.professor.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.avaliacao.adapter.out.persistence.entity.AvaliacaoEntity;
import br.com.escola.avaliacao.adapter.out.persistence.repository.AvaliacaoJpaRepository;
import br.com.escola.frequencia.adapter.out.persistence.entity.FrequenciaAlunoEntity;
import br.com.escola.frequencia.adapter.out.persistence.entity.SituacaoFrequenciaEntity;
import br.com.escola.frequencia.adapter.out.persistence.repository.FrequenciaAlunoJpaRepository;
import br.com.escola.frequencia.adapter.out.persistence.repository.SituacaoFrequenciaJpaRepository;
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
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseFrequenciaRequest;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseResponse;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseSalvarRequest;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseSalvarResponse;
import br.com.escola.professor.adapter.out.persistence.entity.AulaEntity;
import br.com.escola.professor.adapter.out.persistence.entity.ProfessorTurmaDisciplinaEntity;
import br.com.escola.professor.adapter.out.persistence.repository.AulaJpaRepository;
import br.com.escola.professor.adapter.out.persistence.repository.ProfessorTurmaDisciplinaJpaRepository;
import br.com.escola.professor.domain.exception.DiarioClasseLancamentoDuplicadoException;
import br.com.escola.professor.domain.exception.DiarioClasseLancamentoInvalidoException;
import br.com.escola.professor.domain.exception.ProfessorTurmaDisciplinaNaoEncontradaException;
import br.com.escola.professor.domain.exception.SituacaoFrequenciaNaoEncontradaException;

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
    private final SituacaoFrequenciaJpaRepository situacaoFrequenciaJpaRepository;
    private final EscolaContextoPort escolaContextoPort;

    public DiarioClasseConsultaService(
            ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository,
            AulaJpaRepository aulaJpaRepository,
            FrequenciaAlunoJpaRepository frequenciaAlunoJpaRepository,
            MatriculaJpaRepository matriculaJpaRepository,
            PlanejamentoBimestralJpaRepository planejamentoBimestralJpaRepository,
            PlanejamentoBimestralAulaJpaRepository planejamentoBimestralAulaJpaRepository,
            AvaliacaoJpaRepository avaliacaoJpaRepository,
            SituacaoFrequenciaJpaRepository situacaoFrequenciaJpaRepository,
            EscolaContextoPort escolaContextoPort) {
        this.professorTurmaDisciplinaJpaRepository = professorTurmaDisciplinaJpaRepository;
        this.aulaJpaRepository = aulaJpaRepository;
        this.frequenciaAlunoJpaRepository = frequenciaAlunoJpaRepository;
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.planejamentoBimestralJpaRepository = planejamentoBimestralJpaRepository;
        this.planejamentoBimestralAulaJpaRepository = planejamentoBimestralAulaJpaRepository;
        this.avaliacaoJpaRepository = avaliacaoJpaRepository;
        this.situacaoFrequenciaJpaRepository = situacaoFrequenciaJpaRepository;
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

    @Transactional
    public DiarioClasseSalvarResponse salvar(String idDiarioClasse, DiarioClasseSalvarRequest request) {
        if (!idDiarioClasse.equals(request.idDiarioClasse())) {
            throw new DiarioClasseLancamentoInvalidoException("Identificador do diario de classe inconsistente.");
        }

        DiarioClasseId diarioClasseId = parseIdDiarioClasse(idDiarioClasse);
        LocalDate dataLancamento = request.dataLancamento();
        if (dataLancamento.getYear() != diarioClasseId.anoLetivo()
                || dataLancamento.getMonthValue() != diarioClasseId.mes()) {
            throw new DiarioClasseLancamentoInvalidoException("Data de lancamento fora do mes do diario.");
        }

        ProfessorTurmaDisciplinaEntity alocacao = professorTurmaDisciplinaJpaRepository
                .findByIdAndTurmaDisciplina_Turma_Escola_Id(diarioClasseId.professorTurmaDisciplinaId(), escolaId())
                .orElseThrow(ProfessorTurmaDisciplinaNaoEncontradaException::new);
        UUID turmaId = alocacao.getTurmaDisciplina().getTurma().getId();

        List<MatriculaEntity> matriculas = matriculaJpaRepository
                .findByTurma_IdAndPeriodoLetivo_AnoAndTurma_Escola_IdAndStatus_CodigoNotInOrderByAluno_Pessoa_NomeCompletoAsc(
                        turmaId,
                        diarioClasseId.anoLetivo(),
                        escolaId(),
                        STATUS_MATRICULA_EXCLUIDOS);
        if (matriculas.isEmpty()) {
            throw new DiarioClasseLancamentoInvalidoException("Diario de classe sem alunos ativos para lancamento.");
        }

        Map<UUID, MatriculaEntity> matriculasPorAluno = matriculas.stream()
                .collect(Collectors.toMap(matricula -> matricula.getAluno().getId(), Function.identity()));
        validarFrequenciasObrigatorias(request.frequencias(), matriculasPorAluno.keySet(), dataLancamento);

        AulaEntity aula = aulaJpaRepository
                .findByProfessorTurmaDisciplina_IdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_IdAndDataAula(
                        alocacao.getId(),
                        escolaId(),
                        dataLancamento)
                .orElseGet(() -> criarAulaDiario(alocacao, dataLancamento, request));

        for (DiarioClasseFrequenciaRequest frequenciaRequest : request.frequencias()) {
            MatriculaEntity matricula = Optional.ofNullable(matriculasPorAluno.get(frequenciaRequest.idAluno()))
                    .orElseThrow(() -> new DiarioClasseLancamentoInvalidoException("Aluno nao pertence ao diario de classe."));
            frequenciaAlunoJpaRepository
                    .findByAula_IdAndAula_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_IdAndMatricula_Id(
                            aula.getId(),
                            escolaId(),
                            matricula.getId())
                    .ifPresent(frequencia -> {
                        throw new DiarioClasseLancamentoDuplicadoException();
                    });

            FrequenciaAlunoEntity frequencia = FrequenciaAlunoEntity.builder()
                    .aula(aula)
                    .matricula(matricula)
                    .situacaoFrequencia(findSituacaoFrequencia(frequenciaRequest.status()))
                    .createdAt(LocalDateTime.now())
                    .build();
            frequenciaAlunoJpaRepository.save(frequencia);
        }

        return new DiarioClasseSalvarResponse(
                idDiarioClasse,
                "SALVO",
                "Diario de Classe salvo com sucesso.",
                LocalDateTime.now(),
                Boolean.TRUE);
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

    private void validarFrequenciasObrigatorias(
            List<DiarioClasseFrequenciaRequest> frequencias,
            Set<UUID> alunosEsperados,
            LocalDate dataLancamento) {
        Set<UUID> alunosInformados = new HashSet<>();
        for (DiarioClasseFrequenciaRequest frequencia : frequencias) {
            if (!dataLancamento.equals(frequencia.data())) {
                throw new DiarioClasseLancamentoInvalidoException("Frequencia com data diferente do lancamento.");
            }
            if (frequencia.dia() != null && frequencia.dia() != dataLancamento.getDayOfMonth()) {
                throw new DiarioClasseLancamentoInvalidoException("Frequencia com dia diferente da data de lancamento.");
            }
            if (!alunosEsperados.contains(frequencia.idAluno())) {
                throw new DiarioClasseLancamentoInvalidoException("Aluno nao pertence ao diario de classe.");
            }
            if (!alunosInformados.add(frequencia.idAluno())) {
                throw new DiarioClasseLancamentoInvalidoException("Aluno informado mais de uma vez no lancamento.");
            }
            mapearSituacaoBanco(frequencia.status());
        }
        if (!alunosInformados.equals(alunosEsperados)) {
            throw new DiarioClasseLancamentoInvalidoException("Todos os alunos ativos devem ter frequencia informada.");
        }
    }

    private AulaEntity criarAulaDiario(
            ProfessorTurmaDisciplinaEntity alocacao,
            LocalDate dataLancamento,
            DiarioClasseSalvarRequest request) {
        AulaEntity aula = AulaEntity.builder()
                .professorTurmaDisciplina(alocacao)
                .dataAula(dataLancamento)
                .conteudoMinistrado(conteudoMinistrado(request))
                .observacao(observacaoLancamento(request))
                .realizada(Boolean.TRUE)
                .createdAt(LocalDateTime.now())
                .build();
        return aulaJpaRepository.save(aula);
    }

    private SituacaoFrequenciaEntity findSituacaoFrequencia(String statusDiario) {
        return situacaoFrequenciaJpaRepository.findByCodigo(mapearSituacaoBanco(statusDiario))
                .orElseThrow(SituacaoFrequenciaNaoEncontradaException::new);
    }

    private String mapearSituacaoBanco(String statusDiario) {
        return switch (statusDiario) {
            case "P" -> "PRESENTE";
            case "." -> "FALTA_JUSTIFICADA";
            case "F" -> "FALTA";
            default -> throw new DiarioClasseLancamentoInvalidoException("Frequencia aceita apenas P, . ou F.");
        };
    }

    private String conteudoMinistrado(DiarioClasseSalvarRequest request) {
        if (request.conteudos() == null || request.conteudos().isEmpty()) {
            return null;
        }
        return request.conteudos().stream()
                .map(conteudo -> conteudo.descricao())
                .filter(descricao -> descricao != null && !descricao.isBlank())
                .findFirst()
                .orElse(null);
    }

    private String observacaoLancamento(DiarioClasseSalvarRequest request) {
        if (request.observacoes() == null || request.observacoes().isEmpty()) {
            return null;
        }
        return request.observacoes().stream()
                .filter(observacao -> observacao != null && !observacao.isBlank())
                .findFirst()
                .orElse(null);
    }

    private DiarioClasseId parseIdDiarioClasse(String idDiarioClasse) {
        if (idDiarioClasse == null || !idDiarioClasse.startsWith("diario-") || idDiarioClasse.length() <= 15) {
            throw new DiarioClasseLancamentoInvalidoException("Identificador do diario de classe invalido.");
        }
        try {
            Integer anoLetivo = Integer.valueOf(idDiarioClasse.substring(7, 11));
            Integer mes = Integer.valueOf(idDiarioClasse.substring(12, 14));
            UUID alocacaoId = UUID.fromString(idDiarioClasse.substring(15));
            return new DiarioClasseId(anoLetivo, mes, alocacaoId);
        } catch (IllegalArgumentException | IndexOutOfBoundsException ex) {
            throw new DiarioClasseLancamentoInvalidoException("Identificador do diario de classe invalido.");
        }
    }

    private UUID escolaId() {
        return escolaContextoPort.obterContextoPadrao().escolaId();
    }

    private record DiarioClasseId(Integer anoLetivo, Integer mes, UUID professorTurmaDisciplinaId) {
    }
}
