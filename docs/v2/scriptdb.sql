--
-- PostgreSQL database dump
--

\restrict Ye4Ob233ZTRPzP11YhRCMsncVUkkxhMzBUfhTnopocoXdW2ZYsQXfQKAc3bXwOo

-- Dumped from database version 18.3
-- Dumped by pg_dump version 18.3

-- Started on 2026-05-25 10:31:18

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 6 (class 2615 OID 33810)
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 223 (class 1259 OID 34028)
-- Name: aluno; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.aluno (
    id_aluno uuid DEFAULT gen_random_uuid() NOT NULL,
    nome_completo character varying(150) NOT NULL,
    cpf character varying(14) NOT NULL,
    email character varying(150),
    telefone character varying(20),
    data_nascimento date NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    rg character varying(20),
    orgao_emissor_rg character varying(20),
    uf_rg character(2),
    nacionalidade character varying(80),
    naturalidade character varying(100),
    sexo character varying(20),
    nome_social character varying(150),
    cep character varying(8),
    logradouro character varying(150),
    numero character varying(20),
    complemento character varying(100),
    bairro character varying(100),
    cidade character varying(100),
    uf character(2),
    status_aluno character varying(30)
);


ALTER TABLE public.aluno OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 34128)
-- Name: aluno_responsavel; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.aluno_responsavel (
    id_aluno_responsavel uuid NOT NULL,
    id_aluno uuid NOT NULL,
    id_responsavel uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    parentesco character varying(50),
    responsavel_financeiro boolean DEFAULT false NOT NULL,
    responsavel_pedagogico boolean DEFAULT false NOT NULL,
    autorizado_retirar boolean DEFAULT false NOT NULL
);


ALTER TABLE public.aluno_responsavel OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 149513)
-- Name: disciplina; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.disciplina (
    id_disciplina uuid DEFAULT gen_random_uuid() NOT NULL,
    nome character varying(120) NOT NULL,
    carga_horaria integer,
    status character varying(20) DEFAULT 'ATIVA'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.disciplina OWNER TO postgres;

--
-- V2 - modelo documental generico aprovado em 2026-05-28.
-- O modelo definitivo deve guardar o documento uma unica vez e vincular a
-- qualquer entidade de negocio: ALUNO, RESPONSAVEL, FUNCIONARIO, PROFESSOR,
-- SECRETARIO, INSPETOR, SERVENTE ou novos papeis futuros.
--

CREATE TABLE public.documento (
    id_documento uuid DEFAULT gen_random_uuid() NOT NULL,
    tipo_documento character varying(50) NOT NULL,
    numero_documento character varying(120) NOT NULL,
    caminho_arquivo text NOT NULL,
    nome_original_arquivo character varying(255),
    content_type character varying(120),
    tamanho_bytes bigint,
    data_upload timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    observacao text,
    ativo boolean DEFAULT true NOT NULL,
    CONSTRAINT documento_pkey PRIMARY KEY (id_documento),
    CONSTRAINT ck_documento_tipo CHECK (((tipo_documento)::text = ANY ((ARRAY['RG'::character varying, 'CPF'::character varying, 'CERTIDAO_NASCIMENTO'::character varying, 'COMPROVANTE_RESIDENCIA'::character varying, 'HISTORICO_ESCOLAR'::character varying, 'DECLARACAO_TRANSFERENCIA'::character varying, 'CONFIRMACAO_VAGA_DESTINO'::character varying, 'LAUDO'::character varying, 'OUTROS'::character varying])::text[])))
);


ALTER TABLE public.documento OWNER TO postgres;

CREATE TABLE public.documento_vinculo (
    id_documento_vinculo uuid DEFAULT gen_random_uuid() NOT NULL,
    id_documento uuid NOT NULL,
    entidade_tipo character varying(40) NOT NULL,
    entidade_id uuid NOT NULL,
    principal boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT documento_vinculo_pkey PRIMARY KEY (id_documento_vinculo),
    CONSTRAINT documento_vinculo_id_documento_fkey FOREIGN KEY (id_documento) REFERENCES public.documento(id_documento),
    CONSTRAINT ck_documento_vinculo_entidade_tipo CHECK (((entidade_tipo)::text = ANY ((ARRAY['ALUNO'::character varying, 'RESPONSAVEL'::character varying, 'FUNCIONARIO'::character varying, 'PROFESSOR'::character varying, 'SECRETARIO'::character varying, 'INSPETOR'::character varying, 'SERVENTE'::character varying, 'MATRICULA'::character varying, 'TRANSFERENCIA_ALUNO'::character varying])::text[])))
);


ALTER TABLE public.documento_vinculo OWNER TO postgres;

CREATE TABLE public.matricula_pendencia_documental (
    id_pendencia_documental uuid DEFAULT gen_random_uuid() NOT NULL,
    id_matricula uuid NOT NULL,
    entidade_tipo character varying(40) NOT NULL,
    entidade_id uuid NOT NULL,
    tipo_documento character varying(50) NOT NULL,
    status character varying(20) DEFAULT 'PENDENTE'::character varying NOT NULL,
    id_documento uuid,
    obrigatorio boolean DEFAULT true NOT NULL,
    observacao text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT matricula_pendencia_documental_pkey PRIMARY KEY (id_pendencia_documental),
    CONSTRAINT matricula_pendencia_documental_id_documento_fkey FOREIGN KEY (id_documento) REFERENCES public.documento(id_documento),
    CONSTRAINT ck_matricula_pendencia_status CHECK (((status)::text = ANY ((ARRAY['PENDENTE'::character varying, 'ENTREGUE'::character varying, 'DISPENSADO'::character varying])::text[])))
);


ALTER TABLE public.matricula_pendencia_documental OWNER TO postgres;

CREATE TABLE public.matricula_evento_status (
    id_matricula_evento_status uuid DEFAULT gen_random_uuid() NOT NULL,
    id_matricula uuid NOT NULL,
    status_origem character varying(30),
    status_destino character varying(30) NOT NULL,
    justificativa text,
    id_documento uuid,
    usuario_operacao character varying(150),
    data_hora_operacao timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT matricula_evento_status_pkey PRIMARY KEY (id_matricula_evento_status),
    CONSTRAINT matricula_evento_status_id_documento_fkey FOREIGN KEY (id_documento) REFERENCES public.documento(id_documento),
    CONSTRAINT ck_matricula_evento_status_destino CHECK (((status_destino)::text = ANY ((ARRAY['SOLICITADA'::character varying, 'EM_ANDAMENTO'::character varying, 'AGUARDANDO_DOCUMENTOS'::character varying, 'AGUARDANDO_HISTORICO_ESCOLAR'::character varying, 'EFETIVADA'::character varying, 'CANCELADA'::character varying, 'INDEFERIDA'::character varying, 'TRANSFERIDO'::character varying])::text[])))
);


ALTER TABLE public.matricula_evento_status OWNER TO postgres;

--
-- TOC entry 237 (class 1259 OID 149616)
-- Name: escola_origem; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.escola_origem (
    id_escola_origem uuid DEFAULT gen_random_uuid() NOT NULL,
    nome_escola character varying(150) NOT NULL,
    codigo_inep character varying(30),
    cnpj character varying(18),
    cep character varying(10),
    logradouro character varying(150),
    numero character varying(20),
    complemento character varying(100),
    bairro character varying(100),
    cidade character varying(100),
    uf character varying(2),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.escola_origem OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 33829)
-- Name: flyway_audit_marker; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.flyway_audit_marker (
    id bigint NOT NULL,
    description character varying(100) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.flyway_audit_marker OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 33828)
-- Name: flyway_audit_marker_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.flyway_audit_marker_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.flyway_audit_marker_id_seq OWNER TO postgres;

--
-- TOC entry 5203 (class 0 OID 0)
-- Dependencies: 221
-- Name: flyway_audit_marker_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.flyway_audit_marker_id_seq OWNED BY public.flyway_audit_marker.id;


--
-- TOC entry 220 (class 1259 OID 33811)
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 149681)
-- Name: historico_escolar; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.historico_escolar (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    nome_aluno character varying(255) NOT NULL,
    rg_ren character varying(80),
    ra character varying(80),
    rm character varying(80),
    data_nascimento date,
    municipio_nascimento character varying(150),
    estado_nascimento character varying(2),
    pais_nascimento character varying(100),
    nome_escola character varying(255),
    endereco_escola character varying(255),
    municipio_escola character varying(150),
    cep_escola character varying(10),
    telefone_escola character varying(30),
    email_escola character varying(150),
    ano_conclusao integer,
    ensino_concluido character varying(120),
    data_emissao date,
    diretor_nome character varying(150),
    diretor_rg character varying(80),
    gerente_organizacao_nome character varying(150),
    gerente_organizacao_rg character varying(80),
    doe_numero character varying(80),
    doe_data date,
    doe_volume character varying(80),
    doe_pagina character varying(80),
    observacoes text
);


ALTER TABLE public.historico_escolar OWNER TO postgres;

--
-- TOC entry 241 (class 1259 OID 149691)
-- Name: historico_escolar_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.historico_escolar_item (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    historico_escolar_id uuid NOT NULL,
    componente_curricular character varying(150) NOT NULL,
    ano_letivo integer,
    serie character varying(80),
    ciclo character varying(80),
    nota_conceito character varying(40),
    total_aulas integer,
    carga_horaria integer
);


ALTER TABLE public.historico_escolar_item OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 34076)
-- Name: matricula; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.matricula (
    id_matricula uuid DEFAULT gen_random_uuid() NOT NULL,
    id_aluno uuid NOT NULL,
    id_turma uuid NOT NULL,
    id_periodo_letivo uuid NOT NULL,
    status character varying(30) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tipo_matricula character varying(30),
    data_matricula date DEFAULT CURRENT_DATE,
    observacao text,
    CONSTRAINT ck_matricula_status CHECK (((status)::text = ANY ((ARRAY['SOLICITADA'::character varying, 'EM_ANDAMENTO'::character varying, 'AGUARDANDO_DOCUMENTOS'::character varying, 'AGUARDANDO_HISTORICO_ESCOLAR'::character varying, 'EFETIVADA'::character varying, 'CANCELADA'::character varying, 'INDEFERIDA'::character varying, 'TRANSFERIDO'::character varying])::text[]))),
    CONSTRAINT ck_matricula_tipo CHECK (((tipo_matricula IS NULL) OR ((tipo_matricula)::text = ANY ((ARRAY['NOVA'::character varying, 'REMATRICULA'::character varying, 'TRANSFERENCIA'::character varying])::text[]))))
);


ALTER TABLE public.matricula OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 34172)
-- Name: perfil; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.perfil (
    id_perfil uuid NOT NULL,
    codigo character varying(50) NOT NULL,
    nome character varying(120) NOT NULL,
    descricao character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.perfil OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 34217)
-- Name: perfil_permissao; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.perfil_permissao (
    id_perfil_permissao uuid DEFAULT gen_random_uuid() NOT NULL,
    id_perfil uuid NOT NULL,
    id_permissao uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.perfil_permissao OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 34042)
-- Name: periodo_letivo; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.periodo_letivo (
    id_periodo_letivo uuid DEFAULT gen_random_uuid() NOT NULL,
    nome character varying(80) NOT NULL,
    data_inicio date NOT NULL,
    data_fim date NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT ck_periodo_letivo_datas CHECK ((data_fim >= data_inicio))
);


ALTER TABLE public.periodo_letivo OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 34184)
-- Name: permissao; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.permissao (
    id_permissao uuid NOT NULL,
    codigo character varying(80) NOT NULL,
    descricao character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.permissao OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 34115)
-- Name: responsavel; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.responsavel (
    id_responsavel uuid NOT NULL,
    nome_completo character varying(150) NOT NULL,
    cpf character varying(14) NOT NULL,
    email character varying(150),
    telefone character varying(20),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    rg character varying(20),
    cep character varying(8),
    logradouro character varying(150),
    numero character varying(20),
    complemento character varying(100),
    bairro character varying(100),
    cidade character varying(100),
    uf character(2)
);


ALTER TABLE public.responsavel OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 149489)
-- Name: serie; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.serie (
    id_serie uuid DEFAULT gen_random_uuid() NOT NULL,
    nome character varying(80) NOT NULL,
    ordem integer NOT NULL,
    nivel_ensino character varying(80),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.serie OWNER TO postgres;

--
-- TOC entry 234 (class 1259 OID 34239)
-- Name: sessao_autenticacao; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sessao_autenticacao (
    id_sessao_autenticacao uuid NOT NULL,
    id_usuario uuid NOT NULL,
    refresh_token_hash character varying(255) NOT NULL,
    expira_em timestamp without time zone NOT NULL,
    revogado boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    access_token_hash character varying(255),
    access_expira_em timestamp without time zone
);


ALTER TABLE public.sessao_autenticacao OWNER TO postgres;

--
-- TOC entry 238 (class 1259 OID 149628)
-- Name: transferencia_aluno; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.transferencia_aluno (
    id_transferencia uuid DEFAULT gen_random_uuid() NOT NULL,
    id_aluno uuid NOT NULL,
    id_escola_origem uuid NOT NULL,
    serie_origem character varying(80) NOT NULL,
    ano_letivo_origem character varying(20) NOT NULL,
    data_transferencia date,
    motivo_transferencia text,
    situacao_origem character varying(40),
    observacao text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    documentos_entregues text,
    tipo_transferencia character varying(20) DEFAULT 'ENTRADA'::character varying NOT NULL,
    status_transferencia character varying(20) DEFAULT 'EM_ANDAMENTO'::character varying NOT NULL,
    usuario_operacao character varying(150),
    data_hora_operacao timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_documento_confirmacao_vaga uuid,
    CONSTRAINT ck_transferencia_status CHECK (((status_transferencia)::text = ANY ((ARRAY['EM_ANDAMENTO'::character varying, 'CONFIRMADA'::character varying, 'CANCELADA'::character varying])::text[]))),
    CONSTRAINT ck_transferencia_tipo CHECK (((tipo_transferencia)::text = ANY ((ARRAY['ENTRADA'::character varying, 'SAIDA'::character varying])::text[])))
);


ALTER TABLE public.transferencia_aluno OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 34055)
-- Name: turma; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.turma (
    id_turma uuid DEFAULT gen_random_uuid() NOT NULL,
    codigo character varying(20) NOT NULL,
    nome character varying(120) NOT NULL,
    capacidade integer NOT NULL,
    id_periodo_letivo uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_serie uuid NOT NULL,
    turno character varying(30),
    status character varying(20) DEFAULT 'ATIVA'::character varying,
    CONSTRAINT ck_turma_capacidade CHECK ((capacidade > 0))
);


ALTER TABLE public.turma OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 34152)
-- Name: usuario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuario (
    id_usuario uuid NOT NULL,
    username character varying(80) NOT NULL,
    nome character varying(150) NOT NULL,
    email character varying(150) NOT NULL,
    senha_hash character varying(255) NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone
);


