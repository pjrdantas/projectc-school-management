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
import br.com.escola.catalogo.application.dto.internal.DisciplinaBoletimResumo;
import br.com.escola.catalogo.application.port.internal.DisciplinaBoletimPort;
import br.com.escola.catalogo.domain.exception.DisciplinaNaoEncontradaException;
import br.com.escola.historico.adapter.in.web.dto.BoletimFechamentoRequest;
import br.com.escola.historico.adapter.in.web.dto.BoletimIndicadoresResponse;
import br.com.escola.historico.adapter.in.web.dto.BoletimItemResponse;
import br.com.escola.historico.adapter.in.web.dto.BoletimResponse;
import br.com.escola.historico.adapter.out.persistence.entity.BoletimEntity;
import br.com.escola.historico.adapter.out.persistence.entity.BoletimItemEntity;
import br.com.escola.historico.adapter.out.persistence.repository.BoletimItemJpaRepository;
import br.com.escola.historico.adapter.out.persistence.repository.BoletimJpaRepository;
import br.com.escola.historico.application.dto.internal.FrequenciaAcademicaResumo;
import br.com.escola.historico.application.dto.internal.NotaAcademicaResumo;
import br.com.escola.historico.application.port.internal.RendimentoAcademicoPort;
import br.com.escola.historico.domain.exception.BoletimFechamentoDuplicadoException;
import br.com.escola.institucional.application.port.EscolaContextoPort;
import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEntity;
import br.com.escola.matricula.application.dto.internal.MatriculaBoletimResumo;
import br.com.escola.matricula.application.port.internal.MatriculaBoletimPort;
import br.com.escola.matricula.domain.exception.MatriculaNaoEncontradaException;
import jakarta.persistence.EntityManager;

