package br.com.escola.professorservice.infra.migration;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

record ProfessorShadowMigrationSourceConnection(
        JdbcTemplate jdbc,
        TransactionTemplate transaction) {
}
