CREATE TABLE planejamento_ia_read_model_sync_state (
    id_sync_state varchar(512) PRIMARY KEY,
    scope varchar(80) NOT NULL,
    id_escola uuid NOT NULL,
    id_referencia uuid,
    query_key varchar(255) NOT NULL DEFAULT '',
    synced_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_planning_ai_sync_state_scope
    ON planejamento_ia_read_model_sync_state(scope, id_escola, id_referencia);
