CREATE TABLE periodo_letivo (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(80) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_periodo_letivo_datas CHECK (data_fim >= data_inicio)
);
