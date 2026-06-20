CREATE TABLE nivel_ensino (
    id_nivel_ensino uuid PRIMARY KEY,
    codigo varchar(40) NOT NULL UNIQUE,
    descricao varchar(120) NOT NULL
);

CREATE TABLE turno (
    id_turno uuid PRIMARY KEY,
    codigo varchar(40) NOT NULL UNIQUE,
    descricao varchar(120) NOT NULL
);

CREATE TABLE periodo_letivo (
    id_periodo_letivo uuid PRIMARY KEY,
    id_escola uuid NOT NULL,
    nome varchar(80) NOT NULL,
    ano integer NOT NULL,
    data_inicio date NOT NULL,
    data_fim date NOT NULL,
    ativo boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp,
    CONSTRAINT ck_periodo_letivo_ano CHECK (ano > 0),
    CONSTRAINT ck_periodo_letivo_datas CHECK (data_fim >= data_inicio),
    CONSTRAINT uk_periodo_letivo_id_escola UNIQUE (id_periodo_letivo, id_escola),
    CONSTRAINT uk_periodo_letivo_escola_nome UNIQUE (id_escola, nome)
);

CREATE TABLE serie (
    id_serie uuid PRIMARY KEY,
    id_escola uuid NOT NULL,
    nome varchar(80) NOT NULL,
    ordem integer NOT NULL,
    id_nivel_ensino uuid NOT NULL REFERENCES nivel_ensino(id_nivel_ensino),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp,
    CONSTRAINT ck_serie_ordem CHECK (ordem > 0),
    CONSTRAINT uk_serie_id_escola UNIQUE (id_serie, id_escola),
    CONSTRAINT uk_serie_escola_nome UNIQUE (id_escola, nome)
);

CREATE TABLE disciplina (
    id_disciplina uuid PRIMARY KEY,
    id_escola uuid NOT NULL,
    nome varchar(120) NOT NULL,
    carga_horaria integer,
    ativo boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp,
    CONSTRAINT ck_disciplina_carga_horaria CHECK (carga_horaria IS NULL OR carga_horaria > 0),
    CONSTRAINT uk_disciplina_id_escola UNIQUE (id_disciplina, id_escola),
    CONSTRAINT uk_disciplina_escola_nome UNIQUE (id_escola, nome)
);

CREATE TABLE turma (
    id_turma uuid PRIMARY KEY,
    id_escola uuid NOT NULL,
    codigo varchar(20) NOT NULL,
    nome varchar(120) NOT NULL,
    capacidade integer NOT NULL,
    id_periodo_letivo uuid NOT NULL,
    id_serie uuid NOT NULL,
    id_turno uuid NOT NULL REFERENCES turno(id_turno),
    ativo boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp,
    CONSTRAINT ck_turma_capacidade CHECK (capacidade > 0),
    CONSTRAINT uk_turma_id_escola UNIQUE (id_turma, id_escola),
    CONSTRAINT uk_turma_escola_periodo_codigo UNIQUE (id_escola, id_periodo_letivo, codigo),
    CONSTRAINT fk_turma_periodo_escola FOREIGN KEY (id_periodo_letivo, id_escola)
        REFERENCES periodo_letivo(id_periodo_letivo, id_escola),
    CONSTRAINT fk_turma_serie_escola FOREIGN KEY (id_serie, id_escola)
        REFERENCES serie(id_serie, id_escola)
);

CREATE TABLE turma_disciplina (
    id_turma_disciplina uuid PRIMARY KEY,
    id_escola uuid NOT NULL,
    id_turma uuid NOT NULL,
    id_disciplina uuid NOT NULL,
    carga_horaria integer,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_turma_disciplina_carga CHECK (carga_horaria IS NULL OR carga_horaria > 0),
    CONSTRAINT uk_turma_disciplina UNIQUE (id_turma, id_disciplina),
    CONSTRAINT fk_turma_disciplina_turma_escola FOREIGN KEY (id_turma, id_escola)
        REFERENCES turma(id_turma, id_escola),
    CONSTRAINT fk_turma_disciplina_disciplina_escola FOREIGN KEY (id_disciplina, id_escola)
        REFERENCES disciplina(id_disciplina, id_escola)
);

CREATE TABLE outbox_event (
    id_evento uuid PRIMARY KEY,
    aggregate_type varchar(100) NOT NULL,
    aggregate_id uuid NOT NULL,
    event_type varchar(180) NOT NULL,
    event_version integer NOT NULL,
    escola_id uuid NOT NULL,
    correlation_id varchar(100) NOT NULL,
    payload jsonb NOT NULL,
    status varchar(30) NOT NULL DEFAULT 'PENDENTE',
    occurred_at timestamp NOT NULL,
    published_at timestamp,
    attempts integer NOT NULL DEFAULT 0,
    CONSTRAINT ck_outbox_event_version CHECK (event_version > 0),
    CONSTRAINT ck_outbox_attempts CHECK (attempts >= 0)
);

CREATE INDEX idx_periodo_letivo_escola ON periodo_letivo(id_escola);
CREATE INDEX idx_serie_escola ON serie(id_escola);
CREATE INDEX idx_disciplina_escola ON disciplina(id_escola);
CREATE INDEX idx_turma_escola ON turma(id_escola);
CREATE INDEX idx_turma_disciplina_escola ON turma_disciplina(id_escola);
CREATE INDEX idx_outbox_status_occurred ON outbox_event(status, occurred_at);