ALTER TABLE public.usuario OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 34195)
-- Name: usuario_perfil; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuario_perfil (
    id_usuario_perfil uuid DEFAULT gen_random_uuid() NOT NULL,
    id_usuario uuid NOT NULL,
    id_perfil uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.usuario_perfil OWNER TO postgres;

--
-- TOC entry 4874 (class 2604 OID 33832)
-- Name: flyway_audit_marker id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flyway_audit_marker ALTER COLUMN id SET DEFAULT nextval('public.flyway_audit_marker_id_seq'::regclass);


--
-- TOC entry 5178 (class 0 OID 34028)
-- Dependencies: 223
-- Data for Name: aluno; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.aluno (id_aluno, nome_completo, cpf, email, telefone, data_nascimento, created_at, rg, orgao_emissor_rg, uf_rg, nacionalidade, naturalidade, sexo, nome_social, cep, logradouro, numero, complemento, bairro, cidade, uf, status_aluno) FROM stdin;
605f5f8c-34b7-4b25-88f4-1366b72dae38	Pedro Costa	179.142.177-60	pedro.costa@gmail.com	(43) 903165296	1996-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
ce6cba48-faa1-4c58-a758-64438ae13bfa	Rodrigo Araujo	539.828.936-58	rodrigo.araujo@gmail.com	(43) 942346645	2003-05-04	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
e7592ddb-248b-496e-b483-64214b24129b	Mariana Rodrigues	102.505.663-97	mariana.rodrigues@gmail.com	(43) 924198140	1982-05-09	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
36aace50-4c0c-4ce8-8828-7662b441db4a	Fernanda Fernandes	783.461.166-67	fernanda.fernandes@gmail.com	(43) 974259233	1983-05-09	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
ec5aae4d-b9be-476b-9420-2d1bf3fb6d1a	Bruno Rodrigues	445.424.304-21	bruno.rodrigues@gmail.com	(43) 990423509	1991-05-07	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
f48099b9-7986-40a3-9d21-36402d79f9db	Pedro Almeida	133.015.411-86	pedro.almeida@gmail.com	(43) 964319870	1983-05-09	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
fb629e65-0027-4683-b2b2-128dfb160d38	Juliana Fernandes	415.795.908-61	juliana.fernandes@gmail.com	(43) 923100546	1987-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
ca822446-fd71-4056-b829-824a2d01b888	Carlos Araujo	994.514.935-14	carlos.araujo@gmail.com	(43) 919765994	1990-05-07	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
405c6a0c-898d-4bcd-a8b0-20f6ec38a8c2	Rafael Oliveira	043.927.601-20	rafael.oliveira@gmail.com	(43) 917190748	2008-05-02	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
09873526-11b6-40a4-9fb1-284df6ac4232	Pedro Oliveira	924.781.449-99	pedro.oliveira@gmail.com	(43) 944010142	2000-05-04	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
7161984e-6d94-46da-99c8-9e6efd56fdf4	Pedro Lima	070.442.848-25	pedro.lima@gmail.com	(43) 940933057	1995-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
295a47dd-3367-4a2d-8b6c-031549281178	Patricia Souza	286.030.542-40	patricia.souza@gmail.com	(43) 964060370	1992-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
e9446233-6140-4fe0-a710-40e1c449be5d	Juliana Lima	987.871.257-57	juliana.lima@gmail.com	(43) 943376060	1992-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
701689cb-ec91-41d3-a141-0c4568235a0c	Fernanda Silva	572.007.770-72	fernanda.silva@gmail.com	(43) 987481149	1997-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
43d11545-67e6-4f33-8a5b-3c7109e82692	Aline Santos	08544253571	aline.santos@gmail.com	(43) 94121-9970	1993-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	ATIVO
de9db71b-6c36-486a-9773-48589e73bcdf	Pedro Araujo	261.018.825-19	pedro.araujo@gmail.com	(43) 982960770	1986-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
4f92e98a-1bed-4d66-b6bf-f6767b759baf	Fernanda Pereira	108.105.566-93	fernanda.pereira@gmail.com	(43) 963894193	1984-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
75d296cc-5dd8-43f1-b3c9-7892a5f060a7	Mariana Almeida	687.278.890-92	mariana.almeida@gmail.com	(43) 912576058	1998-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
5f73a1ea-bab6-4482-8c4f-3357f782e19e	Juliana Araujo	766.438.396-02	juliana.araujo@gmail.com	(43) 944897734	2003-05-04	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
e485d1f1-da26-426e-98d5-a2152278e216	Juliana Souza	227.535.091-82	juliana.souza@gmail.com	(43) 946158101	1990-05-07	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
94017e27-b80f-4101-aad7-eab6da5e5741	Maria Santos	565.926.630-49	maria.santos@gmail.com	(43) 902616182	1995-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
fc0d9119-d4ab-4ecd-ab38-13244ecd66c0	Rodrigo Costa	939.593.030-68	rodrigo.costa@gmail.com	(43) 914634462	1995-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
41c70bcb-e421-48d7-ab64-bd4062e8f523	Patricia Rodrigues	825.039.984-61	patricia.rodrigues@gmail.com	(43) 946444072	2007-05-03	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
02e35175-2558-486c-8ade-bf49502ca87e	Rafael Araujo	891.316.468-08	rafael.araujo@gmail.com	(43) 987181680	1983-05-09	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
2a7f30e5-da82-42a9-b541-1e4f85d76e05	Mariana Fernandes	591.236.652-97	mariana.fernandes@gmail.com	(43) 915316647	1988-05-07	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
17f3f14e-d043-4edc-b577-33a48bf8e21f	Patricia Souza	111.747.639-14	patricia.souza@gmail.com	(43) 921759000	1985-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
a9b78668-871d-41e9-b515-55d2a9100976	Rodrigo Lima	226.910.711-03	rodrigo.lima@gmail.com	(43) 960330569	2001-05-04	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
fd473569-472b-4685-9087-ce039df96078	João Rodrigues	935.288.762-04	joão.rodrigues@gmail.com	(43) 935341223	2007-05-03	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
869e2678-a39d-487b-8745-01425264e76a	Rafael Souza	856.934.816-97	rafael.souza@gmail.com	(43) 901041284	2006-05-03	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
1db64bf4-8fc9-474e-8c5e-565882ad653f	Rafael Lima	262.071.907-18	rafael.lima@gmail.com	(43) 963398559	1997-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
b42dcbe5-38bf-43ff-a6fc-fb95e34df63a	Ana Oliveira	988.134.210-44	ana.oliveira@gmail.com	(43) 920556696	1985-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
2aaa760b-3e80-4539-84c9-b269ff99760e	Fernanda Souza	395.852.746-98	fernanda.souza@gmail.com	(43) 965862882	1994-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
e8f61467-24f9-4a06-9f2b-5155fb319c37	Ana Rodrigues	664.422.043-67	ana.rodrigues@gmail.com	(43) 944255083	1997-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
d815210a-9ee0-4fec-a9ca-e62cf0ac5c5c	João Lima	914.022.999-82	joão.lima@gmail.com	(43) 906556165	1993-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
5338b0af-de38-4a37-af50-30308126a7e3	Rafael Nascimento	156.302.991-07	rafael.nascimento@gmail.com	(43) 914814560	2008-05-02	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
132ffd97-8e89-4bc1-b69f-c43dce5aae91	Lucas Oliveira	024.084.901-97	lucas.oliveira@gmail.com	(43) 963988731	1989-05-07	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
3851bfd2-6859-486e-8d99-43f389d5a32c	Rodrigo Pereira	546.341.145-00	rodrigo.pereira@gmail.com	(43) 932737801	2006-05-03	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
93383f5b-c64b-4533-a1ee-e43233e5f8c3	Patricia Silva	822.686.083-10	patricia.silva@gmail.com	(43) 931780574	2008-05-02	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
3033309b-b41d-4025-ac33-acdee37177c6	Mariana Souza	108.323.474-93	mariana.souza@gmail.com	(43) 914693826	2008-05-02	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
11dbd6f9-c0f7-4933-a647-22c77580072e	Lucas Lima	136.628.699-51	lucas.lima@gmail.com	(43) 975121569	1995-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
aae4211f-397b-4369-9fe7-5b35eab07977	João Nascimento	876.108.857-97	joão.nascimento@gmail.com	(43) 925441802	1988-05-07	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
fbc38324-1ee8-4c1d-af6d-994e39e04868	Mariana Oliveira	860.993.104-87	mariana.oliveira@gmail.com	(43) 921519929	1997-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
eeac9a52-9aee-42ab-8f36-81cec52d9a0b	Rafael Pereira	422.790.423-47	rafael.pereira@gmail.com	(43) 934555965	1985-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
8268fc78-b302-4cae-9266-fa1938af530a	Maria Araujo	607.383.822-13	maria.araujo@gmail.com	(43) 992277579	1992-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
9475c2e9-6723-45a9-8428-6319ee54f796	Patricia Costa	177.237.610-82	patricia.costa@gmail.com	(43) 955712014	2004-05-03	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
befe028a-6949-421e-9175-0de574d6aa81	João Almeida	660.354.956-56	joão.almeida@gmail.com	(43) 907021352	1987-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
105322f0-6ac6-4f79-a8d1-d5a23378fa5a	Carlos Oliveira	138.320.922-77	carlos.oliveira@gmail.com	(43) 998888369	1988-05-07	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
afdf1f13-c57b-4e98-bd14-bcdff98ddfab	Maria Pereira	233.078.227-64	maria.pereira@gmail.com	(43) 964971318	1985-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
c09c7f5c-75f8-4b74-9ebd-2b00edc91ee0	Rodrigo Nascimento	831.109.014-98	rodrigo.nascimento@gmail.com	(43) 967675858	1994-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
a056c1ae-893e-4860-8cbd-29ab989d8136	Rafael Rodrigues	391.649.078-81	rafael.rodrigues@gmail.com	(43) 948873270	1993-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
3f5d78be-c2e0-49a7-b2f5-92aa9de815b1	Lucas Silva	172.685.476-17	lucas.silva@gmail.com	(43) 978595327	2000-05-04	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
c35c6754-d750-47a8-8644-a00353a23069	Carlos Souza	545.512.682-24	carlos.souza@gmail.com	(43) 966545092	2007-05-03	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
fafbfcf2-00cb-4bc6-839c-f48a55a58212	Maria Costa	049.427.000-43	maria.costa@gmail.com	(43) 907854588	1984-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
e48db26c-526e-46ed-ae60-1b735ce0ac80	Patricia Lima	756.085.219-01	patricia.lima@gmail.com	(43) 921045791	1997-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
83425ca3-9b56-4dfa-abdc-fd17bf577d44	Rodrigo Oliveira	978.883.438-81	rodrigo.oliveira@gmail.com	(43) 936481718	1994-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
f3fd0bae-496d-475b-9d9e-551f21a07423	Fernanda Araujo	074.148.209-63	fernanda.araujo@gmail.com	(43) 902038349	1998-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
b643752a-04fa-4827-8a8c-e9a8722b2b8f	Mariana Pereira	263.606.196-79	mariana.pereira@gmail.com	(43) 936864418	2002-05-04	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
9905d285-f6fd-4fe3-bfd3-cac2666d4dfa	Carlos Pereira	744.030.455-11	carlos.pereira@gmail.com	(43) 957068970	2008-05-02	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
01a05ccd-1239-499a-a245-3624fd7d8e5b	João Lima	154.014.932-31	joão.lima@gmail.com	(43) 953713265	1995-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
0aa812d8-b923-46b5-ae20-d457644b1dcd	Ana Lima	570.859.565-51	ana.lima@gmail.com	(43) 936814060	1985-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
f0114256-e989-49e1-a0d5-65068062b8a2	Maria Souza	493.658.473-89	maria.souza@gmail.com	(43) 956357450	2002-05-04	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
ef73523a-e409-418e-9578-c4b6f94cedaa	Juliana Pereira	290.284.769-62	juliana.pereira@gmail.com	(43) 946734152	2002-05-04	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
5c7bade6-3481-4e9a-a99d-f817cfeec174	Pedro Souza	504.691.944-30	pedro.souza@gmail.com	(43) 997190355	1990-05-07	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
52b7a55a-3a7b-4847-93f2-f8f4a2e61216	João Silva	979.838.997-26	joão.silva@gmail.com	(43) 999785226	1986-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
5e7d9c2e-bc51-4df8-b23f-96fa4d7fb322	Rodrigo Souza	585.810.309-04	rodrigo.souza@gmail.com	(43) 909330724	1996-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
04da4c7d-3423-4958-bc23-415f45e99c4b	Ana Santos	092.068.662-14	ana.santos@gmail.com	(43) 969401189	2000-05-04	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
213050f3-0e38-46ed-8d60-f312751fbdf6	João Costa	865.196.724-16	joão.costa@gmail.com	(43) 904725374	2007-05-03	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
5d6677db-9824-41b0-b17a-adcb346e677c	Mariana Costa	819.989.251-02	mariana.costa@gmail.com	(43) 951143634	2002-05-04	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
d3e1102e-153c-4ca8-b45d-beab2891bbcb	Rafael Fernandes	130.494.939-76	rafael.fernandes@gmail.com	(43) 999048261	1993-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
19910c8b-43f8-48cc-b321-50e41e2a9550	Gabriel Nascimento	982.858.904-49	gabriel.nascimento@gmail.com	(43) 929686410	1986-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
b9247a54-8047-421b-a1db-bf21ec5069c3	Bruno Souza	654.192.713-89	bruno.souza@gmail.com	(43) 912247585	1988-05-07	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
d3a9d878-ac1a-40a3-bd34-22920c6ad5cc	Rafael Santos	789.998.150-64	rafael.santos@gmail.com	(43) 968119104	1992-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
60d24729-b4e0-4c6d-9597-3cef3405aca3	Carlos Fernandes	426.529.406-51	carlos.fernandes@gmail.com	(43) 971357474	1998-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
a86a9f69-7528-4e6f-86f7-9e36ab010ea5	Gabriel Araujo	069.983.547-03	gabriel.araujo@gmail.com	(43) 929510455	1999-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
94c92850-cb2d-4b4f-b7d1-86fc563f53e8	Gabriel Santos	828.366.095-16	gabriel.santos@gmail.com	(43) 913057453	1983-05-09	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
93b7abef-477a-4bfb-b0a9-0f6964534c5c	Bruno Santos	322.079.681-89	bruno.santos@gmail.com	(43) 983936319	1999-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
fbd16a73-e045-46b2-9299-2ef00bce069c	Carlos Silva	477.854.394-78	carlos.silva@gmail.com	(43) 946161020	1990-05-07	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
755136db-d3c3-40cc-8bec-79ec79f98e8b	Juliana Almeida	755.687.109-61	juliana.almeida@gmail.com	(43) 990831152	2006-05-03	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
3288a5fe-9603-4bfe-ad1c-33e2c540fabb	Gabriel Costa	333.482.478-11	gabriel.costa@gmail.com	(43) 972241222	1987-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
5b01a0d0-0037-43ae-8db7-bc2f6a1e2a74	Gabriel Pereira	489.230.361-53	gabriel.pereira@gmail.com	(43) 922707681	1982-05-09	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
8794f02f-12fe-47bc-b393-497bd60587d0	Juliana Rodrigues	313.699.683-61	juliana.rodrigues@gmail.com	(43) 923970642	1983-05-09	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
9b455e13-35d8-4c7a-a4f2-b7512a732488	Mariana Lima	266.119.730-36	mariana.lima@gmail.com	(43) 947029019	1982-05-09	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
c7f40306-053c-4741-9f2e-96f296457efe	João Oliveira	821.510.023-65	joão.oliveira@gmail.com	(43) 985776180	2008-05-02	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
954fd3de-9239-43b3-b410-635c9a3720d3	Bruno Lima	500.765.100-18	bruno.lima@gmail.com	(43) 950780996	2005-05-03	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
51b03519-19d9-4bfa-9cb0-457afc8795ba	Juliana Oliveira	325.079.160-10	juliana.oliveira@gmail.com	(43) 978920444	2004-05-03	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
c907b8b5-459b-4c80-8a31-192c79de95b3	Aline Rodrigues	94233843233	aline.rodrigues@gmail.com	(43) 94161-8805	2007-05-03	2026-04-28 10:55:46.951674	12153456-5	\N	\N	\N	\N	FEMININO	\N	11691026	Rua Domingos Chieus	30	(Cidade Carolina)	Mato Dentro	Ubatuba	SP	ATIVO
d7163e5c-3b5d-46d8-bedf-34131892bffb	Lucas Pereira	077.562.009-26	lucas.pereira@gmail.com	(43) 964976616	1993-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
eee7748e-9676-4f3b-a9b4-d91fea51782a	Lucas Araujo	096.816.610-52	lucas.araujo@gmail.com	(43) 926190784	1987-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
1658b035-7d43-4d9c-be7d-418a93f411ba	Bruno Almeida	589.748.300-19	bruno.almeida@gmail.com	(43) 964745881	1992-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
61649b59-e7ef-4ceb-ba98-6dfa3a51ed16	Aline Silva	799.825.012-75	aline.silva@gmail.com	(43) 913277245	1988-05-07	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
99b88b83-a4dc-4e40-93c1-df55f8cd4144	Gabriel Silva	136.325.474-08	gabriel.silva@gmail.com	(43) 960328238	1998-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
536f928f-ff01-4b26-a6a0-920fd02fde6c	Ana Souza	237.470.527-71	ana.souza@gmail.com	(43) 934251388	1985-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
ed901de3-2fb2-4c09-be84-8d083354cc70	Rodrigo Santos	501.245.974-16	rodrigo.santos@gmail.com	(43) 921642270	1996-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
daed1e0d-84a8-49a9-ba26-12c169b28019	Patricia Araujo	400.735.925-36	patricia.araujo@gmail.com	(43) 921294697	2001-05-04	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
53a7d1cb-7cb6-4af6-86ca-31c686205f42	Mariana Santos	625.056.024-68	mariana.santos@gmail.com	(43) 972501262	1982-05-09	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
559c19db-5726-41a4-bed7-1781bb1b936e	Pedro Fernandes	741.027.501-42	pedro.fernandes@gmail.com	(43) 902844971	1997-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
e032903e-a3d7-44ad-b23b-23502f775da7	Fernanda Rodrigues	632.945.010-21	fernanda.rodrigues@gmail.com	(43) 925372022	1996-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
8090f5aa-f6ae-4e7d-9a47-8dafe92904b0	Carlos Santos	873.866.862-93	carlos.santos@gmail.com	(43) 919450609	1994-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
76fbcbaa-2485-450f-9b4a-cf8a3475c9d1	Lucas Fernandes	147.189.589-11	lucas.fernandes@gmail.com	(43) 952875497	1986-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
06888ba9-c610-4460-9bcf-f4c672b6c8ec	Juliana Nascimento	180.035.693-54	juliana.nascimento@gmail.com	(43) 933700657	1984-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
e7deb194-e245-405e-b9b5-4548d6145f36	Fernanda Nascimento	170.788.230-44	fernanda.nascimento@gmail.com	(43) 926290716	2000-05-04	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
b71dfb88-7b20-4db8-a9c4-be7fb07f6c5c	Rodrigo Fernandes	361.427.535-35	rodrigo.fernandes@gmail.com	(43) 938669754	1983-05-09	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
bd8b393e-149c-4656-9ff8-0058afea9777	Rodrigo Almeida	018.659.643-01	rodrigo.almeida@gmail.com	(43) 935062444	1998-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
6e7df7f1-1029-43b7-947a-12fde311bd2e	Mariana Silva	172.882.421-45	mariana.silva@gmail.com	(43) 900462018	1984-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
5558eb0e-1666-4918-a350-cb1bb93b2eae	João Nascimento	663.680.738-53	joão.nascimento@gmail.com	(43) 957178090	2005-05-03	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
940cc30b-07d2-404f-9e35-dd90daf0d534	Patricia Pereira	166.112.340-63	patricia.pereira@gmail.com	(43) 902015394	1986-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
e587d07d-2c9d-4795-9b29-958d03d58ffa	Fernanda Santos	094.336.320-95	fernanda.santos@gmail.com	(43) 997078385	1996-05-05	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
b19bfc11-dc3a-4ded-91f4-ae8b38222fdb	Pedro Oliveira	873.758.038-89	pedro.oliveira@gmail.com	(43) 927012759	2008-05-02	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
f54eacb3-1e4b-437d-a9fb-873d028715fe	Patricia Nascimento	982.985.517-11	patricia.nascimento@gmail.com	(43) 936850125	1992-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
1bbb7df8-8d38-4d98-be02-db9ba95cfbd2	João Araujo	772.942.236-11	joão.araujo@gmail.com	(43) 924372440	2003-05-04	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
2fbd3344-189a-4bba-a810-094798920c36	Pedro Nascimento	006.971.154-29	pedro.nascimento@gmail.com	(43) 978432437	2003-05-04	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
36721802-5f57-4c66-946c-7cca67229d8e	Maria Nascimento	152.396.838-91	maria.nascimento@gmail.com	(43) 956896053	2001-05-04	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
96d34470-2530-4260-8b00-dad7a15a3187	Fernanda Costa	618.005.342-13	fernanda.costa@gmail.com	(43) 949622319	2000-05-04	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
8b46aa5f-4048-4d64-b977-b75adb416391	Pedro Santos	409.897.034-10	pedro.santos@gmail.com	(43) 908755981	1983-05-09	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
4c3037fa-9595-4a52-bce1-cb8825d984e8	Maria Fernandes	165.778.202-61	maria.fernandes@gmail.com	(43) 934744447	2006-05-03	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
2c70019e-70cc-4c02-943a-004a04ec9e78	Aline Souza	549.359.627-04	aline.souza@gmail.com	(43) 953400162	1984-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
a3e2500c-0ed5-4c4d-8d99-ca16d9040049	Lucas Nascimento	618.573.828-71	lucas.nascimento@gmail.com	(43) 978790727	2007-05-03	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
127bf46c-2525-4e0e-9a87-9fe5262a38c4	Gabriel Araujo	213.313.764-59	gabriel.araujo@gmail.com	(43) 914390999	1992-05-06	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
739acf3b-c466-4757-a0ab-0357fae41d32	Lucas Rodrigues	732.404.393-36	lucas.rodrigues@gmail.com	(43) 994051590	1986-05-08	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
87ca44d2-ec91-4f11-941a-c0f7f9e76c20	Bruno Pereira	611.321.367-68	bruno.pereira@gmail.com	(43) 908613745	1983-05-09	2026-04-28 10:55:46.951674	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
7af88396-918c-4d67-b543-fac9364a14e4	Zoeh Carvalho Dantas	56242691884	zoeh.carvalho.dantas@gmail.com	(12) 98221-4660	2011-02-16	2026-05-23 01:10:03.880974	\N	\N	\N	\N	\N	FEMININO	\N	11691024	Rua Magnólias	98	(Cidade Carolina)	Mato Dentro	Ubatuba	SP	ATIVO
\.


--
-- TOC entry 5183 (class 0 OID 34128)
-- Dependencies: 228
-- Data for Name: aluno_responsavel; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.aluno_responsavel (id_aluno_responsavel, id_aluno, id_responsavel, created_at, parentesco, responsavel_financeiro, responsavel_pedagogico, autorizado_retirar) FROM stdin;
9003bb56-bf6c-43ca-8da4-af58d70148da	7af88396-918c-4d67-b543-fac9364a14e4	e45091cf-b75a-4911-ab83-e83ba28958b3	2026-05-24 14:01:12.599777	\N	f	f	f
a2e83ce7-2459-4b2b-8f15-3f65b62e0559	c907b8b5-459b-4c80-8a31-192c79de95b3	7e17305c-fe8c-4341-8d38-115501107087	2026-05-24 17:26:41.626419	\N	f	f	f
\.


--
-- TOC entry 5191 (class 0 OID 149513)
-- Dependencies: 236
-- Data for Name: disciplina; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.disciplina (id_disciplina, nome, carga_horaria, status, created_at) FROM stdin;
c7d2ebde-1868-4e9a-8f82-f89c5b684e1f	Matematica I	1200	ATIVA	2026-05-24 02:34:00.033561
\.


--
-- TOC entry 5192 (class 0 OID 149616)
-- Dependencies: 237
-- Data for Name: escola_origem; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.escola_origem (id_escola_origem, nome_escola, codigo_inep, cnpj, cep, logradouro, numero, complemento, bairro, cidade, uf, created_at) FROM stdin;
\.


--
-- TOC entry 5177 (class 0 OID 33829)
-- Dependencies: 222
-- Data for Name: flyway_audit_marker; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.flyway_audit_marker (id, description, created_at) FROM stdin;
1	initial baseline	2026-04-24 02:07:18.052318
\.


--
-- TOC entry 5175 (class 0 OID 33811)
-- Dependencies: 220
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	001	initial baseline	SQL	V001__initial_baseline.sql	-177652480	postgres	2026-04-24 02:07:18.033755	13	t
2	002	create table aluno	SQL	V002__create_table_aluno.sql	925670879	postgres	2026-04-24 02:07:18.072616	7	t
3	003	create table periodo letivo	SQL	V003__create_table_periodo_letivo.sql	1033882651	postgres	2026-04-24 14:27:03.463004	78	t
4	004	create table turma	SQL	V004__create_table_turma.sql	1006398486	postgres	2026-04-24 14:27:03.564381	14	t
5	005	create table matricula	SQL	V005__create_table_matricula.sql	-580960952	postgres	2026-04-24 16:00:30.752314	37	t
6	006	add column telefone to aluno	SQL	V006__add_column_telefone_to_aluno.sql	-2102973069	postgres	2026-04-27 02:33:15.135329	36	t
7	007	migrate bigint to uuid	SQL	V007__migrate_bigint_to_uuid.sql	-1547274511	postgres	2026-04-28 10:04:12.887204	75	t
8	008	create table responsavel	SQL	V008__create_table_responsavel.sql	-2110432559	postgres	2026-04-28 11:33:50.046595	47	t
9	009	create table aluno responsavel	SQL	V009__create_table_aluno_responsavel.sql	170818361	postgres	2026-04-28 11:33:50.118087	11	t
10	010	create access control tables	SQL	V010__create_access_control_tables.sql	1768107381	postgres	2026-04-28 18:51:34.915348	120	t
11	011	add access token columns to sessao autenticacao	SQL	V011__add_access_token_columns_to_sessao_autenticacao.sql	966131119	postgres	2026-04-28 21:11:52.287512	55	t
12	012	seed default admin user	SQL	V012__seed_default_admin_user.sql	1444154028	postgres	2026-04-28 23:13:38.34084	23	t
13	013	normalize default admin password	SQL	V013__normalize_default_admin_password.sql	-93742760	postgres	2026-04-28 23:38:13.368981	14	t
14	014	rename usuario permission to admin	SQL	V014__rename_usuario_permission_to_admin.sql	2021371125	postgres	2026-04-30 09:33:27.673718	14	t
15	015	ensure default admin access	SQL	V015__ensure_default_admin_access.sql	1966672025	postgres	2026-04-30 11:07:44.073799	15	t
16	016	set defaults for join table ids	SQL	V016__set_defaults_for_join_table_ids.sql	-2118855116	postgres	2026-05-21 15:56:47.21337	12	t
17	017	allow matricula status transitions	SQL	V017__allow_matricula_status_transitions.sql	1374061471	postgres	2026-05-23 05:34:06.561847	37	t
18	018	add student responsible address fields	SQL	V018__add_student_responsible_address_fields.sql	1300085178	postgres	2026-05-23 20:35:53.974196	110	t
19	019	create serie and update turma	SQL	V019__create_serie_and_update_turma.sql	1883281670	postgres	2026-05-23 20:35:54.117063	39	t
20	020	add matricula type date observation	SQL	V020__add_matricula_type_date_observation.sql	2068571787	postgres	2026-05-23 20:35:54.170436	14	t
21	021	create school history tables	SQL	V021__create_school_history_tables.sql	-1396469219	postgres	2026-05-23 20:35:54.190597	39	t
22	022	create transferencia tables	SQL	V022__create_transferencia_tables.sql	1922535024	postgres	2026-05-23 20:35:54.239401	16	t
24	024	add final check fields	SQL	V024__add_final_check_fields.sql	-1463725686	postgres	2026-05-23 20:35:54.283189	2	t
25	025	refactor historico escolar documental	SQL	V025__refactor_historico_escolar_documental.sql	670320284	postgres	2026-05-24 04:18:15.11212	150	t
26	026	add transferencia operacao fields	SQL	V026__add_transferencia_operacao_fields.sql	553128059	postgres	2026-05-24 18:18:50.105043	95	t
\.


--
-- TOC entry 5195 (class 0 OID 149681)
-- Dependencies: 240
-- Data for Name: historico_escolar; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.historico_escolar (id, nome_aluno, rg_ren, ra, rm, data_nascimento, municipio_nascimento, estado_nascimento, pais_nascimento, nome_escola, endereco_escola, municipio_escola, cep_escola, telefone_escola, email_escola, ano_conclusao, ensino_concluido, data_emissao, diretor_nome, diretor_rg, gerente_organizacao_nome, gerente_organizacao_rg, doe_numero, doe_data, doe_volume, doe_pagina, observacoes) FROM stdin;
\.


--
-- TOC entry 5196 (class 0 OID 149691)
-- Dependencies: 241
-- Data for Name: historico_escolar_item; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.historico_escolar_item (id, historico_escolar_id, componente_curricular, ano_letivo, serie, ciclo, nota_conceito, total_aulas, carga_horaria) FROM stdin;
\.


--
-- TOC entry 5181 (class 0 OID 34076)
-- Dependencies: 226
-- Data for Name: matricula; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.matricula (id_matricula, id_aluno, id_turma, id_periodo_letivo, status, created_at, tipo_matricula, data_matricula, observacao) FROM stdin;
30000000-0000-0000-0000-000000000006	fb629e65-0027-4683-b2b2-128dfb160d38	20000000-0000-0000-0000-000000000004	10000000-0000-0000-0000-000000000013	ATIVA	2026-04-30 04:10:00	\N	2026-05-23	\N
30000000-0000-0000-0000-000000000007	b42dcbe5-38bf-43ff-a6fc-fb95e34df63a	20000000-0000-0000-0000-000000000005	10000000-0000-0000-0000-000000000013	ATIVA	2026-04-30 04:10:00	\N	2026-05-23	\N
30000000-0000-0000-0000-000000000008	405c6a0c-898d-4bcd-a8b0-20f6ec38a8c2	20000000-0000-0000-0000-000000000006	10000000-0000-0000-0000-000000000013	ATIVA	2026-04-30 04:10:00	\N	2026-05-23	\N
30000000-0000-0000-0000-000000000010	7161984e-6d94-46da-99c8-9e6efd56fdf4	20000000-0000-0000-0000-000000000008	10000000-0000-0000-0000-000000000013	ATIVA	2026-04-30 04:10:00	\N	2026-05-23	\N
eecdf59c-02aa-4323-934e-bc5a31b5feda	295a47dd-3367-4a2d-8b6c-031549281178	20000000-0000-0000-0000-000000000010	10000000-0000-0000-0000-000000000013	ATIVA	2026-05-23 04:38:46.207873	\N	2026-05-23	\N
16131e76-61e9-4ac0-a52c-82843dbdc326	e9446233-6140-4fe0-a710-40e1c449be5d	20000000-0000-0000-0000-000000000010	10000000-0000-0000-0000-000000000013	ATIVA	2026-05-23 04:38:53.69988	\N	2026-05-23	\N
16521743-096c-4466-b2d0-36749e120113	5f73a1ea-bab6-4482-8c4f-3357f782e19e	20000000-0000-0000-0000-000000000010	10000000-0000-0000-0000-000000000013	ATIVA	2026-05-23 04:39:04.702804	\N	2026-05-23	\N
19234ecf-9c08-4756-a30e-0fc83c73f2a3	de9db71b-6c36-486a-9773-48589e73bcdf	20000000-0000-0000-0000-000000000010	10000000-0000-0000-0000-000000000013	ATIVA	2026-05-23 04:39:12.281113	\N	2026-05-23	\N
51ad0ec0-c24c-47ab-9975-95f97b2c1e05	75d296cc-5dd8-43f1-b3c9-7892a5f060a7	20000000-0000-0000-0000-000000000010	10000000-0000-0000-0000-000000000013	ATIVA	2026-05-23 04:39:20.660467	\N	2026-05-23	\N
d325c495-05ed-4e97-908c-23dd921cab30	02e35175-2558-486c-8ade-bf49502ca87e	20000000-0000-0000-0000-000000000010	10000000-0000-0000-0000-000000000013	ATIVA	2026-05-23 04:39:30.523877	\N	2026-05-23	\N
9a37d721-eb4f-4af4-bd1a-b2d1c8b6057d	a9b78668-871d-41e9-b515-55d2a9100976	20000000-0000-0000-0000-000000000010	10000000-0000-0000-0000-000000000013	ATIVA	2026-05-23 04:39:50.32729	\N	2026-05-23	\N
e09356cc-e098-4a5c-8543-739efd283f96	fd473569-472b-4685-9087-ce039df96078	20000000-0000-0000-0000-000000000010	10000000-0000-0000-0000-000000000013	ATIVA	2026-05-23 04:39:57.55277	\N	2026-05-23	\N
2761575d-eb3e-4e0e-9354-8e95a9002ebe	ec5aae4d-b9be-476b-9420-2d1bf3fb6d1a	20000000-0000-0000-0000-000000000010	10000000-0000-0000-0000-000000000013	ATIVA	2026-05-23 04:41:01.872851	\N	2026-05-23	\N
01274074-44da-437d-8e5e-0fd3700783af	09873526-11b6-40a4-9fb1-284df6ac4232	20000000-0000-0000-0000-000000000010	10000000-0000-0000-0000-000000000013	ATIVA	2026-05-23 04:41:12.853472	\N	2026-05-23	\N
4702a667-1ad1-4bfe-930f-8415773e3adf	701689cb-ec91-41d3-a141-0c4568235a0c	20000000-0000-0000-0000-000000000010	10000000-0000-0000-0000-000000000013	ATIVA	2026-05-23 04:43:24.598336	\N	2026-05-23	\N
5f680731-3737-4e3a-bfc1-435abd8d7f95	36aace50-4c0c-4ce8-8828-7662b441db4a	20000000-0000-0000-0000-000000000010	10000000-0000-0000-0000-000000000013	ATIVA	2026-05-23 04:43:33.374237	\N	2026-05-23	\N
9e7875a6-b75d-4cbb-94eb-6281c2016019	4f92e98a-1bed-4d66-b6bf-f6767b759baf	20000000-0000-0000-0000-000000000009	10000000-0000-0000-0000-000000000013	ATIVA	2026-05-23 04:58:04.27627	\N	2026-05-23	\N
30000000-0000-0000-0000-000000000002	e7592ddb-248b-496e-b483-64214b24129b	20000000-0000-0000-0000-000000000001	10000000-0000-0000-0000-000000000013	ATIVA	2026-04-30 04:10:00	\N	2026-05-23	\N
361f5061-0408-4da5-a8db-0fad38f2e99f	605f5f8c-34b7-4b25-88f4-1366b72dae38	20000000-0000-0000-0000-000000000001	10000000-0000-0000-0000-000000000013	ATIVA	2026-05-23 00:38:02.663477	\N	2026-05-23	\N
30000000-0000-0000-0000-000000000003	3f5d78be-c2e0-49a7-b2f5-92aa9de815b1	20000000-0000-0000-0000-000000000002	10000000-0000-0000-0000-000000000013	ATIVA	2026-04-30 04:10:00	\N	2026-05-23	\N
30000000-0000-0000-0000-000000000005	f48099b9-7986-40a3-9d21-36402d79f9db	20000000-0000-0000-0000-000000000003	10000000-0000-0000-0000-000000000013	ATIVA	2026-04-30 04:10:00	\N	2026-05-23	\N
\.


--
-- TOC entry 5185 (class 0 OID 34172)
-- Dependencies: 230
-- Data for Name: perfil; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.perfil (id_perfil, codigo, nome, descricao, created_at) FROM stdin;
22222222-2222-2222-2222-222222222222	ADMIN	Administrador	Perfil administrativo	2026-04-28 21:46:16.985701
2d7b407d-c971-41d6-97c1-95b69c4c06c0	USUARIO	USUARIO	\N	2026-05-21 16:08:19.265358
6c5bb0da-068a-441a-9c59-2f3315bff1dc	OPERADOR	OPERADOR	\N	2026-05-21 16:15:18.069469
55ccd517-6a3a-411d-96d4-4c5778a5b1c2	servidor	servidor	\N	2026-05-21 19:46:53.795221
\.


--
-- TOC entry 5188 (class 0 OID 34217)
-- Dependencies: 233
-- Data for Name: perfil_permissao; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.perfil_permissao (id_perfil_permissao, id_perfil, id_permissao, created_at) FROM stdin;
16c8c582-6cc7-40f3-bb49-8c990dcdc814	22222222-2222-2222-2222-222222222222	11111111-1111-1111-1111-111111111111	2026-05-01 13:23:39.42481
0a5b09b8-ea1e-4d30-b172-d5ce736121be	22222222-2222-2222-2222-222222222222	409f3816-3a42-4fa0-ba12-d9150421816f	2026-05-01 13:23:39.426652
aed80c56-6ae9-4dc4-944e-f7d49f50eb40	22222222-2222-2222-2222-222222222222	dd5f36a4-a68f-4a67-932a-32410b23c635	2026-05-01 13:23:39.427261
4a0db779-38db-4711-936d-bb53a27e5652	22222222-2222-2222-2222-222222222222	6a38d53e-95fe-4cdd-85f7-cfda1577ec84	2026-05-01 13:23:39.427773
86edff8f-018e-4c08-a73c-2050dbd4bcf4	22222222-2222-2222-2222-222222222222	c5489e85-0b0b-413c-89af-d132a3f8a719	2026-05-01 13:23:39.428295
b498306e-af83-4dad-88cc-1fcfc3052831	22222222-2222-2222-2222-222222222222	abb88f3a-b7a3-4c09-a5e6-edae92a38871	2026-05-01 13:23:39.428797
202e552d-d14b-4b01-b74b-0a1b1fb3147c	2d7b407d-c971-41d6-97c1-95b69c4c06c0	abb88f3a-b7a3-4c09-a5e6-edae92a38871	2026-05-21 16:14:59.271945
a8ef1df8-2798-49ae-9a4c-b6fef8a71341	2d7b407d-c971-41d6-97c1-95b69c4c06c0	409f3816-3a42-4fa0-ba12-d9150421816f	2026-05-21 16:14:59.271945
eed693fd-9892-4cf3-b166-de793b5c3d9d	2d7b407d-c971-41d6-97c1-95b69c4c06c0	c5489e85-0b0b-413c-89af-d132a3f8a719	2026-05-21 16:14:59.271945
7ac5bb48-a763-49c8-9f93-1786b815aad5	2d7b407d-c971-41d6-97c1-95b69c4c06c0	dd5f36a4-a68f-4a67-932a-32410b23c635	2026-05-21 16:14:59.271945
59661f15-6b15-4a4d-853c-1b40c262d226	6c5bb0da-068a-441a-9c59-2f3315bff1dc	6a38d53e-95fe-4cdd-85f7-cfda1577ec84	2026-05-21 16:47:21.022503
f652ce56-08fc-4f26-9fbb-c84deff096fe	55ccd517-6a3a-411d-96d4-4c5778a5b1c2	c5489e85-0b0b-413c-89af-d132a3f8a719	2026-05-21 19:46:53.795221
\.


--
-- TOC entry 5179 (class 0 OID 34042)
-- Dependencies: 224
-- Data for Name: periodo_letivo; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.periodo_letivo (id_periodo_letivo, nome, data_inicio, data_fim, created_at) FROM stdin;
10000000-0000-0000-0000-000000000001	1 Semestre 2020	2020-01-20	2020-07-10	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000002	2 Semestre 2020	2020-07-20	2020-12-18	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000003	1 Semestre 2021	2021-01-25	2021-07-09	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000004	2 Semestre 2021	2021-07-26	2021-12-17	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000005	1 Semestre 2022	2022-01-24	2022-07-08	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000006	2 Semestre 2022	2022-07-25	2022-12-16	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000007	1 Semestre 2023	2023-01-23	2023-07-07	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000008	2 Semestre 2023	2023-07-24	2023-12-15	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000009	1 Semestre 2024	2024-01-22	2024-07-05	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000010	2 Semestre 2024	2024-07-22	2024-12-13	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000011	1 Semestre 2025	2025-01-20	2025-07-04	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000012	2 Semestre 2025	2025-07-21	2025-12-12	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000013	1 Semestre 2026	2026-01-19	2026-07-03	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000014	2 Semestre 2026	2026-07-20	2026-12-11	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000015	1 Semestre 2027	2027-01-18	2027-07-02	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000016	2 Semestre 2027	2027-07-19	2027-12-10	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000017	1 Semestre 2028	2028-01-24	2028-07-07	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000018	2 Semestre 2028	2028-07-24	2028-12-15	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000019	1 Semestre 2029	2029-01-22	2029-07-06	2026-04-30 04:00:00
10000000-0000-0000-0000-000000000020	2 Semestre 2029	2029-07-23	2029-12-14	2026-04-30 04:00:00
\.


--
-- TOC entry 5186 (class 0 OID 34184)
-- Dependencies: 231
-- Data for Name: permissao; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.permissao (id_permissao, codigo, descricao, created_at) FROM stdin;
409f3816-3a42-4fa0-ba12-d9150421816f	CREATE	Permissão de criar um registro	2026-04-30 08:34:32.124269
dd5f36a4-a68f-4a67-932a-32410b23c635	UPDATE	Permissão de editar um registo	2026-05-01 13:20:16.274521
6a38d53e-95fe-4cdd-85f7-cfda1577ec84	DELETE	Permissão para excluir um registro	2026-05-01 13:20:43.891187
c5489e85-0b0b-413c-89af-d132a3f8a719	READ	Permissão de ler um registro	2026-05-01 13:21:48.194127
abb88f3a-b7a3-4c09-a5e6-edae92a38871	READ ALL	Permissão de ler uma lista de registros	2026-05-01 13:22:37.586079
11111111-1111-1111-1111-111111111111	ADMIN	Adimistração total do sistema	2026-04-28 21:46:16.985701
\.


--
-- TOC entry 5182 (class 0 OID 34115)
-- Dependencies: 227
-- Data for Name: responsavel; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.responsavel (id_responsavel, nome_completo, cpf, email, telefone, created_at, rg, cep, logradouro, numero, complemento, bairro, cidade, uf) FROM stdin;
e45091cf-b75a-4911-ab83-e83ba28958b3	PAULO JOSE ROCHA DANTAS	12189253888	pjr_dantas@hotmail.com	(11) 97954-9530	2026-04-28 12:59:59.345894	\N	\N	\N	\N	\N	\N	\N	\N
43a99439-bb71-4b04-bdd3-ac2eb2856c93	Lucas Pereira	140.055.775-55	lucas.pereira@gmail.com	(43) 939734270	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
d2e95b16-c452-4e76-ab23-ef8e1675cbd6	Carlos Lima	170.653.921-55	carlos.lima@gmail.com	(43) 967221448	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
13851573-1abe-4cda-908c-480c7fa170be	Carlos Araujo	103.369.401-14	carlos.araujo@gmail.com	(43) 904076733	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
8090f252-d097-4ace-9e9f-23bc415dbcd4	Maria Almeida	419.928.428-17	maria.almeida@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
dc253bf6-85b5-4504-87c9-5dea38592d05	João Santos	775.095.771-52	\N	(43) 951310372	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
b648b546-4210-49f0-8822-046c6b4a366e	Carlos Lima	068.265.119-24	carlos.lima@gmail.com	(43) 995451793	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
7e17305c-fe8c-4341-8d38-115501107087	Rafael Pereira	518.133.912-38	rafael.pereira@gmail.com	(43) 961053189	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
57f3c617-82b4-48b8-8bb1-f66f7ff2582b	Rodrigo Pereira	550.626.383-05	rodrigo.pereira@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
55ca9151-7a72-4da9-9c61-2e4f41b6ed26	Fernanda Costa	673.508.301-65	fernanda.costa@gmail.com	(43) 974961983	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
53c7a7e9-2987-4bce-acd7-e68a79f176fb	Aline Oliveira	063.361.081-04	\N	(43) 954145915	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
c5ba65d9-8d0f-460a-8390-db7027f98cbd	Bruno Souza	028.061.061-17	bruno.souza@gmail.com	(43) 975496914	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
b0b5c2d8-dbfd-4b0b-86c7-3d5ae1d6c72c	Bruno Souza	658.567.561-42	bruno.souza@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
d53533e8-a1b4-4ecc-b58b-e59c778c9612	Patricia Almeida	207.711.408-86	patricia.almeida@gmail.com	(43) 985375539	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
ad8526da-7e14-46bb-bab1-76a02805def6	Ana Lima	232.247.761-37	ana.lima@gmail.com	(43) 919196329	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
6939421d-bbae-4b45-a57a-908595a2a129	Carlos Nascimento	940.873.064-02	\N	(43) 984082324	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
9f69ec07-a12c-4bce-a5de-68e62f23cd38	Aline Santos	627.815.235-51	aline.santos@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
4daf0031-88ce-4a1a-88bf-3c669a395479	Ana Oliveira	438.558.536-90	ana.oliveira@gmail.com	(43) 984473075	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
c8ecd5a2-9bff-49b1-bb48-d7ba15cf46dd	Mariana Pereira	938.618.305-66	mariana.pereira@gmail.com	(43) 927829040	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
e851c60e-9a2d-4add-97bf-cd858c899b37	Juliana Rodrigues	440.902.443-40	juliana.rodrigues@gmail.com	(43) 904637951	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
6a3b916d-d8f6-47c7-ad71-568715816efe	Lucas Lima	493.613.494-57	\N	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
d318b0d4-71c3-4a81-8c6d-44f197a2aada	Patricia Costa	574.474.801-69	patricia.costa@gmail.com	(43) 998380610	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
6189c8e3-0df7-4bee-b3b2-349d67bb5779	Patricia Araujo	413.263.292-01	patricia.araujo@gmail.com	(43) 975617686	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
346719d5-f353-4951-9723-25568f90fc5a	Bruno Araujo	562.204.882-61	bruno.araujo@gmail.com	(43) 902705890	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
816e02ed-0a3c-4892-a719-593b6ca4ee74	Patricia Oliveira	107.697.329-92	patricia.oliveira@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
af0818fa-03a1-4f63-bd8d-02f5af0f504a	Rodrigo Souza	349.441.900-18	\N	(43) 993630464	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
a7f1cdbe-4a15-4685-827e-718eebc63cdf	Carlos Araujo	522.031.258-88	carlos.araujo@gmail.com	(43) 900719058	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
69b835b3-33a1-47a9-9b17-f11c4ed89148	Rafael Rodrigues	863.059.880-81	rafael.rodrigues@gmail.com	(43) 950012243	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
717cc309-e67a-4f3e-856b-271f2ca3e6ae	Gabriel Araujo	054.548.410-30	gabriel.araujo@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
2aa3057c-9e54-449d-8518-56529572e318	Ana Araujo	276.514.352-81	ana.araujo@gmail.com	(43) 909555657	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
7f33b5f9-0916-4ac3-9cad-d671d643385a	Gabriel Fernandes	007.794.454-22	\N	(43) 915685279	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
8257689c-a53b-4089-b995-aca16948bf4f	Lucas Costa	362.227.463-81	lucas.costa@gmail.com	(43) 960894848	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
f6dae0f5-cd48-4737-8a01-aaf6e6007843	Carlos Pereira	721.524.061-41	carlos.pereira@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
6d0091bd-0160-42f8-a038-a5c54c949d8a	Rodrigo Nascimento	932.573.247-58	rodrigo.nascimento@gmail.com	(43) 984256738	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
7ff01519-4dae-473d-82d9-784542c70d4d	João Santos	273.379.601-15	joão.santos@gmail.com	(43) 939929279	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
5d18198e-77cf-4434-b959-bf5d6a09005b	Rafael Lima	980.237.599-31	\N	(43) 949515707	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
06902fb6-efbb-43a5-aeb9-a43111730dfd	Bruno Rodrigues	190.323.648-71	bruno.rodrigues@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
07ee6c42-5edf-4825-9693-bd531fd86e40	Fernanda Souza	470.017.561-30	fernanda.souza@gmail.com	(43) 940445621	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
9670fcb1-e4c7-467a-bdaf-1ad582265342	Ana Souza	720.100.517-18	ana.souza@gmail.com	(43) 984578151	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
dc511890-bcdd-410c-9d64-347a1d285d0b	Patricia Santos	948.005.954-10	patricia.santos@gmail.com	(43) 910943923	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
c14044b9-839d-4979-86af-6c9ab7a6c28c	João Lima	452.394.993-06	\N	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
fbb71361-25cf-4da7-914e-db64811b2d26	Carlos Araujo	815.398.398-93	carlos.araujo@gmail.com	(43) 981475710	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
0d9c3e77-ca24-408d-868f-18c42881f01a	Patricia Oliveira	068.761.907-65	patricia.oliveira@gmail.com	(43) 954283420	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
a95e2eb6-c24c-45de-8904-89ce20959305	Rodrigo Fernandes	079.191.937-45	rodrigo.fernandes@gmail.com	(43) 999242153	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
4fd9a2f3-b203-47b9-beb0-f5a4afe0ef8c	Juliana Araujo	032.963.054-75	juliana.araujo@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
2eaa3f88-db01-45e0-bd62-04dd88c75c94	Bruno Araujo	869.394.215-81	\N	(43) 927286026	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
72151689-e007-4571-b938-7f2033cbcbe0	Ana Santos	946.435.919-60	ana.santos@gmail.com	(43) 909176894	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
bd8f415d-c429-4040-b325-b6062beac86f	Maria Souza	404.847.846-09	maria.souza@gmail.com	(43) 933050850	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
8352d14f-286e-493c-9fb3-cd12cc01ecea	Rafael Santos	786.478.947-15	rafael.santos@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
195c28ef-4ea7-4619-ae51-81eacff58206	Maria Araujo	131.923.408-94	maria.araujo@gmail.com	(43) 900983062	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
db768c9a-e54a-45e5-a8b8-da5c54a8cd35	João Almeida	715.702.061-12	\N	(43) 986845240	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
4dbdabff-a096-4c0c-b790-82a3730ec70a	Mariana Silva	498.735.301-69	mariana.silva@gmail.com	(43) 908779368	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
f12f2d5a-ad38-4623-83da-27012e9abc96	Bruno Santos	596.381.392-18	bruno.santos@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
0d1c9d66-8442-4fbf-b215-ed32227544b4	Carlos Fernandes	396.687.294-39	carlos.fernandes@gmail.com	(43) 925680993	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
7e8ba229-34ed-470b-8e73-0b95b31559f9	Juliana Costa	936.031.968-64	juliana.costa@gmail.com	(43) 906015610	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
4b7305f4-3666-482d-99df-c6948e7260d5	Carlos Costa	523.913.797-81	\N	(43) 910602884	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
c603ab97-9486-4df1-b4f2-6606667f4198	Aline Oliveira	165.182.438-05	aline.oliveira@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
c1e65665-8155-412f-ba8a-99d8a4c17afb	Juliana Nascimento	053.301.812-91	juliana.nascimento@gmail.com	(43) 915335833	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
f4b2a859-d619-42c2-89cc-15cddfda8dd5	Ana Santos	687.549.598-88	ana.santos@gmail.com	(43) 985041417	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
f7e17c58-f740-4175-9503-81a4d4eded8b	João Nascimento	702.895.970-81	joão.nascimento@gmail.com	(43) 987032027	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
d0ae3d38-5aa7-4749-8475-a5bded08f2d4	Maria Nascimento	639.618.835-05	\N	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
9e3547dc-0275-434a-ac9e-90f2145b06b4	Mariana Santos	649.518.755-60	mariana.santos@gmail.com	(43) 993078748	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
f7f262d5-2d6a-479d-8057-6a764637e1c2	Maria Santos	094.961.067-49	maria.santos@gmail.com	(43) 951812105	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
fca2be68-aca5-4740-b8fc-bebf026b1079	Fernanda Nascimento	375.306.517-06	fernanda.nascimento@gmail.com	(43) 913852302	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
ac962567-eea7-4648-a688-e9c37966facb	Carlos Silva	742.144.717-25	carlos.silva@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
6eda441b-4889-4fd7-bab7-adfeee072209	Patricia Fernandes	781.483.441-41	\N	(43) 966137210	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
d4ba4524-f84f-428f-877b-5fdb1471fdac	Juliana Fernandes	185.547.554-56	juliana.fernandes@gmail.com	(43) 914786950	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
7584d9da-d6a0-4d9c-a888-8487178ac990	Aline Souza	846.097.997-15	aline.souza@gmail.com	(43) 997111974	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
4a2c9857-5d2f-43eb-8dd3-6bfc0a7036a8	Fernanda Costa	498.163.560-58	fernanda.costa@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
c482c324-072b-4f95-b954-a98ad7c2d4f4	Rodrigo Silva	266.734.876-15	rodrigo.silva@gmail.com	(43) 931671946	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
41284f45-1110-4542-a43f-97d4b53044a6	Patricia Araujo	401.795.460-07	\N	(43) 941054647	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
529c7997-202f-4eb5-bab2-4239811a8e9d	Aline Fernandes	681.912.717-11	aline.fernandes@gmail.com	(43) 930792525	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
8cb71f25-22ce-473a-b00a-b88da717cd6e	Patricia Santos	261.253.636-19	patricia.santos@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
3c3676ab-c60c-4ac6-bc9b-c1949a9f24ca	Juliana Almeida	378.103.357-15	juliana.almeida@gmail.com	(43) 926593280	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
322468e0-87fe-4a11-b4bf-5cb2aa4b35fc	Mariana Oliveira	859.757.935-81	mariana.oliveira@gmail.com	(43) 998741667	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
85aeb48a-b94a-4057-94d7-b0ac6cc8870b	Fernanda Araujo	216.409.219-82	\N	(43) 988517424	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
58de9457-9c6f-4bf6-b821-fa61c248512f	Maria Nascimento	460.109.065-21	maria.nascimento@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
23786a2a-4028-4590-aacb-c52d1d8d007a	Rodrigo Nascimento	898.015.394-53	rodrigo.nascimento@gmail.com	(43) 935349710	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
9d4c2589-1338-4666-ac14-85f066d93423	João Rodrigues	991.807.979-71	joão.rodrigues@gmail.com	(43) 999630779	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
f7f4b84c-e11d-44af-a192-fe604c1f90b9	Patricia Silva	350.696.683-91	patricia.silva@gmail.com	(43) 998210268	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
8476b645-365d-48bf-87c4-867cd8e5bc1b	Patricia Silva	521.731.902-02	\N	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
3aa98c03-cc7f-4860-b0fa-9cc4926d2874	Ana Lima	346.051.572-47	ana.lima@gmail.com	(43) 964640751	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
c30f35fa-e12a-4e58-acc5-17ed9cd11e2f	Carlos Oliveira	334.347.625-15	carlos.oliveira@gmail.com	(43) 936198690	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
ace44d47-4b94-4c7c-9b81-31491eb00a51	Bruno Fernandes	041.572.202-18	bruno.fernandes@gmail.com	(43) 905438294	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
c702ed08-f429-4868-93a7-f2b109a86c99	Maria Almeida	442.560.871-25	maria.almeida@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
79543a2b-8746-4e25-8119-280d7010d940	Gabriel Rodrigues	729.842.569-21	\N	(43) 961444846	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
cd69eecb-c6ed-4868-a309-b459c1c2de8f	Rafael Nascimento	348.128.077-78	rafael.nascimento@gmail.com	(43) 948364690	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
5d4037ef-78b8-4f69-81a2-851232c214bf	Mariana Santos	235.291.241-50	mariana.santos@gmail.com	(43) 940117359	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
8993b565-eab1-4dd6-b354-7587c23c86e7	Aline Fernandes	368.273.434-13	aline.fernandes@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
88b788a4-9b11-4461-a100-f6f49f5db336	Patricia Araujo	828.558.867-08	patricia.araujo@gmail.com	(43) 929199616	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
ccfac929-2dc4-40dd-b636-ef0ef2438c85	Ana Rodrigues	391.334.685-67	\N	(43) 967460873	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
29541923-12b5-4c05-b7ad-2d81f6c98014	Aline Costa	284.260.352-48	aline.costa@gmail.com	(43) 937180957	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
56f24440-057e-4656-bf26-e3618acfdca3	Rafael Silva	152.830.021-10	rafael.silva@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
4f877a61-7670-4fd5-9ca4-ee875dd2e7be	Pedro Souza	890.849.885-15	pedro.souza@gmail.com	(43) 923136778	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
9ea6448b-05d0-4a32-b501-9b3394af31f9	João Nascimento	029.593.211-20	joão.nascimento@gmail.com	(43) 976031031	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
3447f5e2-9650-40bd-b789-d640cd620491	Rodrigo Nascimento	748.352.363-86	\N	(43) 918732205	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
ee3a5991-05a4-4b31-bf17-5f6ccb647d3d	Carlos Lima	700.848.954-46	carlos.lima@gmail.com	\N	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
d8f79ac1-b915-479a-87e7-f9f601ae3134	Mariana Rodrigues	699.415.324-01	mariana.rodrigues@gmail.com	(43) 909217186	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
adc09b2a-ca0b-44f4-bd01-b500dc12a5c0	Aline Oliveira	997.262.300-98	aline.oliveira@gmail.com	(43) 971228699	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
bd886140-8177-446b-bedd-1e490667b9a3	Patricia Fernandes	050.705.961-10	patricia.fernandes@gmail.com	(43) 999325463	2026-04-28 13:04:45.957142	\N	\N	\N	\N	\N	\N	\N	\N
\.


--
-- TOC entry 5190 (class 0 OID 149489)
-- Dependencies: 235
-- Data for Name: serie; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.serie (id_serie, nome, ordem, nivel_ensino, created_at) FROM stdin;
00000000-0000-0000-0000-000000000100	Série não informada	0	Não informado	2026-05-23 20:35:54.126738
\.


--
-- TOC entry 5189 (class 0 OID 34239)
-- Dependencies: 234
-- Data for Name: sessao_autenticacao; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sessao_autenticacao (id_sessao_autenticacao, id_usuario, refresh_token_hash, expira_em, revogado, created_at, access_token_hash, access_expira_em) FROM stdin;
61b864b2-75d3-4c81-a7a8-f5a9099fdde1	44444444-4444-4444-4444-444444444444	gmSw825S7k+Q3nIAJLpmbNU99WAOUi3vDdUnZW9ADlg=	2026-05-28 10:09:30.243554	t	2026-05-21 10:09:30.245551	keeTdVUimlIn8XZQ+hiPlsi0fHtxTdfPyQOEao+17LU=	2026-05-21 10:39:30.243554
fb1499bf-aadf-46a4-8402-b6c9528be381	44444444-4444-4444-4444-444444444444	WJeCeQIxhC71wDrARmVBaxUCdtWOTv7PoHrDfqavK+M=	2026-05-28 12:50:46.46685	f	2026-05-21 12:50:46.467852	6+L5OkWWe6kPEWekyTq12I3TNjXOhY1duIid1WV8wLY=	2026-05-21 13:20:46.46685
8f827734-e834-4768-921f-6d5c1756a810	44444444-4444-4444-4444-444444444444	0kdkuFWlYyFhroNu72Ru6BBLI6sWm3FEhHkddwwt4wQ=	2026-05-28 13:27:40.931217	f	2026-05-21 13:27:40.937202	4KMoMDuaDa6BpnWbHKIIO6EEuqst3Vy3+b3+XCYIUuo=	2026-05-21 13:57:40.931217
30749a57-ad11-4b83-a48f-b2876b61e48b	44444444-4444-4444-4444-444444444444	oKKXOGp2g5lmBCLwr5vuS7TVg+fXOpuKTnrRJpz1W2E=	2026-05-28 14:00:25.489949	f	2026-05-21 12:50:39.541566	aXiFGqSSu4bAgAPZ12AIZ/uZbxw+RRZiOLMzWSY/xsk=	2026-05-21 14:30:25.489949
ac5359df-8184-47c8-be5b-90f5b228923e	44444444-4444-4444-4444-444444444444	iW+IuFWqI9sT0gh+9sjJKrsDq/Q7V7uWAyWsoN3b2uM=	2026-05-28 14:00:42.681862	t	2026-05-21 14:00:42.682863	49yKNlriRNKUIxV5h+6atitxDusqIHqvZL5KoAV/br4=	2026-05-21 14:30:42.681862
c3f7349c-9f63-4e1d-85e3-e7d75d5cb9bd	44444444-4444-4444-4444-444444444444	QL9SQE0qD4n01BCC/SCHkwndbk68ghrx1/RzOUH3TwM=	2026-05-28 14:33:11.359623	t	2026-05-21 14:33:11.360608	QAH3pRr82VZtkDETKAEEYhJTb8anEknEYv6XQgTYGPo=	2026-05-21 15:03:11.359623
6e99f81c-fa0c-466d-911d-77d0211e2b4e	44444444-4444-4444-4444-444444444444	yXJGb9D/ja9+T4XM+6ULQpCoiguJ/7eL84EG242EcZs=	2026-05-28 14:53:21.818454	t	2026-05-21 14:53:21.818454	+ALrHQ1KO1NRijbq6u4t+mCfhYhHOKrhN3P+SaDjOHw=	2026-05-21 15:23:21.818454
fa237302-1fd4-449c-8f37-9d118b545b93	44444444-4444-4444-4444-444444444444	wiUFi1sRi7d9bckfjxQNZC0jqlR+S8e/ertWeKV0mrU=	2026-05-28 14:54:35.47641	f	2026-05-21 14:54:35.47641	q56E55wAldCV6bN9oJmFuCmoBVZSM5k7b9/PDIi+EfI=	2026-05-21 15:24:35.47641
d9c69520-e189-4974-b7cc-0f1e2fc35724	44444444-4444-4444-4444-444444444444	ECzHWt4YGtOpXKeV1GvoFcbEVmjR/1e+Y0S09UXcy1U=	2026-05-28 15:26:16.918294	t	2026-05-21 15:26:16.922294	61DMi5ALvxDxob96EdW3LPQ4oXnrULcFd7IF1ChB+RI=	2026-05-21 15:56:16.918294
2e165bea-c8ac-45ff-9d94-ebb55b53531b	44444444-4444-4444-4444-444444444444	fTs+z9Ukr0tsB0E6vtOrgG8BqNwCcioNmc9F93bWlbw=	2026-05-28 15:35:24.557607	t	2026-05-21 15:35:24.557607	nc+WNE/abSzS/vnMdtBFoNfiwJnBFMXrxQZpneByNLk=	2026-05-21 16:05:24.557607
b5e003a6-4591-4425-ab7c-05c6d265a848	44444444-4444-4444-4444-444444444444	FoBilhemwkVgIdXg02mgSdybZKEUjkN80bUG7eZZAaU=	2026-05-28 15:58:23.960497	f	2026-05-21 15:58:23.961501	7ZZsCwKaP2nVzpGVBIbdkuS5YKnsFEAEKa0YKUhbGEc=	2026-05-21 16:28:23.960497
ad3575a3-8bef-4a25-81f7-f4308566ff37	44444444-4444-4444-4444-444444444444	c+yt8WWaYWy0BJHjF62q4AiDKV8w9KDb8tPAVYWJ0y8=	2026-05-28 16:07:08.080233	t	2026-05-21 16:07:08.086241	8uPXhYTFRR4C0UI/5868R5hkukhaWmkxLNvWorfmrKI=	2026-05-21 16:37:08.080233
f81c6191-ee00-4563-87da-17e21eec3e31	44444444-4444-4444-4444-444444444444	u0BtIeKVzdcT4b+Mau0A7rOo9QrMWEPdKaGqDrXUmjo=	2026-05-28 16:13:45.78024	t	2026-05-21 16:13:45.78424	TC+0X1yAEXv95L2jc+O4xVozqG08FMsNNEo2fGcXNUE=	2026-05-21 16:43:45.78024
a9cfa4ce-6d4d-4f7c-8bf9-b46780ed7ad1	44444444-4444-4444-4444-444444444444	zttre57Z3o50gUTEc4oZqM+S/LyWqw03bYuB6gjL0V0=	2026-05-28 16:18:26.991699	t	2026-05-21 16:18:26.992701	5DCsw21FTY5qp1+a3cYRAz2GQzYeRAlbEAtJSR3LpO4=	2026-05-21 16:48:26.991699
2da9a934-cec6-47b0-8350-6e78bdf5d249	44444444-4444-4444-4444-444444444444	zy871Gi1MrQGuAOMTWRqRMwuxnZsrnI0RGw44TdB7k8=	2026-05-28 16:18:53.682166	t	2026-05-21 16:18:53.682166	iktAVAO7B+lmaAvWBL5kNGwwHjZcX9eIXjb94zMbeac=	2026-05-21 16:48:53.682166
6ef0687f-6c23-4bb0-bc1e-0a1088d8fdad	44444444-4444-4444-4444-444444444444	/QGu6n5IJeiYDk8EeXktyPM5OcRDo7XmzvwD1EZ4T/I=	2026-05-28 16:22:31.209496	t	2026-05-21 16:22:31.209496	xC1BJjwOpvCiTercmcW5Z6PsRCrhslUjXI63hbUGAGk=	2026-05-21 16:52:31.209496
2943c2dd-1926-44b3-aa78-c6f3f761cee1	44444444-4444-4444-4444-444444444444	iDC9ihzFQ7yQtAdxfAbgq5b3wReoP/rHz8lWT3Ruqqs=	2026-05-28 16:26:24.731811	t	2026-05-21 16:26:24.732812	VbzOFNNprucPZoaCEeIYrABrT6eJwQIIsA//QCK3a8M=	2026-05-21 16:56:24.731811
df16996c-cbc0-46dc-96f1-b70da41332d6	44444444-4444-4444-4444-444444444444	fg6mi3pwRkMKHd2Xl3Wc/P2KXlAxYfLukwccvFKnetg=	2026-05-28 16:30:47.211879	t	2026-05-21 16:30:47.21288	QFxfY6e6/fGURulH/blOQKGhqYVVJJmK/0n8tmrnAxI=	2026-05-21 17:00:47.211879
6ad15f46-0569-4299-8fbe-2911e0475e91	44444444-4444-4444-4444-444444444444	jTr/0xKHAFSiRO0eeWdnVadLFNdr+EKRwWkl4QP0/d4=	2026-05-28 16:39:31.532285	t	2026-05-21 16:39:31.532285	pCtzKQWME6MyRWuyhz5DnDBHb49ez4Us2OuSjbeWWyM=	2026-05-21 17:09:31.532285
9c0a8953-93a1-49ba-bb54-4bc11c0f87a1	f3dbb3d6-1230-44fc-9133-cd369355e95a	cZnFNL2I7jjO+Bmim8KghnhJQmadlD9W4vMW7t72VDw=	2026-05-28 16:44:23.899771	f	2026-05-21 16:44:23.900773	PZc0RoQJbvYIxRpTDWOhwPV7AwFz8k8TWsuL/r0YHf0=	2026-05-21 17:14:23.899771
a9485502-ef0a-4028-ba50-9b1d53a4e623	f3dbb3d6-1230-44fc-9133-cd369355e95a	1U5FlBkwH2yubUtC6W6S3F/egETEnM22mQzo/sUULhk=	2026-05-28 16:40:19.912617	t	2026-05-21 16:40:19.912617	QOPKkU3P2bgiQrCeYAjyORNlipGxmXNhHdIK70PcV5A=	2026-05-21 17:10:19.912617
70272025-6743-4464-bfae-bcc011fedcc7	44444444-4444-4444-4444-444444444444	hurA5BzntvOOM78adJymSuRXEbBLzr68ZmYJKG6JGKY=	2026-05-28 16:45:44.086755	t	2026-05-21 16:45:44.086755	gSmAjyY9tbWNeDRoA0tvvra2uHXtaJEteQFfV59HT7A=	2026-05-21 17:15:44.086755
39e27621-063a-4f44-b17e-4f8cb63ead57	44444444-4444-4444-4444-444444444444	5YMsZ+Q0DlgltWeJgUpqOseVfToqjmIz0EJ2tAhxRzU=	2026-05-28 17:05:55.424909	t	2026-05-21 17:05:55.426893	v7H2mMhaiiu/p0rNOcgyIwX39Uf0Ek1AKSLV7kzENOg=	2026-05-21 17:35:55.424909
60fa68cc-233f-4ca7-a8de-d181d674e14f	44444444-4444-4444-4444-444444444444	fYCtbr9SHx0lbDdkcEU4KSQpyfxw8p7p8qO3vXGHN6o=	2026-05-28 17:17:00.901153	t	2026-05-21 17:17:00.902153	xq7KGCLQJZNtcxQPFOc3dlxBrT2/j8mR7zS6dK0Og0E=	2026-05-21 17:47:00.901153
dfe1b0c9-1704-4b1d-a3cf-703643e213e4	f3dbb3d6-1230-44fc-9133-cd369355e95a	9UU7cw2ATOOd0p/xyrDHEhHxte29HKStCk3Bivc1K4c=	2026-05-28 17:20:55.906983	t	2026-05-21 17:20:55.907987	iv55D1tIWMTfq2Or+MxgU7K9nAhsRllNaCE4x8PPgEc=	2026-05-21 17:50:55.906983
0bf79902-14fe-4da9-ad52-8d226c29fa5f	44444444-4444-4444-4444-444444444444	6WNBcsndZXnrbmMvMT7fbIZHv86LFiu2VmyCFx32LM0=	2026-05-28 17:21:20.760852	f	2026-05-21 17:21:20.760852	mURKZwAQrNv/1KxoAebWhGjp2lLXvnT5pOT5tcbMmeY=	2026-05-21 17:51:20.760852
1239f3b3-aacc-41ea-8120-0f3e37db2e95	44444444-4444-4444-4444-444444444444	ub7zY6pbwwL4dlWpj98HEvFMmo0QO/kFukDNJGuDW34=	2026-05-28 19:06:01.556841	t	2026-05-21 19:06:01.564829	MIAlkFAXnYYZuy9NxXMsm9jXJfVUhdn/FZxaxMhUJPg=	2026-05-21 19:36:01.556841
5390f5c3-80ba-4c71-9462-6254d5408dac	44444444-4444-4444-4444-444444444444	1rti5kTPEUD2ZzowWoW4inlz/5S2a3lcyGVJWWw6nTM=	2026-05-28 19:31:28.720757	t	2026-05-21 19:31:28.720757	W/JqUVXcS7S9MZYAetNBVC6z3RfjWv26UQQsUowyAW4=	2026-05-21 20:01:28.720757
266b981f-aa7b-4c6c-9f0b-d8f47bc8de11	44444444-4444-4444-4444-444444444444	KxlQzlbNdD4QwFGvnUu9VueQ08GmR5Ch+QWkjGWLfpQ=	2026-05-28 19:32:12.864774	t	2026-05-21 19:32:12.865775	5YTGrwFW9WRW4Sw5wxjhwZN2T+uBqj2HAXykCeFcAO8=	2026-05-21 20:02:12.864774
745df727-b85c-46cd-80e3-b5065099406e	44444444-4444-4444-4444-444444444444	T/H0abl2UIt6yrBX6z9iIQxEJ87A4poT48NvJ7BscHE=	2026-05-28 19:45:27.39154	t	2026-05-21 19:45:27.392539	vqogsGItrXP9TV6t6fWQPkXS5+ZLah8KQuvjZZkkYdg=	2026-05-21 20:15:27.39154
15d3a36d-2a07-4e59-a5c2-fb5b805ae842	44444444-4444-4444-4444-444444444444	eKG30ityJz2ERHTcpTcJWYqldvFd8Tm9jblHwgO2aV0=	2026-05-30 01:12:24.61915	t	2026-05-23 00:27:38.362389	YZKrl/vAAQ5+DwTdsVZctZqO6ckDU6ioN6hSU+PjRgY=	2026-05-23 01:42:24.61915
faef570e-de4c-4aa4-be3c-499d77ea69e6	44444444-4444-4444-4444-444444444444	nKK+W2GmR1ygEu4fHRCRyxknqqrzrZrZZ4AxKwO1dUQ=	2026-05-30 01:28:49.355189	f	2026-05-23 00:42:05.866625	K418kpSpbteqFrWQeDrwxLUXJf1yFg7CJw42X0j/r20=	2026-05-23 01:58:49.355189
9ca5e78d-8fa4-46b3-8320-ede34e15dc3f	44444444-4444-4444-4444-444444444444	nSMXCzVwVoisMA4ad2JmMySHJ9Kms9zHmLr4wGb2gRY=	2026-05-30 01:29:08.791155	t	2026-05-23 01:29:08.791155	rzn0wQzFYgB1v87XrHknGJz9EAW8+ASsYbfOJiMUFJE=	2026-05-23 01:59:08.791155
1a4820a4-182c-45f1-9996-c4a388496fd9	44444444-4444-4444-4444-444444444444	8OmikYaVWx9PpNz7ELEN78hwYrto5GSeqeR8aiHKD84=	2026-05-30 02:36:30.067995	f	2026-05-23 01:35:28.981269	k/NVjgnuCUzDHV+qQYmFLiLDl18Tk7zeRizGUJsfZaI=	2026-05-23 03:06:30.067995
af0f6c67-b1ff-46b0-b893-8e25a3264acb	44444444-4444-4444-4444-444444444444	gKpv//QUrj9UgVXo94Fet7QmgEtBqs0aj41hXnVSzW0=	2026-05-30 02:36:51.54709	f	2026-05-23 02:36:51.54709	vx/h8m49N+0lWIK06GqRpKWOS5jyLAixiYhSglEO3q0=	2026-05-23 03:06:51.54709
79491675-7351-4179-983f-a92fb9583588	44444444-4444-4444-4444-444444444444	XP7oJMuTgz5/Vzpvhx99akacdX0YZKg17Lpfbrrj0ok=	2026-05-30 03:38:27.106206	t	2026-05-23 03:38:27.106206	Q3BZuTMf7Rnn5KsZwD/qtJM3/lxxyRT6pEnmgdztrNQ=	2026-05-23 04:08:27.106206
7816560e-5b5d-4290-ace8-a9fedcac1dd2	44444444-4444-4444-4444-444444444444	/Gv9OJxvUTGyRFEg2B7QROzzqld9m2MSaEwjZwCk+6U=	2026-05-30 04:07:49.147394	f	2026-05-23 04:07:49.147394	Yy8eYP35zdmHDX7sXBZ5yCCjLQWkHPKRqOezo/le6fY=	2026-05-23 04:37:49.147394
9c13c052-d955-4060-ab9d-d7e89a3b1c62	44444444-4444-4444-4444-444444444444	HL1AlHEj9Chs9AJqLjILe/Yn88hzGfKAlutY2zKxBhc=	2026-05-30 04:07:55.531516	f	2026-05-23 04:07:55.531516	V7jUX2exXOEWisEgbE8Xs6EY619NBX3q0MKrQR/iJEg=	2026-05-23 04:37:55.531516
32b3dd66-235c-448e-ba56-e97b985c246b	44444444-4444-4444-4444-444444444444	wy/axC30xeXahuuWYcKsCa4wibfEEwm9tJSagayd4XY=	2026-05-30 04:08:39.960957	f	2026-05-23 04:08:39.960957	VC9MgS/IGYnPXRje4nNU7+zXG0Mu2xOo8VN91kmfAMA=	2026-05-23 04:38:39.960957
04396221-0c39-46a2-83ec-451492c1c295	44444444-4444-4444-4444-444444444444	OFgwDDJm39LIdaHXefn2N7yPY71LWjA1DFJDGdM23U0=	2026-05-30 03:56:19.920489	t	2026-05-23 03:56:19.920489	UoV5vJ/+C0+xL0q1mPJ+1NdwtQQSZ6ZRhc+xRiNCoRc=	2026-05-23 04:26:19.920489
68b9d4a6-e20f-4fa1-a3ac-c1f156ae493c	44444444-4444-4444-4444-444444444444	1EouAah7b/0Vg1BBHeDBMSE0uUEdj4BJjbn7+LwmFZA=	2026-05-30 04:33:55.360415	t	2026-05-23 04:33:55.362404	Asa+3UyPRPFvRF7AN5Iqjc1ZmfZQR78RuuGteQPJzMM=	2026-05-23 05:03:55.360415
b342b7ad-ca47-401f-ab5b-4720433a4c1f	44444444-4444-4444-4444-444444444444	k3EBppZDaXFPknw5KObtCW96INPX9b53rrhtu0DKBgU=	2026-05-30 04:55:31.370532	t	2026-05-23 04:55:31.383028	LBfEDWCswKsLSJR+ynCgSkPjqFJfF//GQNGu9oWC07Q=	2026-05-23 05:25:31.370532
c0be2336-788a-4278-962f-62c8515ba625	44444444-4444-4444-4444-444444444444	brJZfm7M4XG7kDBzoNoeDLD/N4Bxb/VsNB0yYOuHDJ4=	2026-05-30 05:22:37.917144	t	2026-05-23 05:22:37.918143	b/SFKuhPOf/sHXS2rhGonBabe5jiDrQZXNRdZGKTO3o=	2026-05-23 05:52:37.917144
63f3527b-6942-4494-8182-443b1053564f	44444444-4444-4444-4444-444444444444	VnmE9eBLeokRqtPWxID2I+9phn6SU6AWebzoB2siiDU=	2026-05-30 05:28:38.389719	f	2026-05-23 04:54:11.89204	9rRb0cJ/t+PVr45kwxwViaw1SIakHA01ysT2hq7AGco=	2026-05-23 05:58:38.389719
ef011bd3-b1d1-469a-826e-e271a708d77c	44444444-4444-4444-4444-444444444444	s5YBL3FEbou/8Lwy0HpB0Ee7nR9M92ABWPDVQ5agerM=	2026-05-30 05:35:36.150595	f	2026-05-23 05:35:36.154609	Y7I61xQanXPBaG3p+ye3D0wIG7KAvjExOE1zQNdCO8E=	2026-05-23 06:05:36.150595
e3041093-d688-49fa-bd37-b696616b2589	44444444-4444-4444-4444-444444444444	1RrJdM+URDaLk738nuqXRQJJ5Emw9Z3LWKo+Mfwzygc=	2026-05-30 05:35:47.519207	f	2026-05-23 05:35:47.519207	AwvirEbF9WnniBaAtqiebolbreXnIt7MULqmkBo9LGo=	2026-05-23 06:05:47.519207
79c3d7cc-7fc3-4450-a376-c074df158423	44444444-4444-4444-4444-444444444444	WmZ1PYPoVXEbZBFOCZKgqyLB/7CO7FtBOAqPnX5dGC0=	2026-05-30 06:33:54.901309	f	2026-05-23 05:41:48.598049	XpuxW06QlZpOoYWxj82rL5/WLULCCGQ4PcQOxzLTayM=	2026-05-23 07:03:54.901309
27b40f22-6d55-4b32-a14b-6505ad3d6999	44444444-4444-4444-4444-444444444444	rcK2bFFKcu4bFFirUyIRqQzsNLjKQd3ibYq205noyqY=	2026-05-30 20:36:39.978336	t	2026-05-23 20:36:39.983335	20lmVKycgrGix/OXteYb/JJgMmaFQZfm12Z52tB7YEI=	2026-05-23 21:06:39.978336
dd216e97-1ac3-4d30-9029-9fd1c58a11b7	44444444-4444-4444-4444-444444444444	pcFxSCWGz43wufux7sS1zX93SbxlHa6gTQZZfDkjxuw=	2026-05-30 21:04:27.64321	f	2026-05-23 21:04:27.644236	wPPdtnF4RTIlwudWZLAGkn0mjo4hmOPrTCDZJt7ImqA=	2026-05-23 21:34:27.644236
e01d2e21-a2d4-4357-ba0f-bc4c708a0419	44444444-4444-4444-4444-444444444444	8n/ll2jOyUnj9ReaxkCz0qkD3Q9shoS4FvmW1TOs8RA=	2026-05-31 02:03:57.234272	f	2026-05-24 02:03:57.238626	6VlEMy0Q+9uTslPOjgBtSiVfMUC+UREU84JbHj//+LU=	2026-05-24 02:33:57.234272
42544e25-7564-4738-ad08-79388e6f505c	44444444-4444-4444-4444-444444444444	jFKGM5sIBuM1Fi/rMsOmErPb8L3V6FuFADd7AYYNydY=	2026-05-31 02:05:47.738475	f	2026-05-24 02:05:47.739475	CHHnXis69XUrXvdlYIYKfU/llEm2txjnYlfLjIBpu9s=	2026-05-24 02:35:47.738475
3973a494-9330-47d5-bd5b-0da0e75880e5	44444444-4444-4444-4444-444444444444	v6gXg/daCHYI61H021Myt8DN6onvr8fzu4EFk7GhBQk=	2026-05-31 02:32:15.710345	f	2026-05-24 02:32:15.716346	hFm5nfL89inKteIKv0HVJSqL2xhQNTflzpqmdM+DnGE=	2026-05-24 03:02:15.710345
80d3913f-896d-49b9-8092-38bd3920bb5d	44444444-4444-4444-4444-444444444444	06U/qSfYE+X5ckFiWNDdXhQEBMwABy9A897aNWhmu0A=	2026-05-31 02:41:49.895461	f	2026-05-24 02:41:49.896461	I50RmyTS1NSNgJxZAAvKp2iIsHUtP6uszrlHfUudCIY=	2026-05-24 03:11:49.895461
358f5c78-33c3-4037-82a3-3489ee025eb8	44444444-4444-4444-4444-444444444444	0iqXAKx2nH7uJkhL/S6eZI7pR6HUOjKoW+FlVpPlLIQ=	2026-05-31 04:18:40.083585	f	2026-05-24 04:18:40.088567	/CuhUpUhm9cB7UKWWwlONkXv7PSh7pT7C7on+bY5uAU=	2026-05-24 04:48:40.083585
5901d74f-d97a-43a4-97ee-c70efda20444	44444444-4444-4444-4444-444444444444	cKHe2vNrCB7+xv4P02Y/lpzhqQ9gzEN93LyjGBfQtAI=	2026-05-31 04:26:43.347893	t	2026-05-24 04:26:43.348894	RKVZ6mJI/NrP2uN5cDlUbLVyTaVzD/cnUm3zw0ggPnA=	2026-05-24 04:56:43.347893
fce2636d-9181-4dbb-9d51-e8ae1f86331f	44444444-4444-4444-4444-444444444444	D2HpcBCxsQrvd+FbD5kp0JidY8SvY1yIGuKhzDzudnQ=	2026-05-31 13:26:35.170036	t	2026-05-24 13:26:35.175033	pc3t2CxNwNwDbM0B6WOlh9EVUrNBIvOYnm3tdctTrq4=	2026-05-24 13:56:35.170036
9194de19-1b31-466c-962f-15bbf904ddfd	44444444-4444-4444-4444-444444444444	y6FEeRpRtIeQuwVoKE5WScRXUDRPjVfUaRnWaLz0Bug=	2026-05-31 13:36:34.31796	t	2026-05-24 13:36:34.31896	NTzpg4kVe+/UzoDLN2QYqyF3YG82IcKO0cbLKw8vgMk=	2026-05-24 14:06:34.31796
49786265-f498-4819-bb08-d074a42c4e77	44444444-4444-4444-4444-444444444444	6jb8Z5Ftp9/vCvXCWa+UMA2N4Ou6WvK5UJ38VgoEaxw=	2026-05-31 13:41:09.163473	t	2026-05-24 13:41:09.164476	GlKrcI6Uu5lfW3MnRelR86ze5dMiUtJxivUKzf/NCPo=	2026-05-24 14:11:09.163473
979cdc49-81ac-4577-a66f-e8a0516c835d	44444444-4444-4444-4444-444444444444	nZbiQtSbANIzkAqaJI86RUYFzNHfdJSfGpHSCHFD5xY=	2026-05-31 13:55:24.163618	f	2026-05-24 13:55:24.163618	qraYw/NWuQ5iCRulXSU6GyEJnk8OERpCUNrh+/VIwG4=	2026-05-24 14:25:24.163618
e08b3406-8e4d-4ef0-8f78-a7c6b37da92c	44444444-4444-4444-4444-444444444444	J5Thkp0+2sj/o/IklzFmXirIpm4irSXy2EwP4fwrARY=	2026-05-31 14:00:45.416553	t	2026-05-24 14:00:45.421549	XmBdkCeMGkeFBKtGP7WO67h0JJsatHCNMji9scH9Rv4=	2026-05-24 14:30:45.416553
acf5b391-0e91-4a62-a4f4-ed775bb7c18e	44444444-4444-4444-4444-444444444444	h8aqpoPTtUCOCqO6+7wOuVDprlIg+aSTq9NtRbyoUmI=	2026-05-31 15:39:04.195591	t	2026-05-24 15:39:04.201094	ED2VdrbAcPc8m784C2+Fl0MngW61MTFJBmwWZ87WF2k=	2026-05-24 16:09:04.195591
b645519e-083b-4569-9883-de285e228ebd	44444444-4444-4444-4444-444444444444	wY6ntMM6bxCCGVP4wT1yVW307EUG81Nkl6qhpumeIIs=	2026-05-31 15:45:23.390151	f	2026-05-24 15:45:23.391151	UZ4G9ipDCj+OoeOQ/E1KyKl2HeMFGIk9fuK3XWF5PY0=	2026-05-24 16:15:23.390151
c913b1b0-12b6-4c99-b7d9-9abb24c54aa3	44444444-4444-4444-4444-444444444444	V1R1MTrzWSPUQb/S0HvqAj4109efliplt3DohjYpxLI=	2026-05-31 15:46:01.653387	t	2026-05-24 15:46:01.653387	BqXDM5+mxjfC4poheh8FlJLdras0H+NSeklVPYB0Qog=	2026-05-24 16:16:01.653387
204c6be8-de7f-4d34-a216-8831c2113b31	44444444-4444-4444-4444-444444444444	xm0pZXiz9N1mtvq4/lJ8AHPZYPNSZmKueFhj4LJ1T6o=	2026-05-31 16:23:28.509774	f	2026-05-24 16:23:28.509774	PJWGXJa8SkpwqffATm5CJXbDBUq5S7Dnp6yhVKVcZqU=	2026-05-24 16:53:28.509774
4ec6197e-c640-4e31-bb2b-db796800fb79	44444444-4444-4444-4444-444444444444	H0IOcHYFfzi2g1Qu9TD0VRFdiCA0cxMLiSrCl3kD+IY=	2026-05-31 16:24:23.848865	t	2026-05-24 16:24:23.849891	EqHYmTxWcMwll6xCBQKVQ0mED8bcx0Gjow0pOjqUpV4=	2026-05-24 16:54:23.848865
92fa4f93-fb68-45d1-9bae-48554e2be075	44444444-4444-4444-4444-444444444444	BdMONUy4+xkHBF12oWwTPVEndIKOeJccgFhrLW7a8BU=	2026-05-31 16:29:13.836006	f	2026-05-24 16:29:13.836006	xb7EVWb0OGZxuNRr1yGhD0k8CIFjHGoSeZ/5JbO+KOc=	2026-05-24 16:59:13.836006
affc34e0-3d24-4a5e-98db-ff76fa7a4658	44444444-4444-4444-4444-444444444444	s/aHMV44qwWKhztvUL1k9AN0sgk6f1TVzEvynaITyHQ=	2026-05-31 16:41:49.434155	f	2026-05-24 16:41:49.434155	LINeANGOGrb1aV8x5Hv0R+nr4Y4mKCaCG/jd9eek9Lw=	2026-05-24 17:11:49.434155
2b224b8d-5170-4815-be02-a9f65b3b4715	44444444-4444-4444-4444-444444444444	xCswzmUYa9xf2ghObojHZcqwJkeWta6nDN5Owe4y5Bo=	2026-05-31 17:24:37.314679	t	2026-05-24 17:24:37.314679	rkSVxZ74YxRrmPVlr0xCRPRxC58jWBR113/JTOvLJ6s=	2026-05-24 17:54:37.314679
d959e1de-6bcb-4e49-9e5c-3e7cede21283	44444444-4444-4444-4444-444444444444	cr1KWIkCNBh1h5NNN4nQbttYyvUWQ2xMT9xffvxdKw0=	2026-05-31 17:39:54.182318	t	2026-05-24 17:39:54.182318	UAuFMQvHAYdK0wVm9Ldra0ktVcUwuMMIQhM1oUhs7kY=	2026-05-24 18:09:54.182318
8ad7b762-2451-485c-a57d-05ba28d67919	44444444-4444-4444-4444-444444444444	8n/gqjBYG0RPI3i/EhDZmGuTerbVBbsKCTF7dShIGtU=	2026-05-31 18:00:59.144128	f	2026-05-24 18:00:59.144128	sgqFDo9zgVqoIr9XWMks7KnfaJVQCDvHS8c+cQTVFuw=	2026-05-24 18:30:59.144128
d6bb8aee-fec4-4323-8b05-c027266ac990	44444444-4444-4444-4444-444444444444	w8V9QlhRj5X5hEpHN2sJ6PuYk5quuIthZGfjWvxYwdY=	2026-05-31 18:19:43.342409	f	2026-05-24 18:19:43.34841	GKHdP67QguMjYIN/AFMLbvudNyhrb7Ni93+NaJJmj54=	2026-05-24 18:49:43.342409
\.


--
-- TOC entry 5193 (class 0 OID 149628)
-- Dependencies: 238
-- Data for Name: transferencia_aluno; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.transferencia_aluno (id_transferencia, id_aluno, id_escola_origem, serie_origem, ano_letivo_origem, data_transferencia, motivo_transferencia, situacao_origem, observacao, created_at, documentos_entregues, tipo_transferencia, status_transferencia, usuario_operacao, data_hora_operacao) FROM stdin;
\.


--
-- TOC entry 5180 (class 0 OID 34055)
-- Dependencies: 225
-- Data for Name: turma; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.turma (id_turma, codigo, nome, capacidade, id_periodo_letivo, created_at, id_serie, turno, status) FROM stdin;
20000000-0000-0000-0000-000000000001	2026-MANHA-5A	5 Serie A	30	10000000-0000-0000-0000-000000000013	2026-04-30 04:05:00	00000000-0000-0000-0000-000000000100	\N	ATIVA
20000000-0000-0000-0000-000000000002	2026-MANHA-5B	5 Serie B	30	10000000-0000-0000-0000-000000000013	2026-04-30 04:05:00	00000000-0000-0000-0000-000000000100	\N	ATIVA
20000000-0000-0000-0000-000000000003	2026-TARDE-6A	6 Serie A	30	10000000-0000-0000-0000-000000000013	2026-04-30 04:05:00	00000000-0000-0000-0000-000000000100	\N	ATIVA
20000000-0000-0000-0000-000000000004	2026-TARDE-6B	6 Serie B	30	10000000-0000-0000-0000-000000000013	2026-04-30 04:05:00	00000000-0000-0000-0000-000000000100	\N	ATIVA
20000000-0000-0000-0000-000000000005	2026-NOITE-7A	7 Serie A	30	10000000-0000-0000-0000-000000000013	2026-04-30 04:05:00	00000000-0000-0000-0000-000000000100	\N	ATIVA
20000000-0000-0000-0000-000000000006	2026-NOITE-7B	7 Serie B	30	10000000-0000-0000-0000-000000000013	2026-04-30 04:05:00	00000000-0000-0000-0000-000000000100	\N	ATIVA
20000000-0000-0000-0000-000000000007	2026-INTEGRAL-8A	8 Serie A	30	10000000-0000-0000-0000-000000000013	2026-04-30 04:05:00	00000000-0000-0000-0000-000000000100	\N	ATIVA
20000000-0000-0000-0000-000000000008	2026-INTEGRAL-8B	8 Serie B	30	10000000-0000-0000-0000-000000000013	2026-04-30 04:05:00	00000000-0000-0000-0000-000000000100	\N	ATIVA
20000000-0000-0000-0000-000000000009	2026-MANHA-9A	9 Serie A	30	10000000-0000-0000-0000-000000000013	2026-04-30 04:05:00	00000000-0000-0000-0000-000000000100	\N	ATIVA
20000000-0000-0000-0000-000000000010	2026-TARDE-9-SERIE-B	9 Serie B	13	10000000-0000-0000-0000-000000000013	2026-04-30 04:05:00	00000000-0000-0000-0000-000000000100	\N	ATIVA
\.


--
-- TOC entry 5184 (class 0 OID 34152)
-- Dependencies: 229
-- Data for Name: usuario; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuario (id_usuario, username, nome, email, senha_hash, ativo, created_at, updated_at) FROM stdin;
f3dbb3d6-1230-44fc-9133-cd369355e95a	pdantas	Paulo Dantas	pjr_dantas@hotmail.com	$2a$10$QOSyOPxpVJnoeIbP7q3p4.lUx1lblDlqlQ67k.DiZNAWNuomF53a2	t	2026-05-01 12:40:13.068618	\N
44444444-4444-4444-4444-444444444444	admin	Administrador	admin@escola.com	$2a$10$hmwtSpyjO.twozxBICspNe3V/hrKF0JCiBIcMgKryhvYugbafq4iC	t	2026-04-28 21:46:16.985701	\N
\.


--
-- TOC entry 5187 (class 0 OID 34195)
-- Dependencies: 232
-- Data for Name: usuario_perfil; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.usuario_perfil (id_usuario_perfil, id_usuario, id_perfil, created_at) FROM stdin;
1550fc71-7556-401a-b595-75449f8a36ed	44444444-4444-4444-4444-444444444444	22222222-2222-2222-2222-222222222222	2026-05-02 03:24:38.278824
8ffe3342-b94a-40e0-a2d6-bfb0bc2521ab	f3dbb3d6-1230-44fc-9133-cd369355e95a	2d7b407d-c971-41d6-97c1-95b69c4c06c0	2026-05-21 16:08:49.484915
d944153c-a7dd-4a04-b20d-37f192f27786	f3dbb3d6-1230-44fc-9133-cd369355e95a	6c5bb0da-068a-441a-9c59-2f3315bff1dc	2026-05-21 16:47:37.282015
\.


--
-- TOC entry 5204 (class 0 OID 0)
-- Dependencies: 221
-- Name: flyway_audit_marker_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.flyway_audit_marker_id_seq', 1, true);


--
-- TOC entry 4930 (class 2606 OID 34041)
-- Name: aluno aluno_cpf_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.aluno
    ADD CONSTRAINT aluno_cpf_key UNIQUE (cpf);


--
-- TOC entry 4932 (class 2606 OID 34039)
-- Name: aluno aluno_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.aluno
    ADD CONSTRAINT aluno_pkey PRIMARY KEY (id_aluno);


--
-- TOC entry 4952 (class 2606 OID 34137)
-- Name: aluno_responsavel aluno_responsavel_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.aluno_responsavel
    ADD CONSTRAINT aluno_responsavel_pkey PRIMARY KEY (id_aluno_responsavel);


--
-- TOC entry 4992 (class 2606 OID 149523)
-- Name: disciplina disciplina_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.disciplina
    ADD CONSTRAINT disciplina_pkey PRIMARY KEY (id_disciplina);


--
-- TOC entry 4994 (class 2606 OID 149627)
-- Name: escola_origem escola_origem_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.escola_origem
    ADD CONSTRAINT escola_origem_pkey PRIMARY KEY (id_escola_origem);


--
-- TOC entry 4928 (class 2606 OID 33838)
-- Name: flyway_audit_marker flyway_audit_marker_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flyway_audit_marker
    ADD CONSTRAINT flyway_audit_marker_pkey PRIMARY KEY (id);


--
-- TOC entry 4925 (class 2606 OID 33826)
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- TOC entry 5009 (class 2606 OID 149699)
-- Name: historico_escolar_item historico_escolar_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.historico_escolar_item
    ADD CONSTRAINT historico_escolar_item_pkey PRIMARY KEY (id);


--
-- TOC entry 5005 (class 2606 OID 149690)
-- Name: historico_escolar historico_escolar_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.historico_escolar
    ADD CONSTRAINT historico_escolar_pkey PRIMARY KEY (id);


--
-- TOC entry 4945 (class 2606 OID 34089)
-- Name: matricula matricula_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.matricula
    ADD CONSTRAINT matricula_pkey PRIMARY KEY (id_matricula);


--
-- TOC entry 4966 (class 2606 OID 34183)
-- Name: perfil perfil_codigo_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.perfil
    ADD CONSTRAINT perfil_codigo_key UNIQUE (codigo);


--
-- TOC entry 4982 (class 2606 OID 34226)
-- Name: perfil_permissao perfil_permissao_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.perfil_permissao
    ADD CONSTRAINT perfil_permissao_pkey PRIMARY KEY (id_perfil_permissao);


--
-- TOC entry 4968 (class 2606 OID 34181)
-- Name: perfil perfil_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.perfil
    ADD CONSTRAINT perfil_pkey PRIMARY KEY (id_perfil);


--
-- TOC entry 4934 (class 2606 OID 34054)
-- Name: periodo_letivo periodo_letivo_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.periodo_letivo
    ADD CONSTRAINT periodo_letivo_pkey PRIMARY KEY (id_periodo_letivo);


--
-- TOC entry 4970 (class 2606 OID 34194)
-- Name: permissao permissao_codigo_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permissao
    ADD CONSTRAINT permissao_codigo_key UNIQUE (codigo);


--
-- TOC entry 4972 (class 2606 OID 34192)
-- Name: permissao permissao_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permissao
    ADD CONSTRAINT permissao_pkey PRIMARY KEY (id_permissao);


--
-- TOC entry 4948 (class 2606 OID 34126)
-- Name: responsavel responsavel_cpf_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.responsavel
    ADD CONSTRAINT responsavel_cpf_key UNIQUE (cpf);


--
-- TOC entry 4950 (class 2606 OID 34124)
-- Name: responsavel responsavel_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.responsavel
    ADD CONSTRAINT responsavel_pkey PRIMARY KEY (id_responsavel);


--
-- TOC entry 4990 (class 2606 OID 149499)
-- Name: serie serie_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.serie
    ADD CONSTRAINT serie_pkey PRIMARY KEY (id_serie);


--
-- TOC entry 4988 (class 2606 OID 34251)
-- Name: sessao_autenticacao sessao_autenticacao_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sessao_autenticacao
    ADD CONSTRAINT sessao_autenticacao_pkey PRIMARY KEY (id_sessao_autenticacao);


--
-- TOC entry 4999 (class 2606 OID 149642)
-- Name: transferencia_aluno transferencia_aluno_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transferencia_aluno
    ADD CONSTRAINT transferencia_aluno_pkey PRIMARY KEY (id_transferencia);


--
-- TOC entry 4938 (class 2606 OID 34068)
-- Name: turma turma_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.turma
    ADD CONSTRAINT turma_pkey PRIMARY KEY (id_turma);


--
-- TOC entry 4956 (class 2606 OID 34139)
-- Name: aluno_responsavel uk_aluno_responsavel; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.aluno_responsavel
    ADD CONSTRAINT uk_aluno_responsavel UNIQUE (id_aluno, id_responsavel);


--
-- TOC entry 4984 (class 2606 OID 34228)
-- Name: perfil_permissao uk_perfil_permissao; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.perfil_permissao
    ADD CONSTRAINT uk_perfil_permissao UNIQUE (id_perfil, id_permissao);


--
-- TOC entry 4940 (class 2606 OID 34070)
-- Name: turma uk_turma_codigo_periodo; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.turma
    ADD CONSTRAINT uk_turma_codigo_periodo UNIQUE (codigo, id_periodo_letivo);


--
-- TOC entry 4976 (class 2606 OID 34206)
-- Name: usuario_perfil uk_usuario_perfil; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_perfil
    ADD CONSTRAINT uk_usuario_perfil UNIQUE (id_usuario, id_perfil);


--
-- TOC entry 4960 (class 2606 OID 34171)
-- Name: usuario usuario_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_email_key UNIQUE (email);


--
-- TOC entry 4978 (class 2606 OID 34204)
-- Name: usuario_perfil usuario_perfil_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_perfil
    ADD CONSTRAINT usuario_perfil_pkey PRIMARY KEY (id_usuario_perfil);


--
-- TOC entry 4962 (class 2606 OID 34167)
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id_usuario);


