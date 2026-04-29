INSERT INTO perfil (id_perfil, codigo, nome)
SELECT '11111111-1111-1111-1111-111111111111', 'ADMIN', 'Administrador'
WHERE NOT EXISTS (
    SELECT 1 FROM perfil WHERE codigo = 'ADMIN'
);

INSERT INTO permissao (id_permissao, codigo, descricao)
SELECT '22222222-2222-2222-2222-222222222222', 'USUARIO', 'Gerenciar usuários'
WHERE NOT EXISTS (
    SELECT 1 FROM permissao WHERE codigo = 'USUARIO'
);

INSERT INTO perfil_permissao (id_perfil_permissao, id_perfil, id_permissao)
SELECT
    '33333333-3333-3333-3333-333333333333',
    '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222'
WHERE NOT EXISTS (
    SELECT 1
    FROM perfil_permissao
    WHERE id_perfil = '11111111-1111-1111-1111-111111111111'
      AND id_permissao = '22222222-2222-2222-2222-222222222222'
);

INSERT INTO usuario (id_usuario, username, nome, email, senha_hash, ativo)
SELECT
    '44444444-4444-4444-4444-444444444444',
    'admin',
    'Administrador',
    'admin@local.test',
    '$2a$10$7bWVHpUojDItmgyTg5GY3ufBgdqzqmArli650DhxJLCRML2tjbxtG',
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM usuario WHERE username = 'admin'
);

INSERT INTO usuario_perfil (id_usuario_perfil, id_usuario, id_perfil)
SELECT
    '55555555-5555-5555-5555-555555555555',
    '44444444-4444-4444-4444-444444444444',
    '11111111-1111-1111-1111-111111111111'
WHERE NOT EXISTS (
    SELECT 1
    FROM usuario_perfil
    WHERE id_usuario = '44444444-4444-4444-4444-444444444444'
      AND id_perfil = '11111111-1111-1111-1111-111111111111'
);
