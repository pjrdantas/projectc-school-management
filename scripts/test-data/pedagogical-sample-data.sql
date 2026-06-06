-- Massa local para testar professores, aulas e frequencia.
-- Nao e Flyway. Nao mover para school-management-service/src/main/resources/db/migration.
--
-- Remocao opcional somente desta massa:
-- DELETE FROM frequencia_aluno WHERE id_frequencia_aluno IN (
--   '81000000-0000-0000-0000-000000000001',
--   '81000000-0000-0000-0000-000000000002',
--   '81000000-0000-0000-0000-000000000003',
--   '81000000-0000-0000-0000-000000000004',
--   '81000000-0000-0000-0000-000000000005',
--   '81000000-0000-0000-0000-000000000006',
--   '81000000-0000-0000-0000-000000000007',
--   '81000000-0000-0000-0000-000000000008'
-- );
-- DELETE FROM frequencia_professor WHERE id_frequencia_professor IN (
--   '80000000-0000-0000-0000-000000000001',
--   '80000000-0000-0000-0000-000000000002'
-- );
-- DELETE FROM aula WHERE id_aula IN (
--   '70000000-0000-0000-0000-000000000001',
--   '70000000-0000-0000-0000-000000000002',
--   '70000000-0000-0000-0000-000000000003'
-- );
-- DELETE FROM matricula WHERE id_matricula IN (
--   '60000000-0000-0000-0000-000000000001',
--   '60000000-0000-0000-0000-000000000002',
--   '60000000-0000-0000-0000-000000000003',
--   '60000000-0000-0000-0000-000000000004'
-- );
-- DELETE FROM aluno WHERE id_aluno IN (
--   '50000000-0000-0000-0000-000000000001',
--   '50000000-0000-0000-0000-000000000002',
--   '50000000-0000-0000-0000-000000000003',
--   '50000000-0000-0000-0000-000000000004'
-- );
-- DELETE FROM professor_turma_disciplina WHERE id_professor_turma_disciplina IN (
--   '40000000-0000-0000-0000-000000000001',
--   '40000000-0000-0000-0000-000000000002'
-- );
-- DELETE FROM professor WHERE id_professor IN (
--   '30000000-0000-0000-0000-000000000001',
--   '30000000-0000-0000-0000-000000000002'
-- );
-- DELETE FROM funcionario WHERE id_funcionario IN (
--   '31000000-0000-0000-0000-000000000001',
--   '31000000-0000-0000-0000-000000000002'
-- );
-- DELETE FROM turma_disciplina WHERE id_turma_disciplina IN (
--   '22000000-0000-0000-0000-000000000001',
--   '22000000-0000-0000-0000-000000000002'
-- );
-- DELETE FROM turma WHERE id_turma = '21000000-0000-0000-0000-000000000001';
-- DELETE FROM disciplina WHERE id_disciplina IN (
--   '20000000-0000-0000-0000-000000000001',
--   '20000000-0000-0000-0000-000000000002'
-- );
-- DELETE FROM serie WHERE id_serie = '12000000-0000-0000-0000-000000000001';
-- DELETE FROM periodo_letivo WHERE id_periodo_letivo = '10000000-0000-0000-0000-000000000001';
-- DELETE FROM pessoa WHERE id_pessoa IN (
--   '90000000-0000-0000-0000-000000000001',
--   '90000000-0000-0000-0000-000000000002',
--   '90000000-0000-0000-0000-000000000003',
--   '90000000-0000-0000-0000-000000000004',
--   '90000000-0000-0000-0000-000000000005',
--   '90000000-0000-0000-0000-000000000006'
-- );

BEGIN;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM turno WHERE codigo = 'MANHA')
        OR NOT EXISTS (SELECT 1 FROM nivel_ensino WHERE codigo = 'ENSINO_FUNDAMENTAL')
        OR NOT EXISTS (SELECT 1 FROM status_aluno WHERE codigo = 'ATIVO')
        OR NOT EXISTS (SELECT 1 FROM tipo_matricula WHERE codigo = 'PRIMEIRA_MATRICULA')
        OR NOT EXISTS (SELECT 1 FROM status_matricula WHERE codigo = 'EFETIVADA')
        OR NOT EXISTS (SELECT 1 FROM situacao_frequencia WHERE codigo = 'PRESENTE')
        OR NOT EXISTS (SELECT 1 FROM situacao_frequencia WHERE codigo = 'FALTA')
        OR NOT EXISTS (SELECT 1 FROM situacao_frequencia WHERE codigo = 'FALTA_JUSTIFICADA')
    THEN
        RAISE EXCEPTION 'Catalogos obrigatorios ausentes. Execute a carga base oficial antes desta massa.';
    END IF;