--
-- TOC entry 4964 (class 2606 OID 34169)
-- Name: usuario usuario_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_username_key UNIQUE (username);


--
-- TOC entry 4926 (class 1259 OID 33827)
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- TOC entry 4953 (class 1259 OID 34150)
-- Name: idx_aluno_responsavel_aluno; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_aluno_responsavel_aluno ON public.aluno_responsavel USING btree (id_aluno);


--
-- TOC entry 4954 (class 1259 OID 34151)
-- Name: idx_aluno_responsavel_responsavel; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_aluno_responsavel_responsavel ON public.aluno_responsavel USING btree (id_responsavel);


--
-- TOC entry 5010 (class 1259 OID 149708)
-- Name: idx_historico_escolar_item_componente; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_historico_escolar_item_componente ON public.historico_escolar_item USING btree (componente_curricular);


--
-- TOC entry 5011 (class 1259 OID 149707)
-- Name: idx_historico_escolar_item_historico; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_historico_escolar_item_historico ON public.historico_escolar_item USING btree (historico_escolar_id);


--
-- TOC entry 5006 (class 1259 OID 149705)
-- Name: idx_historico_escolar_nome_aluno; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_historico_escolar_nome_aluno ON public.historico_escolar USING btree (nome_aluno);


--
-- TOC entry 5007 (class 1259 OID 149706)
-- Name: idx_historico_escolar_ra; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_historico_escolar_ra ON public.historico_escolar USING btree (ra);


