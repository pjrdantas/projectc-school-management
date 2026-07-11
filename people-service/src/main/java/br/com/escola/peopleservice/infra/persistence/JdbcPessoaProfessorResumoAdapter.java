package br.com.escola.peopleservice.infra.persistence;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.escola.peopleservice.application.dto.PessoaProfessorResumoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaProfessorResumoPort;
import br.com.escola.peopleservice.infra.config.PeopleReadModelMigrationProperties;

@Component
public class JdbcPessoaProfessorResumoAdapter implements PessoaProfessorResumoPort {

    private static final String BASE_SELECT = """
            SELECT id_professor,
                   id_pessoa,
                   id_funcionario,
                   id_escola,
                   nome_completo,
                   ativo,
                   created_at
            FROM people_professor_read_model
            WHERE id_escola = ?
            """;

    private final PeopleReadModelMigrationProperties properties;

    public JdbcPessoaProfessorResumoAdapter(PeopleReadModelMigrationProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<PessoaProfessorResumoResponse> buscarProfessorPorId(UUID professorId, UUID escolaId) {
        List<PessoaProfessorResumoResponse> professores = consultar(
                BASE_SELECT + " AND id_professor = ? ORDER BY created_at DESC, id_professor",
                escolaId,
                professorId);
        if (professores.size() > 1) {
            throw new IllegalStateException("professor-local-read-duplicate-id");
        }
        return professores.stream().findFirst();
    }

    @Override
    public List<PessoaProfessorResumoResponse> listarProfessoresPorEscola(UUID escolaId) {
        return consultar(
                BASE_SELECT + " AND ativo = TRUE ORDER BY nome_completo ASC, id_professor",
                escolaId,
                null);
    }

    private List<PessoaProfessorResumoResponse> consultar(String sql, UUID escolaId, UUID professorId) {
        if (!StringUtils.hasText(properties.url())) {
            throw new IllegalStateException("professor-local-read-url-required");
        }
        loadDriver(properties.driverClassName());

        try (var connection = DriverManager.getConnection(
                properties.url(),
                properties.username(),
                properties.password());
                var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, escolaId);
            if (professorId != null) {
                statement.setObject(2, professorId);
            }
            try (var resultSet = statement.executeQuery()) {
                List<PessoaProfessorResumoResponse> professores = new ArrayList<>();
                while (resultSet.next()) {
                    professores.add(new PessoaProfessorResumoResponse(
                            resultSet.getObject("id_professor", UUID.class),
                            resultSet.getObject("id_pessoa", UUID.class),
                            resultSet.getObject("id_funcionario", UUID.class),
                            resultSet.getObject("id_escola", UUID.class),
                            resultSet.getString("nome_completo"),
                            resultSet.getBoolean("ativo")));
                }
                return List.copyOf(professores);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("professor-local-read-failed", ex);
        }
    }

    private void loadDriver(String driverClassName) {
        if (!StringUtils.hasText(driverClassName)) {
            return;
        }
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("professor-local-read-driver-not-found", ex);
        }
    }
}
