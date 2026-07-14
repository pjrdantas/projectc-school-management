ALTER TABLE planejamento_ia_interacao
    ADD COLUMN escola_nome varchar(180);

ALTER TABLE planejamento_ia_conteudo_gerado
    ADD COLUMN escola_nome varchar(180);

ALTER TABLE biblioteca_conteudo_pedagogico
    ADD COLUMN escola_nome varchar(180);

ALTER TABLE biblioteca_conteudo_pedagogico
    ADD COLUMN professor_nome varchar(180);

ALTER TABLE biblioteca_conteudo_pedagogico
    ADD COLUMN disciplina_nome varchar(180);
