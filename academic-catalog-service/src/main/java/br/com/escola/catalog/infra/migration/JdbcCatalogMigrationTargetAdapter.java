package br.com.escola.catalog.infra.migration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalog.application.migration.CatalogMigrationSnapshot;
import br.com.escola.catalog.application.port.out.CatalogMigrationTargetPort;

@Component
@ConditionalOnProperty(name = "catalog.migration.enabled", havingValue = "true")
public class JdbcCatalogMigrationTargetAdapter implements CatalogMigrationTargetPort {

    private final JdbcTemplate jdbc;

    public JdbcCatalogMigrationTargetAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void aplicar(CatalogMigrationSnapshot snapshot) {
        snapshot.niveisEnsino().forEach(row -> jdbc.update("""
                INSERT INTO nivel_ensino (id_nivel_ensino, codigo, descricao)
                VALUES (?, ?, ?)
                ON CONFLICT (id_nivel_ensino) DO UPDATE
                    SET codigo = EXCLUDED.codigo, descricao = EXCLUDED.descricao
                """, row.id(), row.codigo(), row.descricao()));
        snapshot.turnos().forEach(row -> jdbc.update("""
                INSERT INTO turno (id_turno, codigo, descricao)
                VALUES (?, ?, ?)
                ON CONFLICT (id_turno) DO UPDATE
                    SET codigo = EXCLUDED.codigo, descricao = EXCLUDED.descricao
                """, row.id(), row.codigo(), row.descricao()));
        snapshot.periodos().forEach(row -> jdbc.update("""
                INSERT INTO periodo_letivo
                    (id_periodo_letivo, id_escola, nome, ano, data_inicio, data_fim, ativo, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id_periodo_letivo) DO UPDATE SET
                    id_escola = EXCLUDED.id_escola, nome = EXCLUDED.nome, ano = EXCLUDED.ano,
                    data_inicio = EXCLUDED.data_inicio, data_fim = EXCLUDED.data_fim,
                    ativo = EXCLUDED.ativo, created_at = EXCLUDED.created_at
                """, row.id(), row.escolaId(), row.nome(), row.ano(), row.dataInicio(), row.dataFim(),
                row.ativo(), row.createdAt()));
        snapshot.series().forEach(row -> jdbc.update("""
                INSERT INTO serie
                    (id_serie, id_escola, nome, ordem, id_nivel_ensino, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (id_serie) DO UPDATE SET
                    id_escola = EXCLUDED.id_escola, nome = EXCLUDED.nome, ordem = EXCLUDED.ordem,
                    id_nivel_ensino = EXCLUDED.id_nivel_ensino, created_at = EXCLUDED.created_at
                """, row.id(), row.escolaId(), row.nome(), row.ordem(), row.nivelEnsinoId(), row.createdAt()));
        snapshot.disciplinas().forEach(row -> jdbc.update("""
                INSERT INTO disciplina
                    (id_disciplina, id_escola, nome, carga_horaria, ativo, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (id_disciplina) DO UPDATE SET
                    id_escola = EXCLUDED.id_escola, nome = EXCLUDED.nome,
                    carga_horaria = EXCLUDED.carga_horaria, ativo = EXCLUDED.ativo,
                    created_at = EXCLUDED.created_at
                """, row.id(), row.escolaId(), row.nome(), row.cargaHoraria(), row.ativo(), row.createdAt()));
        snapshot.turmas().forEach(row -> jdbc.update("""
                INSERT INTO turma
                    (id_turma, id_escola, codigo, nome, capacidade, id_periodo_letivo,
                     id_serie, id_turno, ativo, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id_turma) DO UPDATE SET
                    id_escola = EXCLUDED.id_escola, codigo = EXCLUDED.codigo, nome = EXCLUDED.nome,
                    capacidade = EXCLUDED.capacidade, id_periodo_letivo = EXCLUDED.id_periodo_letivo,
                    id_serie = EXCLUDED.id_serie, id_turno = EXCLUDED.id_turno,
                    ativo = EXCLUDED.ativo, created_at = EXCLUDED.created_at
                """, row.id(), row.escolaId(), row.codigo(), row.nome(), row.capacidade(),
                row.periodoLetivoId(), row.serieId(), row.turnoId(), row.ativo(), row.createdAt()));
        snapshot.turmaDisciplinas().forEach(row -> jdbc.update("""
                INSERT INTO turma_disciplina
                    (id_turma_disciplina, id_escola, id_turma, id_disciplina, carga_horaria, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (id_turma_disciplina) DO UPDATE SET
                    id_escola = EXCLUDED.id_escola, id_turma = EXCLUDED.id_turma,
                    id_disciplina = EXCLUDED.id_disciplina,
                    carga_horaria = EXCLUDED.carga_horaria, created_at = EXCLUDED.created_at
                """, row.id(), row.escolaId(), row.turmaId(), row.disciplinaId(),
                row.cargaHoraria(), row.createdAt()));
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogMigrationSnapshot carregarSnapshot() {
        return JdbcCatalogSnapshotReader.read(jdbc, false);
    }
}
