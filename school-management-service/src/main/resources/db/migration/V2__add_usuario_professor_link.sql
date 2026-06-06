ALTER TABLE professor
    ADD COLUMN IF NOT EXISTS id_usuario uuid;

CREATE UNIQUE INDEX IF NOT EXISTS uk_professor_usuario
    ON professor (id_usuario)
    WHERE id_usuario IS NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_professor_usuario'
    ) THEN
        ALTER TABLE professor
            ADD CONSTRAINT fk_professor_usuario
            FOREIGN KEY (id_usuario)
            REFERENCES usuario(id_usuario);
    END IF;
END $$;
