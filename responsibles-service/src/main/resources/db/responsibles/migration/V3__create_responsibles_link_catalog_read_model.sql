CREATE TABLE IF NOT EXISTS parentesco (
    id_parentesco UUID NOT NULL PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    descricao VARCHAR(120) NOT NULL,
    CONSTRAINT uk_responsibles_read_model_parentesco_codigo UNIQUE (codigo)
);
