CREATE TABLE IF NOT EXISTS aluno (
    id_aluno UUID NOT NULL PRIMARY KEY,
    id_pessoa UUID,
    nome_completo VARCHAR(150) NOT NULL,
    cpf VARCHAR(14),
    email VARCHAR(150),
    telefone VARCHAR(20),
    data_nascimento DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_people_read_model_aluno_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id_pessoa)
);

CREATE INDEX IF NOT EXISTS idx_people_read_model_aluno_nome
    ON aluno(nome_completo);

CREATE INDEX IF NOT EXISTS idx_people_read_model_aluno_cpf
    ON aluno(cpf);

CREATE INDEX IF NOT EXISTS idx_people_read_model_aluno_pessoa
    ON aluno(id_pessoa);

CREATE TABLE IF NOT EXISTS responsavel (
    id_responsavel UUID NOT NULL PRIMARY KEY,
    id_pessoa UUID,
    nome_completo VARCHAR(150) NOT NULL,
    cpf VARCHAR(14),
    email VARCHAR(150),
    telefone VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_people_read_model_responsavel_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id_pessoa)
);

CREATE INDEX IF NOT EXISTS idx_people_read_model_responsavel_nome
    ON responsavel(nome_completo);

CREATE INDEX IF NOT EXISTS idx_people_read_model_responsavel_cpf
    ON responsavel(cpf);

CREATE INDEX IF NOT EXISTS idx_people_read_model_responsavel_pessoa
    ON responsavel(id_pessoa);

CREATE TABLE IF NOT EXISTS aluno_responsavel (
    id_aluno_responsavel UUID NOT NULL PRIMARY KEY,
    id_aluno UUID NOT NULL,
    id_responsavel UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_people_read_model_aluno_responsavel_aluno
        FOREIGN KEY (id_aluno) REFERENCES aluno(id_aluno),
    CONSTRAINT fk_people_read_model_aluno_responsavel_responsavel
        FOREIGN KEY (id_responsavel) REFERENCES responsavel(id_responsavel),
    CONSTRAINT uk_people_read_model_aluno_responsavel
        UNIQUE (id_aluno, id_responsavel)
);

CREATE INDEX IF NOT EXISTS idx_people_read_model_aluno_responsavel_aluno
    ON aluno_responsavel(id_aluno);

CREATE INDEX IF NOT EXISTS idx_people_read_model_aluno_responsavel_responsavel
    ON aluno_responsavel(id_responsavel);
