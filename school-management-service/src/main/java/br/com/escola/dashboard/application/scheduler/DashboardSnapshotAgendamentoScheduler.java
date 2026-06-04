package br.com.escola.dashboard.application.scheduler;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.escola.dashboard.application.service.DashboardSnapshotGeradorService;
import br.com.escola.professor.adapter.out.persistence.repository.ProfessorJpaRepository;

@Component
public class DashboardSnapshotAgendamentoScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardSnapshotAgendamentoScheduler.class);

    private final DashboardSnapshotGeradorService geradorService;
    private final ProfessorJpaRepository professorJpaRepository;
    private final boolean habilitado;
    private final String publicosConfigurados;

    public DashboardSnapshotAgendamentoScheduler(
            DashboardSnapshotGeradorService geradorService,
            ProfessorJpaRepository professorJpaRepository,
            @Value("${dashboard.snapshots.scheduler.enabled:true}") boolean habilitado,
            @Value("${dashboard.snapshots.scheduler.publicos:ACADEMICO,SECRETARIA,DIRETOR}") String publicosConfigurados) {
        this.geradorService = geradorService;
        this.professorJpaRepository = professorJpaRepository;
        this.habilitado = habilitado;
        this.publicosConfigurados = publicosConfigurados;
    }

    @Scheduled(
            cron = "${dashboard.snapshots.scheduler.cron:0 15 1 * * *}",
            zone = "${dashboard.snapshots.scheduler.zone:America/Sao_Paulo}")
    public void executarAgendado() {
        if (!habilitado) {
            LOGGER.debug("Agendamento de snapshots de dashboard desabilitado");
            return;
        }

        DashboardSnapshotAgendamentoResultado resultado = executarAgora(LocalDate.now());
        if (resultado.erros().isEmpty()) {
            LOGGER.info(
                    "Snapshots de dashboard gerados em {}: publicos={}, professores={}",
                    resultado.referenciaData(),
                    resultado.publicosProcessados(),
                    resultado.professoresProcessados());
            return;
        }

        LOGGER.warn(
                "Snapshots de dashboard gerados parcialmente em {}: publicos={}, professores={}, erros={}",
                resultado.referenciaData(),
                resultado.publicosProcessados(),
                resultado.professoresProcessados(),
                resultado.erros());
    }

    public DashboardSnapshotAgendamentoResultado executarAgora(LocalDate referenciaData) {
        LocalDate dataReferencia = referenciaData == null ? LocalDate.now() : referenciaData;
        List<String> erros = new java.util.ArrayList<>();
        int publicosProcessados = gerarPublicos(dataReferencia, erros);
        int professoresProcessados = gerarProfessores(dataReferencia, erros);

        return new DashboardSnapshotAgendamentoResultado(
                dataReferencia,
                publicosProcessados,
                professoresProcessados,
                List.copyOf(erros));
    }

    private int gerarPublicos(LocalDate referenciaData, List<String> erros) {
        int processados = 0;
        for (String publico : publicos()) {
            try {
                geradorService.gerar(publico, referenciaData);
                processados++;
            } catch (RuntimeException ex) {
                erros.add("Público " + publico + ": " + ex.getMessage());
            }
        }
        return processados;
    }

    private int gerarProfessores(LocalDate referenciaData, List<String> erros) {
        int processados = 0;
        for (var professor : professorJpaRepository.findByAtivoTrueOrderByCreatedAtAsc()) {
            try {
                geradorService.gerarProfessor(professor.getId(), referenciaData);
                processados++;
            } catch (RuntimeException ex) {
                erros.add("Professor " + professor.getId() + ": " + ex.getMessage());
            }
        }
        return processados;
    }

    private List<String> publicos() {
        return Arrays.stream(publicosConfigurados.split(","))
                .map(String::trim)
                .filter(publico -> !publico.isBlank())
                .map(publico -> publico.toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
    }
}
