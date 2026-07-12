CREATE TABLE IF NOT EXISTS public.historico_escolar_pendencia (
    id_historico_escolar_pendencia uuid DEFAULT gen_random_uuid() NOT NULL,
    id_historico_escolar uuid NOT NULL,
    codigo varchar(80) NOT NULL,
    severidade varchar(20) NOT NULL,
    aba varchar(80),
    mensagem text NOT NULL,
    resolvida boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    resolved_at timestamp without time zone,
    CONSTRAINT pk_historico_escolar_pendencia PRIMARY KEY (id_historico_escolar_pendencia)
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'fk_historico_pendencia_historico'
    ) AND EXISTS (
        SELECT 1
          FROM information_schema.tables
         WHERE table_schema = 'public'
           AND table_name = 'historico_escolar'
    ) THEN
        ALTER TABLE public.historico_escolar_pendencia
            ADD CONSTRAINT fk_historico_pendencia_historico
            FOREIGN KEY (id_historico_escolar)
            REFERENCES public.historico_escolar(id_historico_escolar)
            ON DELETE CASCADE;
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_hist_pendencia_aberta
    ON public.historico_escolar_pendencia (id_historico_escolar, resolvida);

CREATE UNIQUE INDEX IF NOT EXISTS uk_hist_pendencia_codigo_aberta
    ON public.historico_escolar_pendencia (id_historico_escolar, codigo)
    WHERE resolvida = false;
