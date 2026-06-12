-- Massa de testes para planejamento bimestral, IA e biblioteca pedagogica.
-- Uso:
--   $env:PGPASSWORD='root123'
--   psql -U postgres -d gestao_escolar -f docs/sql/seed-planejamento-ia-multiescola.sql
--
-- Este script nao pertence ao Flyway. Ele e idempotente e limpa apenas os
-- registros criados com os UUIDs fixos abaixo.

BEGIN;

DO $$
DECLARE
  escola_padrao uuid := '00000000-0000-0000-0000-000000000047'::uuid;
  nivel_fundamental uuid := '47c10000-0000-0000-0000-000000000001'::uuid;
  turno_manha uuid := '47c10000-0000-0000-0000-000000000002'::uuid;
  status_rascunho uuid := '47c10000-0000-0000-0000-000000000003'::uuid;
  status_aprovado uuid := '47c10000-0000-0000-0000-000000000004'::uuid;
  tipo_prova uuid := '47c10000-0000-0000-0000-000000000005'::uuid;
  tipo_plano_bimestral uuid := '47c10000-0000-0000-0000-000000000006'::uuid;
  tipo_atividade uuid := '47c10000-0000-0000-0000-000000000007'::uuid;
  status_gerado uuid := '47c10000-0000-0000-0000-000000000008'::uuid;
  status_edicao uuid := '47c10000-0000-0000-0000-000000000009'::uuid;
  status_conteudo_aprovado uuid := '47c10000-0000-0000-0000-00000000000a'::uuid;
  periodo_letivo_id uuid := '47c20000-0000-0000-0000-000000000001'::uuid;
  periodo_avaliativo_1 uuid := '47c20000-0000-0000-0000-000000000002'::uuid;
  periodo_avaliativo_2 uuid := '47c20000-0000-0000-0000-000000000003'::uuid;
  serie_id uuid := '47c30000-0000-0000-0000-000000000001'::uuid;
  turma_id uuid := '47c40000-0000-0000-0000-000000000001'::uuid;
  disciplina_id uuid := '47c50000-0000-0000-0000-000000000001'::uuid;
  turma_disciplina_id uuid := '47c60000-0000-0000-0000-000000000001'::uuid;
  pessoa_professor uuid := '47c70000-0000-0000-0000-000000000001'::uuid;
  professor_id uuid := '47c80000-0000-0000-0000-000000000001'::uuid;
  alocacao uuid := '47c90000-0000-0000-0000-000000000001'::uuid;
  planejamento_aprovado uuid := '47ca0000-0000-0000-0000-000000000001'::uuid;
  planejamento_rascunho uuid := '47ca0000-0000-0000-0000-000000000002'::uuid;
  aula_1 uuid := '47cb0000-0000-0000-0000-000000000001'::uuid;
  aula_2 uuid := '47cb0000-0000-0000-0000-000000000002'::uuid;
  avaliacao_1 uuid := '47cc0000-0000-0000-0000-000000000001'::uuid;
  interacao_1 uuid := '47cd0000-0000-0000-0000-000000000001'::uuid;
  interacao_2 uuid := '47cd0000-0000-0000-0000-000000000002'::uuid;
  conteudo_aprovado uuid := '47ce0000-0000-0000-0000-000000000001'::uuid;
  conteudo_edicao uuid := '47ce0000-0000-0000-0000-000000000002'::uuid;
  versao_1 uuid := '47cf0000-0000-0000-0000-000000000001'::uuid;
  versao_2 uuid := '47cf0000-0000-0000-0000-000000000002'::uuid;
  versao_3 uuid := '47cf0000-0000-0000-0000-000000000003'::uuid;
  biblioteca_1 uuid := '47d00000-0000-0000-0000-000000000001'::uuid;
