package br.com.escola.responsiblesservice.infra.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import br.com.escola.responsiblesservice.application.dto.CadastrarResponsavelCommand;
import br.com.escola.responsiblesservice.application.dto.CriacaoAlunoResponsavelResultado;
import br.com.escola.responsiblesservice.application.dto.DesvinculoAlunoResponsavelResultado;
import br.com.escola.responsiblesservice.application.dto.VincularAlunoResponsavelCommand;
import br.com.escola.responsiblesservice.infra.config.ResponsiblesPersistenceProperties;

class JdbcAlunoResponsavelWriteAdapterTest {

    @Test
    void deveCriarVinculoComParentescoPadraoECamposBooleanos() throws Exception {
        String url = h2Url("responsibles_student_link_" + UUID.randomUUID());
        migrate(url);
        JdbcResponsavelWriteAdapter responsavelAdapter = responsavelAdapter(url);
        JdbcAlunoResponsavelWriteAdapter vinculoAdapter = new JdbcAlunoResponsavelWriteAdapter(properties(url));
        UUID escolaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        var responsavel = responsavelAdapter.criar(command(), escolaId);

        var resultado = vinculoAdapter.vincular(alunoId,
                new VincularAlunoResponsavelCommand(responsavel.id(), "RESPONSAVEL_LEGAL", null, true, null), escolaId);

        assertThat(resultado).isEqualTo(CriacaoAlunoResponsavelResultado.CRIADO);
        try (var connection = DriverManager.getConnection(url, "sa", "");
                var statement = connection.prepareStatement("""
                        SELECT p.codigo, ar.responsavel_financeiro, ar.responsavel_pedagogico, ar.autorizado_retirar
                        FROM aluno_responsavel ar
                        JOIN parentesco p ON p.id_parentesco = ar.id_parentesco
                        WHERE ar.id_aluno = ? AND ar.id_responsavel = ?
                        """)) {
            statement.setObject(1, alunoId);
            statement.setObject(2, responsavel.id());
            try (var result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString("codigo")).isEqualTo("RESPONSAVEL_LEGAL");
                assertThat(result.getBoolean("responsavel_financeiro")).isFalse();
                assertThat(result.getBoolean("responsavel_pedagogico")).isTrue();
                assertThat(result.getBoolean("autorizado_retirar")).isFalse();
            }
        }
    }

    @Test
    void deveBloquearVinculoDuplicado() {
        String url = h2Url("responsibles_student_link_duplicate_" + UUID.randomUUID());
        migrate(url);
        JdbcResponsavelWriteAdapter responsavelAdapter = responsavelAdapter(url);
        JdbcAlunoResponsavelWriteAdapter vinculoAdapter = new JdbcAlunoResponsavelWriteAdapter(properties(url));
        UUID escolaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        var responsavel = responsavelAdapter.criar(command(), escolaId);
        VincularAlunoResponsavelCommand command = new VincularAlunoResponsavelCommand(
                responsavel.id(), "RESPONSAVEL_LEGAL", false, false, false);
        vinculoAdapter.vincular(alunoId, command, escolaId);

        assertThat(vinculoAdapter.vincular(alunoId, command, escolaId))
                .isEqualTo(CriacaoAlunoResponsavelResultado.VINCULO_DUPLICADO);
    }

    @Test
    void deveDesvincularEPermitirInativacaoDoResponsavel() {
        String url = h2Url("responsibles_student_unlink_" + UUID.randomUUID());
        migrate(url);
        JdbcResponsavelWriteAdapter responsavelAdapter = responsavelAdapter(url);
        JdbcAlunoResponsavelWriteAdapter vinculoAdapter = new JdbcAlunoResponsavelWriteAdapter(properties(url));
        UUID escolaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        var responsavel = responsavelAdapter.criar(command(), escolaId);
        VincularAlunoResponsavelCommand command = new VincularAlunoResponsavelCommand(
                responsavel.id(), "RESPONSAVEL_LEGAL", false, false, false);
        vinculoAdapter.vincular(alunoId, command, escolaId);

        assertThat(vinculoAdapter.desvincular(alunoId, responsavel.id(), escolaId))
                .isEqualTo(DesvinculoAlunoResponsavelResultado.DESVINCULADO);
        assertThat(responsavelAdapter.excluir(responsavel.id(), escolaId))
                .isEqualTo(br.com.escola.responsiblesservice.application.dto.ExclusaoResponsavelResultado.EXCLUIDO);
    }

    private JdbcResponsavelWriteAdapter responsavelAdapter(String url) {
        return new JdbcResponsavelWriteAdapter(properties(url));
    }

    private ResponsiblesPersistenceProperties properties(String url) {
        return new ResponsiblesPersistenceProperties(url, "sa", "", "org.h2.Driver");
    }

    private void migrate(String url) {
        Flyway.configure().dataSource(url, "sa", "")
                .locations("classpath:db/responsibles/migration")
                .load().migrate();
    }

    private String h2Url(String database) {
        return "jdbc:h2:mem:" + database + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    }

    private CadastrarResponsavelCommand command() {
        return new CadastrarResponsavelCommand(
                "Maria Souza", "12345678901", null, null, null, null, null, null, null, null, null, null);
    }
}
