CREATE TABLE planejamento_ia_interacao (
    id_planejamento_ia_interacao uuid PRIMARY KEY,
    id_escola uuid NOT NULL,
    id_planejamento_bimestral uuid NOT NULL,
    id_usuario uuid,
    prompt_professor text NOT NULL,
    resposta_ia text NOT NULL,
    modelo_ia varchar(120),
    tokens_entrada integer,
    tokens_saida integer,
    custo_estimado numeric(12, 4),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE planejamento_ia_conteudo_gerado (
    id_planejamento_ia_conteudo_gerado uuid PRIMARY KEY,
    id_escola uuid NOT NULL,
    id_planejamento_bimestral uuid NOT NULL,
    id_planejamento_ia_interacao uuid,
    titulo varchar(180) NOT NULL,
    conteudo text NOT NULL,
    versao integer NOT NULL,
    hash_conteudo varchar(128),
    aprovado_pelo_professor boolean NOT NULL DEFAULT false,
    reutilizavel boolean NOT NULL DEFAULT true,
    ativo boolean NOT NULL DEFAULT true,
    status varchar(40) NOT NULL,
    tipo_conteudo varchar(60) NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp,
    CONSTRAINT fk_planning_ai_conteudo_interacao FOREIGN KEY (id_planejamento_ia_interacao)
        REFERENCES planejamento_ia_interacao(id_planejamento_ia_interacao),
    CONSTRAINT ck_planning_ai_conteudo_versao CHECK (versao > 0)
);

CREATE TABLE planejamento_ia_conteudo_versao (
    id_planejamento_ia_conteudo_versao uuid PRIMARY KEY,
    id_escola uuid NOT NULL,
    id_planejamento_ia_conteudo_gerado uuid NOT NULL,
    alterado_por uuid,
    numero_versao integer NOT NULL,
    conteudo text NOT NULL,
    motivo_alteracao text,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_planning_ai_versao_conteudo FOREIGN KEY (id_planejamento_ia_conteudo_gerado)
        REFERENCES planejamento_ia_conteudo_gerado(id_planejamento_ia_conteudo_gerado),
    CONSTRAINT uk_planning_ai_versao_numero UNIQUE (id_planejamento_ia_conteudo_gerado, numero_versao),
    CONSTRAINT ck_planning_ai_versao_numero CHECK (numero_versao > 0)
);

CREATE TABLE biblioteca_conteudo_pedagogico (
    id_biblioteca_conteudo_pedagogico uuid PRIMARY KEY,
    id_escola uuid NOT NULL,
    id_conteudo_origem uuid NOT NULL,
    id_professor uuid,
    id_disciplina uuid,
    tipo_conteudo varchar(60) NOT NULL,
    titulo varchar(180) NOT NULL,
    tema varchar(180),
    conteudo text NOT NULL,
    origem varchar(40) NOT NULL,
    reutilizavel boolean NOT NULL DEFAULT true,
    ativo boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp,
    CONSTRAINT fk_biblioteca_conteudo_origem FOREIGN KEY (id_conteudo_origem)
        REFERENCES planejamento_ia_conteudo_gerado(id_planejamento_ia_conteudo_gerado)
);

CREATE INDEX idx_planning_ai_interacao_escola_planejamento
    ON planejamento_ia_interacao(id_escola, id_planejamento_bimestral);

CREATE INDEX idx_planning_ai_conteudo_escola_planejamento
    ON planejamento_ia_conteudo_gerado(id_escola, id_planejamento_bimestral);

CREATE INDEX idx_planning_ai_conteudo_interacao
    ON planejamento_ia_conteudo_gerado(id_planejamento_ia_interacao);

CREATE INDEX idx_planning_ai_versao_escola_conteudo
    ON planejamento_ia_conteudo_versao(id_escola, id_planejamento_ia_conteudo_gerado);

CREATE INDEX idx_biblioteca_escola_tipo
    ON biblioteca_conteudo_pedagogico(id_escola, tipo_conteudo);

