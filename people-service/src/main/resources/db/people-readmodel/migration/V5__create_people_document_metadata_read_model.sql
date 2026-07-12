CREATE TABLE IF NOT EXISTS people_documento_read_model (
    id_pessoa_documento UUID PRIMARY KEY,
    id_pessoa UUID NOT NULL,
    id_documento UUID NOT NULL,
    id_tipo_documento UUID NOT NULL,
    tipo_documento_codigo VARCHAR(100) NOT NULL,
    tipo_documento_descricao VARCHAR(200) NOT NULL,
    numero_documento VARCHAR(120),
    caminho_arquivo VARCHAR(500) NOT NULL,
    observacao TEXT,
    data_upload TIMESTAMP WITH TIME ZONE,
    id_escola UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_people_documento_read_model_documento
    ON people_documento_read_model (id_documento);

CREATE INDEX IF NOT EXISTS idx_people_documento_read_model_pessoa_escola
    ON people_documento_read_model (id_pessoa, id_escola);

CREATE INDEX IF NOT EXISTS idx_people_documento_read_model_escola_data_upload
    ON people_documento_read_model (id_escola, data_upload DESC);
