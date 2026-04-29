ALTER TABLE sessao_autenticacao
    ADD COLUMN access_token_hash VARCHAR(255);

ALTER TABLE sessao_autenticacao
    ADD COLUMN access_expira_em TIMESTAMP;

CREATE INDEX idx_sessao_autenticacao_access_token_hash ON sessao_autenticacao (access_token_hash);
