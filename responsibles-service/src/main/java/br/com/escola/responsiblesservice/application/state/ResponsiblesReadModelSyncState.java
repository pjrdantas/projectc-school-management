package br.com.escola.responsiblesservice.application.state;

import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class ResponsiblesReadModelSyncState {

    private static final Set<String> LINK_ROUTE_TABLES = Set.of("responsavel", "parentesco", "aluno_responsavel");

    private volatile ResponsiblesReadModelSyncSummary lastSummary;

    public void update(ResponsiblesReadModelSyncSummary summary) {
        this.lastSummary = summary;
    }

    public ResponsiblesReadModelSyncSummary lastSummary() {
        return lastSummary;
    }

    public boolean isLinkRouteReady() {
        if (lastSummary == null || !"completed".equals(lastSummary.status()) || lastSummary.divergentRecords() > 0) {
            return false;
        }
        return lastSummary.tables().stream()
                .filter(report -> LINK_ROUTE_TABLES.contains(report.table()))
                .allMatch(report -> "success".equals(report.status()) && report.divergentRecords() == 0);
    }
}
