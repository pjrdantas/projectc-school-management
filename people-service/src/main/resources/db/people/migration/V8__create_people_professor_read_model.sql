CREATE TABLE IF NOT EXISTS people_professor_read_model (
    id_professor UUID NOT NULL PRIMARY KEY,
    id_pessoa UUID NOT NULL,
    id_funcionario UUID,
    id_escola UUID NOT NULL,
    nome_completo VARCHAR(200) NOT NULL,
    ativo BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
