-- Observação: por decisão do time, os dados atuais são de teste e podem ser descartados.
-- Portanto, esta migration recria as tabelas com UUID sem manter colunas legadas.

DROP TABLE IF EXISTS matricula;
DROP TABLE IF EXISTS turma;
DROP TABLE IF EXISTS periodo_letivo;
DROP TABLE IF EXISTS aluno;

CREATE TABLE aluno (
    id_aluno UUID PRIMARY KEY,
    nome_completo VARCHAR(150) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(150),
    telefone VARCHAR(20),
    data_nascimento DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE periodo_letivo (
    id_periodo_letivo UUID PRIMARY KEY,
    nome VARCHAR(80) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_periodo_letivo_datas CHECK (data_fim >= data_inicio)
);

CREATE TABLE turma (
    id_turma UUID PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    capacidade INTEGER NOT NULL,
    id_periodo_letivo UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_turma_periodo_letivo FOREIGN KEY (id_periodo_letivo) REFERENCES periodo_letivo (id_periodo_letivo),
    CONSTRAINT ck_turma_capacidade CHECK (capacidade > 0),
    CONSTRAINT uk_turma_codigo_periodo UNIQUE (codigo, id_periodo_letivo)
);

CREATE TABLE matricula (
    id_matricula UUID PRIMARY KEY,
    id_aluno UUID NOT NULL,
    id_turma UUID NOT NULL,
    id_periodo_letivo UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_matricula_aluno FOREIGN KEY (id_aluno) REFERENCES aluno (id_aluno),
    CONSTRAINT fk_matricula_turma FOREIGN KEY (id_turma) REFERENCES turma (id_turma),
    CONSTRAINT fk_matricula_periodo_letivo FOREIGN KEY (id_periodo_letivo) REFERENCES periodo_letivo (id_periodo_letivo),
    CONSTRAINT ck_matricula_status CHECK (status IN ('ATIVA'))
);

CREATE INDEX idx_matricula_aluno ON matricula (id_aluno);
CREATE INDEX idx_matricula_turma ON matricula (id_turma);
CREATE INDEX idx_turma_periodo ON turma (id_periodo_letivo);
