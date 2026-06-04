package br.com.escola.historico.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalogo.adapter.out.persistence.entity.DisciplinaEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.DisciplinaJpaRepository;
import br.com.escola.avaliacao.adapter.out.persistence.entity.NotaAlunoEntity;
import br.com.escola.avaliacao.adapter.out.persistence.repository.NotaAlunoJpaRepository;
import br.com.escola.frequencia.adapter.out.persistence.entity.FrequenciaAlunoEntity;
import br.com.escola.frequencia.adapter.out.persistence.repository.FrequenciaAlunoJpaRepository;
import br.com.escola.historico.adapter.in.web.dto.BoletimFechamentoRequest;
import br.com.escola.historico.adapter.in.web.dto.BoletimIndicadoresResponse;
import br.com.escola.historico.adapter.in.web.dto.BoletimItemResponse;
import br.com.escola.historico.adapter.in.web.dto.BoletimResponse;
import br.com.escola.historico.adapter.out.persistence.entity.BoletimEntity;
import br.com.escola.historico.adapter.out.persistence.entity.BoletimItemEntity;
import br.com.escola.historico.adapter.out.persistence.repository.BoletimItemJpaRepository;
import br.com.escola.historico.adapter.out.persistence.repository.BoletimJpaRepository;
import br.com.escola.historico.domain.exception.BoletimFechamentoDuplicadoException;
import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.matricula.domain.exception.MatriculaNaoEncontradaException;