--
-- TOC entry 4941 (class 1259 OID 34105)
-- Name: idx_matricula_aluno; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_matricula_aluno ON public.matricula USING btree (id_aluno);


--
-- TOC entry 4942 (class 1259 OID 149512)
-- Name: idx_matricula_aluno_periodo_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_matricula_aluno_periodo_status ON public.matricula USING btree (id_aluno, id_periodo_letivo, status);


--
-- TOC entry 4943 (class 1259 OID 34106)
-- Name: idx_matricula_turma; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_matricula_turma ON public.matricula USING btree (id_turma);


--
-- TOC entry 4979 (class 1259 OID 34261)
-- Name: idx_perfil_permissao_perfil; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_perfil_permissao_perfil ON public.perfil_permissao USING btree (id_perfil);


--
-- TOC entry 4980 (class 1259 OID 34262)
-- Name: idx_perfil_permissao_permissao; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_perfil_permissao_permissao ON public.perfil_permissao USING btree (id_permissao);


--
-- TOC entry 4946 (class 1259 OID 34127)
-- Name: idx_responsavel_nome; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_responsavel_nome ON public.responsavel USING btree (nome_completo);


--
-- TOC entry 4985 (class 1259 OID 34266)
-- Name: idx_sessao_autenticacao_access_token_hash; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sessao_autenticacao_access_token_hash ON public.sessao_autenticacao USING btree (access_token_hash);


