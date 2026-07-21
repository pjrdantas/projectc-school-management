package br.com.escola.responsiblesservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.DriverManager;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import br.com.escola.responsiblesservice.application.dto.CadastrarResponsavelCommand;
import br.com.escola.responsiblesservice.application.dto.ExclusaoResponsavelResultado;
import br.com.escola.responsiblesservice.application.dto.AtualizarResponsavelCommand;
import br.com.escola.responsiblesservice.application.exception.ResponsavelDuplicadoException;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesPersistenceProperties;

class JdbcResponsavelWriteAdapterTest {

    @Test
    void devePersistirResponsavelNoBancoProprioDaEscola() throws Exception {
        String url = h2Url("responsibles_write_create_" + UUID.randomUUID());
        migrate(url);
        JdbcResponsavelWriteAdapter adapter = adapter(url);
        UUID escolaId = UUID.randomUUID();

        var response = adapter.criar(command("Maria Souza", "12345678901"), escolaId);

        assertThat(response.escolaId()).isEqualTo(escolaId);
        assertThat(response.escolaNome()).isNull();
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.prepareStatement("SELECT ativo, cpf FROM responsavel WHERE id_responsavel = ?")) {
            statement.setObject(1, response.id());
            try (var result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getBoolean("ativo")).isTrue();
                assertThat(result.getString("cpf")).isEqualTo("12345678901");
            }
        }
    }

    @Test
    void deveBloquearCpfDuplicadoNaMesmaEscola() {
        String url = h2Url("responsibles_write_duplicate_" + UUID.randomUUID());
        migrate(url);
        JdbcResponsavelWriteAdapter adapter = adapter(url);
        UUID escolaId = UUID.randomUUID();
        adapter.criar(command("Maria Souza", "12345678901"), escolaId);

        assertThatThrownBy(() -> adapter.criar(command("Maria Oliveira", "12345678901"), escolaId))
                .isInstanceOf(ResponsavelDuplicadoException.class);
    }

    @Test
    void deveAtualizarApenasResponsavelAtivoDaMesmaEscola() {
        String url = h2Url("responsibles_write_update_" + UUID.randomUUID());
        migrate(url);
        JdbcResponsavelWriteAdapter adapter = adapter(url);
        UUID escolaId = UUID.randomUUID();
        var created = adapter.criar(command("Maria Souza", "12345678901"), escolaId);

        var updated = adapter.atualizar(
                created.id(),
                new AtualizarResponsavelCommand(
                        "Maria Oliveira", "10987654321", "oliveira@example.com", null, null, null, null, null,
                        null, null, null, null),
                escolaId);

        assertThat(updated).isPresent();
        assertThat(updated.orElseThrow().nomeCompleto()).isEqualTo("Maria Oliveira");
        assertThat(updated.orElseThrow().cpf()).isEqualTo("10987654321");
        assertThat(adapter.atualizar(created.id(), new AtualizarResponsavelCommand(
                "Outra", "10987654321", null, null, null, null, null, null, null, null, null, null), UUID.randomUUID()))
                .isEmpty();
    }

    @Test
    void deveInativarResponsavelSemVinculoEManterRegistro() throws Exception {
        String url = h2Url("responsibles_write_delete_" + UUID.randomUUID());
        migrate(url);
        JdbcResponsavelWriteAdapter adapter = adapter(url);
        UUID escolaId = UUID.randomUUID();
        var created = adapter.criar(command("Maria Souza", "12345678901"), escolaId);

        assertThat(adapter.excluir(created.id(), escolaId)).isEqualTo(ExclusaoResponsavelResultado.EXCLUIDO);
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.prepareStatement("SELECT ativo, updated_at FROM responsavel WHERE id_responsavel = ?")) {
            statement.setObject(1, created.id());
            try (var result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getBoolean("ativo")).isFalse();
                assertThat(result.getTimestamp("updated_at")).isNotNull();
            }
        }
    }

    @Test
    void deveBloquearExclusaoQuandoResponsavelPossuiAlunoVinculado() throws Exception {
        String url = h2Url("responsibles_write_delete_linked_" + UUID.randomUUID());
        migrate(url);
        JdbcResponsavelWriteAdapter adapter = adapter(url);
        UUID escolaId = UUID.randomUUID();
        var created = adapter.criar(command("Maria Souza", "12345678901"), escolaId);
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.prepareStatement("""
                        INSERT INTO aluno_responsavel (id_aluno_responsavel, id_aluno, id_responsavel)
                        VALUES (?, ?, ?)
                        """)) {
            statement.setObject(1, UUID.randomUUID());
            statement.setObject(2, UUID.randomUUID());
            statement.setObject(3, created.id());
            statement.executeUpdate();
        }

        assertThat(adapter.excluir(created.id(), escolaId))
                .isEqualTo(ExclusaoResponsavelResultado.POSSUI_ALUNO_VINCULADO);
    }

    private JdbcResponsavelWriteAdapter adapter(String url) {
        return new JdbcResponsavelWriteAdapter(
                new ResponsiblesPersistenceProperties(url, "sa", "", "org.h2.Driver"));
    }

    private void migrate(String url) {
        Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/responsibles/migration")
                .load()
                .migrate();
    }

    private String h2Url(String database) {
        return "jdbc:h2:mem:" + database + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    }

    private CadastrarResponsavelCommand command(String nomeCompleto, String cpf) {
        return new CadastrarResponsavelCommand(
                nomeCompleto, cpf, "maria@example.com", "11999999999", null,
                "01001000", "Rua Central", "100", null, "Centro", "Sao Paulo", "SP");
    }
}
