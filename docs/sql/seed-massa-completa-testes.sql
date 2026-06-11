-- Massa completa de testes para a base local gestao_escolar.
-- Uso:
--   $env:PGPASSWORD='root123'
--   psql -U postgres -d gestao_escolar -f docs/sql/seed-massa-completa-testes.sql
--
-- Este script nao pertence ao Flyway. Ele e idempotente e limpa apenas os
-- registros de teste criados com os prefixos/UUIDs abaixo.

BEGIN;

DO $$
DECLARE
  senha_teste text := '$2a$10$AmWkxuvYPImdddGBN/Zfau8Ed9oNl2s7Edt38jxTxupX9UxjHt4NO'; -- admin123
  id_admin uuid;
  id_secretaria uuid;
  id_diretor uuid;
  id_professor_perfil uuid;
  id_cargo_secretaria uuid;
  id_cargo_diretor uuid;
  id_cargo_coordenador uuid;
  id_status_aluno_ativo uuid;
  id_status_aluno_transferido uuid;
  id_tipo_primeira uuid;
  id_tipo_renovacao uuid;
  id_tipo_transferencia_entrada uuid;
  id_status_solicitada uuid;
  id_status_andamento uuid;
  id_status_aguardando_doc uuid;
  id_status_aguardando_hist uuid;
  id_status_concluida uuid;
  id_status_efetivada uuid;
  id_doc_cpf uuid;
  id_doc_rg uuid;
  id_doc_residencia uuid;
  id_doc_historico uuid;
  id_doc_certidao uuid;
  id_tipo_prova uuid;
  id_tipo_trabalho uuid;
  id_tipo_atividade uuid;
  id_freq_presente uuid;
  id_freq_falta uuid;
  id_freq_justificada uuid;
  id_parentesco_mae uuid;
  id_parentesco_pai uuid;
  id_parentesco_resp uuid;
  id_tipo_transf_saida uuid;
  id_status_transf_solicitada uuid;
  id_status_transf_confirmada uuid;
  id_publico_academico uuid;
  id_publico_secretaria uuid;
  id_publico_diretor uuid;
  id_publico_professor uuid;
  id_nivel_fundamental uuid;
  id_turno_manha uuid;
  id_turno_tarde uuid;
  id_turno_integral uuid;
  id_periodo_1 uuid := '91000000-0000-0000-0000-000000000001'::uuid;
  id_periodo_2 uuid := '91000000-0000-0000-0000-000000000002'::uuid;
  i int;
  j int;
  id_turma uuid;
  id_disciplina uuid;
  id_turma_disciplina uuid;
  id_professor uuid;
  id_ptd uuid;
  id_aluno uuid;
  id_matricula uuid;
  id_aula uuid;
  id_avaliacao uuid;
  id_boletim uuid;
  id_dashboard uuid;
