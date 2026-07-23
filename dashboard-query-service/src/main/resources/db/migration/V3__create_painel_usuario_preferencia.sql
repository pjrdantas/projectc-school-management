CREATE TABLE painel_usuario_preferencia (
    id UUID PRIMARY KEY,
    escola_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    widget_id UUID NOT NULL,
    visivel BOOLEAN NOT NULL,
    ordem INTEGER,
    configuracao_json TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_painel_usuario_preferencia_widget FOREIGN KEY (widget_id) REFERENCES painel_widget (id),
    CONSTRAINT uk_painel_usuario_preferencia_widget UNIQUE (escola_id, usuario_id, widget_id)
);

CREATE INDEX idx_painel_usuario_preferencia_usuario ON painel_usuario_preferencia (escola_id, usuario_id, updated_at);
