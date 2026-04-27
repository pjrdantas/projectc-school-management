CREATE TABLE matricula (
    id BIGSERIAL PRIMARY KEY,
    aluno_id BIGINT NOT NULL,
    turma_id BIGINT NOT NULL,
    periodo_letivo_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_matricula_aluno FOREIGN KEY (aluno_id) REFERENCES aluno (id),
    CONSTRAINT fk_matricula_turma FOREIGN KEY (turma_id) REFERENCES turma (id),
    CONSTRAINT fk_matricula_periodo_letivo FOREIGN KEY (periodo_letivo_id) REFERENCES periodo_letivo (id),
    CONSTRAINT ck_matricula_status CHECK (status IN ('ATIVA'))
);
