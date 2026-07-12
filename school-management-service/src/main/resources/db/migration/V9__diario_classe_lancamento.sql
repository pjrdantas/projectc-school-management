CREATE TABLE IF NOT EXISTS public.diario_classe_lancamento (
    id_diario_classe_lancamento uuid NOT NULL,
    id_professor_turma_disciplina uuid NOT NULL,
    data_lancamento date NOT NULL,
    mes integer NOT NULL,
    ano integer NOT NULL,
    status varchar(30) DEFAULT 'EM_PREENCHIMENTO' NOT NULL,
    bloqueado boolean DEFAULT false NOT NULL,
    assinatura_professor varchar(150),
    data_assinatura date,
    salvo_em timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,
    CONSTRAINT pk_diario_classe_lancamento PRIMARY KEY (id_diario_classe_lancamento)
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'fk_diario_lancamento_prof_turma_disc'
    ) THEN
        ALTER TABLE public.diario_classe_lancamento
            ADD CONSTRAINT fk_diario_lancamento_prof_turma_disc
            FOREIGN KEY (id_professor_turma_disciplina)
            REFERENCES public.professor_turma_disciplina(id_professor_turma_disciplina);
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'ck_diario_lancamento_mes'
    ) THEN
        ALTER TABLE public.diario_classe_lancamento
            ADD CONSTRAINT ck_diario_lancamento_mes
            CHECK (mes BETWEEN 1 AND 12);
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'ck_diario_lancamento_status'
    ) THEN
        ALTER TABLE public.diario_classe_lancamento
            ADD CONSTRAINT ck_diario_lancamento_status
            CHECK (status IN ('EM_PREENCHIMENTO', 'SALVO', 'BLOQUEADO'));
    END IF;
END
$$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_diario_lancamento_prof_turma_disc_data
    ON public.diario_classe_lancamento (id_professor_turma_disciplina, data_lancamento);

ALTER TABLE public.aula
    ADD COLUMN IF NOT EXISTS id_diario_classe_lancamento uuid;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'fk_aula_diario_classe_lancamento'
    ) THEN
        ALTER TABLE public.aula
            ADD CONSTRAINT fk_aula_diario_classe_lancamento
            FOREIGN KEY (id_diario_classe_lancamento)
            REFERENCES public.diario_classe_lancamento(id_diario_classe_lancamento);
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_aula_prof_turma_disc_data
    ON public.aula (id_professor_turma_disciplina, data_aula);