--
-- TOC entry 4986 (class 1259 OID 34263)
-- Name: idx_sessao_autenticacao_usuario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sessao_autenticacao_usuario ON public.sessao_autenticacao USING btree (id_usuario);


--
-- TOC entry 4995 (class 1259 OID 149653)
-- Name: idx_transferencia_aluno_aluno; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_transferencia_aluno_aluno ON public.transferencia_aluno USING btree (id_aluno);


--
-- TOC entry 4996 (class 1259 OID 149654)
-- Name: idx_transferencia_aluno_escola_origem; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_transferencia_aluno_escola_origem ON public.transferencia_aluno USING btree (id_escola_origem);


--
-- TOC entry 4997 (class 1259 OID 149726)
-- Name: idx_transferencia_aluno_tipo_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_transferencia_aluno_tipo_status ON public.transferencia_aluno USING btree (tipo_transferencia, status_transferencia);


--
-- TOC entry 4935 (class 1259 OID 34107)
-- Name: idx_turma_periodo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_turma_periodo ON public.turma USING btree (id_periodo_letivo);


--
-- TOC entry 4936 (class 1259 OID 149507)
-- Name: idx_turma_serie; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_turma_serie ON public.turma USING btree (id_serie);


--
-- TOC entry 4957 (class 1259 OID 34258)
-- Name: idx_usuario_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuario_email ON public.usuario USING btree (email);


