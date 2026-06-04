package br.com.escola.dashboard.application.scheduler;

import java.time.LocalDate;
import java.util.List;

public record DashboardSnapshotAgendamentoResultado(
        LocalDate referenciaData,
        int publicosProcessados,
        int professoresProcessados,
        List<String> erros) {
}
