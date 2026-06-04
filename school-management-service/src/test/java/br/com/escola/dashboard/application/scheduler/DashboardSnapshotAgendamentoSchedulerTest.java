package br.com.escola.dashboard.application.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.escola.dashboard.application.service.DashboardSnapshotGeradorService;
import br.com.escola.professor.adapter.out.persistence.entity.ProfessorEntity;
import br.com.escola.professor.adapter.out.persistence.repository.ProfessorJpaRepository;

@ExtendWith(MockitoExtension.class)
class DashboardSnapshotAgendamentoSchedulerTest {

    @Mock
    private DashboardSnapshotGeradorService geradorService;

    @Mock
    private ProfessorJpaRepository professorJpaRepository;

    @Test
    void deveGerarSnapshotsDosPublicosConfiguradosEDosProfessoresAtivos() {
        LocalDate referenciaData = LocalDate.of(2057, 1, 10);
        UUID professorId = UUID.randomUUID();
        when(professorJpaRepository.findByAtivoTrueOrderByCreatedAtAsc())
                .thenReturn(List.of(professor(professorId)));

        DashboardSnapshotAgendamentoScheduler scheduler = scheduler(true, "academico, secretaria, diretor");

        DashboardSnapshotAgendamentoResultado resultado = scheduler.executarAgora(referenciaData);

        assertThat(resultado.referenciaData()).isEqualTo(referenciaData);
        assertThat(resultado.publicosProcessados()).isEqualTo(3);
        assertThat(resultado.professoresProcessados()).isEqualTo(1);
        assertThat(resultado.erros()).isEmpty();

        verify(geradorService).gerar("ACADEMICO", referenciaData);
        verify(geradorService).gerar("SECRETARIA", referenciaData);
        verify(geradorService).gerar("DIRETOR", referenciaData);
        verify(geradorService).gerarProfessor(professorId, referenciaData);
    }

    @Test
    void deveContinuarGeracaoQuandoUmPublicoFalhar() {
        LocalDate referenciaData = LocalDate.of(2057, 1, 11);
        doThrow(new IllegalStateException("público ausente"))
                .when(geradorService)
                .gerar("ACADEMICO", referenciaData);
        when(professorJpaRepository.findByAtivoTrueOrderByCreatedAtAsc())
                .thenReturn(List.of());

        DashboardSnapshotAgendamentoScheduler scheduler = scheduler(true, "academico, secretaria");

        DashboardSnapshotAgendamentoResultado resultado = scheduler.executarAgora(referenciaData);

        assertThat(resultado.publicosProcessados()).isEqualTo(1);
        assertThat(resultado.professoresProcessados()).isZero();
        assertThat(resultado.erros()).hasSize(1);
        assertThat(resultado.erros().getFirst()).contains("ACADEMICO").contains("público ausente");

        verify(geradorService).gerar("ACADEMICO", referenciaData);
        verify(geradorService).gerar("SECRETARIA", referenciaData);
    }

    @Test
    void naoDeveExecutarAgendamentoQuandoDesabilitado() {
        DashboardSnapshotAgendamentoScheduler scheduler = scheduler(false, "ACADEMICO");

        scheduler.executarAgendado();

        verify(geradorService, never()).gerar(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
        verify(professorJpaRepository, never()).findByAtivoTrueOrderByCreatedAtAsc();
    }

    private DashboardSnapshotAgendamentoScheduler scheduler(boolean habilitado, String publicos) {
        return new DashboardSnapshotAgendamentoScheduler(
                geradorService,
                professorJpaRepository,
                habilitado,
                publicos);
    }

    private ProfessorEntity professor(UUID id) {
        return ProfessorEntity.builder()
                .id(id)
                .ativo(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
