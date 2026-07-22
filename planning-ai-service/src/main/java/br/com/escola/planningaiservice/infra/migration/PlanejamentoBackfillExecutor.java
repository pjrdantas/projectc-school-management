package br.com.escola.planningaiservice.infra.migration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

/** One-shot, opt-in import of the planning aggregate; normal runtime never uses the source datasource. */
public class PlanejamentoBackfillExecutor {
    private final JdbcTemplate source;
    private final JdbcTemplate target;
    private final int batchSize;

    public PlanejamentoBackfillExecutor(JdbcTemplate source, JdbcTemplate target, int batchSize) {
        this.source = source;
        this.target = target;
        this.batchSize = batchSize;
    }

    public PlanejamentoBackfillReport execute() {
        List<Map<String, Object>> planejamentos = source.queryForList("""
                SELECT pb.*, pe.id_escola, sp.codigo AS status_codigo FROM planejamento_bimestral pb
                JOIN professor_turma_disciplina ptd ON ptd.id_professor_turma_disciplina = pb.id_professor_turma_disciplina
                JOIN professor p ON p.id_professor = ptd.id_professor
                JOIN pessoa pe ON pe.id_pessoa = p.id_pessoa
                JOIN status_planejamento sp ON sp.id_status_planejamento = pb.id_status_planejamento
                ORDER BY pb.id_planejamento_bimestral LIMIT ?
                """, batchSize);
        List<Map<String, Object>> aulas = source.queryForList("SELECT * FROM planejamento_bimestral_aula ORDER BY id_planejamento_bimestral_aula LIMIT ?", batchSize);
        List<Map<String, Object>> avaliacoes = source.queryForList("""
                SELECT pa.*, ta.codigo AS tipo_codigo FROM planejamento_bimestral_avaliacao pa
                JOIN tipo_avaliacao ta ON ta.id_tipo_avaliacao = pa.id_tipo_avaliacao
                ORDER BY pa.id_planejamento_bimestral_avaliacao LIMIT ?
                """, batchSize);
        planejamentos.forEach(this::upsertPlanejamento);
        aulas.forEach(this::upsertAula);
        avaliacoes.forEach(this::upsertAvaliacao);
        Map<String, Integer> sourceRows = Map.of("planejamento_bimestral", planejamentos.size(), "planejamento_bimestral_aula", aulas.size(), "planejamento_bimestral_avaliacao", avaliacoes.size());
        Map<String, Integer> reconciled = new LinkedHashMap<>();
        sourceRows.forEach((table, count) -> reconciled.put(table, target.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class)));
        boolean ok = sourceRows.entrySet().stream().allMatch(entry -> reconciled.get(entry.getKey()) >= entry.getValue());
        return new PlanejamentoBackfillReport(sourceRows, Map.copyOf(reconciled), ok);
    }

    private void upsertPlanejamento(Map<String, Object> row) {
        target.update("INSERT INTO planejamento_bimestral (id_planejamento_bimestral,id_escola,id_professor_turma_disciplina,id_periodo_avaliativo,status,titulo,tema_principal,descricao_inicial,objetivo_geral,observacao_professor,conteudo_final_aprovado,reutilizavel,criado_com_auxilio_ia,aprovado_pelo_professor,data_aprovacao,created_at,updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id_planejamento_bimestral) DO UPDATE SET status=EXCLUDED.status,titulo=EXCLUDED.titulo,tema_principal=EXCLUDED.tema_principal,descricao_inicial=EXCLUDED.descricao_inicial,updated_at=EXCLUDED.updated_at",
                value(row,"id_planejamento_bimestral"), value(row,"id_escola"), value(row,"id_professor_turma_disciplina"), value(row,"id_periodo_avaliativo"), value(row,"status_codigo"), value(row,"titulo"), value(row,"tema_principal"), value(row,"descricao_inicial"), value(row,"objetivo_geral"), value(row,"observacao_professor"), value(row,"conteudo_final_aprovado"), value(row,"reutilizavel"), value(row,"criado_com_auxilio_ia"), value(row,"aprovado_pelo_professor"), value(row,"data_aprovacao"), value(row,"created_at"), value(row,"updated_at"));
    }

    private void upsertAula(Map<String, Object> row) {
        target.update("INSERT INTO planejamento_bimestral_aula (id_planejamento_bimestral_aula,id_planejamento_bimestral,numero_aula,tema_aula,objetivo_aula,conteudo_previsto,metodologia,recursos,atividade_prevista,observacao,created_at,updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id_planejamento_bimestral_aula) DO NOTHING",
                value(row,"id_planejamento_bimestral_aula"), value(row,"id_planejamento_bimestral"), value(row,"numero_aula"), value(row,"tema_aula"), value(row,"objetivo_aula"), value(row,"conteudo_previsto"), value(row,"metodologia"), value(row,"recursos"), value(row,"atividade_prevista"), value(row,"observacao"), value(row,"created_at"), value(row,"updated_at"));
    }

    private void upsertAvaliacao(Map<String, Object> row) {
        target.update("INSERT INTO planejamento_bimestral_avaliacao (id_planejamento_bimestral_avaliacao,id_planejamento_bimestral,titulo,descricao,data_prevista,peso,valor_maximo,tipo_avaliacao,conteudo_cobrado,orientacao_aplicacao,created_at,updated_at,chave_idempotencia) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id_planejamento_bimestral_avaliacao) DO NOTHING",
                value(row,"id_planejamento_bimestral_avaliacao"), value(row,"id_planejamento_bimestral"), value(row,"titulo"), value(row,"descricao"), value(row,"data_prevista"), value(row,"peso"), value(row,"valor_maximo"), value(row,"tipo_codigo"), value(row,"conteudo_cobrado"), value(row,"orientacao_aplicacao"), value(row,"created_at"), value(row,"updated_at"), "LEGACY:" + value(row,"id_planejamento_bimestral_avaliacao"));
    }

    private Object value(Map<String, Object> row, String column) { return row.get(column); }
}
