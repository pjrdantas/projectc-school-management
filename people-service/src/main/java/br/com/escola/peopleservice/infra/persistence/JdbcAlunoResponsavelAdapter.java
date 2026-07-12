package br.com.escola.peopleservice.infra.persistence;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.peopleservice.application.dto.PessoaAlunoResponsaveisResponse;
import br.com.escola.peopleservice.application.dto.PessoaConsultaCadastralPageResponse;
import br.com.escola.peopleservice.application.dto.PessoaResponsavelResumoResponse;
import br.com.escola.peopleservice.application.port.out.AlunoResponsavelPort;
import br.com.escola.peopleservice.infra.config.PeopleReadModelMigrationProperties;

@Component
public class JdbcAlunoResponsavelAdapter implements AlunoResponsavelPort {

    private static final String FILTERS = """
            FROM aluno a
              LEFT JOIN aluno_responsavel ar ON ar.id_aluno = a.id_aluno
              LEFT JOIN responsavel r ON r.id_responsavel = ar.id_responsavel
            WHERE (? IS NULL OR LOWER(a.nome_completo) LIKE LOWER(?))
              AND (? IS NULL OR a.cpf = ?)
              AND (? IS NULL OR LOWER(r.nome_completo) LIKE LOWER(?))
              AND (? IS NULL OR r.cpf = ?)
            """;

    private final PeopleReadModelMigrationProperties properties;

    public JdbcAlunoResponsavelAdapter(PeopleReadModelMigrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public PessoaConsultaCadastralPageResponse consultarCadastro(
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size) {
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("student-responsible-local-read-url-required");
        }
        loadDriver(properties.driverClassName());

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : Math.min(size, 100);
        FilterValues filters = FilterValues.from(nomeAluno, cpfAluno, nomeResponsavel, cpfResponsavel);

        try (var connection = DriverManager.getConnection(
                properties.url(),
                properties.username(),
                properties.password())) {
            long total = count(connection, filters);
            List<UUID> alunoIds = findAlunoIds(connection, filters, safePage, safeSize);
            if (alunoIds.isEmpty()) {
                return new PessoaConsultaCadastralPageResponse(List.of(), total, safePage, safeSize);
            }
            return new PessoaConsultaCadastralPageResponse(readAggregates(connection, alunoIds), total, safePage, safeSize);
        } catch (SQLException ex) {
            throw new IllegalStateException("student-responsible-local-read-failed", ex);
        }
    }