END $$;

INSERT INTO pessoa (
    id_pessoa, nome_completo, cpf, rg, orgao_emissor_rg, uf_rg, email, telefone,
    data_nascimento, sexo, nacionalidade, naturalidade, ativo
) VALUES
    ('90000000-0000-0000-0000-000000000001', 'Mariana Teste Professora', '99100000001', 'MG100001', 'SSP', 'SP', 'mariana.professora.teste@escola.local', '(11) 90000-0001', '1984-03-12', 'FEMININO', 'Brasileira', 'Sao Paulo', true),
    ('90000000-0000-0000-0000-000000000002', 'Carlos Teste Professor', '99100000002', 'MG100002', 'SSP', 'SP', 'carlos.professor.teste@escola.local', '(11) 90000-0002', '1979-08-22', 'MASCULINO', 'Brasileiro', 'Campinas', true),
    ('90000000-0000-0000-0000-000000000003', 'Ana Teste Aluna', '99100000003', 'MG100003', 'SSP', 'SP', 'ana.aluna.teste@escola.local', '(11) 90000-0003', '2016-01-10', 'FEMININO', 'Brasileira', 'Sao Paulo', true),
    ('90000000-0000-0000-0000-000000000004', 'Bruno Teste Aluno', '99100000004', 'MG100004', 'SSP', 'SP', 'bruno.aluno.teste@escola.local', '(11) 90000-0004', '2015-11-19', 'MASCULINO', 'Brasileiro', 'Sao Paulo', true),
    ('90000000-0000-0000-0000-000000000005', 'Clara Teste Aluna', '99100000005', 'MG100005', 'SSP', 'SP', 'clara.aluna.teste@escola.local', '(11) 90000-0005', '2016-04-03', 'FEMININO', 'Brasileira', 'Santo Andre', true),
    ('90000000-0000-0000-0000-000000000006', 'Diego Teste Aluno', '99100000006', 'MG100006', 'SSP', 'SP', 'diego.aluno.teste@escola.local', '(11) 90000-0006', '2015-07-28', 'MASCULINO', 'Brasileiro', 'Osasco', true)
ON CONFLICT (id_pessoa) DO UPDATE SET
    nome_completo = EXCLUDED.nome_completo,
    email = EXCLUDED.email,
    telefone = EXCLUDED.telefone,
    ativo = true,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO funcionario (id_funcionario, id_pessoa, id_cargo, ativo)
SELECT '31000000-0000-0000-0000-000000000001', '90000000-0000-0000-0000-000000000001', id_cargo, true
FROM (SELECT COALESCE((SELECT id_cargo FROM cargo WHERE codigo = 'COORDENADOR'), (SELECT id_cargo FROM cargo LIMIT 1)) AS id_cargo) catalog
ON CONFLICT (id_funcionario) DO UPDATE SET ativo = true, updated_at = CURRENT_TIMESTAMP;

INSERT INTO funcionario (id_funcionario, id_pessoa, id_cargo, ativo)
SELECT '31000000-0000-0000-0000-000000000002', '90000000-0000-0000-0000-000000000002', id_cargo, true
FROM (SELECT COALESCE((SELECT id_cargo FROM cargo WHERE codigo = 'COORDENADOR'), (SELECT id_cargo FROM cargo LIMIT 1)) AS id_cargo) catalog
ON CONFLICT (id_funcionario) DO UPDATE SET ativo = true, updated_at = CURRENT_TIMESTAMP;

INSERT INTO professor (id_professor, id_pessoa, registro_profissional, formacao, ativo) VALUES
    ('30000000-0000-0000-0000-000000000001', '90000000-0000-0000-0000-000000000001', 'PROF-TESTE-001', 'Pedagogia e Matematica', true),
    ('30000000-0000-0000-0000-000000000002', '90000000-0000-0000-0000-000000000002', 'PROF-TESTE-002', 'Letras', true)
ON CONFLICT (id_professor) DO UPDATE SET
    registro_profissional = EXCLUDED.registro_profissional,
    formacao = EXCLUDED.formacao,
    ativo = true,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO aluno (id_aluno, id_pessoa, id_status_aluno, ra, rm, emancipado, data_ingresso, ativo)
