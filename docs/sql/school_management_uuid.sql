-- ==========================================================
-- Script de bootstrap/rebuild do banco (PostgreSQL) com IDs UUID
-- Projeto: school-management-service
-- Uso sugerido:
--   psql -U postgres -d school_management -f docs/school_management_uuid.sql
--
-- Observação:
--   Este script é destrutivo para as tabelas do domínio escolar.
-- ==========================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE SCHEMA IF NOT EXISTS public;

-- Rebuild limpo para evitar conflito com schema antigo (BIGINT)
DROP TABLE IF EXISTS public.aluno_responsavel CASCADE;
DROP TABLE IF EXISTS public.responsavel CASCADE;
DROP TABLE IF EXISTS public.matricula CASCADE;
DROP TABLE IF EXISTS public.turma CASCADE;
DROP TABLE IF EXISTS public.periodo_letivo CASCADE;
DROP TABLE IF EXISTS public.aluno CASCADE;

CREATE TABLE public.aluno (
    id_aluno UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_completo VARCHAR(150) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(150),
    telefone VARCHAR(20),
    data_nascimento DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.periodo_letivo (
    id_periodo_letivo UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(80) NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_periodo_letivo_datas CHECK (data_fim >= data_inicio)
);

CREATE TABLE public.turma (
    id_turma UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(20) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    capacidade INTEGER NOT NULL,
    id_periodo_letivo UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_turma_periodo_letivo
        FOREIGN KEY (id_periodo_letivo) REFERENCES public.periodo_letivo(id_periodo_letivo),
    CONSTRAINT ck_turma_capacidade CHECK (capacidade > 0),
    CONSTRAINT uk_turma_codigo_periodo UNIQUE (codigo, id_periodo_letivo)
);


CREATE TABLE public.responsavel (
    id_responsavel UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_completo VARCHAR(150) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(150),
    telefone VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.aluno_responsavel (
    id_aluno_responsavel UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_aluno UUID NOT NULL,
    id_responsavel UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_aluno_responsavel_aluno
        FOREIGN KEY (id_aluno) REFERENCES public.aluno(id_aluno),
    CONSTRAINT fk_aluno_responsavel_responsavel
        FOREIGN KEY (id_responsavel) REFERENCES public.responsavel(id_responsavel),
    CONSTRAINT uk_aluno_responsavel UNIQUE (id_aluno, id_responsavel)
);

CREATE TABLE public.matricula (
    id_matricula UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_aluno UUID NOT NULL,
    id_turma UUID NOT NULL,
    id_periodo_letivo UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_matricula_aluno
        FOREIGN KEY (id_aluno) REFERENCES public.aluno(id_aluno),
    CONSTRAINT fk_matricula_turma
        FOREIGN KEY (id_turma) REFERENCES public.turma(id_turma),
    CONSTRAINT fk_matricula_periodo_letivo
        FOREIGN KEY (id_periodo_letivo) REFERENCES public.periodo_letivo(id_periodo_letivo),
    CONSTRAINT ck_matricula_status CHECK (status IN ('ATIVA'))
);

CREATE INDEX idx_matricula_aluno ON public.matricula (id_aluno);
CREATE INDEX idx_matricula_turma ON public.matricula (id_turma);
CREATE INDEX idx_turma_periodo ON public.turma (id_periodo_letivo);
CREATE INDEX idx_responsavel_nome ON public.responsavel (nome_completo);
CREATE INDEX idx_aluno_responsavel_aluno ON public.aluno_responsavel (id_aluno);
CREATE INDEX idx_aluno_responsavel_responsavel ON public.aluno_responsavel (id_responsavel);
