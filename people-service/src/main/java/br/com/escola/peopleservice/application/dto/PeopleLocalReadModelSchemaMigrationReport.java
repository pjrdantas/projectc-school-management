package br.com.escola.peopleservice.application.dto;

import java.util.List;

public record PeopleLocalReadModelSchemaMigrationReport(
        boolean enabled,
        boolean executed,
        boolean success,
        String status,
        String reason,
        List<String> locations,
        List<String> tables,
        int migrationsExecuted) {
}
