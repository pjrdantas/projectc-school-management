package br.com.escola.peopleservice.application.state;

import java.util.List;

public record PeopleReadModelMigrationSummary(
        boolean enabled,
        boolean executed,
        boolean success,
        String status,
        String reason,
        List<String> locations,
        List<String> tables,
        int migrationsExecuted) {
}

