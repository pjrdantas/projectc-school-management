UPDATE permissao
SET codigo = 'ADMIN', descricao = 'Acesso administrativo'
WHERE codigo = 'USUARIO';

INSERT INTO permissao (id_permissao, codigo, descricao)
SELECT 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ADMIN', 'Acesso administrativo'
WHERE NOT EXISTS (SELECT 1 FROM permissao WHERE codigo = 'ADMIN');

INSERT INTO perfil_permissao (id_perfil_permissao, id_perfil, id_permissao)
SELECT
  'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
  p.id_perfil,
  pe.id_permissao
FROM perfil p
JOIN permissao pe ON pe.codigo = 'ADMIN'
WHERE p.codigo = 'ADMIN'
AND NOT EXISTS (
  SELECT 1 FROM perfil_permissao pp
  WHERE pp.id_perfil = p.id_perfil AND pp.id_permissao = pe.id_permissao
);
