-- ==========================================================
-- Script de bootstrap do banco (PostgreSQL) com IDs UUID
-- Projeto: school-management-service
-- Uso sugerido:
--   psql -U postgres -d school_management -f docs/school_management_uuid.sql
-- ==========================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SCHEMA IF NOT EXISTS public;

-- ==========================================================
-- TABELA: aluno
-- ==========================================================
CREATE TABLE IF NOT EXISTS public.aluno (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_completo VARCHAR(150) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(150),
    telefone VARCHAR(20),
    data_nascimento DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================================
-- TABELA: periodo_letivo
-- ==========================================================
CREATE TABLE IF NOT EXISTS public.periodo_letivo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(80) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_periodo_letivo_datas CHECK (data_fim >= data_inicio)
);

-- ==========================================================
-- TABELA: turma
-- ==========================================================
CREATE TABLE IF NOT EXISTS public.turma (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(20) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    capacidade INTEGER NOT NULL,
    periodo_letivo_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_turma_periodo_letivo
        FOREIGN KEY (periodo_letivo_id) REFERENCES public.periodo_letivo(id),
    CONSTRAINT ck_turma_capacidade CHECK (capacidade > 0),
    CONSTRAINT uk_turma_codigo_periodo UNIQUE (codigo, periodo_letivo_id)
);

-- ==========================================================
-- TABELA: matricula
-- ==========================================================
CREATE TABLE IF NOT EXISTS public.matricula (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id UUID NOT NULL,
    turma_id UUID NOT NULL,
    periodo_letivo_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_matricula_aluno
        FOREIGN KEY (aluno_id) REFERENCES public.aluno(id),
    CONSTRAINT fk_matricula_turma
        FOREIGN KEY (turma_id) REFERENCES public.turma(id),
    CONSTRAINT fk_matricula_periodo_letivo
        FOREIGN KEY (periodo_letivo_id) REFERENCES public.periodo_letivo(id),
    CONSTRAINT ck_matricula_status CHECK (status IN ('ATIVA'))
);

-- ==========================================================
-- ÍNDICES
-- ==========================================================
CREATE INDEX IF NOT EXISTS idx_matricula_aluno ON public.matricula (aluno_id);
CREATE INDEX IF NOT EXISTS idx_matricula_turma ON public.matricula (turma_id);
CREATE INDEX IF NOT EXISTS idx_turma_periodo ON public.turma (periodo_letivo_id);
