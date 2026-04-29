INSERT INTO perfil (id_perfil, codigo, nome)
VALUES ('11111111-1111-1111-1111-111111111111', 'ADMIN', 'Administrador');

INSERT INTO permissao (id_permissao, codigo, descricao)
VALUES ('22222222-2222-2222-2222-222222222222', 'USUARIO', 'Gerenciar usuários');

INSERT INTO perfil_permissao (id_perfil_permissao, id_perfil, id_permissao)
VALUES (
    '33333333-3333-3333-3333-333333333333',
    '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222'
);

INSERT INTO usuario (id_usuario, username, nome, email, senha_hash, ativo)
VALUES (
    '44444444-4444-4444-4444-444444444444',
    'admin',
    'Administrador',
    'admin@local.test',
    '$2a$10$7bWVHpUojDItmgyTg5GY3ufBgdqzqmArli650DhxJLCRML2tjbxtG',
    TRUE
);

INSERT INTO usuario_perfil (id_usuario_perfil, id_usuario, id_perfil)
VALUES (
    '55555555-5555-5555-5555-555555555555',
    '44444444-4444-4444-4444-444444444444',
    '11111111-1111-1111-1111-111111111111'
);