SELECT data.id_aluno::uuid, data.id_pessoa::uuid, catalog.id_status_aluno, data.ra, data.rm, false, CURRENT_DATE - INTERVAL '90 days', true
FROM (
    VALUES
        ('50000000-0000-0000-0000-000000000001', '90000000-0000-0000-0000-000000000003', 'RA-TESTE-001', 'RM-TESTE-001'),
        ('50000000-0000-0000-0000-000000000002', '90000000-0000-0000-0000-000000000004', 'RA-TESTE-002', 'RM-TESTE-002'),
        ('50000000-0000-0000-0000-000000000003', '90000000-0000-0000-0000-000000000005', 'RA-TESTE-003', 'RM-TESTE-003'),
        ('50000000-0000-0000-0000-000000000004', '90000000-0000-0000-0000-000000000006', 'RA-TESTE-004', 'RM-TESTE-004')
) AS data(id_aluno, id_pessoa, ra, rm)
CROSS JOIN (SELECT id_status_aluno FROM status_aluno WHERE codigo = 'ATIVO') catalog
ON CONFLICT (id_aluno) DO UPDATE SET
    id_status_aluno = EXCLUDED.id_status_aluno,
    ra = EXCLUDED.ra,
    rm = EXCLUDED.rm,
    ativo = true,
    data_exclusao = NULL,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO periodo_letivo (id_periodo_letivo, nome, ano, data_inicio, data_fim, ativo)
VALUES (
    '10000000-0000-0000-0000-000000000001',
    'Ano Letivo Teste 2026',
    2026,
    DATE '2026-02-02',
    DATE '2026-12-18',
    true
)
ON CONFLICT (id_periodo_letivo) DO UPDATE SET
    nome = EXCLUDED.nome,
    ano = EXCLUDED.ano,
    data_inicio = EXCLUDED.data_inicio,
    data_fim = EXCLUDED.data_fim,
    ativo = true;

INSERT INTO serie (id_serie, id_nivel_ensino, nome, ordem)
SELECT
    '12000000-0000-0000-0000-000000000001',
    id_nivel_ensino,
    '3 ano - Teste',
    3
FROM nivel_ensino
WHERE codigo = 'ENSINO_FUNDAMENTAL'
ON CONFLICT (id_serie) DO UPDATE SET
    nome = EXCLUDED.nome,
    ordem = EXCLUDED.ordem,
    id_nivel_ensino = EXCLUDED.id_nivel_ensino;

INSERT INTO disciplina (id_disciplina, nome, carga_horaria, ativo) VALUES
    ('20000000-0000-0000-0000-000000000001', 'Matematica - Teste', 120, true),
    ('20000000-0000-0000-0000-000000000002', 'Lingua Portuguesa - Teste', 120, true)
ON CONFLICT (id_disciplina) DO UPDATE SET
    nome = EXCLUDED.nome,
    carga_horaria = EXCLUDED.carga_horaria,
    ativo = true;

INSERT INTO turma (id_turma, id_periodo_letivo, id_serie, id_turno, codigo, nome, capacidade, ativo)
SELECT
    '21000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001',
    '12000000-0000-0000-0000-000000000001',
    turno.id_turno,
    '3A-TESTE',
    '3 ano A - Teste',
    30,
    true
FROM turno
WHERE turno.codigo = 'MANHA'
ON CONFLICT (id_turma) DO UPDATE SET
    nome = EXCLUDED.nome,
    capacidade = EXCLUDED.capacidade,
    ativo = true;

INSERT INTO turma_disciplina (id_turma_disciplina, id_turma, id_disciplina, carga_horaria) VALUES
    ('22000000-0000-0000-0000-000000000001', '21000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 120),
    ('22000000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', 120)
ON CONFLICT (id_turma_disciplina) DO UPDATE SET
    carga_horaria = EXCLUDED.carga_horaria;

INSERT INTO professor_turma_disciplina (
    id_professor_turma_disciplina, id_professor, id_turma_disciplina, data_inicio, data_fim, ativo
) VALUES
    ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '22000000-0000-0000-0000-000000000001', DATE '2026-02-02', NULL, true),
    ('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', '22000000-0000-0000-0000-000000000002', DATE '2026-02-02', NULL, true)
ON CONFLICT (id_professor_turma_disciplina) DO UPDATE SET
    data_inicio = EXCLUDED.data_inicio,
    data_fim = NULL,
    ativo = true;