--
-- TOC entry 4973 (class 1259 OID 34260)
-- Name: idx_usuario_perfil_perfil; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuario_perfil_perfil ON public.usuario_perfil USING btree (id_perfil);


--
-- TOC entry 4974 (class 1259 OID 34259)
-- Name: idx_usuario_perfil_usuario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuario_perfil_usuario ON public.usuario_perfil USING btree (id_usuario);


--
-- TOC entry 4958 (class 1259 OID 34257)
-- Name: idx_usuario_username; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuario_username ON public.usuario USING btree (username);


--
-- TOC entry 5017 (class 2606 OID 34140)
-- Name: aluno_responsavel fk_aluno_responsavel_aluno; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.aluno_responsavel
    ADD CONSTRAINT fk_aluno_responsavel_aluno FOREIGN KEY (id_aluno) REFERENCES public.aluno(id_aluno);


--
-- TOC entry 5018 (class 2606 OID 34145)
-- Name: aluno_responsavel fk_aluno_responsavel_responsavel; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.aluno_responsavel
    ADD CONSTRAINT fk_aluno_responsavel_responsavel FOREIGN KEY (id_responsavel) REFERENCES public.responsavel(id_responsavel);


--
-- TOC entry 5027 (class 2606 OID 149700)
-- Name: historico_escolar_item fk_historico_escolar_item_historico; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.historico_escolar_item
    ADD CONSTRAINT fk_historico_escolar_item_historico FOREIGN KEY (historico_escolar_id) REFERENCES public.historico_escolar(id) ON DELETE CASCADE;


