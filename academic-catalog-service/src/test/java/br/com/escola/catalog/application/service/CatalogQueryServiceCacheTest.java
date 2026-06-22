package br.com.escola.catalog.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.escola.catalog.application.cache.CatalogReadSnapshot;
import br.com.escola.catalog.application.context.InternalRequestContext;
import br.com.escola.catalog.application.dto.DisciplinaResponse;
import br.com.escola.catalog.application.port.out.CatalogReadCachePort;
import br.com.escola.catalog.domain.repository.DisciplinaRepository;
import br.com.escola.catalog.domain.repository.NivelEnsinoRepository;
import br.com.escola.catalog.domain.repository.PeriodoLetivoRepository;
import br.com.escola.catalog.domain.repository.SerieRepository;
import br.com.escola.catalog.domain.repository.TurmaDisciplinaRepository;
import br.com.escola.catalog.domain.repository.TurmaRepository;
import br.com.escola.catalog.domain.repository.TurnoRepository;
import br.com.escola.catalog.domain.valueobject.EscolaId;

class CatalogQueryServiceCacheTest {

    @Mock private NivelEnsinoRepository nivelRepository;
    @Mock private PeriodoLetivoRepository periodoRepository;
    @Mock private SerieRepository serieRepository;
    @Mock private TurnoRepository turnoRepository;
    @Mock private DisciplinaRepository disciplinaRepository;
    @Mock private TurmaRepository turmaRepository;
    @Mock private TurmaDisciplinaRepository turmaDisciplinaRepository;
    @Mock private CatalogReadCachePort cachePort;

    private CatalogQueryService service;
    private InternalRequestContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        context = new InternalRequestContext("corr-cache", UUID.randomUUID(), new EscolaId(UUID.randomUUID()));
        service = new CatalogQueryService(
                nivelRepository, periodoRepository, serieRepository, turnoRepository,
                disciplinaRepository, turmaRepository, turmaDisciplinaRepository,
                cachePort, new CatalogCacheSettings(true, Duration.ofMinutes(5)));
    }

    @Test
    void deveUsarSnapshotDaEscolaSemConsultarPostgresql() {
        DisciplinaResponse disciplina = new DisciplinaResponse(
                UUID.randomUUID(), "Matematica", 80, true,
                context.escolaId().value(), LocalDateTime.now());
        when(cachePort.buscar(context.escolaId())).thenReturn(Optional.of(snapshot(List.of(disciplina))));

        assertThat(service.listarDisciplinas(context)).containsExactly(disciplina);
        verify(disciplinaRepository, never()).listarDisciplinas(any());
    }

    @Test
    void deveCarregarPostgresqlEArmazenarSnapshotNoCacheMiss() {
        when(cachePort.buscar(context.escolaId())).thenReturn(Optional.empty());
        when(nivelRepository.listarNiveisEnsino()).thenReturn(List.of());
        when(turnoRepository.listarTurnos()).thenReturn(List.of());
        when(periodoRepository.listarPeriodos(context.escolaId())).thenReturn(List.of());
        when(serieRepository.listarSeries(context.escolaId())).thenReturn(List.of());
        when(turmaRepository.listarTurmas(context.escolaId())).thenReturn(List.of());
        when(disciplinaRepository.listarDisciplinas(context.escolaId())).thenReturn(List.of());

        assertThat(service.listarDisciplinas(context)).isEmpty();
        verify(cachePort).armazenar(any(), any(), any());
        verify(disciplinaRepository).listarDisciplinas(context.escolaId());
    }

    private CatalogReadSnapshot snapshot(List<DisciplinaResponse> disciplinas) {
        return new CatalogReadSnapshot(
                List.of(), List.of(), List.of(), List.of(), List.of(), disciplinas, List.of());
    }
}