INSERT INTO matricula (
    id_matricula, id_aluno, id_turma, id_periodo_letivo, id_tipo_matricula, id_status_matricula,
    data_solicitacao, data_efetivacao, observacao
)
SELECT
    data.id_matricula::uuid,
    data.id_aluno::uuid,
    '21000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001',
    tipo.id_tipo_matricula,
    status.id_status_matricula,
    DATE '2026-01-15',
    DATE '2026-01-20',
    data.observacao
FROM (
    VALUES
        ('60000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', 'Massa teste: aluna com presenca e falta justificada.'),
        ('60000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000002', 'Massa teste: aluno com presenca e falta.'),
        ('60000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000003', 'Massa teste: aluna com presenca.'),
        ('60000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000004', 'Massa teste: aluno sem lancamento em uma aula para testar registro.')
) AS data(id_matricula, id_aluno, observacao)
CROSS JOIN (SELECT id_tipo_matricula FROM tipo_matricula WHERE codigo = 'PRIMEIRA_MATRICULA') tipo
CROSS JOIN (SELECT id_status_matricula FROM status_matricula WHERE codigo = 'EFETIVADA') status
ON CONFLICT (id_matricula) DO UPDATE SET
    id_status_matricula = EXCLUDED.id_status_matricula,
    data_efetivacao = EXCLUDED.data_efetivacao,
    observacao = EXCLUDED.observacao,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO aula (
    id_aula, id_professor_turma_disciplina, data_aula, horario_inicio, horario_fim,
    conteudo_ministrado, observacao, realizada
) VALUES
    ('70000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', DATE '2026-06-01', TIME '07:30', TIME '08:20', 'Operacoes com multiplicacao e resolucao de problemas.', 'Aula realizada para testar frequencia completa.', true),
    ('70000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000001', DATE '2026-06-03', TIME '07:30', TIME '08:20', 'Divisao e estrategias de calculo mental.', 'Aula realizada com uma frequencia pendente para registro manual.', true),
    ('70000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000002', DATE '2026-06-04', TIME '09:10', TIME '10:00', 'Leitura, interpretacao de texto e producao curta.', 'Aula planejada sem frequencias para testar lancamento inicial.', false)
ON CONFLICT (id_aula) DO UPDATE SET
    data_aula = EXCLUDED.data_aula,
    horario_inicio = EXCLUDED.horario_inicio,
    horario_fim = EXCLUDED.horario_fim,
    conteudo_ministrado = EXCLUDED.conteudo_ministrado,
    observacao = EXCLUDED.observacao,
    realizada = EXCLUDED.realizada;

INSERT INTO frequencia_professor (
    id_frequencia_professor, id_aula, id_professor, presente, justificativa
) VALUES
    ('80000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', true, NULL),
    ('80000000-0000-0000-0000-000000000002', '70000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', true, 'Registro confirmado pela secretaria.')
ON CONFLICT (id_frequencia_professor) DO UPDATE SET
    presente = EXCLUDED.presente,
    justificativa = EXCLUDED.justificativa;

INSERT INTO frequencia_aluno (
    id_frequencia_aluno, id_aula, id_matricula, id_situacao_frequencia, justificativa
)
SELECT data.id_frequencia_aluno::uuid, data.id_aula::uuid, data.id_matricula::uuid, situacao.id_situacao_frequencia, data.justificativa
FROM (
    VALUES
        ('81000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000001', 'PRESENTE', NULL),
        ('81000000-0000-0000-0000-000000000002', '70000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000002', 'PRESENTE', NULL),
        ('81000000-0000-0000-0000-000000000003', '70000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000003', 'FALTA_JUSTIFICADA', 'Atestado informado pela familia.'),
        ('81000000-0000-0000-0000-000000000004', '70000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000004', 'FALTA', 'Ausencia sem justificativa.'),
        ('81000000-0000-0000-0000-000000000005', '70000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000001', 'PRESENTE', NULL),
        ('81000000-0000-0000-0000-000000000006', '70000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000002', 'FALTA', 'Faltou sem aviso previo.'),
        ('81000000-0000-0000-0000-000000000007', '70000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000003', 'PRESENTE', NULL)
) AS data(id_frequencia_aluno, id_aula, id_matricula, situacao_codigo, justificativa)
JOIN situacao_frequencia situacao ON situacao.codigo = data.situacao_codigo
ON CONFLICT (id_frequencia_aluno) DO UPDATE SET
    id_situacao_frequencia = EXCLUDED.id_situacao_frequencia,
    justificativa = EXCLUDED.justificativa;

COMMIT;
