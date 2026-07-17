package br.com.escola.professorservice.infra.migration;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

record MigracaoOrigemConnection(
        JdbcTemplate jdbc,
        TransactionTemplate transaction) {
}

