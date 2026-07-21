package br.com.escola.peopleservice.infra.persistence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Copies only the source records required for the student write aggregate. */
public class PeopleWriteBackfillExecutor {

    private final JdbcTemplate source;
    private final JdbcTemplate target;
    private final int batchSize;

    public PeopleWriteBackfillExecutor(JdbcTemplate source, JdbcTemplate target, int batchSize) {
        this.source = source;
        this.target = target;
        this.batchSize = batchSize;
    }

    public PeopleWriteBackfillReport execute() {
        Map<TableSpec, List<Map<String, Object>>> snapshot = new LinkedHashMap<>();
        for (TableSpec table : TableSpec.values()) {
            snapshot.put(table, sourceRows(table));
        }

        Map<String, Integer> copiedRows = new LinkedHashMap<>();
        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(
                Objects.requireNonNull(target.getDataSource())));
        transaction.executeWithoutResult(status -> snapshot.forEach((table, rows) -> {
            rows.forEach(row -> upsert(table, row));
            copiedRows.put(table.name, rows.size());
        }));

        Map<String, Integer> sourceRows = new LinkedHashMap<>();
        Map<String, Integer> reconciledRows = new LinkedHashMap<>();
        boolean reconciled = true;
        for (Map.Entry<TableSpec, List<Map<String, Object>>> entry : snapshot.entrySet()) {
            sourceRows.put(entry.getKey().name, entry.getValue().size());
            int matching = reconcile(entry.getKey(), entry.getValue());
            reconciledRows.put(entry.getKey().name, matching);
            reconciled &= matching == entry.getValue().size();
        }
        return new PeopleWriteBackfillReport(
                Map.copyOf(copiedRows), Map.copyOf(sourceRows), Map.copyOf(reconciledRows), reconciled);
    }

    private List<Map<String, Object>> sourceRows(TableSpec table) {
        List<Map<String, Object>> rows = new ArrayList<>();
        int offset = 0;
        List<Map<String, Object>> batch;
        do {
            batch = source.queryForList(table.sourceQuery + " LIMIT ? OFFSET ?", batchSize, offset);
            rows.addAll(batch);
            offset += batch.size();
        } while (batch.size() == batchSize);
        return rows;
    }

    private void upsert(TableSpec table, Map<String, Object> row) {
        List<String> mutableColumns = table.columns.stream()
                .filter(column -> !column.equals(table.primaryKey))
                .toList();
        Object[] updateArguments = new Object[mutableColumns.size() + 1];
        for (int index = 0; index < mutableColumns.size(); index++) {
            updateArguments[index] = row.get(mutableColumns.get(index));
        }
        updateArguments[mutableColumns.size()] = row.get(table.primaryKey);

        int updated = target.update("UPDATE " + table.name + " SET "
                + String.join(", ", mutableColumns.stream().map(column -> column + " = ?").toList())
                + " WHERE " + table.primaryKey + " = ?", updateArguments);
        if (updated == 0) {
            target.update("INSERT INTO " + table.name + " (" + String.join(", ", table.columns) + ") VALUES ("
                    + String.join(", ", table.columns.stream().map(column -> "?").toList()) + ")",
                    table.columns.stream().map(row::get).toArray());
        }
    }

    private int reconcile(TableSpec table, List<Map<String, Object>> sourceRows) {
        MessageDigest sourceDigest = digest();
        MessageDigest targetDigest = digest();
        int matching = 0;
        for (Map<String, Object> sourceRow : sourceRows) {
            updateDigest(sourceDigest, table, sourceRow);
            try {
                Map<String, Object> targetRow = target.queryForMap(
                        "SELECT " + String.join(", ", table.columns) + " FROM " + table.name
                                + " WHERE " + table.primaryKey + " = ?",
                        sourceRow.get(table.primaryKey));
                updateDigest(targetDigest, table, targetRow);
                if (sameValues(table, sourceRow, targetRow)) {
                    matching++;
                }
            } catch (EmptyResultDataAccessException exception) {
                targetDigest.update("<missing>".getBytes(StandardCharsets.UTF_8));
            }
        }
        return sameDigest(sourceDigest, targetDigest) ? matching : 0;
    }

    private boolean sameValues(TableSpec table, Map<String, Object> left, Map<String, Object> right) {
        return table.columns.stream().allMatch(column -> normalize(left.get(column)).equals(normalize(right.get(column))));
    }

    private MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Algoritmo de reconciliacao indisponivel", exception);
        }
    }

    private void updateDigest(MessageDigest digest, TableSpec table, Map<String, Object> row) {
        table.columns.forEach(column -> {
            digest.update(normalize(row.get(column)).getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
        });
    }

    private boolean sameDigest(MessageDigest sourceDigest, MessageDigest targetDigest) {
        return Base64.getEncoder().encodeToString(sourceDigest.digest())
                .equals(Base64.getEncoder().encodeToString(targetDigest.digest()));
    }

    private String normalize(Object value) {
        return value == null ? "<null>" : value.toString();
    }

    private enum TableSpec {
        TIPO_PESSOA("tipo_pessoa", "id_tipo_pessoa", """
                SELECT id_tipo_pessoa, codigo, descricao, created_at
                FROM tipo_pessoa
                ORDER BY id_tipo_pessoa
                """, "id_tipo_pessoa", "codigo", "descricao", "created_at"),
        TIPO_ENDERECO("tipo_endereco", "id_tipo_endereco", """
                SELECT id_tipo_endereco, codigo, descricao
                FROM tipo_endereco
                ORDER BY id_tipo_endereco
                """, "id_tipo_endereco", "codigo", "descricao"),
        STATUS_ALUNO("status_aluno", "id_status_aluno", """
                SELECT id_status_aluno, codigo, descricao
                FROM status_aluno
                ORDER BY id_status_aluno
                """, "id_status_aluno", "codigo", "descricao"),
        PESSOA("pessoa", "id_pessoa", """
                SELECT p.id_pessoa, p.id_escola, e.nome AS escola_nome, p.nome_completo, p.cpf, p.rg,
                       p.orgao_emissor_rg, p.uf_rg, p.email, p.telefone, p.data_nascimento, p.sexo,
                       p.nome_social, p.nacionalidade, p.naturalidade, p.ativo, p.created_at, p.updated_at
                FROM pessoa p
                JOIN escola e ON e.id_escola = p.id_escola
                WHERE EXISTS (SELECT 1 FROM aluno a WHERE a.id_pessoa = p.id_pessoa)
                ORDER BY p.id_pessoa
                """, "id_pessoa", "id_escola", "escola_nome", "nome_completo", "cpf", "rg",
                "orgao_emissor_rg", "uf_rg", "email", "telefone", "data_nascimento", "sexo",
                "nome_social", "nacionalidade", "naturalidade", "ativo", "created_at", "updated_at"),
        PESSOA_TIPO_PESSOA("pessoa_tipo_pessoa", "id_pessoa_tipo_pessoa", """
                SELECT ptp.id_pessoa_tipo_pessoa, ptp.id_pessoa, ptp.id_tipo_pessoa, ptp.created_at
                FROM pessoa_tipo_pessoa ptp
                WHERE EXISTS (SELECT 1 FROM aluno a WHERE a.id_pessoa = ptp.id_pessoa)
                ORDER BY ptp.id_pessoa_tipo_pessoa
                """, "id_pessoa_tipo_pessoa", "id_pessoa", "id_tipo_pessoa", "created_at"),
        ENDERECO("endereco", "id_endereco", """
                SELECT e.id_endereco, e.cep, e.logradouro, e.numero, e.complemento, e.bairro,
                       e.cidade, e.uf, e.created_at, e.updated_at
                FROM endereco e
                WHERE EXISTS (
                    SELECT 1
                    FROM pessoa_endereco pe
                    JOIN aluno a ON a.id_pessoa = pe.id_pessoa
                    WHERE pe.id_endereco = e.id_endereco
                )
                ORDER BY e.id_endereco
                """, "id_endereco", "cep", "logradouro", "numero", "complemento", "bairro",
                "cidade", "uf", "created_at", "updated_at"),
        PESSOA_ENDERECO("pessoa_endereco", "id_pessoa_endereco", """
                SELECT pe.id_pessoa_endereco, pe.id_pessoa, pe.id_endereco, pe.id_tipo_endereco,
                       pe.principal, pe.created_at
                FROM pessoa_endereco pe
                WHERE EXISTS (SELECT 1 FROM aluno a WHERE a.id_pessoa = pe.id_pessoa)
                ORDER BY pe.id_pessoa_endereco
                """, "id_pessoa_endereco", "id_pessoa", "id_endereco", "id_tipo_endereco",
                "principal", "created_at"),
        ALUNO("aluno", "id_aluno", """
                SELECT a.id_aluno, a.id_pessoa, p.id_escola, a.id_status_aluno, p.nome_completo,
                       p.cpf, p.email, p.telefone, p.data_nascimento, a.ra, a.rm, a.emancipado,
                       a.data_ingresso, a.data_saida, a.motivo_saida, a.ativo, a.created_at,
                       NULL AS updated_at
                FROM aluno a
                JOIN pessoa p ON p.id_pessoa = a.id_pessoa
                ORDER BY a.id_aluno
                """, "id_aluno", "id_pessoa", "id_escola", "id_status_aluno", "nome_completo",
                "cpf", "email", "telefone", "data_nascimento", "ra", "rm", "emancipado",
                "data_ingresso", "data_saida", "motivo_saida", "ativo", "created_at", "updated_at");

        private final String name;
        private final String primaryKey;
        private final String sourceQuery;
        private final List<String> columns;

        TableSpec(String name, String primaryKey, String sourceQuery, String... columns) {
            this.name = name;
            this.primaryKey = primaryKey;
            this.sourceQuery = sourceQuery;
            this.columns = List.of(columns);
        }
    }
}
