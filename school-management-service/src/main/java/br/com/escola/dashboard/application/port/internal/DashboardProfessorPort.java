package br.com.escola.dashboard.application.port.internal;

import java.util.UUID;

import br.com.escola.dashboard.application.dto.internal.DashboardProfessorResumo;

public interface DashboardProfessorPort {

    DashboardProfessorResumo consultarResumo(UUID professorId);
}
