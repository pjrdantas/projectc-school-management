DO $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM information_schema.tables
         WHERE table_schema = 'public'
           AND table_name = 'historico_escolar'
    ) THEN
        ALTER TABLE public.historico_escolar
            ADD COLUMN IF NOT EXISTS id_matricula uuid,
            ADD COLUMN IF NOT EXISTS id_transferencia_aluno uuid,
            ADD COLUMN IF NOT EXISTS status varchar(30) DEFAULT 'RASCUNHO' NOT NULL,
            ADD COLUMN IF NOT EXISTS bloqueado boolean DEFAULT false NOT NULL,
            ADD COLUMN IF NOT EXISTS serie_matricula_atual integer,
            ADD COLUMN IF NOT EXISTS serie_concluida_origem integer,
            ADD COLUMN IF NOT EXISTS escola_origem_nome varchar(150),
            ADD COLUMN IF NOT EXISTS data_transferencia date,
            ADD COLUMN IF NOT EXISTS atualizado_em timestamp without time zone;
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM information_schema.tables
         WHERE table_schema = 'public'
           AND table_name = 'historico_escolar'
    ) AND NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'ck_historico_escolar_status'
    ) THEN
        ALTER TABLE public.historico_escolar
            ADD CONSTRAINT ck_historico_escolar_status
            CHECK (status IN ('RASCUNHO', 'PENDENTE', 'COMPLETO', 'BLOQUEADO'));
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM information_schema.tables
         WHERE table_schema = 'public'
           AND table_name = 'historico_escolar'
    ) AND NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'ck_historico_serie_matricula_atual'
    ) THEN
        ALTER TABLE public.historico_escolar
            ADD CONSTRAINT ck_historico_serie_matricula_atual
            CHECK (serie_matricula_atual IS NULL OR serie_matricula_atual BETWEEN 1 AND 9);
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM information_schema.tables
         WHERE table_schema = 'public'
           AND table_name = 'historico_escolar'
    ) AND NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'ck_historico_serie_concluida_origem'
    ) THEN
        ALTER TABLE public.historico_escolar
            ADD CONSTRAINT ck_historico_serie_concluida_origem
            CHECK (serie_concluida_origem IS NULL OR serie_concluida_origem BETWEEN 0 AND 9);
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM information_schema.tables
         WHERE table_schema = 'public'
           AND table_name = 'historico_escolar'
    ) AND NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'fk_historico_escolar_matricula'
    ) THEN
        ALTER TABLE public.historico_escolar
            ADD CONSTRAINT fk_historico_escolar_matricula
            FOREIGN KEY (id_matricula)
            REFERENCES public.matricula(id_matricula);
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM information_schema.tables
         WHERE table_schema = 'public'
           AND table_name = 'historico_escolar'
    ) AND NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'fk_historico_escolar_transferencia'
    ) THEN
        ALTER TABLE public.historico_escolar
            ADD CONSTRAINT fk_historico_escolar_transferencia
            FOREIGN KEY (id_transferencia_aluno)
            REFERENCES public.transferencia_aluno(id_transferencia_aluno);
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_historico_escolar_matricula
    ON public.historico_escolar (id_matricula);

CREATE INDEX IF NOT EXISTS idx_historico_escolar_status
    ON public.historico_escolar (status);