BEGIN
  SELECT id_perfil INTO id_admin FROM perfil WHERE upper(codigo) = 'ADMIN' LIMIT 1;
  SELECT id_perfil INTO id_secretaria FROM perfil WHERE upper(codigo) = 'SECRETARIA' LIMIT 1;
  SELECT id_perfil INTO id_diretor FROM perfil WHERE upper(codigo) = 'DIRETOR' LIMIT 1;
  SELECT id_perfil INTO id_professor_perfil FROM perfil WHERE upper(codigo) = 'PROFESSOR' LIMIT 1;

  SELECT id_cargo INTO id_cargo_secretaria FROM cargo WHERE codigo = 'SECRETARIA' LIMIT 1;
  SELECT id_cargo INTO id_cargo_diretor FROM cargo WHERE codigo = 'DIRETOR' LIMIT 1;
  SELECT id_cargo INTO id_cargo_coordenador FROM cargo WHERE codigo = 'COORDENADOR' LIMIT 1;

  SELECT id_status_aluno INTO id_status_aluno_ativo FROM status_aluno WHERE codigo = 'ATIVO' LIMIT 1;
  SELECT id_status_aluno INTO id_status_aluno_transferido FROM status_aluno WHERE codigo = 'TRANSFERIDO' LIMIT 1;

  SELECT id_tipo_matricula INTO id_tipo_primeira FROM tipo_matricula WHERE codigo = 'PRIMEIRA_MATRICULA' LIMIT 1;
  SELECT id_tipo_matricula INTO id_tipo_renovacao FROM tipo_matricula WHERE codigo = 'RENOVACAO' LIMIT 1;
  SELECT id_tipo_matricula INTO id_tipo_transferencia_entrada FROM tipo_matricula WHERE codigo = 'TRANSFERENCIA_ENTRADA' LIMIT 1;
  SELECT id_status_matricula INTO id_status_solicitada FROM status_matricula WHERE codigo = 'SOLICITADA' LIMIT 1;
  SELECT id_status_matricula INTO id_status_andamento FROM status_matricula WHERE codigo = 'EM_ANDAMENTO' LIMIT 1;
  SELECT id_status_matricula INTO id_status_aguardando_doc FROM status_matricula WHERE codigo = 'AGUARDANDO_DOCUMENTOS' LIMIT 1;
  SELECT id_status_matricula INTO id_status_aguardando_hist FROM status_matricula WHERE codigo = 'AGUARDANDO_HISTORICO_ESCOLAR' LIMIT 1;
  SELECT id_status_matricula INTO id_status_concluida FROM status_matricula WHERE codigo = 'CONCLUIDA' LIMIT 1;
  SELECT id_status_matricula INTO id_status_efetivada FROM status_matricula WHERE codigo = 'EFETIVADA' LIMIT 1;

  SELECT id_tipo_documento INTO id_doc_cpf FROM tipo_documento WHERE codigo = 'CPF' LIMIT 1;
  SELECT id_tipo_documento INTO id_doc_rg FROM tipo_documento WHERE codigo = 'RG' LIMIT 1;
  SELECT id_tipo_documento INTO id_doc_residencia FROM tipo_documento WHERE codigo = 'COMPROVANTE_RESIDENCIA' LIMIT 1;
  SELECT id_tipo_documento INTO id_doc_historico FROM tipo_documento WHERE codigo = 'HISTORICO_ESCOLAR' LIMIT 1;
  SELECT id_tipo_documento INTO id_doc_certidao FROM tipo_documento WHERE codigo = 'CERTIDAO_NASCIMENTO' LIMIT 1;

  SELECT id_tipo_avaliacao INTO id_tipo_prova FROM tipo_avaliacao WHERE codigo = 'PROVA' LIMIT 1;
  SELECT id_tipo_avaliacao INTO id_tipo_trabalho FROM tipo_avaliacao WHERE codigo = 'TRABALHO' LIMIT 1;
  SELECT id_tipo_avaliacao INTO id_tipo_atividade FROM tipo_avaliacao WHERE codigo = 'ATIVIDADE' LIMIT 1;
  SELECT id_situacao_frequencia INTO id_freq_presente FROM situacao_frequencia WHERE codigo = 'PRESENTE' LIMIT 1;
  SELECT id_situacao_frequencia INTO id_freq_falta FROM situacao_frequencia WHERE codigo = 'FALTA' LIMIT 1;
  SELECT id_situacao_frequencia INTO id_freq_justificada FROM situacao_frequencia WHERE codigo = 'FALTA_JUSTIFICADA' LIMIT 1;

  SELECT id_parentesco INTO id_parentesco_mae FROM parentesco WHERE upper(codigo) IN ('MAE', 'MÃE') LIMIT 1;
  SELECT id_parentesco INTO id_parentesco_pai FROM parentesco WHERE codigo = 'PAI' LIMIT 1;
  SELECT id_parentesco INTO id_parentesco_resp FROM parentesco ORDER BY descricao LIMIT 1;

  SELECT id_tipo_transferencia INTO id_tipo_transf_saida FROM tipo_transferencia WHERE codigo = 'SAIDA' LIMIT 1;
  SELECT id_status_transferencia INTO id_status_transf_solicitada FROM status_transferencia WHERE codigo = 'SOLICITADA' LIMIT 1;
  SELECT id_status_transferencia INTO id_status_transf_confirmada FROM status_transferencia WHERE codigo = 'CONFIRMADA' LIMIT 1;

  SELECT id_publico_dashboard INTO id_publico_academico FROM publico_dashboard WHERE codigo = 'ACADEMICO' LIMIT 1;
  SELECT id_publico_dashboard INTO id_publico_secretaria FROM publico_dashboard WHERE codigo = 'SECRETARIA' LIMIT 1;
  SELECT id_publico_dashboard INTO id_publico_diretor FROM publico_dashboard WHERE codigo = 'DIRETOR' LIMIT 1;
  SELECT id_publico_dashboard INTO id_publico_professor FROM publico_dashboard WHERE codigo = 'PROFESSOR' LIMIT 1;
  SELECT id_nivel_ensino INTO id_nivel_fundamental FROM nivel_ensino WHERE codigo = 'ENSINO_FUNDAMENTAL' LIMIT 1;
  SELECT id_turno INTO id_turno_manha FROM turno WHERE codigo = 'MANHA' LIMIT 1;
  SELECT id_turno INTO id_turno_tarde FROM turno WHERE codigo = 'TARDE' LIMIT 1;
  SELECT id_turno INTO id_turno_integral FROM turno WHERE codigo = 'INTEGRAL' LIMIT 1;

  -- Limpeza dos dados desta massa.
  DELETE FROM boletim_item WHERE boletim_item.id_boletim IN (SELECT ('9b000000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 30) n);
  DELETE FROM boletim WHERE boletim.id_boletim IN (SELECT ('9b000000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 30) n);
  DELETE FROM nota_aluno WHERE nota_aluno.id_nota_aluno IN (SELECT ('9a000000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 120) n);
  DELETE FROM frequencia_aluno WHERE frequencia_aluno.id_frequencia_aluno IN (SELECT ('99000000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 240) n);
  DELETE FROM frequencia_professor WHERE frequencia_professor.id_frequencia_professor IN (SELECT ('98000000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 20) n);
  DELETE FROM aula WHERE aula.id_aula IN (SELECT ('97000000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 20) n);
  DELETE FROM avaliacao WHERE avaliacao.id_avaliacao IN (SELECT ('96000000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 12) n);
  DELETE FROM planejamento_bimestral_avaliacao WHERE planejamento_bimestral_avaliacao.id_planejamento_bimestral_avaliacao IN (SELECT ('95a00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 8) n);
  DELETE FROM planejamento_bimestral_aula WHERE planejamento_bimestral_aula.id_planejamento_bimestral_aula IN (SELECT ('95b00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 12) n);
  DELETE FROM planejamento_aula WHERE planejamento_aula.id_planejamento_aula IN (SELECT ('95c00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 12) n);
  DELETE FROM planejamento_bimestral WHERE planejamento_bimestral.id_planejamento_bimestral IN (SELECT ('95d00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 8) n);
  DELETE FROM planejamento_professor WHERE planejamento_professor.id_planejamento_professor IN (SELECT ('95e00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 8) n);
  DELETE FROM professor_turma_disciplina WHERE professor_turma_disciplina.id_professor_turma_disciplina IN (SELECT ('95000000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 16) n);
  DELETE FROM turma_disciplina WHERE turma_disciplina.id_turma_disciplina IN (SELECT ('94000000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 24) n);
  DELETE FROM matricula_documento_entregue WHERE matricula_documento_entregue.id_matricula_documento_entregue IN (SELECT ('93e00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 90) n);
  DELETE FROM pessoa_documento WHERE pessoa_documento.id_pessoa_documento IN (SELECT ('93d00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 90) n);
  DELETE FROM documento WHERE documento.id_documento IN (SELECT ('93c00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 90) n);
  DELETE FROM matricula_etapa WHERE matricula_etapa.id_matricula_etapa IN (SELECT ('93b00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 120) n);
  DELETE FROM transferencia_aluno WHERE transferencia_aluno.id_transferencia_aluno IN (SELECT ('93a00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 6) n);
  DELETE FROM historico_escolar WHERE historico_escolar.id_historico_escolar IN (SELECT ('93900000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 12) n);
  DELETE FROM matricula WHERE matricula.id_matricula IN (SELECT ('93000000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 30) n);
  DELETE FROM aluno_responsavel WHERE aluno_responsavel.id_aluno_responsavel IN (SELECT ('92f00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 60) n);
  DELETE FROM professor WHERE professor.id_professor IN (SELECT ('92e00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 4) n);
  DELETE FROM funcionario WHERE funcionario.id_funcionario IN (SELECT ('92d00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 4) n);
  DELETE FROM responsavel WHERE responsavel.id_responsavel IN (SELECT ('92c00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 12) n);
  DELETE FROM aluno WHERE aluno.id_aluno IN (SELECT ('92b00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 30) n);
  DELETE FROM usuario_perfil WHERE usuario_perfil.id_usuario IN (SELECT ('92a00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 4) n);
  DELETE FROM usuario WHERE usuario.id_usuario IN (SELECT ('92a00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 4) n);
  DELETE FROM pessoa WHERE pessoa.id_pessoa IN (SELECT ('92000000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 60) n);
  DELETE FROM dashboard_indicador_snapshot WHERE dashboard_indicador_snapshot.codigo_indicador LIKE 'TESTE_%';
  DELETE FROM escola WHERE escola.id_escola IN (SELECT ('91f00000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 3) n);
  DELETE FROM turma WHERE turma.id_turma IN (SELECT ('91300000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 6) n);
  DELETE FROM disciplina WHERE disciplina.id_disciplina IN (SELECT ('91200000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 6) n);
  DELETE FROM serie WHERE serie.id_serie IN (SELECT ('91100000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid FROM generate_series(1, 4) n);
  DELETE FROM periodo_letivo WHERE periodo_letivo.id_periodo_letivo IN (id_periodo_1, id_periodo_2);

  -- Usuarios de cenario: senha admin123.
  INSERT INTO usuario (id_usuario, username, nome, email, senha_hash, ativo)
  VALUES
    ('92a00000-0000-0000-0000-000000000001', 'secretaria.teste', 'Secretaria Teste Operacional', 'secretaria.teste@escola.local', senha_teste, true),
    ('92a00000-0000-0000-0000-000000000002', 'diretor.teste', 'Diretor Teste Executivo', 'diretor.teste@escola.local', senha_teste, true),
    ('92a00000-0000-0000-0000-000000000003', 'professor.teste', 'Professor Teste Docente', 'professor.teste@escola.local', senha_teste, true),
    ('92a00000-0000-0000-0000-000000000004', 'professora.teste', 'Professora Teste Linguagens', 'professora.teste@escola.local', senha_teste, true);

  INSERT INTO usuario_perfil (id_usuario_perfil, id_usuario, id_perfil)
  VALUES
    ('92aa0000-0000-0000-0000-000000000001', '92a00000-0000-0000-0000-000000000001', id_secretaria),
    ('92aa0000-0000-0000-0000-000000000002', '92a00000-0000-0000-0000-000000000002', id_diretor),
    ('92aa0000-0000-0000-0000-000000000003', '92a00000-0000-0000-0000-000000000003', id_professor_perfil),
    ('92aa0000-0000-0000-0000-000000000004', '92a00000-0000-0000-0000-000000000004', id_professor_perfil);

  INSERT INTO periodo_letivo (id_periodo_letivo, nome, ano, data_inicio, data_fim, ativo)
  VALUES
    (id_periodo_1, 'Teste 2026', 2026, '2026-02-02', '2026-12-18', true),
    (id_periodo_2, 'Teste 2027', 2027, '2027-02-01', '2027-12-17', true);

  FOR i IN 1..4 LOOP
    INSERT INTO serie (id_serie, id_nivel_ensino, nome, ordem)
    VALUES (('91100000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid, id_nivel_fundamental, (5 + i)::text || ' Ano Teste', 5 + i);
  END LOOP;

  FOR i IN 1..6 LOOP
    INSERT INTO disciplina (id_disciplina, nome, carga_horaria, ativo)
    VALUES (('91200000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      (ARRAY['Matematica Teste','Lingua Portuguesa Teste','Ciencias Teste','Historia Teste','Geografia Teste','Ingles Teste'])[i],
      (ARRAY[120,120,80,80,80,80])[i],
      true);
  END LOOP;

  FOR i IN 1..6 LOOP
    INSERT INTO turma (id_turma, id_periodo_letivo, id_serie, id_turno, codigo, nome, capacidade, ativo)
    VALUES (
      ('91300000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      id_periodo_1,
      ('91100000-0000-0000-0000-' || lpad(((i - 1) % 4 + 1)::text, 12, '0'))::uuid,
      CASE WHEN i IN (1,2) THEN id_turno_manha WHEN i IN (3,4) THEN id_turno_tarde ELSE id_turno_integral END,
      'TST26-' || i::text || 'A',
      'Turma Teste ' || i::text || 'A',
      CASE WHEN i = 6 THEN 24 ELSE 30 END,
      true);
  END LOOP;

  FOR i IN 1..6 LOOP
    FOR j IN 1..4 LOOP
      INSERT INTO turma_disciplina (id_turma_disciplina, id_turma, id_disciplina, carga_horaria)
      VALUES (
        ('94000000-0000-0000-0000-' || lpad((((i - 1) * 4) + j)::text, 12, '0'))::uuid,
        ('91300000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
        ('91200000-0000-0000-0000-' || lpad((((j - 1) % 6) + 1)::text, 12, '0'))::uuid,
        80);
    END LOOP;
  END LOOP;

  -- Pessoas: alunos 1..30, responsaveis 31..42, funcionarios/professores 51..54.
  FOR i IN 1..30 LOOP
    INSERT INTO pessoa (id_pessoa, nome_completo, cpf, rg, email, telefone, data_nascimento, sexo, nacionalidade, naturalidade, ativo)
    VALUES (
      ('92000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      'Aluno Teste ' || lpad(i::text, 2, '0'),
      '989000' || lpad(i::text, 5, '0'),
      'RG-TST-A' || lpad(i::text, 3, '0'),
      'aluno.teste' || lpad(i::text, 2, '0') || '@escola.local',
      '1198' || lpad(i::text, 7, '0'),
      date '2012-01-01' + (i * interval '35 days'),
      CASE WHEN i % 2 = 0 THEN 'FEMININO' ELSE 'MASCULINO' END,
      'Brasileira',
      'Sao Paulo',
      true);

    INSERT INTO aluno (id_aluno, id_pessoa, id_status_aluno, ra, rm, emancipado, data_ingresso, ativo)
    VALUES (
      ('92b00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      ('92000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      CASE WHEN i IN (29,30) THEN id_status_aluno_transferido ELSE id_status_aluno_ativo END,
      'RA-TST-' || lpad(i::text, 4, '0'),
      'RM-TST-' || lpad(i::text, 4, '0'),
      false,
      '2026-02-02',
      true);
  END LOOP;

  FOR i IN 31..42 LOOP
    INSERT INTO pessoa (id_pessoa, nome_completo, cpf, rg, email, telefone, data_nascimento, sexo, nacionalidade, naturalidade, ativo)
    VALUES (
      ('92000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      'Responsavel Teste ' || lpad((i - 30)::text, 2, '0'),
      '989100' || lpad((i - 30)::text, 5, '0'),
      'RG-TST-R' || lpad((i - 30)::text, 3, '0'),
      'responsavel.teste' || lpad((i - 30)::text, 2, '0') || '@escola.local',
      '1197' || lpad((i - 30)::text, 7, '0'),
      date '1980-01-01' + ((i - 30) * interval '180 days'),
      CASE WHEN i % 2 = 0 THEN 'FEMININO' ELSE 'MASCULINO' END,
      'Brasileira',
      'Sao Paulo',
      true);

    INSERT INTO responsavel (id_responsavel, id_pessoa)
    VALUES (
      ('92c00000-0000-0000-0000-' || lpad((i - 30)::text, 12, '0'))::uuid,
      ('92000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid);
  END LOOP;

  FOR i IN 51..54 LOOP
    INSERT INTO pessoa (id_pessoa, nome_completo, cpf, rg, email, telefone, data_nascimento, sexo, nacionalidade, naturalidade, ativo)
    VALUES (
      ('92000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      (ARRAY['Secretaria Teste Operacional','Diretor Teste Executivo','Professor Teste Docente','Professora Teste Linguagens'])[i - 50],
      '989200' || lpad((i - 50)::text, 5, '0'),
      'RG-TST-F' || lpad((i - 50)::text, 3, '0'),
      (ARRAY['secretaria.teste@escola.local','diretor.teste@escola.local','professor.teste@escola.local','professora.teste@escola.local'])[i - 50],
      '1196' || lpad((i - 50)::text, 7, '0'),
      date '1978-01-01' + ((i - 50) * interval '365 days'),
      CASE WHEN i IN (51,54) THEN 'FEMININO' ELSE 'MASCULINO' END,
      'Brasileira',
      'Sao Paulo',
      true);

    INSERT INTO funcionario (id_funcionario, id_pessoa, id_cargo, ativo)
    VALUES (
      ('92d00000-0000-0000-0000-' || lpad((i - 50)::text, 12, '0'))::uuid,
      ('92000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      CASE WHEN i = 51 THEN id_cargo_secretaria WHEN i = 52 THEN id_cargo_diretor ELSE id_cargo_coordenador END,
      true);
  END LOOP;

  FOR i IN 1..4 LOOP
    INSERT INTO professor (id_professor, id_pessoa, registro_profissional, formacao, ativo, id_usuario)
    VALUES (
      ('92e00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      ('92000000-0000-0000-0000-' || lpad((50 + i)::text, 12, '0'))::uuid,
      'REG-TST-' || lpad(i::text, 4, '0'),
      CASE WHEN i <= 2 THEN 'Pedagogia' ELSE 'Licenciatura plena' END,
      true,
      CASE WHEN i IN (3,4) THEN ('92a00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid ELSE NULL END);
  END LOOP;

  FOR i IN 1..30 LOOP
    INSERT INTO aluno_responsavel (id_aluno_responsavel, id_aluno, id_responsavel, id_parentesco, responsavel_financeiro, responsavel_pedagogico, autorizado_retirar)
    VALUES (
      ('92f00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      ('92b00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      ('92c00000-0000-0000-0000-' || lpad((((i - 1) % 12) + 1)::text, 12, '0'))::uuid,
      CASE WHEN i % 3 = 0 THEN COALESCE(id_parentesco_pai, id_parentesco_resp) ELSE COALESCE(id_parentesco_mae, id_parentesco_resp) END,
      i % 2 = 0,
      true,
      true);
  END LOOP;

  FOR i IN 1..30 LOOP
    INSERT INTO matricula (id_matricula, id_aluno, id_turma, id_periodo_letivo, id_tipo_matricula, id_status_matricula, data_solicitacao, data_efetivacao, observacao)
    VALUES (
      ('93000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      ('92b00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      ('91300000-0000-0000-0000-' || lpad((((i - 1) % 6) + 1)::text, 12, '0'))::uuid,
      id_periodo_1,
      CASE WHEN i % 5 = 0 THEN id_tipo_transferencia_entrada WHEN i % 3 = 0 THEN id_tipo_renovacao ELSE id_tipo_primeira END,
      CASE
        WHEN i IN (1,2,3,4,5,6,7,8) THEN id_status_efetivada
        WHEN i IN (9,10,11,12,13) THEN id_status_concluida
        WHEN i IN (14,15,16,17,18) THEN id_status_aguardando_doc
        WHEN i IN (19,20,21) THEN id_status_aguardando_hist
        WHEN i IN (22,23,24,25) THEN id_status_andamento
        ELSE id_status_solicitada
      END,
      date '2026-01-10' + i,
      CASE WHEN i <= 13 THEN date '2026-01-25' + i ELSE NULL END,
      'Massa completa de testes - matricula ' || i::text);
  END LOOP;

  FOR i IN 1..30 LOOP
    FOR j IN 1..3 LOOP
      INSERT INTO matricula_etapa (id_matricula_etapa, id_matricula, id_status_etapa_matricula, descricao, ordem, data_inicio, data_conclusao, observacao)
      VALUES (
        ('93b00000-0000-0000-0000-' || lpad((((i - 1) * 3) + j)::text, 12, '0'))::uuid,
        ('93000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
        (SELECT id_status_etapa_matricula FROM status_etapa_matricula ORDER BY descricao LIMIT 1),
        (ARRAY['Cadastro conferido','Documentos analisados','Resultado da matricula'])[j],
        j,
        now() - (30 - i) * interval '1 day',
        CASE WHEN i <= 13 OR j = 1 THEN now() - (20 - i) * interval '1 day' ELSE NULL END,
        'Etapa de teste');
    END LOOP;
  END LOOP;

  FOR i IN 1..30 LOOP
    FOR j IN 1..3 LOOP
      INSERT INTO documento (id_documento, id_tipo_documento, nome_arquivo, url_arquivo, content_type, tamanho_bytes, observacao)
      VALUES (
        ('93c00000-0000-0000-0000-' || lpad((((i - 1) * 3) + j)::text, 12, '0'))::uuid,
        (ARRAY[id_doc_cpf, id_doc_rg, id_doc_residencia])[j],
        'teste-aluno-' || i::text || '-doc-' || j::text || '.pdf',
        '/massa-testes/alunos/' || i::text || '/doc-' || j::text || '.pdf',
        'application/pdf',
        128000 + i * 100 + j,
        'Documento de massa de testes');

      INSERT INTO pessoa_documento (id_pessoa_documento, id_pessoa, id_documento)
      VALUES (
        ('93d00000-0000-0000-0000-' || lpad((((i - 1) * 3) + j)::text, 12, '0'))::uuid,
        ('92000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
        ('93c00000-0000-0000-0000-' || lpad((((i - 1) * 3) + j)::text, 12, '0'))::uuid);

      IF i <= 18 OR j <= 2 THEN
        INSERT INTO matricula_documento_entregue (id_matricula_documento_entregue, id_matricula, id_documento, conferido, conferido_por, data_conferencia, observacao)
        VALUES (
          ('93e00000-0000-0000-0000-' || lpad((((i - 1) * 3) + j)::text, 12, '0'))::uuid,
          ('93000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
          ('93c00000-0000-0000-0000-' || lpad((((i - 1) * 3) + j)::text, 12, '0'))::uuid,
          i <= 13,
          CASE WHEN i <= 13 THEN '92a00000-0000-0000-0000-000000000001'::uuid ELSE NULL END,
          CASE WHEN i <= 13 THEN now() - interval '3 days' ELSE NULL END,
          'Entrega de teste');
      END IF;
    END LOOP;
  END LOOP;

  INSERT INTO escola (id_escola, nome, codigo_inep, cnpj, telefone, email)
  VALUES
    ('91f00000-0000-0000-0000-000000000001', 'Escola Municipal Teste Origem', '35000001', '12.345.678/0001-01', '1133330001', 'origem@teste.local'),
    ('91f00000-0000-0000-0000-000000000002', 'Escola Estadual Teste Destino', '35000002', '12.345.678/0001-02', '1133330002', 'destino@teste.local');

  FOR i IN 1..6 LOOP
    INSERT INTO historico_escolar (id_historico_escolar, id_aluno, origem, id_escola, ano_conclusao, ensino_concluido, data_emissao, diretor_nome, diretor_rg, observacoes)
    VALUES (
      ('93900000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      ('92b00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      CASE WHEN i <= 3 THEN 'INTERNO' ELSE 'EXTERNO' END,
      '91f00000-0000-0000-0000-000000000001'::uuid,
      2025,
      'Ensino Fundamental - etapa anterior',
      date '2026-01-20' + i,
      'Diretor Teste Executivo',
      'RG-DIR-TST',
      'Historico escolar de massa de testes');
  END LOOP;

  FOR i IN 1..4 LOOP
    INSERT INTO transferencia_aluno (id_transferencia_aluno, id_aluno, id_matricula, id_tipo_transferencia, id_status_transferencia, id_escola_origem, id_escola_destino, serie_origem, ano_letivo_origem, data_solicitacao, data_confirmacao, motivo_transferencia, observacao)
    VALUES (
      ('93a00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      ('92b00000-0000-0000-0000-' || lpad((26 + i)::text, 12, '0'))::uuid,
      ('93000000-0000-0000-0000-' || lpad((26 + i)::text, 12, '0'))::uuid,
      id_tipo_transf_saida,
      CASE WHEN i <= 2 THEN id_status_transf_confirmada ELSE id_status_transf_solicitada END,
      '91f00000-0000-0000-0000-000000000001'::uuid,
      '91f00000-0000-0000-0000-000000000002'::uuid,
      '6 Ano',
      '2026',
      date '2026-03-01' + i,
      CASE WHEN i <= 2 THEN date '2026-03-08' + i ELSE NULL END,
      'Mudanca de cidade',
      'Transferencia de teste');
  END LOOP;

  FOR i IN 1..16 LOOP
    INSERT INTO professor_turma_disciplina (id_professor_turma_disciplina, id_professor, id_turma_disciplina, data_inicio, ativo)
    VALUES (
      ('95000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      ('92e00000-0000-0000-0000-' || lpad((((i - 1) % 4) + 1)::text, 12, '0'))::uuid,
      ('94000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      '2026-02-02',
      true);
  END LOOP;

  FOR i IN 1..8 LOOP
    id_ptd := ('95000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid;
    INSERT INTO planejamento_professor (id_planejamento_professor, id_professor_turma_disciplina, titulo, objetivo, metodologia, recursos, periodo_inicio, periodo_fim)
    VALUES (('95e00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid, id_ptd, 'Planejamento Teste ' || i::text, 'Objetivo de aprendizagem de teste', 'Aula expositiva e atividade orientada', 'Livro, quadro e laboratório', '2026-02-02', '2026-04-30');

    INSERT INTO planejamento_bimestral (id_planejamento_bimestral, id_professor_turma_disciplina, id_status_planejamento, titulo, tema_principal, descricao_inicial, objetivo_geral, reutilizavel, criado_com_auxilio_ia, aprovado_pelo_professor, data_aprovacao)
    VALUES (('95d00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid, id_ptd, (SELECT id_status_planejamento FROM status_planejamento ORDER BY descricao LIMIT 1), 'Bimestre Teste ' || i::text, 'Tema Teste ' || i::text, 'Descricao inicial de teste', 'Consolidar habilidades do bimestre', true, i % 2 = 0, i <= 4, CASE WHEN i <= 4 THEN now() - interval '5 days' ELSE NULL END);

    INSERT INTO planejamento_aula (id_planejamento_aula, id_planejamento_professor, data_prevista, conteudo, habilidade_bncc, avaliacao_prevista)
    VALUES (('95c00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid, ('95e00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid, date '2026-02-10' + i, 'Conteudo planejado de teste', 'EF0' || i::text, 'Atividade diagnostica');

    INSERT INTO planejamento_bimestral_aula (id_planejamento_bimestral_aula, id_planejamento_bimestral, id_planejamento_aula, numero_aula, tema_aula, objetivo_aula, conteudo_previsto, metodologia, recursos, atividade_prevista)
    VALUES (('95b00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid, ('95d00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid, ('95c00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid, 1, 'Aula Teste ' || i::text, 'Objetivo da aula', 'Conteudo previsto', 'Metodologia ativa', 'Recursos digitais', 'Lista de exercicios');

    INSERT INTO planejamento_bimestral_avaliacao (id_planejamento_bimestral_avaliacao, id_planejamento_bimestral, id_tipo_avaliacao, titulo, descricao, data_prevista, peso, valor_maximo, conteudo_cobrado)
    VALUES (('95a00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid, ('95d00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid, CASE WHEN i % 2 = 0 THEN id_tipo_trabalho ELSE id_tipo_prova END, 'Avaliacao Planejada Teste ' || i::text, 'Avaliacao planejada pela massa de testes', date '2026-03-10' + i, 1, 10, 'Conteudos do bimestre');
  END LOOP;

  FOR i IN 1..12 LOOP
    id_ptd := ('95000000-0000-0000-0000-' || lpad((((i - 1) % 8) + 1)::text, 12, '0'))::uuid;
    INSERT INTO aula (id_aula, id_professor_turma_disciplina, id_planejamento_aula, data_aula, horario_inicio, horario_fim, conteudo_ministrado, observacao, realizada)
    VALUES (('97000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid, id_ptd, CASE WHEN i <= 8 THEN ('95c00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid ELSE NULL END, date '2026-02-15' + i, time '07:30', time '08:20', 'Conteudo ministrado teste ' || i::text, 'Aula de massa de testes', i <= 10);

    INSERT INTO frequencia_professor (id_frequencia_professor, id_aula, id_professor, presente, justificativa)
    VALUES (('98000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid, ('97000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid, ('92e00000-0000-0000-0000-' || lpad((((i - 1) % 4) + 1)::text, 12, '0'))::uuid, i <> 11, CASE WHEN i = 11 THEN 'Ausencia justificada em teste' ELSE NULL END);
  END LOOP;

  FOR i IN 1..12 LOOP
    INSERT INTO avaliacao (id_avaliacao, id_professor_turma_disciplina, id_tipo_avaliacao, id_planejamento_bimestral_avaliacao, titulo, descricao, data_aplicacao, valor_maximo, peso)
    VALUES (
      ('96000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
      ('95000000-0000-0000-0000-' || lpad((((i - 1) % 8) + 1)::text, 12, '0'))::uuid,
      CASE WHEN i % 3 = 0 THEN id_tipo_atividade WHEN i % 2 = 0 THEN id_tipo_trabalho ELSE id_tipo_prova END,
      CASE WHEN i <= 8 THEN ('95a00000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid ELSE NULL END,
      'Avaliacao Teste ' || i::text,
      'Avaliacao da massa completa de testes',
      date '2026-03-01' + i,
      10,
      CASE WHEN i % 2 = 0 THEN 2 ELSE 1 END);
  END LOOP;

  FOR i IN 1..12 LOOP
    id_matricula := ('93000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid;
    FOR j IN 1..4 LOOP
      INSERT INTO nota_aluno (id_nota_aluno, id_avaliacao, id_matricula, nota, observacao)
      VALUES (
        ('9a000000-0000-0000-0000-' || lpad((((i - 1) * 4) + j)::text, 12, '0'))::uuid,
        ('96000000-0000-0000-0000-' || lpad(j::text, 12, '0'))::uuid,
        id_matricula,
        CASE WHEN i IN (11,12) THEN NULL ELSE 5 + ((i + j) % 6) END,
        CASE WHEN i IN (11,12) THEN 'Nota pendente para teste' ELSE 'Nota lancada pela massa' END);
    END LOOP;
  END LOOP;

  FOR i IN 1..12 LOOP
    id_aula := ('97000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid;
    FOR j IN 1..10 LOOP
      INSERT INTO frequencia_aluno (id_frequencia_aluno, id_aula, id_matricula, id_situacao_frequencia, justificativa)
      VALUES (
        ('99000000-0000-0000-0000-' || lpad((((i - 1) * 10) + j)::text, 12, '0'))::uuid,
        id_aula,
        ('93000000-0000-0000-0000-' || lpad(j::text, 12, '0'))::uuid,
        CASE WHEN (i + j) % 11 = 0 THEN id_freq_justificada WHEN (i + j) % 7 = 0 THEN id_freq_falta ELSE id_freq_presente END,
        CASE WHEN (i + j) % 11 = 0 THEN 'Justificativa de teste' ELSE NULL END);
    END LOOP;
  END LOOP;

  FOR i IN 1..10 LOOP
    id_boletim := ('9b000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid;
    INSERT INTO boletim (id_boletim, id_matricula, periodo_referencia, data_fechamento, observacao)
    VALUES (id_boletim, ('93000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid, '1 Bimestre 2026', date '2026-04-20', 'Boletim de teste');

    FOR j IN 1..3 LOOP
      INSERT INTO boletim_item (id_boletim_item, id_boletim, id_disciplina, media, frequencia_percentual, resultado, carga_horaria, observacao)
      VALUES (
        ('9c000000-0000-0000-0000-' || lpad((((i - 1) * 3) + j)::text, 12, '0'))::uuid,
        id_boletim,
        ('91200000-0000-0000-0000-' || lpad(j::text, 12, '0'))::uuid,
        5 + ((i + j) % 5),
        75 + ((i + j) % 20),
        CASE WHEN (i + j) % 5 = 0 THEN 'RECUPERACAO' ELSE 'APROVADO' END,
        80,
        'Item de boletim de teste');
    END LOOP;
  END LOOP;

  -- Snapshots de dashboards para historico.
  FOR i IN 1..4 LOOP
    INSERT INTO dashboard_indicador_snapshot (id_dashboard_indicador_snapshot, id_publico_dashboard, codigo_indicador, descricao, valor_numeric, referencia_data)
    VALUES
      (('9d000000-0000-0000-0000-' || lpad(((i - 1) * 4 + 1)::text, 12, '0'))::uuid, id_publico_academico, 'TESTE_TOTAL_MATRICULAS', 'Total de matriculas teste', 24 + i, date '2026-03-01' + (i * interval '7 days')),
      (('9d000000-0000-0000-0000-' || lpad(((i - 1) * 4 + 2)::text, 12, '0'))::uuid, id_publico_secretaria, 'TESTE_DOCUMENTOS_PENDENTES', 'Documentos pendentes teste', 8 - i, date '2026-03-01' + (i * interval '7 days')),
      (('9d000000-0000-0000-0000-' || lpad(((i - 1) * 4 + 3)::text, 12, '0'))::uuid, id_publico_diretor, 'TESTE_AULAS_REALIZADAS', 'Aulas realizadas teste', 30 + (i * 3), date '2026-03-01' + (i * interval '7 days')),
      (('9d000000-0000-0000-0000-' || lpad(((i - 1) * 4 + 4)::text, 12, '0'))::uuid, id_publico_professor, 'TESTE_PROFESSOR_AULAS', 'Aulas do professor teste', 4 + i, date '2026-03-01' + (i * interval '7 days'));
  END LOOP;
END $$;

COMMIT;

SELECT 'massa_completa_testes_aplicada' AS status,
       (SELECT count(*) FROM aluno WHERE ra LIKE 'RA-TST-%') AS alunos,
       (SELECT count(*) FROM responsavel r JOIN pessoa p ON p.id_pessoa = r.id_pessoa WHERE p.nome_completo LIKE 'Responsavel Teste%') AS responsaveis,
       (SELECT count(*) FROM professor WHERE registro_profissional LIKE 'REG-TST-%') AS professores,
       (SELECT count(*) FROM matricula WHERE observacao LIKE 'Massa completa de testes%') AS matriculas,
       (SELECT count(*) FROM aula WHERE observacao = 'Aula de massa de testes') AS aulas,
       (SELECT count(*) FROM avaliacao WHERE descricao = 'Avaliacao da massa completa de testes') AS avaliacoes,
       (SELECT count(*) FROM dashboard_indicador_snapshot WHERE codigo_indicador LIKE 'TESTE_%') AS snapshots;
