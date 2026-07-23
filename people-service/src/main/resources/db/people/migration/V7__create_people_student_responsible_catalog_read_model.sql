CREATE TABLE IF NOT EXISTS status_aluno (
    id_status_aluno UUID NOT NULL PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    descricao VARCHAR(120) NOT NULL,
    CONSTRAINT uk_people_read_model_status_aluno_codigo UNIQUE (codigo)
);

CREATE TABLE IF NOT EXISTS parentesco (
    id_parentesco UUID NOT NULL PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL,
    descricao VARCHAR(120) NOT NULL,
    CONSTRAINT uk_people_read_model_parentesco_codigo UNIQUE (codigo)
);
