ALTER TABLE planejamento_bimestral_avaliacao
    ADD COLUMN chave_idempotencia VARCHAR(128) NOT NULL DEFAULT '';

CREATE UNIQUE INDEX uk_planejamento_bimestral_avaliacao_idempotencia
    ON planejamento_bimestral_avaliacao(id_planejamento_bimestral, chave_idempotencia);
