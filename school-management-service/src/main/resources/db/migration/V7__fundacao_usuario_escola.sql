CREATE TABLE IF NOT EXISTS usuario_escola (
    id_usuario_escola uuid PRIMARY KEY,
    id_usuario uuid NOT NULL,
    id_escola uuid NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_usuario_escola_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_usuario_escola_escola
        FOREIGN KEY (id_escola) REFERENCES escola(id_escola),
    CONSTRAINT uk_usuario_escola_usuario_escola
        UNIQUE (id_usuario, id_escola)
);

INSERT INTO usuario_escola (id_usuario_escola, id_usuario, id_escola, created_at)
SELECT RANDOM_UUID(), u.id_usuario, u.id_escola, CURRENT_TIMESTAMP
FROM usuario u
WHERE u.id_escola IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM usuario_escola ue
      WHERE ue.id_usuario = u.id_usuario
        AND ue.id_escola = u.id_escola
  );

CREATE INDEX IF NOT EXISTS idx_usuario_escola_usuario
    ON usuario_escola(id_usuario);

CREATE INDEX IF NOT EXISTS idx_usuario_escola_escola
    ON usuario_escola(id_escola);
