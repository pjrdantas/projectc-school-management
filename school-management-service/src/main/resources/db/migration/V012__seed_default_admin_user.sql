INSERT INTO perfil (id_perfil, codigo, nome)
SELECT '11111111-1111-1111-1111-111111111111', 'ADMIN', 'Administrador'
WHERE NOT EXISTS (
    SELECT 1 FROM perfil WHERE codigo = 'ADMIN' OR id_perfil = '11111111-1111-1111-1111-111111111111'
);

INSERT INTO permissao (id_permissao, codigo, descricao)
SELECT '22222222-2222-2222-2222-222222222222', 'USUARIO', 'Gerenciar usuários'
WHERE NOT EXISTS (
    SELECT 1 FROM permissao WHERE codigo = 'USUARIO' OR id_permissao = '22222222-2222-2222-2222-222222222222'
);

INSERT INTO perfil_permissao (id_perfil_permissao, id_perfil, id_permissao)
SELECT
    '33333333-3333-3333-3333-333333333333',
    p.id_perfil,
    pe.id_permissao
FROM perfil p
JOIN permissao pe ON pe.codigo = 'USUARIO'
WHERE p.codigo = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM perfil_permissao pp
      WHERE pp.id_perfil_permissao = '33333333-3333-3333-3333-333333333333'
         OR (pp.id_perfil = p.id_perfil AND pp.id_permissao = pe.id_permissao)
  );

INSERT INTO usuario (id_usuario, username, nome, email, senha_hash, ativo)
SELECT
    '44444444-4444-4444-4444-444444444444',
    'admin',
    'Administrador',
    'admin@local.test',
    '$2a$10$8WAxDjIVLhL2.XAhJP0uyuC9O64.iGkLpcD1TQFxJUutLD8dnGxxO',
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM usuario WHERE username = 'admin' OR id_usuario = '44444444-4444-4444-4444-444444444444'
);

INSERT INTO usuario_perfil (id_usuario_perfil, id_usuario, id_perfil)
SELECT
    '55555555-5555-5555-5555-555555555555',
    u.id_usuario,
    p.id_perfil
FROM usuario u
JOIN perfil p ON p.codigo = 'ADMIN'
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM usuario_perfil up
      WHERE up.id_usuario_perfil = '55555555-5555-5555-5555-555555555555'
         OR (up.id_usuario = u.id_usuario AND up.id_perfil = p.id_perfil)
  );
