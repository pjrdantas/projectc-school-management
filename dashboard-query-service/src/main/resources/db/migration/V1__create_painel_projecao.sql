CREATE TABLE painel_projecao (
    id UUID PRIMARY KEY,
    escola_id UUID NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    chave_projecao VARCHAR(500) NOT NULL,
    publico_codigo VARCHAR(100),
    professor_id UUID,
    usuario_id UUID,
    referencia_data DATE,
    payload TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_painel_projecao_escola_chave UNIQUE (escola_id, chave_projecao)
);

CREATE INDEX idx_painel_projecao_escola_tipo
    ON painel_projecao (escola_id, tipo);
