INSERT INTO usuario (id_usuario, username, nome, email, senha_hash, ativo)
SELECT
    '44444444-4444-4444-4444-444444444444',
    'admin',
    'Administrador',
    'admin@escola.com',
    '$2a$10$8WAxDjIVLhL2.XAhJP0uyuC9O64.iGkLpcD1TQFxJUutLD8dnGxxO',
    TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM usuario WHERE lower(username) = 'admin'
);

UPDATE usuario
SET senha_hash = '$2a$10$8WAxDjIVLhL2.XAhJP0uyuC9O64.iGkLpcD1TQFxJUutLD8dnGxxO',
    ativo = TRUE
WHERE lower(username) = 'admin';

INSERT INTO usuario_perfil (id_usuario_perfil, id_usuario, id_perfil)
SELECT 'cccccccc-cccc-cccc-cccc-cccccccccccc', u.id_usuario, p.id_perfil
FROM usuario u
JOIN perfil p ON p.codigo = 'ADMIN'
WHERE lower(u.username) = 'admin'
AND NOT EXISTS (
    SELECT 1 FROM usuario_perfil up
    WHERE up.id_usuario = u.id_usuario AND up.id_perfil = p.id_perfil
);
