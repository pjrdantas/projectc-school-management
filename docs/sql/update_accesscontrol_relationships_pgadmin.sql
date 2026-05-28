-- Script para executar no pgAdmin (database: school_management)
-- Objetivo: garantir que a estrutura relacional usada pelo módulo accesscontrol
-- esteja presente com UUID e tabelas/colunas atuais.

BEGIN;

-- 1) Garantir tabela usuario_perfil (N:N usuario x perfil)
CREATE TABLE IF NOT EXISTS usuario_perfil (
    id_usuario_perfil UUID PRIMARY KEY,
    id_usuario UUID NOT NULL,
    id_perfil UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2) Garantir tabela perfil_permissao (N:N perfil x permissao)
CREATE TABLE IF NOT EXISTS perfil_permissao (
    id_perfil_permissao UUID PRIMARY KEY,
    id_perfil UUID NOT NULL,
    id_permissao UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3) Garantir FKs da usuario_perfil
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_usuario_perfil_usuario'
    ) THEN
        ALTER TABLE usuario_perfil
            ADD CONSTRAINT fk_usuario_perfil_usuario
            FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_usuario_perfil_perfil'
    ) THEN
        ALTER TABLE usuario_perfil
            ADD CONSTRAINT fk_usuario_perfil_perfil
            FOREIGN KEY (id_perfil) REFERENCES perfil (id_perfil);
    END IF;
END $$;

-- 4) Garantir FKs da perfil_permissao
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_perfil_permissao_perfil'
    ) THEN
        ALTER TABLE perfil_permissao
            ADD CONSTRAINT fk_perfil_permissao_perfil
            FOREIGN KEY (id_perfil) REFERENCES perfil (id_perfil);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_perfil_permissao_permissao'
    ) THEN
        ALTER TABLE perfil_permissao
            ADD CONSTRAINT fk_perfil_permissao_permissao
            FOREIGN KEY (id_permissao) REFERENCES permissao (id_permissao);
    END IF;
END $$;

-- 5) Garantir unicidade dos relacionamentos N:N
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_usuario_perfil'
    ) THEN
        ALTER TABLE usuario_perfil
            ADD CONSTRAINT uk_usuario_perfil UNIQUE (id_usuario, id_perfil);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_perfil_permissao'
    ) THEN
        ALTER TABLE perfil_permissao
            ADD CONSTRAINT uk_perfil_permissao UNIQUE (id_perfil, id_permissao);
    END IF;
END $$;

-- 6) Índices para performance
CREATE INDEX IF NOT EXISTS idx_usuario_perfil_usuario ON usuario_perfil (id_usuario);
CREATE INDEX IF NOT EXISTS idx_usuario_perfil_perfil ON usuario_perfil (id_perfil);
CREATE INDEX IF NOT EXISTS idx_perfil_permissao_perfil ON perfil_permissao (id_perfil);
CREATE INDEX IF NOT EXISTS idx_perfil_permissao_permissao ON perfil_permissao (id_permissao);

COMMIT;
