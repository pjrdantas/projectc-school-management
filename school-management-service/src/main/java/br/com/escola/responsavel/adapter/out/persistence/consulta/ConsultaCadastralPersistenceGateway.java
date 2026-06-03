package br.com.escola.responsavel.adapter.out.persistence.consulta;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import br.com.escola.responsavel.application.dto.consulta.AlunoComResponsaveisOutput;
import br.com.escola.responsavel.application.dto.consulta.ConsultaCadastralPageOutput;
import br.com.escola.responsavel.application.dto.consulta.ResponsavelResumoOutput;
import br.com.escola.responsavel.application.port.out.consulta.ConsultaCadastralGateway;

@Component
public class ConsultaCadastralPersistenceGateway implements ConsultaCadastralGateway {

    private static final String FILTERS = """
            FROM aluno a
              LEFT JOIN aluno_responsavel ar ON ar.id_aluno = a.id_aluno
              LEFT JOIN responsavel r ON r.id_responsavel = ar.id_responsavel
            WHERE (:nomeAluno IS NULL OR LOWER(a.nome_completo) LIKE LOWER(:nomeAlunoLike))
              AND (:cpfAluno IS NULL OR a.cpf = :cpfAluno)
              AND (:nomeResponsavel IS NULL OR LOWER(r.nome_completo) LIKE LOWER(:nomeResponsavelLike))
              AND (:cpfResponsavel IS NULL OR r.cpf = :cpfResponsavel)
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ConsultaCadastralPersistenceGateway(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ConsultaCadastralPageOutput consultar(
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size) {

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : Math.min(size, 100);

        MapSqlParameterSource params = buildParams(nomeAluno, cpfAluno, nomeResponsavel, cpfResponsavel)
                .addValue("limit", safeSize)
                .addValue("offset", safePage * safeSize);

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT a.id_aluno) " + FILTERS,
                params,
                Long.class);

        List<UUID> alunoIds = jdbcTemplate.query(
                "SELECT DISTINCT a.id_aluno " + FILTERS + " ORDER BY a.nome_completo LIMIT :limit OFFSET :offset",
                params,
                (rs, rowNum) -> rs.getObject("id_aluno", UUID.class));

        if (alunoIds.isEmpty()) {
            return new ConsultaCadastralPageOutput(List.of(), total == null ? 0 : total, safePage, safeSize);
        }

        MapSqlParameterSource dataParams = new MapSqlParameterSource().addValue("alunoIds", alunoIds);

        String dataSql = """
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
                WHERE a.id_aluno IN (:alunoIds)
                ORDER BY a.nome_completo, r.nome_completo
                """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(dataSql, dataParams);
        List<AlunoComResponsaveisOutput> content = aggregate(rows);

        return new ConsultaCadastralPageOutput(content, total == null ? 0 : total, safePage, safeSize);
    }

    private MapSqlParameterSource buildParams(
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel) {
        String nomeAlunoNorm = normalize(nomeAluno);
        String cpfAlunoNorm = normalize(cpfAluno);
        String nomeRespNorm = normalize(nomeResponsavel);
        String cpfRespNorm = normalize(cpfResponsavel);

        return new MapSqlParameterSource()
                .addValue("nomeAluno", nomeAlunoNorm)
                .addValue("cpfAluno", cpfAlunoNorm)
                .addValue("nomeResponsavel", nomeRespNorm)
                .addValue("cpfResponsavel", cpfRespNorm)
                .addValue("nomeAlunoLike", nomeAlunoNorm == null ? null : "%" + nomeAlunoNorm + "%")
                .addValue("nomeResponsavelLike", nomeRespNorm == null ? null : "%" + nomeRespNorm + "%");
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private List<AlunoComResponsaveisOutput> aggregate(List<Map<String, Object>> rows) {
        Map<UUID, AlunoAggregate> grouped = new LinkedHashMap<>();

        for (Map<String, Object> row : rows) {
            UUID idAluno = (UUID) row.get("id_aluno");
            AlunoAggregate agg = grouped.computeIfAbsent(idAluno, key -> new AlunoAggregate(
                    idAluno,
                    (String) row.get("aluno_nome"),
                    (String) row.get("aluno_cpf"),
                    (String) row.get("aluno_email"),
                    (String) row.get("aluno_telefone"),
                    toLocalDate(row.get("aluno_data_nascimento")),
                    toLocalDateTime(row.get("aluno_created_at"))));

            UUID idResponsavel = (UUID) row.get("id_responsavel");
            if (idResponsavel != null) {
                agg.responsaveis.add(new ResponsavelResumoOutput(
                        idResponsavel,
                        (String) row.get("responsavel_nome"),
                        (String) row.get("responsavel_cpf"),
                        (String) row.get("responsavel_email"),
                        (String) row.get("responsavel_telefone"),
                        toLocalDateTime(row.get("responsavel_created_at"))));
            }
        }

        return grouped.values().stream()
                .map(agg -> new AlunoComResponsaveisOutput(
                        agg.idAluno,
                        agg.nomeCompleto,
                        agg.cpf,
                        agg.email,
                        agg.telefone,
                        agg.dataNascimento,
                        agg.createdAt,
                        agg.responsaveis))
                .toList();
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return null;
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }

    private static class AlunoAggregate {
        private final UUID idAluno;
        private final String nomeCompleto;
        private final String cpf;
        private final String email;
        private final String telefone;
        private final LocalDate dataNascimento;
        private final LocalDateTime createdAt;
        private final List<ResponsavelResumoOutput> responsaveis = new ArrayList<>();

        private AlunoAggregate(
                UUID idAluno,
                String nomeCompleto,
                String cpf,
                String email,
                String telefone,
                LocalDate dataNascimento,
                LocalDateTime createdAt) {
            this.idAluno = idAluno;
            this.nomeCompleto = nomeCompleto;
            this.cpf = cpf;
            this.email = email;
            this.telefone = telefone;
            this.dataNascimento = dataNascimento;
            this.createdAt = createdAt;
        }
    }
}
