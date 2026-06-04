ALTER TABLE dashboard_usuario_configuracao
    ADD COLUMN IF NOT EXISTS configuracao_json TEXT;
