CREATE TABLE planejamento_bimestral (
    id_planejamento_bimestral UUID PRIMARY KEY,
    id_escola UUID NOT NULL,
    id_professor_turma_disciplina UUID NOT NULL,
    id_periodo_avaliativo UUID,
    status VARCHAR(60) NOT NULL DEFAULT 'RASCUNHO',
    titulo VARCHAR(180) NOT NULL,
    tema_principal VARCHAR(180) NOT NULL,
    descricao_inicial TEXT NOT NULL,
    objetivo_geral TEXT,
    observacao_professor TEXT,
    conteudo_final_aprovado TEXT,
    reutilizavel BOOLEAN NOT NULL DEFAULT TRUE,
    criado_com_auxilio_ia BOOLEAN NOT NULL DEFAULT FALSE,
    aprovado_pelo_professor BOOLEAN NOT NULL DEFAULT FALSE,
    data_aprovacao TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE planejamento_bimestral_aula (
    id_planejamento_bimestral_aula UUID PRIMARY KEY,
    id_planejamento_bimestral UUID NOT NULL,
    numero_aula INTEGER NOT NULL,
    tema_aula VARCHAR(180) NOT NULL,
    objetivo_aula TEXT,
    conteudo_previsto TEXT,
    metodologia TEXT,
    recursos TEXT,
    atividade_prevista TEXT,
    observacao TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_planejamento_bimestral_aula_planejamento FOREIGN KEY (id_planejamento_bimestral)
        REFERENCES planejamento_bimestral(id_planejamento_bimestral),
    CONSTRAINT uk_planejamento_bimestral_aula_numero UNIQUE (id_planejamento_bimestral, numero_aula),
    CONSTRAINT ck_planejamento_bimestral_aula_numero CHECK (numero_aula > 0)
);

CREATE TABLE planejamento_bimestral_avaliacao (
    id_planejamento_bimestral_avaliacao UUID PRIMARY KEY,
    id_planejamento_bimestral UUID NOT NULL,
    titulo VARCHAR(180) NOT NULL,
    descricao TEXT,
    data_prevista DATE,
    peso NUMERIC(12, 4) NOT NULL,
    valor_maximo NUMERIC(12, 4),
    tipo_avaliacao VARCHAR(50) NOT NULL,
    conteudo_cobrado TEXT,
    orientacao_aplicacao TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_planejamento_bimestral_avaliacao_planejamento FOREIGN KEY (id_planejamento_bimestral)
        REFERENCES planejamento_bimestral(id_planejamento_bimestral),
    CONSTRAINT ck_planejamento_bimestral_avaliacao_peso CHECK (peso > 0),
    CONSTRAINT ck_planejamento_bimestral_avaliacao_valor CHECK (valor_maximo IS NULL OR valor_maximo > 0)
);

CREATE INDEX idx_planejamento_bimestral_escola ON planejamento_bimestral(id_escola, created_at);
CREATE INDEX idx_planejamento_bimestral_professor_turma_disciplina
    ON planejamento_bimestral(id_professor_turma_disciplina);
CREATE INDEX idx_planejamento_bimestral_periodo ON planejamento_bimestral(id_periodo_avaliativo);
