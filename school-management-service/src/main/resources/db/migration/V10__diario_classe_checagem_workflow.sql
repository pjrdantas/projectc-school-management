ALTER TABLE public.diario_classe_lancamento
    ADD COLUMN IF NOT EXISTS checado_coordenacao_por_funcionario uuid,
    ADD COLUMN IF NOT EXISTS checado_coordenacao_em timestamp without time zone,
    ADD COLUMN IF NOT EXISTS observacao_coordenacao varchar(500),
    ADD COLUMN IF NOT EXISTS checado_direcao_por_funcionario uuid,
    ADD COLUMN IF NOT EXISTS checado_direcao_em timestamp without time zone,
    ADD COLUMN IF NOT EXISTS observacao_direcao varchar(500);

ALTER TABLE public.diario_classe_lancamento
    DROP CONSTRAINT IF EXISTS ck_diario_lancamento_status;

ALTER TABLE public.diario_classe_lancamento
    ADD CONSTRAINT ck_diario_lancamento_status
    CHECK (status IN (
        'EM_PREENCHIMENTO',
        'SALVO',
        'BLOQUEADO',
        'CHECADO_COORDENACAO',
        'CHECADO_DIRECAO'
    ));

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'fk_diario_lancamento_checado_coord_funcionario'
    ) THEN
        ALTER TABLE public.diario_classe_lancamento
            ADD CONSTRAINT fk_diario_lancamento_checado_coord_funcionario
            FOREIGN KEY (checado_coordenacao_por_funcionario)
            REFERENCES public.funcionario(id_funcionario);
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'fk_diario_lancamento_checado_direcao_funcionario'
    ) THEN
        ALTER TABLE public.diario_classe_lancamento
            ADD CONSTRAINT fk_diario_lancamento_checado_direcao_funcionario
            FOREIGN KEY (checado_direcao_por_funcionario)
            REFERENCES public.funcionario(id_funcionario);
    END IF;
END
$$;
