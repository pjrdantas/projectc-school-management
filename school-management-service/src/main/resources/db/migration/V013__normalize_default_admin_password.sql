UPDATE usuario
SET senha_hash = '$2a$10$8WAxDjIVLhL2.XAhJP0uyuC9O64.iGkLpcD1TQFxJUutLD8dnGxxO'
WHERE lower(username) = 'admin'
  AND senha_hash <> '$2a$10$8WAxDjIVLhL2.XAhJP0uyuC9O64.iGkLpcD1TQFxJUutLD8dnGxxO';