--
-- TOC entry 5014 (class 2606 OID 34090)
-- Name: matricula fk_matricula_aluno; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.matricula
    ADD CONSTRAINT fk_matricula_aluno FOREIGN KEY (id_aluno) REFERENCES public.aluno(id_aluno);


--
-- TOC entry 5015 (class 2606 OID 34100)
-- Name: matricula fk_matricula_periodo_letivo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.matricula
    ADD CONSTRAINT fk_matricula_periodo_letivo FOREIGN KEY (id_periodo_letivo) REFERENCES public.periodo_letivo(id_periodo_letivo);


--
-- TOC entry 5016 (class 2606 OID 34095)
-- Name: matricula fk_matricula_turma; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.matricula
    ADD CONSTRAINT fk_matricula_turma FOREIGN KEY (id_turma) REFERENCES public.turma(id_turma);


--
-- TOC entry 5021 (class 2606 OID 34229)
-- Name: perfil_permissao fk_perfil_permissao_perfil; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.perfil_permissao
    ADD CONSTRAINT fk_perfil_permissao_perfil FOREIGN KEY (id_perfil) REFERENCES public.perfil(id_perfil);


--
-- TOC entry 5022 (class 2606 OID 34234)
-- Name: perfil_permissao fk_perfil_permissao_permissao; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.perfil_permissao
    ADD CONSTRAINT fk_perfil_permissao_permissao FOREIGN KEY (id_permissao) REFERENCES public.permissao(id_permissao);