@Service
public class BoletimService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    private static final BigDecimal MEDIA_MINIMA = BigDecimal.valueOf(6);
    private static final BigDecimal FREQUENCIA_MINIMA = BigDecimal.valueOf(75);

    private final MatriculaJpaRepository matriculaJpaRepository;
    private final NotaAlunoJpaRepository notaAlunoJpaRepository;
    private final FrequenciaAlunoJpaRepository frequenciaAlunoJpaRepository;
    private final DisciplinaJpaRepository disciplinaJpaRepository;
    private final BoletimJpaRepository boletimJpaRepository;
    private final BoletimItemJpaRepository boletimItemJpaRepository;

    public BoletimService(
            MatriculaJpaRepository matriculaJpaRepository,
            NotaAlunoJpaRepository notaAlunoJpaRepository,
            FrequenciaAlunoJpaRepository frequenciaAlunoJpaRepository,
            DisciplinaJpaRepository disciplinaJpaRepository,
            BoletimJpaRepository boletimJpaRepository,
            BoletimItemJpaRepository boletimItemJpaRepository) {
        this.matriculaJpaRepository = matriculaJpaRepository;
        this.notaAlunoJpaRepository = notaAlunoJpaRepository;
        this.frequenciaAlunoJpaRepository = frequenciaAlunoJpaRepository;
        this.disciplinaJpaRepository = disciplinaJpaRepository;
        this.boletimJpaRepository = boletimJpaRepository;
        this.boletimItemJpaRepository = boletimItemJpaRepository;
    }

    @Transactional(readOnly = true)
    public BoletimResponse consultarPorMatricula(UUID matriculaId) {
        MatriculaEntity matricula = matriculaJpaRepository.findById(matriculaId)
                .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaId));
        return gerarBoletimCalculado(matricula);
    }

    @Transactional
    public BoletimResponse fecharBoletim(UUID matriculaId, BoletimFechamentoRequest request) {
        MatriculaEntity matricula = matriculaJpaRepository.findById(matriculaId)
                .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaId));
        String periodoReferencia = request.periodoReferencia().trim();
        BoletimResponse calculado = gerarBoletimCalculado(matricula);

        BoletimEntity boletim = boletimJpaRepository
                .findByMatriculaIdAndPeriodoReferencia(matriculaId, periodoReferencia)
                .map(existente -> prepararBoletimExistente(existente, request))
                .orElseGet(() -> novoBoletim(matricula, request, periodoReferencia));

        boletim = boletimJpaRepository.save(boletim);
        boletimItemJpaRepository.deleteByBoletimId(boletim.getId());
        salvarItens(boletim, calculado.itens());

        return toBoletimPersistidoResponse(matricula, boletim, calculado.itens());
    }

    @Transactional(readOnly = true)
    public List<BoletimResponse> listarFechamentos(UUID matriculaId) {
        MatriculaEntity matricula = matriculaJpaRepository.findById(matriculaId)
                .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaId));

        return boletimJpaRepository.findByMatriculaId(matriculaId).stream()
                .sorted(Comparator.comparing(BoletimEntity::getDataFechamento, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(BoletimEntity::getPeriodoReferencia))
                .map(boletim -> toBoletimPersistidoResponse(
                        matricula,
                        boletim,
                        boletimItemJpaRepository.findByBoletimId(boletim.getId()).stream()
                                .sorted(Comparator.comparing(item -> item.getDisciplina().getNome()))
                                .map(this::toItemResponse)
                                .toList()))
                .toList();
    }

    private BoletimResponse gerarBoletimCalculado(MatriculaEntity matricula) {
        UUID matriculaId = matricula.getId();
        Map<UUID, DisciplinaBoletim> disciplinas = new LinkedHashMap<>();

        notaAlunoJpaRepository.findByMatriculaId(matriculaId)
                .forEach(nota -> disciplinas.computeIfAbsent(disciplinaId(nota), id -> fromNota(nota)).notas.add(nota));
        frequenciaAlunoJpaRepository.findByMatriculaId(matriculaId)
                .forEach(frequencia -> disciplinas.computeIfAbsent(disciplinaId(frequencia), id -> fromFrequencia(frequencia))
                        .frequencias.add(frequencia));

        List<BoletimItemResponse> itens = disciplinas.values().stream()
                .sorted(Comparator.comparing(DisciplinaBoletim::nome))
                .map(this::toItemResponse)
                .toList();

        return new BoletimResponse(
                null,
                matricula.getId(),
                matricula.getAluno().getId(),
                matricula.getAluno().getPessoa().getNomeCompleto(),
                matricula.getTurma().getId(),
                matricula.getTurma().getNome(),
                matricula.getPeriodoLetivo().getId(),
                matricula.getPeriodoLetivo().getNome(),
                LocalDate.now(),
                null,
                null,
                null,
                false,
                toIndicadores(itens),
                itens);
    }

    private BoletimEntity prepararBoletimExistente(BoletimEntity boletim, BoletimFechamentoRequest request) {
        if (!Boolean.TRUE.equals(request.sobrescrever())) {
            throw new BoletimFechamentoDuplicadoException(boletim.getMatricula().getId(), request.periodoReferencia().trim());
        }
        boletim.setDataFechamento(dataFechamento(request));
        boletim.setObservacao(request.observacao());
        return boletim;
    }

    private BoletimEntity novoBoletim(MatriculaEntity matricula, BoletimFechamentoRequest request, String periodoReferencia) {
        return BoletimEntity.builder()
                .matricula(matricula)
                .periodoReferencia(periodoReferencia)
                .dataFechamento(dataFechamento(request))
                .observacao(request.observacao())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private LocalDate dataFechamento(BoletimFechamentoRequest request) {
        return request.dataFechamento() == null ? LocalDate.now() : request.dataFechamento();
    }

    private void salvarItens(BoletimEntity boletim, List<BoletimItemResponse> itens) {
        List<BoletimItemEntity> entities = itens.stream()
                .map(item -> {
                    DisciplinaEntity disciplina = disciplina(item.disciplinaId());
                    return BoletimItemEntity.builder()
                            .boletim(boletim)
                            .disciplina(disciplina)
                            .media(item.media())
                            .frequenciaPercentual(item.frequenciaPercentual())
                            .resultado(item.resultado())
                            .cargaHoraria(disciplina.getCargaHoraria())
                            .observacao("Fechamento gerado automaticamente")
                            .build();
                })
                .toList();
        boletimItemJpaRepository.saveAll(entities);
    }

    private DisciplinaEntity disciplina(UUID disciplinaId) {
        return disciplinaJpaRepository.findById(disciplinaId)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina não encontrada: " + disciplinaId));
    }

    private BoletimResponse toBoletimPersistidoResponse(
            MatriculaEntity matricula,
            BoletimEntity boletim,
            List<BoletimItemResponse> itens) {
        return new BoletimResponse(
                boletim.getId(),
                matricula.getId(),
                matricula.getAluno().getId(),
                matricula.getAluno().getPessoa().getNomeCompleto(),
                matricula.getTurma().getId(),
                matricula.getTurma().getNome(),
                matricula.getPeriodoLetivo().getId(),
                matricula.getPeriodoLetivo().getNome(),
                LocalDate.now(),
                boletim.getPeriodoReferencia(),
                boletim.getDataFechamento(),
                boletim.getObservacao(),
                true,
                toIndicadores(itens),
                itens);
    }

    private UUID disciplinaId(NotaAlunoEntity nota) {
        return nota.getAvaliacao().getProfessorTurmaDisciplina().getTurmaDisciplina().getDisciplina().getId();
    }

    private UUID disciplinaId(FrequenciaAlunoEntity frequencia) {
        return frequencia.getAula().getProfessorTurmaDisciplina().getTurmaDisciplina().getDisciplina().getId();
    }

    private DisciplinaBoletim fromNota(NotaAlunoEntity nota) {
        return new DisciplinaBoletim(
                disciplinaId(nota),
                nota.getAvaliacao().getProfessorTurmaDisciplina().getTurmaDisciplina().getDisciplina().getNome());
    }

    private DisciplinaBoletim fromFrequencia(FrequenciaAlunoEntity frequencia) {
        return new DisciplinaBoletim(
                disciplinaId(frequencia),
                frequencia.getAula().getProfessorTurmaDisciplina().getTurmaDisciplina().getDisciplina().getNome());
    }

    private BoletimItemResponse toItemResponse(DisciplinaBoletim disciplina) {
        BigDecimal media = media(disciplina.notas);
        BigDecimal frequenciaPercentual = frequenciaPercentual(disciplina.frequencias);
        return new BoletimItemResponse(
                disciplina.id,
                disciplina.nome,
                media,
                frequenciaPercentual,
                disciplina.notas.size(),
                disciplina.frequencias.size(),
                resultado(media, frequenciaPercentual, disciplina.notas.size(), disciplina.frequencias.size()));
    }

    private BoletimItemResponse toItemResponse(BoletimItemEntity item) {
        return new BoletimItemResponse(
                item.getDisciplina().getId(),
                item.getDisciplina().getNome(),
                zeroIfNull(item.getMedia()),
                zeroIfNull(item.getFrequenciaPercentual()),
                0,
                0,
                item.getResultado());
    }

    private BigDecimal zeroIfNull(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private BoletimIndicadoresResponse toIndicadores(List<BoletimItemResponse> itens) {
        if (itens.isEmpty()) {
            return new BoletimIndicadoresResponse(0, BigDecimal.ZERO, BigDecimal.ZERO, "PENDENTE");
        }

        BigDecimal mediaGeral = itens.stream()
                .map(BoletimItemResponse::media)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(itens.size()), 2, RoundingMode.HALF_UP);
        BigDecimal frequenciaGeral = itens.stream()
                .map(BoletimItemResponse::frequenciaPercentual)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(itens.size()), 2, RoundingMode.HALF_UP);
        String resultadoGeral = itens.stream().anyMatch(item -> "PENDENTE".equals(item.resultado()))
                ? "PENDENTE"
                : resultado(mediaGeral, frequenciaGeral, itens.size(), itens.size());

        return new BoletimIndicadoresResponse(itens.size(), mediaGeral, frequenciaGeral, resultadoGeral);
    }

    private BigDecimal media(List<NotaAlunoEntity> notas) {
        if (notas.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return notas.stream()
                .map(NotaAlunoEntity::getNota)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(notas.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal frequenciaPercentual(List<FrequenciaAlunoEntity> frequencias) {
        if (frequencias.isEmpty()) {
            return BigDecimal.ZERO;
        }
        long presencas = frequencias.stream()
                .filter(frequencia -> "PRESENTE".equals(frequencia.getSituacaoFrequencia().getCodigo()))
                .count();
        return BigDecimal.valueOf(presencas)
                .multiply(CEM)
                .divide(BigDecimal.valueOf(frequencias.size()), 2, RoundingMode.HALF_UP);
    }

    private String resultado(BigDecimal media, BigDecimal frequenciaPercentual, long totalNotas, long totalFrequencias) {
        if (totalNotas == 0 || totalFrequencias == 0) {
            return "PENDENTE";
        }
        if (media.compareTo(MEDIA_MINIMA) >= 0 && frequenciaPercentual.compareTo(FREQUENCIA_MINIMA) >= 0) {
            return "APROVADO";
        }
        return "REPROVADO";
    }

    private static final class DisciplinaBoletim {

        private final UUID id;
        private final String nome;
        private final List<NotaAlunoEntity> notas = new java.util.ArrayList<>();
        private final List<FrequenciaAlunoEntity> frequencias = new java.util.ArrayList<>();

        private DisciplinaBoletim(UUID id, String nome) {
            this.id = id;
            this.nome = nome;
        }

        private String nome() {
            return nome;
        }
    }
}
