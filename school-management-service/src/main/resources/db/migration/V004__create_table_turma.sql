CREATE TABLE turma (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    capacidade INTEGER NOT NULL,
    periodo_letivo_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_turma_periodo_letivo FOREIGN KEY (periodo_letivo_id) REFERENCES periodo_letivo (id),
    CONSTRAINT ck_turma_capacidade CHECK (capacidade > 0),
    CONSTRAINT uk_turma_codigo_periodo UNIQUE (codigo, periodo_letivo_id)
);
