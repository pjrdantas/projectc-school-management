package br.com.escola.responsiblesservice.application.state;

import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class LeituraModeloSyncState {

    private static final String RESPONSAVEL_TABLE = "responsavel";
    private static final Set<String> LINK_ROUTE_TABLES = Set.of("responsavel", "parentesco", "aluno_responsavel");

    private volatile LeituraModeloSyncSummary lastSummary;

    public void update(LeituraModeloSyncSummary summary) {
        this.lastSummary = summary;
    }

    public LeituraModeloSyncSummary lastSummary() {
        return lastSummary;
    }

    public boolean isCatalogRouteReady() {
        return isTableReady(RESPONSAVEL_TABLE);
    }

    public boolean isLinkRouteReady() {
        return isTablesReady(LINK_ROUTE_TABLES);
    }

    private boolean isTablesReady(Set<String> tables) {
        if (lastSummary == null) {
            return false;
        }
        return lastSummary.tables().stream()
                .filter(report -> tables.contains(report.table()))
                .allMatch(report -> "success".equals(report.status()) && report.divergentRecords() == 0);
    }

    private boolean isTableReady(String table) {
        if (lastSummary == null) {
            return false;
        }
        return lastSummary.tables().stream()
                .filter(report -> table.equals(report.table()))
                .findFirst()
                .map(report -> "success".equals(report.status()) && report.divergentRecords() == 0)
                .orElse(false);
    }
}