@Service
public class BoletimService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    private static final BigDecimal MEDIA_MINIMA = BigDecimal.valueOf(6);
    private static final BigDecimal FREQUENCIA_MINIMA = BigDecimal.valueOf(75);

    private final MatriculaBoletimPort matriculaBoletimPort;
    private final RendimentoAcademicoPort rendimentoAcademicoPort;
    private final DisciplinaBoletimPort disciplinaBoletimPort;
    private final BoletimJpaRepository boletimJpaRepository;
    private final BoletimItemJpaRepository boletimItemJpaRepository;
    private final EscolaContextoPort escolaContextoPort;
    private final EntityManager entityManager;

    public BoletimService(
            MatriculaBoletimPort matriculaBoletimPort,
            RendimentoAcademicoPort rendimentoAcademicoPort,
            DisciplinaBoletimPort disciplinaBoletimPort,
            BoletimJpaRepository boletimJpaRepository,
            BoletimItemJpaRepository boletimItemJpaRepository,
            EscolaContextoPort escolaContextoPort,
            EntityManager entityManager) {
        this.matriculaBoletimPort = matriculaBoletimPort;
        this.rendimentoAcademicoPort = rendimentoAcademicoPort;
        this.disciplinaBoletimPort = disciplinaBoletimPort;
        this.boletimJpaRepository = boletimJpaRepository;
        this.boletimItemJpaRepository = boletimItemJpaRepository;
        this.escolaContextoPort = escolaContextoPort;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public BoletimResponse consultarPorMatricula(UUID matriculaId) {
        UUID escolaId = escolaId();
        MatriculaBoletimResumo matricula = matriculaBoletimPort.buscarResumoPorIdEEscola(matriculaId, escolaId)
                .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaId));
        return gerarBoletimCalculado(matricula, escolaId);
    }

    @Transactional
    public BoletimResponse fecharBoletim(UUID matriculaId, BoletimFechamentoRequest request) {
        UUID escolaId = escolaId();
        MatriculaBoletimResumo matricula = matriculaBoletimPort.buscarResumoPorIdEEscola(matriculaId, escolaId)
                .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaId));
        String periodoReferencia = request.periodoReferencia().trim();
        BoletimResponse calculado = gerarBoletimCalculado(matricula, escolaId);

        BoletimEntity boletim = boletimJpaRepository
                .findByMatricula_IdAndMatricula_Turma_Escola_IdAndPeriodoReferencia(matriculaId, escolaId, periodoReferencia)
                .map(existente -> prepararBoletimExistente(existente, request))
                .orElseGet(() -> novoBoletim(matriculaId, request, periodoReferencia));

        boletim = boletimJpaRepository.save(boletim);
        boletimItemJpaRepository.deleteByBoletimId(boletim.getId());
        salvarItens(boletim, calculado.itens());

        return toBoletimPersistidoResponse(matricula, boletim, calculado.itens());
    }

    @Transactional(readOnly = true)
    public List<BoletimResponse> listarFechamentos(UUID matriculaId) {
        UUID escolaId = escolaId();
        MatriculaBoletimResumo matricula = matriculaBoletimPort.buscarResumoPorIdEEscola(matriculaId, escolaId)
                .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaId));

        return boletimJpaRepository.findByMatricula_IdAndMatricula_Turma_Escola_Id(matriculaId, escolaId).stream()
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

    private BoletimResponse gerarBoletimCalculado(MatriculaBoletimResumo matricula, UUID escolaId) {
        UUID matriculaId = matricula.matriculaId();
        Map<UUID, DisciplinaBoletim> disciplinas = new LinkedHashMap<>();
        var rendimento = rendimentoAcademicoPort.consultarPorMatricula(matriculaId, escolaId);

        rendimento.notas()
                .forEach(nota -> disciplinas.computeIfAbsent(nota.disciplinaId(), id -> fromNota(nota)).notas.add(nota));
        rendimento.frequencias()
                .forEach(frequencia -> disciplinas.computeIfAbsent(
                        frequencia.disciplinaId(),
                        id -> fromFrequencia(frequencia)).frequencias.add(frequencia));

        List<BoletimItemResponse> itens = disciplinas.values().stream()
                .sorted(Comparator.comparing(DisciplinaBoletim::nome))
                .map(this::toItemResponse)
                .toList();

        return new BoletimResponse(
                null,
                matricula.matriculaId(),
                matricula.alunoId(),
                matricula.alunoNome(),
                matricula.turmaId(),
                matricula.turmaNome(),
                matricula.periodoLetivoId(),
                matricula.periodoLetivoNome(),
                matricula.escolaId(),
                matricula.escolaNome(),
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

    private BoletimEntity novoBoletim(UUID matriculaId, BoletimFechamentoRequest request, String periodoReferencia) {
        return BoletimEntity.builder()
                .matricula(entityManager.getReference(MatriculaEntity.class, matriculaId))
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
        UUID escolaId = escolaId();
        List<BoletimItemEntity> entities = itens.stream()
                .map(item -> {
                    DisciplinaBoletimResumo disciplina = disciplina(item.disciplinaId(), escolaId);
                    return BoletimItemEntity.builder()
                            .boletim(boletim)
                            .disciplina(entityManager.getReference(DisciplinaEntity.class, disciplina.disciplinaId()))
                            .media(item.media())
                            .frequenciaPercentual(item.frequenciaPercentual())
                            .resultado(item.resultado())
                            .cargaHoraria(disciplina.cargaHoraria())
                            .observacao("Fechamento gerado automaticamente")
                            .build();
                })
                .toList();
        boletimItemJpaRepository.saveAll(entities);
    }

    private DisciplinaBoletimResumo disciplina(UUID disciplinaId, UUID escolaId) {
        return disciplinaBoletimPort.buscarResumoPorIdEEscola(disciplinaId, escolaId)
                .orElseThrow(() -> new DisciplinaNaoEncontradaException(disciplinaId));
    }

    private BoletimResponse toBoletimPersistidoResponse(
            MatriculaBoletimResumo matricula,
            BoletimEntity boletim,
            List<BoletimItemResponse> itens) {
        return new BoletimResponse(
                boletim.getId(),
                matricula.matriculaId(),
                matricula.alunoId(),
                matricula.alunoNome(),
                matricula.turmaId(),
                matricula.turmaNome(),
                matricula.periodoLetivoId(),
                matricula.periodoLetivoNome(),
                matricula.escolaId(),
                matricula.escolaNome(),
                LocalDate.now(),
                boletim.getPeriodoReferencia(),
                boletim.getDataFechamento(),
                boletim.getObservacao(),
                true,
                toIndicadores(itens),
                itens);
    }

    private UUID escolaId() {
        return escolaContextoPort.obterContextoPadrao().escolaId();
    }

    private DisciplinaBoletim fromNota(NotaAcademicaResumo nota) {
        return new DisciplinaBoletim(
                nota.disciplinaId(),
                nota.disciplinaNome());
    }

    private DisciplinaBoletim fromFrequencia(FrequenciaAcademicaResumo frequencia) {
        return new DisciplinaBoletim(
                frequencia.disciplinaId(),
                frequencia.disciplinaNome());
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

    private BigDecimal media(List<NotaAcademicaResumo> notas) {
        if (notas.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return notas.stream()
                .map(NotaAcademicaResumo::nota)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(notas.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal frequenciaPercentual(List<FrequenciaAcademicaResumo> frequencias) {
        if (frequencias.isEmpty()) {
            return BigDecimal.ZERO;
        }
        long presencas = frequencias.stream()
                .filter(frequencia -> "PRESENTE".equals(frequencia.situacao()))
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
        private final List<NotaAcademicaResumo> notas = new java.util.ArrayList<>();
        private final List<FrequenciaAcademicaResumo> frequencias = new java.util.ArrayList<>();

        private DisciplinaBoletim(UUID id, String nome) {
            this.id = id;
            this.nome = nome;
        }

        private String nome() {
            return nome;
        }
    }
}
