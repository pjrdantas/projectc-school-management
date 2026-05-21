-- Permite inserts via @ManyToMany sem precisar informar as PKs técnicas das tabelas de junção
ALTER TABLE usuario_perfil
    ALTER COLUMN id_usuario_perfil SET DEFAULT gen_random_uuid();

ALTER TABLE perfil_permissao
    ALTER COLUMN id_perfil_permissao SET DEFAULT gen_random_uuid();
