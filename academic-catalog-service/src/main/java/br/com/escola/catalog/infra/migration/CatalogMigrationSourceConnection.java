package br.com.escola.catalog.infra.migration;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

record CatalogMigrationSourceConnection(JdbcTemplate jdbc, TransactionTemplate transaction) {
}
