package br.com.escola.professorservice.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.escola.professorservice.application.service.ProfessorShadowMigrationService;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowJpaRepository;
import br.com.escola.professorservice.infra.database.repository.ProfessorShadowSyncStateJpaRepository;

@SpringBootTest(properties = {
        "professor.shadow.migration.enabled=true",
        "professor.shadow.migration.runner-enabled=false",
        "professor.shadow.migration.source.url=jdbc:h2:mem:professor_shadow_source;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "professor.shadow.migration.source.username=sa",
        "professor.shadow.migration.source.password=",
        "professor.shadow.migration.source.driver-class-name=org.h2.Driver"
})
class ProfessorShadowMigrationIT {

    private static final UUID ESCOLA_A = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID PESSOA_A = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID PROFESSOR_A = UUID.fromString("20000000-0000-0000-0000-000000000001");

    @Autowired
    private ProfessorShadowMigrationService service;

    @Autowired
    private ProfessorShadowJpaRepository repository;

    @Autowired
    private ProfessorShadowSyncStateJpaRepository syncStateRepository;

    @BeforeEach
    void setUp() throws Exception {
        repository.deleteAll();
        syncStateRepository.deleteAll();
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:professor_shadow_source;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "sa",
                "");
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS professor");
            statement.execute("DROP TABLE IF EXISTS pessoa");
            statement.execute("DROP TABLE IF EXISTS escola");
            statement.execute("""
                    CREATE TABLE escola (
                        id_escola UUID PRIMARY KEY,
                        nome VARCHAR(150) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE pessoa (
                        id_pessoa UUID PRIMARY KEY,
                        nome_completo VARCHAR(150) NOT NULL,
                        id_escola UUID NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE professor (
                        id_professor UUID PRIMARY KEY,
                        id_pessoa UUID NOT NULL,
                        registro_profissional VARCHAR(80),
                        formacao VARCHAR(150),
                        ativo BOOLEAN NOT NULL,
                        created_at TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP,
                        id_usuario UUID
                    )
                    """);
            statement.execute("""
                    INSERT INTO escola (id_escola, nome) VALUES
                    ('00000000-0000-0000-0000-0000000000a1', 'Escola A')
                    """);
            statement.execute("""
                    INSERT INTO pessoa (id_pessoa, nome_completo, id_escola) VALUES
                    ('10000000-0000-0000-0000-000000000001', 'Professor A', '00000000-0000-0000-0000-0000000000a1')
                    """);
            statement.execute("""
                    INSERT INTO professor (id_professor, id_pessoa, registro_profissional, formacao, ativo, created_at, updated_at, id_usuario)
                    VALUES ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'RP-A', 'Licenciatura', TRUE, TIMESTAMP '2026-06-29 09:00:00', TIMESTAMP '2026-06-29 09:15:00', NULL)
                    """);
        }
    }

    @Test
    void deveMigrarRepetirReconciliarEDetectarDivergencia() {
        var first = service.executar(true);

        assertThat(first.applied()).isTrue();
        assertThat(first.reconciled()).isTrue();
        assertThat(repository.count()).isEqualTo(1);
        assertThat(syncStateRepository.findById(ESCOLA_A))
                .isPresent()
                .get()
                .satisfies(state -> {
                    assertThat(state.getProfessoresCompletos()).isTrue();
                    assertThat(state.getProfessorCount()).isEqualTo(1L);
                });
        assertThat(first.tables())
                .filteredOn(table -> "professor".equals(table.table()) && ESCOLA_A.equals(table.escolaId()))
                .singleElement()
                .satisfies(table -> {
                    assertThat(table.sourceCount()).isEqualTo(1);
                    assertThat(table.targetCount()).isEqualTo(1);
                    assertThat(table.reconciled()).isTrue();
                });

        var second = service.executar(true);
        assertThat(second.reconciled()).isTrue();
        assertThat(repository.count()).isEqualTo(1);

        repository.findById(PROFESSOR_A).ifPresent(entity -> repository.save(
                new br.com.escola.professorservice.infra.database.entity.ProfessorShadowJpaEntity(
                        entity.getId(),
                        entity.getPessoaId(),
                        "Professor Divergente",
                        entity.getEscolaId(),
                        entity.getEscolaNome(),
                        entity.getRegistroProfissional(),
                        entity.getFormacao(),
                        entity.getAtivo(),
                        entity.getCreatedAt(),
                        entity.getUpdatedAt(),
                        entity.getUsuarioId())));

        var dryRun = service.executar(false);

        assertThat(dryRun.applied()).isFalse();
        assertThat(dryRun.reconciled()).isFalse();
        assertThat(dryRun.tables())
                .filteredOn(table -> "professor".equals(table.table()) && ESCOLA_A.equals(table.escolaId()))
                .singleElement()
                .satisfies(table -> assertThat(table.divergentIds()).containsExactly(PROFESSOR_A));
    }
}