    private long count(java.sql.Connection connection, FilterValues filters) throws SQLException {
        try (var statement = connection.prepareStatement("SELECT COUNT(DISTINCT a.id_aluno) " + FILTERS)) {
            bindFilters(statement, filters);
            try (var resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private List<UUID> findAlunoIds(
            java.sql.Connection connection,
            FilterValues filters,
            int safePage,
            int safeSize) throws SQLException {
        String sql = "SELECT DISTINCT a.id_aluno, a.nome_completo " + FILTERS
                + " ORDER BY a.nome_completo LIMIT ? OFFSET ?";
        try (var statement = connection.prepareStatement(sql)) {
            bindFilters(statement, filters);
            statement.setInt(9, safeSize);
            statement.setInt(10, safePage * safeSize);
            try (var resultSet = statement.executeQuery()) {
                List<UUID> ids = new ArrayList<>();
                while (resultSet.next()) {
                    ids.add(resultSet.getObject("id_aluno", UUID.class));
                }
                return ids;
            }
        }
    }

    private List<PessoaAlunoResponsaveisResponse> readAggregates(
            java.sql.Connection connection,
            List<UUID> alunoIds) throws SQLException {
        String placeholders = String.join(",", java.util.Collections.nCopies(alunoIds.size(), "?"));
        String sql = """
                SELECT a.id_aluno,
                       a.nome_completo AS aluno_nome,
                       a.cpf AS aluno_cpf,
                       a.email AS aluno_email,
                       a.telefone AS aluno_telefone,
                       a.data_nascimento AS aluno_data_nascimento,
                       a.created_at AS aluno_created_at,
                       r.id_responsavel,
                       r.nome_completo AS responsavel_nome,
                       r.cpf AS responsavel_cpf,
                       r.email AS responsavel_email,
                       r.telefone AS responsavel_telefone,
                       r.created_at AS responsavel_created_at
                FROM aluno a
                  LEFT JOIN aluno_responsavel ar ON ar.id_aluno = a.id_aluno
                  LEFT JOIN responsavel r ON r.id_responsavel = ar.id_responsavel
                WHERE a.id_aluno IN (%s)
                ORDER BY a.nome_completo, r.nome_completo
                """.formatted(placeholders);

        try (var statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < alunoIds.size(); i++) {
                statement.setObject(i + 1, alunoIds.get(i));
            }
            try (var resultSet = statement.executeQuery()) {
                Map<UUID, AlunoAggregate> grouped = new LinkedHashMap<>();
                while (resultSet.next()) {
                    UUID alunoId = resultSet.getObject("id_aluno", UUID.class);
                    AlunoAggregate aggregate = grouped.computeIfAbsent(alunoId, id -> new AlunoAggregate(
                            id,
                            getString(resultSet, "aluno_nome"),
                            getString(resultSet, "aluno_cpf"),
                            getString(resultSet, "aluno_email"),
                            getString(resultSet, "aluno_telefone"),
                            getLocalDate(resultSet, "aluno_data_nascimento"),
                            getLocalDateTime(resultSet, "aluno_created_at")));

                    UUID responsavelId = resultSet.getObject("id_responsavel", UUID.class);
                    if (responsavelId != null) {
                        aggregate.responsaveis().add(new PessoaResponsavelResumoResponse(
                                responsavelId,
                                getString(resultSet, "responsavel_nome"),
                                getString(resultSet, "responsavel_cpf"),
                                getString(resultSet, "responsavel_email"),
                                getString(resultSet, "responsavel_telefone"),
                                getLocalDateTime(resultSet, "responsavel_created_at")));
                    }
                }
                return grouped.values().stream()
                        .map(AlunoAggregate::toResponse)
                        .toList();
            }
        }
    }

    private void bindFilters(java.sql.PreparedStatement statement, FilterValues filters) throws SQLException {
        statement.setString(1, filters.nomeAluno());
        statement.setString(2, filters.nomeAlunoLike());
        statement.setString(3, filters.cpfAluno());
        statement.setString(4, filters.cpfAluno());
        statement.setString(5, filters.nomeResponsavel());
        statement.setString(6, filters.nomeResponsavelLike());
        statement.setString(7, filters.cpfResponsavel());
        statement.setString(8, filters.cpfResponsavel());
    }

    private String getString(java.sql.ResultSet resultSet, String column) {
        try {
            return resultSet.getString(column);
        } catch (SQLException ex) {
            throw new IllegalStateException("student-responsible-local-read-mapping-failed", ex);
        }
    }

    private LocalDate getLocalDate(java.sql.ResultSet resultSet, String column) {
        try {
            var date = resultSet.getDate(column);
            return date == null ? null : date.toLocalDate();
        } catch (SQLException ex) {
            throw new IllegalStateException("student-responsible-local-read-mapping-failed", ex);
        }
    }

    private LocalDateTime getLocalDateTime(java.sql.ResultSet resultSet, String column) {
        try {
            Timestamp timestamp = resultSet.getTimestamp(column);
            return timestamp == null ? null : timestamp.toLocalDateTime();
        } catch (SQLException ex) {
            throw new IllegalStateException("student-responsible-local-read-mapping-failed", ex);
        }
    }

    private void loadDriver(String driverClassName) {
        if (!StringUtils.hasText(driverClassName)) {
            return;
        }
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("student-responsible-local-read-driver-not-found", ex);
        }
    }

    private record FilterValues(
            String nomeAluno,
            String nomeAlunoLike,
            String cpfAluno,
            String nomeResponsavel,
            String nomeResponsavelLike,
            String cpfResponsavel) {

        static FilterValues from(String nomeAluno, String cpfAluno, String nomeResponsavel, String cpfResponsavel) {
            String normalizedNomeAluno = normalize(nomeAluno);
            String normalizedNomeResponsavel = normalize(nomeResponsavel);
            return new FilterValues(
                    normalizedNomeAluno,
                    normalizedNomeAluno == null ? null : "%" + normalizedNomeAluno + "%",
                    normalize(cpfAluno),
                    normalizedNomeResponsavel,
                    normalizedNomeResponsavel == null ? null : "%" + normalizedNomeResponsavel + "%",
                    normalize(cpfResponsavel));
        }

        private static String normalize(String value) {
            return value == null || value.isBlank() ? null : value.trim();
        }
    }

    private record AlunoAggregate(
            UUID id,
            String nomeCompleto,
            String cpf,
            String email,
            String telefone,
            LocalDate dataNascimento,
            LocalDateTime createdAt,
            List<PessoaResponsavelResumoResponse> responsaveis) {

        AlunoAggregate(
                UUID id,
                String nomeCompleto,
                String cpf,
                String email,
                String telefone,
                LocalDate dataNascimento,
                LocalDateTime createdAt) {
            this(id, nomeCompleto, cpf, email, telefone, dataNascimento, createdAt, new ArrayList<>());
        }

        PessoaAlunoResponsaveisResponse toResponse() {
            return new PessoaAlunoResponsaveisResponse(
                    id,
                    nomeCompleto,
                    cpf,
                    email,
                    telefone,
                    dataNascimento,
                    createdAt,
                    List.copyOf(responsaveis));
        }
    }
}