BEGIN
  INSERT INTO escola (id_escola, nome, ativo, created_at)
  VALUES (escola_padrao, 'Escola padrao', true, CURRENT_TIMESTAMP)
  ON CONFLICT (id_escola) DO NOTHING;

  INSERT INTO nivel_ensino (id_nivel_ensino, codigo, descricao)
  SELECT nivel_fundamental, 'ENSINO_FUNDAMENTAL_TESTE_IA', 'Ensino Fundamental - Teste IA'
  WHERE NOT EXISTS (SELECT 1 FROM nivel_ensino WHERE codigo = 'ENSINO_FUNDAMENTAL_TESTE_IA');

  INSERT INTO turno (id_turno, codigo, descricao)
  SELECT turno_manha, 'MANHA_TESTE_IA', 'Manha - Teste IA'
  WHERE NOT EXISTS (SELECT 1 FROM turno WHERE codigo = 'MANHA_TESTE_IA');

  INSERT INTO status_planejamento (id_status_planejamento, codigo, descricao)
  SELECT status_rascunho, 'RASCUNHO', 'Rascunho'
  WHERE NOT EXISTS (SELECT 1 FROM status_planejamento WHERE codigo = 'RASCUNHO');

  INSERT INTO status_planejamento (id_status_planejamento, codigo, descricao)
  SELECT status_aprovado, 'APROVADO', 'Aprovado'
  WHERE NOT EXISTS (SELECT 1 FROM status_planejamento WHERE codigo = 'APROVADO');

  INSERT INTO tipo_avaliacao (id_tipo_avaliacao, codigo, descricao)
  SELECT tipo_prova, 'PROVA', 'Prova'
  WHERE NOT EXISTS (SELECT 1 FROM tipo_avaliacao WHERE codigo = 'PROVA');

  INSERT INTO tipo_conteudo_ia (id_tipo_conteudo_ia, codigo, descricao)
  SELECT tipo_plano_bimestral, 'PLANO_BIMESTRAL', 'Plano bimestral'
  WHERE NOT EXISTS (SELECT 1 FROM tipo_conteudo_ia WHERE codigo = 'PLANO_BIMESTRAL');

  INSERT INTO tipo_conteudo_ia (id_tipo_conteudo_ia, codigo, descricao)
  SELECT tipo_atividade, 'ATIVIDADE', 'Atividade'
  WHERE NOT EXISTS (SELECT 1 FROM tipo_conteudo_ia WHERE codigo = 'ATIVIDADE');

  INSERT INTO status_conteudo_ia (id_status_conteudo_ia, codigo, descricao)
  SELECT status_gerado, 'GERADO', 'Gerado'
  WHERE NOT EXISTS (SELECT 1 FROM status_conteudo_ia WHERE codigo = 'GERADO');

  INSERT INTO status_conteudo_ia (id_status_conteudo_ia, codigo, descricao)
  SELECT status_edicao, 'EM_EDICAO', 'Em edicao'
  WHERE NOT EXISTS (SELECT 1 FROM status_conteudo_ia WHERE codigo = 'EM_EDICAO');

  INSERT INTO status_conteudo_ia (id_status_conteudo_ia, codigo, descricao)
  SELECT status_conteudo_aprovado, 'APROVADO', 'Aprovado'
  WHERE NOT EXISTS (SELECT 1 FROM status_conteudo_ia WHERE codigo = 'APROVADO');

  SELECT id_nivel_ensino INTO nivel_fundamental FROM nivel_ensino WHERE codigo = 'ENSINO_FUNDAMENTAL_TESTE_IA';
  SELECT id_turno INTO turno_manha FROM turno WHERE codigo = 'MANHA_TESTE_IA';
  SELECT id_status_planejamento INTO status_rascunho FROM status_planejamento WHERE codigo = 'RASCUNHO';
  SELECT id_status_planejamento INTO status_aprovado FROM status_planejamento WHERE codigo = 'APROVADO';
  SELECT id_tipo_avaliacao INTO tipo_prova FROM tipo_avaliacao WHERE codigo = 'PROVA';
  SELECT id_tipo_conteudo_ia INTO tipo_plano_bimestral FROM tipo_conteudo_ia WHERE codigo = 'PLANO_BIMESTRAL';
  SELECT id_tipo_conteudo_ia INTO tipo_atividade FROM tipo_conteudo_ia WHERE codigo = 'ATIVIDADE';
  SELECT id_status_conteudo_ia INTO status_gerado FROM status_conteudo_ia WHERE codigo = 'GERADO';
  SELECT id_status_conteudo_ia INTO status_edicao FROM status_conteudo_ia WHERE codigo = 'EM_EDICAO';
  SELECT id_status_conteudo_ia INTO status_conteudo_aprovado FROM status_conteudo_ia WHERE codigo = 'APROVADO';

  DELETE FROM biblioteca_conteudo_pedagogico WHERE id_biblioteca_conteudo_pedagogico = biblioteca_1;
  DELETE FROM planejamento_ia_conteudo_versao WHERE id_planejamento_ia_conteudo_versao IN (versao_1, versao_2, versao_3);
  DELETE FROM planejamento_ia_conteudo_gerado WHERE id_planejamento_ia_conteudo_gerado IN (conteudo_aprovado, conteudo_edicao);
  DELETE FROM planejamento_ia_interacao WHERE id_planejamento_ia_interacao IN (interacao_1, interacao_2);
  DELETE FROM planejamento_bimestral_avaliacao WHERE id_planejamento_bimestral_avaliacao = avaliacao_1;
  DELETE FROM planejamento_bimestral_aula WHERE id_planejamento_bimestral_aula IN (aula_1, aula_2);
  DELETE FROM planejamento_bimestral WHERE id_planejamento_bimestral IN (planejamento_aprovado, planejamento_rascunho);
  DELETE FROM professor_turma_disciplina WHERE id_professor_turma_disciplina = alocacao;
  DELETE FROM turma_disciplina WHERE id_turma_disciplina = turma_disciplina_id;
  DELETE FROM professor WHERE id_professor = professor_id;
  DELETE FROM pessoa WHERE id_pessoa = pessoa_professor;
  DELETE FROM turma WHERE id_turma = turma_id;
  DELETE FROM disciplina WHERE id_disciplina = disciplina_id;
  DELETE FROM serie WHERE id_serie = serie_id;
  DELETE FROM periodo_avaliativo WHERE id_periodo_avaliativo IN (periodo_avaliativo_1, periodo_avaliativo_2);
  DELETE FROM periodo_letivo WHERE id_periodo_letivo = periodo_letivo_id;

  INSERT INTO periodo_letivo (id_periodo_letivo, id_escola, nome, ano, data_inicio, data_fim, ativo, created_at)
  VALUES (periodo_letivo_id, escola_padrao, 'Teste IA 2026', 2026, DATE '2026-02-02', DATE '2026-12-18', true, CURRENT_TIMESTAMP);

  INSERT INTO periodo_avaliativo (id_periodo_avaliativo, id_periodo_letivo, numero, nome, data_inicio, data_fim, ativo, created_at)
  VALUES
    (periodo_avaliativo_1, periodo_letivo_id, 1, '1o bimestre - Teste IA', DATE '2026-02-02', DATE '2026-04-30', true, CURRENT_TIMESTAMP),
    (periodo_avaliativo_2, periodo_letivo_id, 2, '2o bimestre - Teste IA', DATE '2026-05-01', DATE '2026-07-10', true, CURRENT_TIMESTAMP);

  INSERT INTO serie (id_serie, id_escola, id_nivel_ensino, nome, ordem, created_at)
  VALUES (serie_id, escola_padrao, nivel_fundamental, '7 Ano - Teste IA', 7, CURRENT_TIMESTAMP);

  INSERT INTO disciplina (id_disciplina, id_escola, nome, carga_horaria, ativo, created_at)
  VALUES (disciplina_id, escola_padrao, 'Ciencias - Teste IA', 80, true, CURRENT_TIMESTAMP);

  INSERT INTO turma (id_turma, id_escola, id_periodo_letivo, id_serie, id_turno, codigo, nome, capacidade, ativo, created_at)
  VALUES (turma_id, escola_padrao, periodo_letivo_id, serie_id, turno_manha, 'IA47C-7A', '7 Ano A - Teste IA', 32, true, CURRENT_TIMESTAMP);

  INSERT INTO turma_disciplina (id_turma_disciplina, id_turma, id_disciplina, carga_horaria, created_at)
  VALUES (turma_disciplina_id, turma_id, disciplina_id, 80, CURRENT_TIMESTAMP);

  INSERT INTO pessoa (id_pessoa, id_escola, nome_completo, cpf, email, ativo, created_at)
  VALUES (pessoa_professor, escola_padrao, 'Professor Teste Planejamento IA', '47000000001', 'professor.ia47c@escola.local', true, CURRENT_TIMESTAMP);

  INSERT INTO professor (id_professor, id_pessoa, registro_profissional, formacao, ativo, created_at)
  VALUES (professor_id, pessoa_professor, 'RP-IA47C', 'Licenciatura em Ciencias', true, CURRENT_TIMESTAMP);

  INSERT INTO professor_turma_disciplina (id_professor_turma_disciplina, id_professor, id_turma_disciplina, data_inicio, ativo, created_at)
  VALUES (alocacao, professor_id, turma_disciplina_id, DATE '2026-02-02', true, CURRENT_TIMESTAMP);

  INSERT INTO planejamento_bimestral (
      id_planejamento_bimestral, id_professor_turma_disciplina, id_periodo_avaliativo, id_status_planejamento,
      titulo, tema_principal, descricao_inicial, objetivo_geral, observacao_professor, conteudo_final_aprovado,
      reutilizavel, criado_com_auxilio_ia, aprovado_pelo_professor, data_aprovacao, created_at
  )
  VALUES
    (
      planejamento_aprovado, alocacao, periodo_avaliativo_1, status_aprovado,
      'Planejamento IA - Ecossistemas', 'Ecossistemas brasileiros',
      'Sequencia bimestral com investigacao de cadeias alimentares.',
      'Relacionar fatores bioticos, abioticos e acao humana nos ecossistemas.',
      'Massa de teste com conteudo IA aprovado e publicado.',
      'Conteudo final aprovado para o bimestre: ecossistemas brasileiros, biomas, cadeias alimentares e projeto investigativo.',
      true, true, true, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '7 days'
    ),
    (
      planejamento_rascunho, alocacao, periodo_avaliativo_2, status_rascunho,
      'Planejamento IA - Energia', 'Energia e transformacao',
      'Plano em rascunho para explorar formas de energia.',
      'Identificar transformacoes de energia em situacoes cotidianas.',
      'Massa de teste com conteudo ainda em edicao.',
      NULL,
      true, true, false, NULL, CURRENT_TIMESTAMP - INTERVAL '3 days'
    );

  INSERT INTO planejamento_bimestral_aula (
      id_planejamento_bimestral_aula, id_planejamento_bimestral, numero_aula, tema_aula,
      objetivo_aula, conteudo_previsto, metodologia, recursos, atividade_prevista, created_at
  )
  VALUES
    (aula_1, planejamento_aprovado, 1, 'Biomas brasileiros', 'Comparar biomas por clima, fauna e flora.', 'Biomas e biodiversidade.', 'Roda de conversa e mapa colaborativo.', 'Mapa, projetor e imagens.', 'Mapa comparativo dos biomas.', CURRENT_TIMESTAMP),
    (aula_2, planejamento_aprovado, 2, 'Cadeias alimentares', 'Representar relacoes alimentares em ecossistemas.', 'Produtores, consumidores e decompositores.', 'Estudo de caso em grupos.', 'Cartoes de especies.', 'Montagem de cadeia alimentar.', CURRENT_TIMESTAMP);

  INSERT INTO planejamento_bimestral_avaliacao (
      id_planejamento_bimestral_avaliacao, id_planejamento_bimestral, id_tipo_avaliacao,
      titulo, descricao, data_prevista, peso, valor_maximo, conteudo_cobrado, orientacao_aplicacao, created_at
  )
  VALUES (
      avaliacao_1, planejamento_aprovado, tipo_prova,
      'Avaliacao IA - Ecossistemas', 'Avaliacao com questoes discursivas e analise de caso.',
      DATE '2026-04-15', 1.00, 10.00, 'Biomas, cadeias alimentares e impactos humanos.',
      'Aplicacao individual com consulta ao mapa produzido em aula.', CURRENT_TIMESTAMP
  );

  INSERT INTO planejamento_ia_interacao (
      id_planejamento_ia_interacao, id_planejamento_bimestral, prompt_professor, resposta_ia,
      modelo_ia, tokens_entrada, tokens_saida, custo_estimado, created_at
  )
  VALUES
    (
      interacao_1, planejamento_aprovado,
      'Gerar uma sequencia didatica sobre ecossistemas brasileiros com duas aulas e avaliacao.',
      'Sugestao pedagogica simulada: trabalhar biomas, cadeias alimentares e impacto humano com atividade investigativa.',
      'simulado-local-v1', 145, 420, 0.000000, CURRENT_TIMESTAMP - INTERVAL '6 days'
    ),
    (
      interacao_2, planejamento_rascunho,
      'Gerar uma atividade pratica sobre transformacao de energia.',
      'Sugestao pedagogica simulada: experimento orientado sobre energia luminosa, termica e eletrica.',
      'simulado-local-v1', 98, 310, 0.000000, CURRENT_TIMESTAMP - INTERVAL '2 days'
    );

  INSERT INTO planejamento_ia_conteudo_gerado (
      id_planejamento_ia_conteudo_gerado, id_planejamento_bimestral, id_planejamento_ia_interacao,
      id_tipo_conteudo_ia, id_status_conteudo_ia, titulo, conteudo, versao, hash_conteudo,
      aprovado_pelo_professor, reutilizavel, ativo, created_at, updated_at
  )
  VALUES
    (
      conteudo_aprovado, planejamento_aprovado, interacao_1, tipo_plano_bimestral, status_conteudo_aprovado,
      'Sequencia didatica aprovada - Ecossistemas',
      'Versao aprovada: plano bimestral com estudo de biomas, cadeias alimentares e projeto investigativo sobre impactos humanos.',
      2, 'seed-planejamento-ia-aprovado-v2', true, true, true,
      CURRENT_TIMESTAMP - INTERVAL '6 days', CURRENT_TIMESTAMP - INTERVAL '2 days'
    ),
    (
      conteudo_edicao, planejamento_rascunho, interacao_2, tipo_atividade, status_edicao,
      'Atividade em edicao - Energia',
      'Rascunho de atividade pratica sobre transformacao de energia com registro de hipoteses.',
      1, 'seed-planejamento-ia-edicao-v1', false, true, true,
      CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '1 day'
    );

  INSERT INTO planejamento_ia_conteudo_versao (
      id_planejamento_ia_conteudo_versao, id_planejamento_ia_conteudo_gerado, numero_versao,
      conteudo, motivo_alteracao, created_at
  )
  VALUES
    (versao_1, conteudo_aprovado, 1, 'Versao inicial gerada sobre ecossistemas brasileiros.', 'Versao inicial gerada pela IA simulada.', CURRENT_TIMESTAMP - INTERVAL '6 days'),
    (versao_2, conteudo_aprovado, 2, 'Versao aprovada: plano bimestral com estudo de biomas, cadeias alimentares e projeto investigativo sobre impactos humanos.', 'Revisao do professor antes da aprovacao.', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    (versao_3, conteudo_edicao, 1, 'Rascunho de atividade pratica sobre transformacao de energia com registro de hipoteses.', 'Versao em edicao para teste.', CURRENT_TIMESTAMP - INTERVAL '1 day');

  INSERT INTO biblioteca_conteudo_pedagogico (
      id_biblioteca_conteudo_pedagogico, id_professor, id_disciplina, id_tipo_conteudo_ia,
      titulo, tema, conteudo, origem, reutilizavel, ativo, created_at
  )
  VALUES (
      biblioteca_1, professor_id, disciplina_id, tipo_plano_bimestral,
      'Biblioteca IA - Ecossistemas brasileiros', 'Ecossistemas brasileiros',
      'Conteudo publicado para reuso: sequencia bimestral sobre biomas, cadeias alimentares e impactos humanos.',
      'PLANEJAMENTO_IA', true, true, CURRENT_TIMESTAMP - INTERVAL '2 days'
  );
END $$;

COMMIT;