--
-- TOC entry 5023 (class 2606 OID 34252)
-- Name: sessao_autenticacao fk_sessao_autenticacao_usuario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sessao_autenticacao
    ADD CONSTRAINT fk_sessao_autenticacao_usuario FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);


--
-- TOC entry 5012 (class 2606 OID 34071)
-- Name: turma fk_turma_periodo_letivo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.turma
    ADD CONSTRAINT fk_turma_periodo_letivo FOREIGN KEY (id_periodo_letivo) REFERENCES public.periodo_letivo(id_periodo_letivo);


--
-- TOC entry 5013 (class 2606 OID 149502)
-- Name: turma fk_turma_serie; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.turma
    ADD CONSTRAINT fk_turma_serie FOREIGN KEY (id_serie) REFERENCES public.serie(id_serie);


--
-- TOC entry 5019 (class 2606 OID 34212)
-- Name: usuario_perfil fk_usuario_perfil_perfil; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_perfil
    ADD CONSTRAINT fk_usuario_perfil_perfil FOREIGN KEY (id_perfil) REFERENCES public.perfil(id_perfil);


--
-- TOC entry 5020 (class 2606 OID 34207)
-- Name: usuario_perfil fk_usuario_perfil_usuario; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuario_perfil
    ADD CONSTRAINT fk_usuario_perfil_usuario FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);


--
-- TOC entry 5024 (class 2606 OID 149643)
-- Name: transferencia_aluno transferencia_aluno_id_aluno_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transferencia_aluno
    ADD CONSTRAINT transferencia_aluno_id_aluno_fkey FOREIGN KEY (id_aluno) REFERENCES public.aluno(id_aluno);


--
-- TOC entry 5025 (class 2606 OID 149648)
-- Name: transferencia_aluno transferencia_aluno_id_escola_origem_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transferencia_aluno
    ADD CONSTRAINT transferencia_aluno_id_escola_origem_fkey FOREIGN KEY (id_escola_origem) REFERENCES public.escola_origem(id_escola_origem);


--
-- TOC entry 5202 (class 0 OID 0)
-- Dependencies: 6
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2026-05-25 10:31:18

--
-- PostgreSQL database dump complete
--

\unrestrict Ye4Ob233ZTRPzP11YhRCMsncVUkkxhMzBUfhTnopocoXdW2ZYsQXfQKAc3bXwOo
