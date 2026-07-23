CREATE TABLE painel_indicador_snapshot (
    id UUID PRIMARY KEY,
    escola_id UUID NOT NULL,
    publico_id UUID NOT NULL,
    codigo_indicador VARCHAR(100) NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    valor_numeric DECIMAL(19, 4),
    valor_texto VARCHAR(255),
    escola_nome VARCHAR(255),
    referencia_data DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_painel_indicador_snapshot_publico FOREIGN KEY (publico_id) REFERENCES painel_publico (id),
    CONSTRAINT uk_painel_indicador_snapshot_chave UNIQUE (escola_id, publico_id, codigo_indicador, referencia_data)
);

CREATE INDEX idx_painel_indicador_snapshot_historico
    ON painel_indicador_snapshot (escola_id, publico_id, codigo_indicador, referencia_data);
