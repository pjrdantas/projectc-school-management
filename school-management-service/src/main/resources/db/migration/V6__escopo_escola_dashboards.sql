ALTER TABLE dashboard_indicador_snapshot
    ADD COLUMN IF NOT EXISTS id_escola uuid;

UPDATE dashboard_indicador_snapshot
   SET id_escola = '00000000-0000-0000-0000-000000000047'
 WHERE id_escola IS NULL;

ALTER TABLE dashboard_indicador_snapshot
    ALTER COLUMN id_escola SET NOT NULL;

ALTER TABLE dashboard_indicador_snapshot
    ADD CONSTRAINT fk_dashboard_indicador_snapshot_escola
    FOREIGN KEY (id_escola) REFERENCES escola(id_escola);

CREATE INDEX IF NOT EXISTS idx_dashboard_indicador_snapshot_escola
    ON dashboard_indicador_snapshot(id_escola);

CREATE INDEX IF NOT EXISTS idx_dashboard_indicador_snapshot_publico_escola_data
    ON dashboard_indicador_snapshot(id_publico_dashboard, id_escola, referencia_data);
