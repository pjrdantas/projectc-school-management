-- Modelo normalizado para gestão escolar
-- PostgreSQL
-- Todos os IDs principais usam UUID com gen_random_uuid()
-- Recomendado rodar em uma base limpa ou após backup.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================================================
-- DOMÍNIOS / TABELAS DE APOIO
-- =========================================================

CREATE TABLE IF NOT EXISTS tipo_pessoa (
    id_tipo_pessoa uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(40) NOT NULL UNIQUE,
    descricao varchar(120) NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS tipo_documento (
    id_tipo_documento uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(60) NOT NULL UNIQUE,
    descricao varchar(150) NOT NULL,
    obrigatorio_padrao boolean DEFAULT false NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS tipo_endereco (
    id_tipo_endereco uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(40) NOT NULL UNIQUE,
    descricao varchar(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS parentesco (
    id_parentesco uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(40) NOT NULL UNIQUE,
    descricao varchar(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS status_aluno (
    id_status_aluno uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(40) NOT NULL UNIQUE,
    descricao varchar(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS tipo_matricula (
    id_tipo_matricula uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(60) NOT NULL UNIQUE,
    descricao varchar(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS status_matricula (
    id_status_matricula uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(60) NOT NULL UNIQUE,
    descricao varchar(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS etapa_matricula_modelo (
    id_etapa_matricula_modelo uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_tipo_matricula uuid NOT NULL REFERENCES tipo_matricula(id_tipo_matricula),
    codigo varchar(60) NOT NULL,
    descricao varchar(150) NOT NULL,
    ordem integer NOT NULL,
    obrigatoria boolean DEFAULT true NOT NULL,
    UNIQUE (id_tipo_matricula, codigo),
    UNIQUE (id_tipo_matricula, ordem)
);

CREATE TABLE IF NOT EXISTS status_etapa_matricula (
    id_status_etapa_matricula uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(60) NOT NULL UNIQUE,
    descricao varchar(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS tipo_transferencia (
    id_tipo_transferencia uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(40) NOT NULL UNIQUE,
    descricao varchar(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS status_transferencia (
    id_status_transferencia uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(40) NOT NULL UNIQUE,
    descricao varchar(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS turno (
    id_turno uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(40) NOT NULL UNIQUE,
    descricao varchar(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS nivel_ensino (
    id_nivel_ensino uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(60) NOT NULL UNIQUE,
    descricao varchar(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS tipo_avaliacao (
    id_tipo_avaliacao uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(50) NOT NULL UNIQUE,
    descricao varchar(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS situacao_frequencia (
    id_situacao_frequencia uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(40) NOT NULL UNIQUE,
    descricao varchar(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS tipo_evento_aluno (
    id_tipo_evento_aluno uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(80) NOT NULL UNIQUE,
    descricao varchar(180) NOT NULL
);

CREATE TABLE IF NOT EXISTS cargo (
    id_cargo uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(60) NOT NULL UNIQUE,
    descricao varchar(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS status_planejamento (
    id_status_planejamento uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(60) NOT NULL UNIQUE,
    descricao varchar(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS tipo_conteudo_ia (
    id_tipo_conteudo_ia uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(60) NOT NULL UNIQUE,
    descricao varchar(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS status_conteudo_ia (
    id_status_conteudo_ia uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(60) NOT NULL UNIQUE,
    descricao varchar(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS publico_dashboard (
    id_publico_dashboard uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(40) NOT NULL UNIQUE,
    descricao varchar(120) NOT NULL
);


-- =========================================================
-- PESSOAS, ENDEREÇOS E DOCUMENTOS
-- =========================================================

CREATE TABLE IF NOT EXISTS pessoa (
    id_pessoa uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    nome_completo varchar(150) NOT NULL,
    cpf varchar(14) UNIQUE,
    rg varchar(20),
    orgao_emissor_rg varchar(20),
    uf_rg char(2),
    email varchar(150),
    telefone varchar(20),
    data_nascimento date,
    sexo varchar(20),
    nome_social varchar(150),
    nacionalidade varchar(80),
    naturalidade varchar(100),
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp
);

CREATE TABLE IF NOT EXISTS pessoa_tipo_pessoa (
    id_pessoa_tipo_pessoa uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_pessoa uuid NOT NULL REFERENCES pessoa(id_pessoa),
    id_tipo_pessoa uuid NOT NULL REFERENCES tipo_pessoa(id_tipo_pessoa),
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (id_pessoa, id_tipo_pessoa)
);

CREATE TABLE IF NOT EXISTS endereco (
    id_endereco uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    cep varchar(10),
    logradouro varchar(150),
    numero varchar(20),
    complemento varchar(100),
    bairro varchar(100),
    cidade varchar(100),
    uf char(2),
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp
);

CREATE TABLE IF NOT EXISTS pessoa_endereco (
    id_pessoa_endereco uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_pessoa uuid NOT NULL REFERENCES pessoa(id_pessoa),
    id_endereco uuid NOT NULL REFERENCES endereco(id_endereco),
    id_tipo_endereco uuid REFERENCES tipo_endereco(id_tipo_endereco),
    principal boolean DEFAULT true NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS documento (
    id_documento uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_tipo_documento uuid NOT NULL REFERENCES tipo_documento(id_tipo_documento),

    -- Nome original enviado pelo usuário e nome físico gerado pelo sistema.
    nome_original_arquivo varchar(255) NOT NULL,
    nome_arquivo_armazenado varchar(255) NOT NULL,

    -- Estratégia de armazenamento. Inicialmente LOCAL, futuramente pode ser S3, MINIO, AZURE_BLOB etc.
    storage_provider varchar(40) DEFAULT 'LOCAL' NOT NULL,
    diretorio_base varchar(500),
    caminho_relativo text NOT NULL,
    caminho_absoluto text,
    url_acesso text,

    -- Metadados técnicos do arquivo.
    content_type varchar(120),
    extensao varchar(20),
    tamanho_bytes bigint,
    hash_sha256 varchar(64),

    -- Controle e rastreabilidade.
    observacao text,
    data_upload timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    uploaded_by uuid,
    ativo boolean DEFAULT true NOT NULL,

    CONSTRAINT chk_documento_storage_provider
        CHECK (storage_provider IN ('LOCAL', 'S3', 'MINIO', 'AZURE_BLOB', 'GOOGLE_CLOUD_STORAGE'))
);

CREATE TABLE IF NOT EXISTS pessoa_documento (
    id_pessoa_documento uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_pessoa uuid NOT NULL REFERENCES pessoa(id_pessoa),
    id_documento uuid NOT NULL REFERENCES documento(id_documento),
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (id_pessoa, id_documento)
);

-- =========================================================
-- ALUNO, RESPONSÁVEL, PROFESSOR E FUNCIONÁRIO
-- =========================================================

CREATE TABLE IF NOT EXISTS aluno (
    id_aluno uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_pessoa uuid NOT NULL UNIQUE REFERENCES pessoa(id_pessoa),
    id_status_aluno uuid REFERENCES status_aluno(id_status_aluno),
    ra varchar(80),
    rm varchar(80),
    emancipado boolean DEFAULT false NOT NULL,
    data_ingresso date,
    data_saida date,
    motivo_saida text,
    ativo boolean DEFAULT true NOT NULL,
    data_exclusao timestamp,
    excluido_por uuid,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp
);

CREATE TABLE IF NOT EXISTS responsavel (
    id_responsavel uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_pessoa uuid NOT NULL UNIQUE REFERENCES pessoa(id_pessoa),
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp
);

CREATE TABLE IF NOT EXISTS aluno_responsavel (
    id_aluno_responsavel uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_aluno uuid NOT NULL REFERENCES aluno(id_aluno),
    id_responsavel uuid NOT NULL REFERENCES responsavel(id_responsavel),
    id_parentesco uuid REFERENCES parentesco(id_parentesco),
    responsavel_financeiro boolean DEFAULT false NOT NULL,
    responsavel_pedagogico boolean DEFAULT false NOT NULL,
    autorizado_retirar boolean DEFAULT false NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (id_aluno, id_responsavel)
);

CREATE TABLE IF NOT EXISTS professor (
    id_professor uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_pessoa uuid NOT NULL UNIQUE REFERENCES pessoa(id_pessoa),
    registro_profissional varchar(80),
    formacao varchar(150),
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp
);

CREATE TABLE IF NOT EXISTS funcionario (
    id_funcionario uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_pessoa uuid NOT NULL UNIQUE REFERENCES pessoa(id_pessoa),
    id_cargo uuid REFERENCES cargo(id_cargo),
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp
);

-- =========================================================
-- ESTRUTURA ESCOLAR
-- =========================================================

CREATE TABLE IF NOT EXISTS escola (
    id_escola uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    nome varchar(150) NOT NULL,
    codigo_inep varchar(30),
    cnpj varchar(18),
    telefone varchar(30),
    email varchar(150),
    id_endereco uuid REFERENCES endereco(id_endereco),
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp
);

CREATE TABLE IF NOT EXISTS periodo_letivo (
    id_periodo_letivo uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    nome varchar(80) NOT NULL,
    ano integer NOT NULL,
    data_inicio date NOT NULL,
    data_fim date NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT ck_periodo_letivo_datas CHECK (data_fim >= data_inicio)
);

CREATE TABLE IF NOT EXISTS serie (
    id_serie uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_nivel_ensino uuid REFERENCES nivel_ensino(id_nivel_ensino),
    nome varchar(80) NOT NULL,
    ordem integer NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS disciplina (
    id_disciplina uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    nome varchar(120) NOT NULL,
    carga_horaria integer,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS turma (
    id_turma uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_periodo_letivo uuid NOT NULL REFERENCES periodo_letivo(id_periodo_letivo),
    id_serie uuid NOT NULL REFERENCES serie(id_serie),
    id_turno uuid REFERENCES turno(id_turno),
    codigo varchar(20) NOT NULL,
    nome varchar(120) NOT NULL,
    capacidade integer NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT ck_turma_capacidade CHECK (capacidade > 0),
    UNIQUE (id_periodo_letivo, codigo)
);

CREATE TABLE IF NOT EXISTS turma_disciplina (
    id_turma_disciplina uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_turma uuid NOT NULL REFERENCES turma(id_turma),
    id_disciplina uuid NOT NULL REFERENCES disciplina(id_disciplina),
    carga_horaria integer,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (id_turma, id_disciplina)
);

CREATE TABLE IF NOT EXISTS professor_turma_disciplina (
    id_professor_turma_disciplina uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_professor uuid NOT NULL REFERENCES professor(id_professor),
    id_turma_disciplina uuid NOT NULL REFERENCES turma_disciplina(id_turma_disciplina),
    data_inicio date,
    data_fim date,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- =========================================================
-- MATRÍCULA, ETAPAS, DOCUMENTOS E TRANSFERÊNCIA
-- =========================================================

CREATE TABLE IF NOT EXISTS matricula (
    id_matricula uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_aluno uuid NOT NULL REFERENCES aluno(id_aluno),
    id_turma uuid NOT NULL REFERENCES turma(id_turma),
    id_periodo_letivo uuid NOT NULL REFERENCES periodo_letivo(id_periodo_letivo),
    id_tipo_matricula uuid NOT NULL REFERENCES tipo_matricula(id_tipo_matricula),
    id_status_matricula uuid NOT NULL REFERENCES status_matricula(id_status_matricula),
    data_solicitacao date DEFAULT CURRENT_DATE NOT NULL,
    data_efetivacao date,
    observacao text,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp,
    UNIQUE (id_aluno, id_periodo_letivo)
);

CREATE TABLE IF NOT EXISTS matricula_etapa (
    id_matricula_etapa uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_matricula uuid NOT NULL REFERENCES matricula(id_matricula),
    id_etapa_matricula_modelo uuid REFERENCES etapa_matricula_modelo(id_etapa_matricula_modelo),
    id_status_etapa_matricula uuid NOT NULL REFERENCES status_etapa_matricula(id_status_etapa_matricula),
    descricao varchar(150) NOT NULL,
    ordem integer NOT NULL,
    data_inicio timestamp,
    data_conclusao timestamp,
    observacao text,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (id_matricula, ordem)
);

CREATE TABLE IF NOT EXISTS matricula_documento_exigido (
    id_matricula_documento_exigido uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_tipo_matricula uuid NOT NULL REFERENCES tipo_matricula(id_tipo_matricula),
    id_tipo_documento uuid NOT NULL REFERENCES tipo_documento(id_tipo_documento),
    obrigatorio boolean DEFAULT true NOT NULL,
    ordem integer,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (id_tipo_matricula, id_tipo_documento)
);

CREATE TABLE IF NOT EXISTS matricula_documento_entregue (
    id_matricula_documento_entregue uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_matricula uuid NOT NULL REFERENCES matricula(id_matricula),
    id_documento uuid NOT NULL REFERENCES documento(id_documento),
    conferido boolean DEFAULT false NOT NULL,
    conferido_por uuid,
    data_conferencia timestamp,
    observacao text,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (id_matricula, id_documento)
);

CREATE TABLE IF NOT EXISTS transferencia_aluno (
    id_transferencia_aluno uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_aluno uuid NOT NULL REFERENCES aluno(id_aluno),
    id_matricula uuid REFERENCES matricula(id_matricula),
    id_tipo_transferencia uuid NOT NULL REFERENCES tipo_transferencia(id_tipo_transferencia),
    id_status_transferencia uuid NOT NULL REFERENCES status_transferencia(id_status_transferencia),
    id_escola_origem uuid REFERENCES escola(id_escola),
    id_escola_destino uuid REFERENCES escola(id_escola),
    serie_origem varchar(80),
    ano_letivo_origem varchar(20),
    data_solicitacao date DEFAULT CURRENT_DATE NOT NULL,
    data_confirmacao date,
    motivo_transferencia text,
    observacao text,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- =========================================================
-- AULAS, FREQUÊNCIA, PLANEJAMENTO, AVALIAÇÃO E BOLETIM
-- =========================================================

CREATE TABLE IF NOT EXISTS planejamento_professor (
    id_planejamento_professor uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_professor_turma_disciplina uuid NOT NULL REFERENCES professor_turma_disciplina(id_professor_turma_disciplina),
    titulo varchar(150) NOT NULL,
    objetivo text,
    metodologia text,
    recursos text,
    periodo_inicio date,
    periodo_fim date,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp
);

CREATE TABLE IF NOT EXISTS planejamento_aula (
    id_planejamento_aula uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_planejamento_professor uuid NOT NULL REFERENCES planejamento_professor(id_planejamento_professor),
    data_prevista date,
    conteudo text NOT NULL,
    habilidade_bncc varchar(80),
    avaliacao_prevista text,
    observacao text,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS aula (
    id_aula uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_professor_turma_disciplina uuid NOT NULL REFERENCES professor_turma_disciplina(id_professor_turma_disciplina),
    id_planejamento_aula uuid REFERENCES planejamento_aula(id_planejamento_aula),
    data_aula date NOT NULL,
    horario_inicio time,
    horario_fim time,
    conteudo_ministrado text,
    observacao text,
    realizada boolean DEFAULT true NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS frequencia_aluno (
    id_frequencia_aluno uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_aula uuid NOT NULL REFERENCES aula(id_aula),
    id_matricula uuid NOT NULL REFERENCES matricula(id_matricula),
    id_situacao_frequencia uuid NOT NULL REFERENCES situacao_frequencia(id_situacao_frequencia),
    justificativa text,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (id_aula, id_matricula)
);

CREATE TABLE IF NOT EXISTS frequencia_professor (
    id_frequencia_professor uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_aula uuid NOT NULL REFERENCES aula(id_aula),
    id_professor uuid NOT NULL REFERENCES professor(id_professor),
    presente boolean DEFAULT true NOT NULL,
    justificativa text,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (id_aula, id_professor)
);

CREATE TABLE IF NOT EXISTS avaliacao (
    id_avaliacao uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_professor_turma_disciplina uuid NOT NULL REFERENCES professor_turma_disciplina(id_professor_turma_disciplina),
    id_tipo_avaliacao uuid NOT NULL REFERENCES tipo_avaliacao(id_tipo_avaliacao),
    titulo varchar(150) NOT NULL,
    descricao text,
    data_aplicacao date,
    valor_maximo numeric(5,2) NOT NULL,
    peso numeric(5,2) DEFAULT 1 NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS nota_aluno (
    id_nota_aluno uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_avaliacao uuid NOT NULL REFERENCES avaliacao(id_avaliacao),
    id_matricula uuid NOT NULL REFERENCES matricula(id_matricula),
    nota numeric(5,2),
    observacao text,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp,
    UNIQUE (id_avaliacao, id_matricula)
);

CREATE TABLE IF NOT EXISTS boletim (
    id_boletim uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_matricula uuid NOT NULL REFERENCES matricula(id_matricula),
    periodo_referencia varchar(60) NOT NULL,
    data_fechamento date,
    observacao text,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (id_matricula, periodo_referencia)
);

CREATE TABLE IF NOT EXISTS boletim_item (
    id_boletim_item uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_boletim uuid NOT NULL REFERENCES boletim(id_boletim),
    id_disciplina uuid NOT NULL REFERENCES disciplina(id_disciplina),
    media numeric(5,2),
    frequencia_percentual numeric(5,2),
    resultado varchar(40),
    carga_horaria integer,
    observacao text,
    UNIQUE (id_boletim, id_disciplina)
);



-- =========================================================
-- PLANEJAMENTO BIMESTRAL COM APOIO DE IA
-- =========================================================

CREATE TABLE IF NOT EXISTS periodo_avaliativo (
    id_periodo_avaliativo uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_periodo_letivo uuid NOT NULL REFERENCES periodo_letivo(id_periodo_letivo),
    numero integer NOT NULL,
    nome varchar(80) NOT NULL,
    data_inicio date,
    data_fim date,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT ck_periodo_avaliativo_numero CHECK (numero BETWEEN 1 AND 6),
    UNIQUE (id_periodo_letivo, numero)
);

CREATE TABLE IF NOT EXISTS planejamento_bimestral (
    id_planejamento_bimestral uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_professor_turma_disciplina uuid NOT NULL REFERENCES professor_turma_disciplina(id_professor_turma_disciplina),
    id_periodo_avaliativo uuid REFERENCES periodo_avaliativo(id_periodo_avaliativo),
    id_status_planejamento uuid REFERENCES status_planejamento(id_status_planejamento),
    titulo varchar(180) NOT NULL,
    tema_principal varchar(180) NOT NULL,
    descricao_inicial text NOT NULL,
    objetivo_geral text,
    observacao_professor text,
    conteudo_final_aprovado text,
    reutilizavel boolean DEFAULT true NOT NULL,
    criado_com_auxilio_ia boolean DEFAULT false NOT NULL,
    aprovado_pelo_professor boolean DEFAULT false NOT NULL,
    data_aprovacao timestamp,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp,
    UNIQUE (id_professor_turma_disciplina, id_periodo_avaliativo, tema_principal)
);

CREATE TABLE IF NOT EXISTS planejamento_bimestral_aula (
    id_planejamento_bimestral_aula uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_planejamento_bimestral uuid NOT NULL REFERENCES planejamento_bimestral(id_planejamento_bimestral),
    id_planejamento_aula uuid REFERENCES planejamento_aula(id_planejamento_aula),
    numero_aula integer NOT NULL,
    tema_aula varchar(180) NOT NULL,
    objetivo_aula text,
    conteudo_previsto text,
    metodologia text,
    recursos text,
    atividade_prevista text,
    observacao text,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp,
    CONSTRAINT ck_planejamento_bimestral_aula_numero CHECK (numero_aula > 0),
    UNIQUE (id_planejamento_bimestral, numero_aula)
);

CREATE TABLE IF NOT EXISTS planejamento_bimestral_avaliacao (
    id_planejamento_bimestral_avaliacao uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_planejamento_bimestral uuid NOT NULL REFERENCES planejamento_bimestral(id_planejamento_bimestral),
    id_tipo_avaliacao uuid NOT NULL REFERENCES tipo_avaliacao(id_tipo_avaliacao),
    titulo varchar(180) NOT NULL,
    descricao text,
    data_prevista date,
    peso numeric(5,2) DEFAULT 1 NOT NULL,
    valor_maximo numeric(5,2),
    conteudo_cobrado text,
    orientacao_aplicacao text,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp
);

CREATE TABLE IF NOT EXISTS planejamento_ia_interacao (
    id_planejamento_ia_interacao uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_planejamento_bimestral uuid NOT NULL REFERENCES planejamento_bimestral(id_planejamento_bimestral),
    id_usuario uuid, -- usuário/professor que solicitou a geração; FK pode ser adicionada após a criação de usuario
    prompt_professor text NOT NULL,
    resposta_ia text NOT NULL,
    modelo_ia varchar(120),
    tokens_entrada integer,
    tokens_saida integer,
    custo_estimado numeric(12,6),
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS planejamento_ia_conteudo_gerado (
    id_planejamento_ia_conteudo_gerado uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_planejamento_bimestral uuid NOT NULL REFERENCES planejamento_bimestral(id_planejamento_bimestral),
    id_planejamento_ia_interacao uuid REFERENCES planejamento_ia_interacao(id_planejamento_ia_interacao),
    id_tipo_conteudo_ia uuid NOT NULL REFERENCES tipo_conteudo_ia(id_tipo_conteudo_ia),
    id_status_conteudo_ia uuid REFERENCES status_conteudo_ia(id_status_conteudo_ia),
    titulo varchar(180) NOT NULL,
    conteudo text NOT NULL,
    versao integer DEFAULT 1 NOT NULL,
    hash_conteudo varchar(128),
    aprovado_pelo_professor boolean DEFAULT false NOT NULL,
    reutilizavel boolean DEFAULT true NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp,
    CONSTRAINT ck_planejamento_ia_conteudo_versao CHECK (versao > 0)
);

CREATE TABLE IF NOT EXISTS planejamento_ia_conteudo_versao (
    id_planejamento_ia_conteudo_versao uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_planejamento_ia_conteudo_gerado uuid NOT NULL REFERENCES planejamento_ia_conteudo_gerado(id_planejamento_ia_conteudo_gerado),
    numero_versao integer NOT NULL,
    conteudo text NOT NULL,
    motivo_alteracao text,
    alterado_por uuid, -- usuário que alterou; FK pode ser adicionada após a criação de usuario
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT ck_planejamento_ia_conteudo_versao_numero CHECK (numero_versao > 0),
    UNIQUE (id_planejamento_ia_conteudo_gerado, numero_versao)
);

CREATE TABLE IF NOT EXISTS biblioteca_conteudo_pedagogico (
    id_biblioteca_conteudo_pedagogico uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_professor uuid REFERENCES professor(id_professor),
    id_disciplina uuid REFERENCES disciplina(id_disciplina),
    id_tipo_conteudo_ia uuid REFERENCES tipo_conteudo_ia(id_tipo_conteudo_ia),
    titulo varchar(180) NOT NULL,
    tema varchar(180),
    conteudo text NOT NULL,
    origem varchar(40) DEFAULT 'PLANEJAMENTO_IA' NOT NULL,
    reutilizavel boolean DEFAULT true NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp
);

ALTER TABLE avaliacao
    ADD COLUMN IF NOT EXISTS id_planejamento_bimestral_avaliacao uuid REFERENCES planejamento_bimestral_avaliacao(id_planejamento_bimestral_avaliacao);

-- =========================================================
-- HISTÓRICO ESCOLAR E EVENTOS DO ALUNO
-- =========================================================

CREATE TABLE IF NOT EXISTS historico_escolar (
    id_historico_escolar uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_aluno uuid NOT NULL REFERENCES aluno(id_aluno),
    origem varchar(20) NOT NULL, -- INTERNO ou EXTERNO
    id_escola uuid REFERENCES escola(id_escola),
    ano_conclusao integer,
    ensino_concluido varchar(120),
    data_emissao date,
    diretor_nome varchar(150),
    diretor_rg varchar(80),
    gerente_organizacao_nome varchar(150),
    gerente_organizacao_rg varchar(80),
    doe_numero varchar(80),
    doe_data date,
    doe_volume varchar(80),
    doe_pagina varchar(80),
    observacoes text,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT ck_historico_origem CHECK (origem IN ('INTERNO', 'EXTERNO'))
);

CREATE TABLE IF NOT EXISTS historico_escolar_item (
    id_historico_escolar_item uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_historico_escolar uuid NOT NULL REFERENCES historico_escolar(id_historico_escolar),
    id_periodo_letivo uuid REFERENCES periodo_letivo(id_periodo_letivo),
    id_serie uuid REFERENCES serie(id_serie),
    id_disciplina uuid REFERENCES disciplina(id_disciplina),
    componente_curricular varchar(150), -- usado para histórico externo quando não houver disciplina cadastrada
    ano_letivo integer,
    serie_descricao varchar(80), -- usado para histórico externo
    nota_conceito varchar(40),
    frequencia_percentual numeric(5,2),
    total_aulas integer,
    carga_horaria integer,
    resultado varchar(40)
);

CREATE TABLE IF NOT EXISTS aluno_historico_evento (
    id_aluno_historico_evento uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_aluno uuid NOT NULL REFERENCES aluno(id_aluno),
    id_tipo_evento_aluno uuid NOT NULL REFERENCES tipo_evento_aluno(id_tipo_evento_aluno),
    id_matricula uuid REFERENCES matricula(id_matricula),
    id_usuario uuid,
    descricao text NOT NULL,
    data_evento timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- =========================================================
-- APROVAÇÃO DE EXCLUSÃO LÓGICA DO ALUNO
-- =========================================================

CREATE TABLE IF NOT EXISTS solicitacao_exclusao_aluno (
    id_solicitacao_exclusao_aluno uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_aluno uuid NOT NULL REFERENCES aluno(id_aluno),
    solicitado_por uuid NOT NULL,
    autorizado_por uuid,
    data_solicitacao timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_autorizacao timestamp,
    motivo text NOT NULL,
    status varchar(30) DEFAULT 'PENDENTE' NOT NULL,
    observacao_autorizacao text,
    CONSTRAINT ck_solicitacao_exclusao_status CHECK (status IN ('PENDENTE', 'APROVADA', 'REPROVADA', 'CANCELADA'))
);

-- =========================================================
-- SEGURANÇA / AUTENTICAÇÃO
-- =========================================================

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    username varchar(80) NOT NULL UNIQUE,
    nome varchar(150) NOT NULL,
    email varchar(150) NOT NULL UNIQUE,
    senha_hash varchar(255) NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp
);

CREATE TABLE IF NOT EXISTS perfil (
    id_perfil uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(50) NOT NULL UNIQUE,
    nome varchar(120) NOT NULL,
    descricao varchar(255),
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS permissao (
    id_permissao uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    codigo varchar(80) NOT NULL UNIQUE,
    descricao varchar(255),
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS usuario_perfil (
    id_usuario_perfil uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_usuario uuid NOT NULL REFERENCES usuario(id_usuario),
    id_perfil uuid NOT NULL REFERENCES perfil(id_perfil),
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (id_usuario, id_perfil)
);

CREATE TABLE IF NOT EXISTS perfil_permissao (
    id_perfil_permissao uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_perfil uuid NOT NULL REFERENCES perfil(id_perfil),
    id_permissao uuid NOT NULL REFERENCES permissao(id_permissao),
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (id_perfil, id_permissao)
);

CREATE TABLE IF NOT EXISTS sessao_autenticacao (
    id_sessao_autenticacao uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_usuario uuid NOT NULL REFERENCES usuario(id_usuario),
    refresh_token_hash varchar(255) NOT NULL,
    expira_em timestamp NOT NULL,
    revogado boolean DEFAULT false NOT NULL,
    access_token_hash varchar(255),
    access_expira_em timestamp,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
);





DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_planejamento_ia_interacao_usuario'
    ) THEN
        ALTER TABLE planejamento_ia_interacao
            ADD CONSTRAINT fk_planejamento_ia_interacao_usuario
            FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_planejamento_ia_conteudo_versao_usuario'
    ) THEN
        ALTER TABLE planejamento_ia_conteudo_versao
            ADD CONSTRAINT fk_planejamento_ia_conteudo_versao_usuario
            FOREIGN KEY (alterado_por) REFERENCES usuario(id_usuario);
    END IF;
END $$;

-- =========================================================
-- DASHBOARDS OPERACIONAIS
-- =========================================================

CREATE TABLE IF NOT EXISTS dashboard (
    id_dashboard uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_publico_dashboard uuid NOT NULL REFERENCES publico_dashboard(id_publico_dashboard),
    codigo varchar(80) NOT NULL UNIQUE,
    nome varchar(150) NOT NULL,
    descricao text,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS dashboard_widget (
    id_dashboard_widget uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_dashboard uuid NOT NULL REFERENCES dashboard(id_dashboard),
    codigo varchar(80) NOT NULL,
    titulo varchar(150) NOT NULL,
    descricao text,
    tipo_widget varchar(40) NOT NULL,
    ordem integer DEFAULT 1 NOT NULL,
    query_referencia varchar(150),
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (id_dashboard, codigo)
);

CREATE TABLE IF NOT EXISTS dashboard_usuario_configuracao (
    id_dashboard_usuario_configuracao uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_usuario uuid NOT NULL REFERENCES usuario(id_usuario),
    id_dashboard_widget uuid NOT NULL REFERENCES dashboard_widget(id_dashboard_widget),
    visivel boolean DEFAULT true NOT NULL,
    ordem integer,
    configuracao_json text,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp,
    UNIQUE (id_usuario, id_dashboard_widget)
);

CREATE TABLE IF NOT EXISTS dashboard_indicador_snapshot (
    id_dashboard_indicador_snapshot uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    id_publico_dashboard uuid NOT NULL REFERENCES publico_dashboard(id_publico_dashboard),
    codigo_indicador varchar(100) NOT NULL,
    descricao varchar(180) NOT NULL,
    valor_numeric numeric(14,2),
    valor_texto varchar(180),
    referencia_data date DEFAULT CURRENT_DATE NOT NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (id_publico_dashboard, codigo_indicador, referencia_data)
);

CREATE OR REPLACE VIEW vw_dashboard_professor_resumo AS
SELECT
    ptd.id_professor_turma_disciplina,
    prof.id_professor,
    pes.nome_completo AS professor,
    t.nome AS turma,
    d.nome AS disciplina,
    COUNT(DISTINCT a.id_aula) AS total_aulas_registradas,
    COUNT(DISTINCT pb.id_planejamento_bimestral) AS planejamentos_bimestrais,
    COUNT(DISTINCT av.id_avaliacao) AS avaliacoes_criadas,
    COUNT(DISTINCT fa.id_frequencia_aluno) FILTER (WHERE sf.codigo = 'FALTA') AS faltas_alunos
FROM professor_turma_disciplina ptd
JOIN professor prof ON prof.id_professor = ptd.id_professor
JOIN pessoa pes ON pes.id_pessoa = prof.id_pessoa
JOIN turma_disciplina td ON td.id_turma_disciplina = ptd.id_turma_disciplina
JOIN turma t ON t.id_turma = td.id_turma
JOIN disciplina d ON d.id_disciplina = td.id_disciplina
LEFT JOIN aula a ON a.id_professor_turma_disciplina = ptd.id_professor_turma_disciplina
LEFT JOIN planejamento_bimestral pb ON pb.id_professor_turma_disciplina = ptd.id_professor_turma_disciplina
LEFT JOIN avaliacao av ON av.id_professor_turma_disciplina = ptd.id_professor_turma_disciplina
LEFT JOIN frequencia_aluno fa ON fa.id_aula = a.id_aula
LEFT JOIN situacao_frequencia sf ON sf.id_situacao_frequencia = fa.id_situacao_frequencia
GROUP BY ptd.id_professor_turma_disciplina, prof.id_professor, pes.nome_completo, t.nome, d.nome;

CREATE OR REPLACE VIEW vw_dashboard_secretaria_resumo AS
SELECT
    COUNT(DISTINCT al.id_aluno) FILTER (WHERE al.ativo = true) AS alunos_ativos,
    COUNT(DISTINCT m.id_matricula) FILTER (WHERE sm.codigo = 'SOLICITADA') AS matriculas_solicitadas,
    COUNT(DISTINCT m.id_matricula) FILTER (WHERE sm.codigo = 'EM_ANDAMENTO') AS matriculas_em_andamento,
    COUNT(DISTINCT m.id_matricula) FILTER (WHERE sm.codigo = 'AGUARDANDO_DOCUMENTOS') AS matriculas_aguardando_documentos,
    COUNT(DISTINCT m.id_matricula) FILTER (WHERE sm.codigo = 'AGUARDANDO_HISTORICO_ESCOLAR') AS matriculas_aguardando_historico,
    COUNT(DISTINCT te.id_transferencia_aluno) FILTER (WHERE st.codigo = 'SOLICITADA') AS transferencias_solicitadas,
    COUNT(DISTINCT se.id_solicitacao_exclusao_aluno) FILTER (WHERE se.status = 'PENDENTE') AS exclusoes_pendentes
FROM aluno al
LEFT JOIN matricula m ON m.id_aluno = al.id_aluno
LEFT JOIN status_matricula sm ON sm.id_status_matricula = m.id_status_matricula
LEFT JOIN transferencia_aluno te ON te.id_aluno = al.id_aluno
LEFT JOIN status_transferencia st ON st.id_status_transferencia = te.id_status_transferencia
LEFT JOIN solicitacao_exclusao_aluno se ON se.id_aluno = al.id_aluno;

CREATE OR REPLACE VIEW vw_dashboard_diretor_resumo AS
SELECT
    (SELECT COUNT(*) FROM aluno al WHERE al.ativo = true) AS alunos_ativos,
    (SELECT COUNT(*) FROM professor prof WHERE prof.ativo = true) AS professores_ativos,
    (SELECT COUNT(*) FROM turma t WHERE t.ativo = true) AS turmas_ativas,
    (SELECT COUNT(*) FROM matricula m JOIN status_matricula sm ON sm.id_status_matricula = m.id_status_matricula WHERE sm.codigo = 'EFETIVADA') AS matriculas_efetivadas,
    (SELECT COUNT(*) FROM aula) AS aulas_registradas,
    (SELECT COUNT(*) FROM avaliacao) AS avaliacoes_registradas,
    (SELECT COUNT(*) FROM planejamento_bimestral) AS planejamentos_bimestrais,
    (SELECT COUNT(*) FROM historico_escolar) AS historicos_emitidos;

-- FK adicionada após a criação de usuario, porque documento é criado antes de usuario.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_documento_uploaded_by'
    ) THEN
        ALTER TABLE documento
            ADD CONSTRAINT fk_documento_uploaded_by
            FOREIGN KEY (uploaded_by) REFERENCES usuario(id_usuario);
    END IF;
END $$;

-- =========================================================
-- ÍNDICES IMPORTANTES
-- =========================================================

CREATE INDEX IF NOT EXISTS idx_pessoa_nome ON pessoa(nome_completo);
CREATE INDEX IF NOT EXISTS idx_pessoa_cpf ON pessoa(cpf);
CREATE INDEX IF NOT EXISTS idx_aluno_pessoa ON aluno(id_pessoa);
CREATE INDEX IF NOT EXISTS idx_matricula_aluno ON matricula(id_aluno);
CREATE INDEX IF NOT EXISTS idx_matricula_turma ON matricula(id_turma);
CREATE INDEX IF NOT EXISTS idx_documento_tipo ON documento(id_tipo_documento);
CREATE INDEX IF NOT EXISTS idx_aula_data ON aula(data_aula);
CREATE INDEX IF NOT EXISTS idx_frequencia_aluno_matricula ON frequencia_aluno(id_matricula);
CREATE INDEX IF NOT EXISTS idx_nota_aluno_matricula ON nota_aluno(id_matricula);
CREATE INDEX IF NOT EXISTS idx_evento_aluno ON aluno_historico_evento(id_aluno, data_evento);

CREATE INDEX IF NOT EXISTS idx_periodo_avaliativo_periodo ON periodo_avaliativo(id_periodo_letivo);
CREATE INDEX IF NOT EXISTS idx_planejamento_bimestral_ptd ON planejamento_bimestral(id_professor_turma_disciplina);
CREATE INDEX IF NOT EXISTS idx_planejamento_bimestral_periodo ON planejamento_bimestral(id_periodo_avaliativo);
CREATE INDEX IF NOT EXISTS idx_planejamento_bimestral_aula ON planejamento_bimestral_aula(id_planejamento_bimestral);
CREATE INDEX IF NOT EXISTS idx_planejamento_ia_interacao_planejamento ON planejamento_ia_interacao(id_planejamento_bimestral);
CREATE INDEX IF NOT EXISTS idx_planejamento_ia_conteudo_planejamento ON planejamento_ia_conteudo_gerado(id_planejamento_bimestral);
CREATE INDEX IF NOT EXISTS idx_biblioteca_conteudo_tema ON biblioteca_conteudo_pedagogico(tema);
CREATE INDEX IF NOT EXISTS idx_dashboard_snapshot_publico ON dashboard_indicador_snapshot(id_publico_dashboard, referencia_data);


-- =========================================================
-- CARGA INICIAL DE DOMÍNIOS
-- =========================================================

INSERT INTO tipo_pessoa (codigo, descricao) VALUES
('ALUNO', 'Aluno'),
('RESPONSAVEL', 'Responsável'),
('PROFESSOR', 'Professor'),
('FUNCIONARIO', 'Funcionário')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO tipo_documento (codigo, descricao, obrigatorio_padrao) VALUES
('RG', 'RG', false),
('CPF', 'CPF', false),
('CERTIDAO_NASCIMENTO', 'Certidão de nascimento', false),
('COMPROVANTE_RESIDENCIA', 'Comprovante de residência', true),
('HISTORICO_ESCOLAR', 'Histórico escolar', false),
('DECLARACAO_TRANSFERENCIA', 'Declaração ou pedido de transferência', false),
('LAUDO', 'Laudo', false),
('OUTROS', 'Outros', false)
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO tipo_endereco (codigo, descricao) VALUES
('RESIDENCIAL', 'Residencial'),
('COMERCIAL', 'Comercial'),
('ESCOLAR', 'Escolar')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO parentesco (codigo, descricao) VALUES
('PAI', 'Pai'),
('MAE', 'Mãe'),
('AVO', 'Avô/Avó'),
('TIO', 'Tio/Tia'),
('RESPONSAVEL_LEGAL', 'Responsável legal'),
('OUTRO', 'Outro')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO status_aluno (codigo, descricao) VALUES
('ATIVO', 'Ativo'),
('INATIVO', 'Inativo'),
('TRANSFERIDO', 'Transferido'),
('CONCLUIDO', 'Concluído'),
('EXCLUIDO', 'Excluído logicamente')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO tipo_matricula (codigo, descricao) VALUES
('PRIMEIRA_MATRICULA', 'Primeira matrícula'),
('TRANSFERENCIA_ENTRADA', 'Transferência de entrada'),
('RENOVACAO', 'Renovação de matrícula'),
('TRANSFERENCIA_SAIDA', 'Transferência de saída')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO status_matricula (codigo, descricao) VALUES
('SOLICITADA', 'Matrícula solicitada'),
('EM_ANDAMENTO', 'Matrícula em andamento'),
('AGUARDANDO_DOCUMENTOS', 'Aguardando documentos'),
('AGUARDANDO_HISTORICO_ESCOLAR', 'Aguardando histórico escolar'),
('EFETIVADA', 'Matrícula efetivada'),
('CANCELADA', 'Matrícula cancelada'),
('INDEFERIDA', 'Matrícula indeferida'),
('CONCLUIDA', 'Matrícula concluída')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO status_etapa_matricula (codigo, descricao) VALUES
('PENDENTE', 'Pendente'),
('EM_ANALISE', 'Em análise'),
('CONCLUIDA', 'Concluída'),
('REPROVADA', 'Reprovada')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO tipo_transferencia (codigo, descricao) VALUES
('ENTRADA', 'Transferência de entrada'),
('SAIDA', 'Transferência de saída')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO status_transferencia (codigo, descricao) VALUES
('SOLICITADA', 'Solicitada'),
('EM_ANDAMENTO', 'Em andamento'),
('CONFIRMADA', 'Confirmada'),
('CANCELADA', 'Cancelada')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO turno (codigo, descricao) VALUES
('MANHA', 'Manhã'),
('TARDE', 'Tarde'),
('NOITE', 'Noite'),
('INTEGRAL', 'Integral')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO nivel_ensino (codigo, descricao) VALUES
('EDUCACAO_INFANTIL', 'Educação Infantil'),
('ENSINO_FUNDAMENTAL', 'Ensino Fundamental'),
('ENSINO_MEDIO', 'Ensino Médio')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO tipo_avaliacao (codigo, descricao) VALUES
('PROVA', 'Prova'),
('TRABALHO', 'Trabalho'),
('ATIVIDADE', 'Atividade'),
('SEMINARIO', 'Seminário'),
('RECUPERACAO', 'Recuperação')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO situacao_frequencia (codigo, descricao) VALUES
('PRESENTE', 'Presente'),
('FALTA', 'Falta'),
('FALTA_JUSTIFICADA', 'Falta justificada')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO tipo_evento_aluno (codigo, descricao) VALUES
('MATRICULA_SOLICITADA', 'Matrícula solicitada'),
('MATRICULA_EFETIVADA', 'Matrícula efetivada'),
('DOCUMENTO_ENTREGUE', 'Documento entregue'),
('HISTORICO_ESCOLAR_ENTREGUE', 'Histórico escolar entregue'),
('TRANSFERENCIA_SOLICITADA', 'Transferência solicitada'),
('TRANSFERENCIA_EFETIVADA', 'Transferência efetivada'),
('CONCLUSAO_ESTUDOS', 'Conclusão de estudos'),
('EXCLUSAO_SOLICITADA', 'Exclusão solicitada'),
('EXCLUSAO_APROVADA', 'Exclusão aprovada')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO cargo (codigo, descricao) VALUES
('SECRETARIA', 'Secretaria'),
('SUPERVISOR', 'Supervisor'),
('DIRETOR', 'Diretor'),
('COORDENADOR', 'Coordenador')
ON CONFLICT (codigo) DO NOTHING;


INSERT INTO status_planejamento (codigo, descricao) VALUES
('RASCUNHO', 'Rascunho'),
('EM_REVISAO', 'Em revisão'),
('APROVADO', 'Aprovado pelo professor'),
('ARQUIVADO', 'Arquivado')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO tipo_conteudo_ia (codigo, descricao) VALUES
('PLANO_BIMESTRAL', 'Plano bimestral'),
('PLANO_AULA', 'Plano de aula'),
('ATIVIDADE', 'Atividade'),
('PROVA', 'Prova'),
('QUESTOES', 'Questões de avaliação'),
('RESUMO', 'Resumo de conteúdo'),
('MATERIAL_APOIO', 'Material de apoio'),
('RUBRICA', 'Rubrica de correção')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO status_conteudo_ia (codigo, descricao) VALUES
('GERADO', 'Gerado pela IA'),
('EM_EDICAO', 'Em edição pelo professor'),
('APROVADO', 'Aprovado pelo professor'),
('DESCARTADO', 'Descartado')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO publico_dashboard (codigo, descricao) VALUES
('PROFESSOR', 'Dashboard do professor'),
('SECRETARIA', 'Dashboard da secretaria'),
('DIRETOR', 'Dashboard do diretor')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO dashboard (id_publico_dashboard, codigo, nome, descricao)
SELECT pd.id_publico_dashboard, 'DASH_PROFESSOR', 'Dashboard do Professor', 'Resumo das turmas, aulas, planejamentos, avaliações e frequência sob gestão do professor'
FROM publico_dashboard pd WHERE pd.codigo = 'PROFESSOR'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO dashboard (id_publico_dashboard, codigo, nome, descricao)
SELECT pd.id_publico_dashboard, 'DASH_SECRETARIA', 'Dashboard da Secretaria', 'Resumo operacional de matrículas, documentos, transferências e solicitações administrativas'
FROM publico_dashboard pd WHERE pd.codigo = 'SECRETARIA'
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO dashboard (id_publico_dashboard, codigo, nome, descricao)
SELECT pd.id_publico_dashboard, 'DASH_DIRETOR', 'Dashboard do Diretor', 'Visão geral da escola com indicadores acadêmicos, pedagógicos e administrativos'
FROM publico_dashboard pd WHERE pd.codigo = 'DIRETOR'
ON CONFLICT (codigo) DO NOTHING;
