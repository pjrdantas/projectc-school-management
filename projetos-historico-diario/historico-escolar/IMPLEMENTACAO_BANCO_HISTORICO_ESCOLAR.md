# Implementação de Banco de Dados para Histórico Escolar

Este documento descreve **somente os ajustes necessários para a tela de Histórico Escolar** do projeto `historico-escolar-material`.

A proposta considera a base atual já existente, principalmente as tabelas:

- `aluno`
- `pessoa`
- `escola`
- `endereco`
- `matricula`
- `turma`
- `serie`
- `periodo_letivo`
- `disciplina`
- `transferencia_aluno`
- `historico_escolar`
- `historico_escolar_item`

A ideia é **não refazer o modelo escolar inteiro**, mas acrescentar o que falta para o backend Java conseguir alimentar, salvar, editar e validar o payload da tela de Histórico Escolar.

---

## 1. Regra funcional da tela

A tela de Histórico Escolar trabalha com dois estados principais:

1. **Cadastro inicial**
   - usado quando a escola recebe aluno transferido de outra escola;
   - o formulário pode abrir vazio;
   - o usuário deve preencher pelo menos os blocos iniciais:
     - dados da escola/cabeçalho;
     - dados do aluno/nascimento.

2. **Edição do histórico**
   - usado para completar anos, componentes, estudos realizados, observações e certificado;
   - a tela deve continuar exibindo aviso de pendência enquanto o histórico não estiver completo.

### Regras principais

1. Para salvar o cadastro inicial, os blocos **Cabeçalho da escola** e **Dados do aluno** são obrigatórios.
2. A aba **Anos e componentes** pode ficar pendente no primeiro salvamento.
3. A aba **Estudos realizados** pode ficar pendente no primeiro salvamento.
4. A aba **Observações e certificado** pode ficar pendente no primeiro salvamento.
5. Mesmo salvando com pendência, o backend deve retornar a lista de pendências.
6. Se o aluno está matriculado na 6ª série/6º ano, o histórico precisa estar preenchido pelo menos até a 5ª série/5º ano.
7. O certificado deve representar a última série concluída na escola de origem.
8. O certificado pode ser atualizado futuramente conforme o aluno evolua para outras séries.
9. A assinatura no PDF continua manual; o sistema salva apenas nomes, RGs e data/publicação.
10. O PDF deve ser gerado com o modelo oficial e com os dados salvos/carregados.

---

## 2. Campos mínimos obrigatórios no cadastro

### 2.1 Cabeçalho da escola

Campos obrigatórios para liberar o botão **Salvar** no cadastro inicial:

| Campo da tela | Origem sugerida | Observação |
|---|---|---|
| Governo | padrão do sistema ou tabela `escola` | Ex.: Governo do Estado de São Paulo |
| Secretaria | padrão do sistema ou tabela `escola` | Ex.: Secretaria de Estado da Educação |
| Diretoria de Ensino | `escola.diretoria_ensino` | Campo a adicionar |
| Escola | `escola.nome` ou digitado | Obrigatório |
| Ato legal de criação | `escola.ato_legal_criacao` | Campo a adicionar |
| Endereço | `endereco.logradouro` ou snapshot | Obrigatório |
| Número | `endereco.numero` ou snapshot | Obrigatório |
| Bairro | `endereco.bairro` ou snapshot | Obrigatório |
| Município | `endereco.cidade` ou snapshot | Obrigatório |
| CEP | `endereco.cep` ou snapshot | Obrigatório |
| Telefone | `escola.telefone` ou snapshot | Obrigatório |
| E-mail | `escola.email` ou snapshot | Obrigatório |

### 2.2 Dados do aluno

Campos obrigatórios:

| Campo da tela | Origem sugerida | Observação |
|---|---|---|
| Nome do aluno | `pessoa.nome_completo` | Obrigatório |
| RG/RNM | `pessoa.rg` | Obrigatório |
| RA | `aluno.ra` | Obrigatório |
| Município de nascimento | `pessoa.naturalidade` ou campo próprio | Obrigatório |
| Estado de nascimento | campo próprio | Obrigatório |
| País de nascimento | campo próprio | Obrigatório |
| Data de nascimento | `pessoa.data_nascimento` | Obrigatório |

---

## 3. Ajustes em tabelas existentes

### 3.1 Ajustes em `escola`

A tabela `escola` já possui nome, telefone, e-mail e endereço. Para atender o formulário, adicionar:

```sql
ALTER TABLE public.escola
ADD COLUMN IF NOT EXISTS governo varchar(150),
ADD COLUMN IF NOT EXISTS secretaria varchar(150),
ADD COLUMN IF NOT EXISTS diretoria_ensino varchar(150),
ADD COLUMN IF NOT EXISTS ato_legal_criacao varchar(255);
```

Valores padrão sugeridos para escolas estaduais paulistas:

```sql
UPDATE public.escola
SET governo = COALESCE(governo, 'GOVERNO DO ESTADO DE SÃO PAULO'),
    secretaria = COALESCE(secretaria, 'SECRETARIA DE ESTADO DA EDUCAÇÃO')
WHERE governo IS NULL OR secretaria IS NULL;
```

---

## 4. Estrutura principal do Histórico Escolar

A tabela `historico_escolar` já existe, mas precisa guardar mais contexto da tela e do processo de transferência.

### 4.1 Ajustes sugeridos em `historico_escolar`

```sql
ALTER TABLE public.historico_escolar
ADD COLUMN IF NOT EXISTS id_matricula uuid,
ADD COLUMN IF NOT EXISTS id_transferencia_aluno uuid,
ADD COLUMN IF NOT EXISTS status varchar(30) DEFAULT 'RASCUNHO' NOT NULL,
ADD COLUMN IF NOT EXISTS bloqueado boolean DEFAULT false NOT NULL,
ADD COLUMN IF NOT EXISTS serie_matricula_atual integer,
ADD COLUMN IF NOT EXISTS serie_concluida_origem integer,
ADD COLUMN IF NOT EXISTS escola_origem_nome varchar(150),
ADD COLUMN IF NOT EXISTS data_transferencia date,
ADD COLUMN IF NOT EXISTS atualizado_em timestamp without time zone,
ADD COLUMN IF NOT EXISTS atualizado_por uuid;
```

### 4.2 Constraints sugeridas

```sql
ALTER TABLE public.historico_escolar
ADD CONSTRAINT ck_historico_escolar_status
CHECK (status IN ('RASCUNHO', 'PENDENTE', 'COMPLETO', 'BLOQUEADO'));

ALTER TABLE public.historico_escolar
ADD CONSTRAINT ck_historico_serie_matricula_atual
CHECK (serie_matricula_atual IS NULL OR serie_matricula_atual BETWEEN 1 AND 9);

ALTER TABLE public.historico_escolar
ADD CONSTRAINT ck_historico_serie_concluida_origem
CHECK (serie_concluida_origem IS NULL OR serie_concluida_origem BETWEEN 0 AND 9);
```

### 4.3 FKs sugeridas

```sql
ALTER TABLE public.historico_escolar
ADD CONSTRAINT fk_historico_escolar_matricula
FOREIGN KEY (id_matricula)
REFERENCES public.matricula(id_matricula);

ALTER TABLE public.historico_escolar
ADD CONSTRAINT fk_historico_escolar_transferencia
FOREIGN KEY (id_transferencia_aluno)
REFERENCES public.transferencia_aluno(id_transferencia_aluno);
```

### 4.4 Índices sugeridos

```sql
CREATE INDEX IF NOT EXISTS idx_historico_escolar_aluno
ON public.historico_escolar (id_aluno);

CREATE INDEX IF NOT EXISTS idx_historico_escolar_matricula
ON public.historico_escolar (id_matricula);

CREATE INDEX IF NOT EXISTS idx_historico_escolar_status
ON public.historico_escolar (status);
```

---

## 5. Snapshot do cabeçalho e do aluno

Mesmo que os dados venham de `escola`, `endereco`, `aluno` e `pessoa`, é recomendável salvar um **snapshot** no histórico.

Motivo: o Histórico Escolar é documento oficial. Se a escola alterar endereço, telefone ou dados cadastrais no futuro, o histórico antigo não deve mudar automaticamente.

### 5.1 Criar tabela `historico_escolar_cabecalho`

```sql
CREATE TABLE IF NOT EXISTS public.historico_escolar_cabecalho (
    id_historico_escolar_cabecalho uuid DEFAULT gen_random_uuid() NOT NULL,
    id_historico_escolar uuid NOT NULL,
    governo varchar(150) NOT NULL,
    secretaria varchar(150) NOT NULL,
    diretoria_ensino varchar(150) NOT NULL,
    escola_nome varchar(150) NOT NULL,
    ato_legal_criacao varchar(255) NOT NULL,
    endereco varchar(255) NOT NULL,
    numero varchar(30) NOT NULL,
    bairro varchar(100) NOT NULL,
    municipio varchar(100) NOT NULL,
    cep varchar(20) NOT NULL,
    telefone varchar(30) NOT NULL,
    email varchar(150) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,

    CONSTRAINT pk_historico_escolar_cabecalho
        PRIMARY KEY (id_historico_escolar_cabecalho),

    CONSTRAINT fk_historico_cabecalho_historico
        FOREIGN KEY (id_historico_escolar)
        REFERENCES public.historico_escolar(id_historico_escolar)
        ON DELETE CASCADE
);
```

### 5.2 Um cabeçalho por histórico

```sql
CREATE UNIQUE INDEX IF NOT EXISTS uk_historico_escolar_cabecalho
ON public.historico_escolar_cabecalho (id_historico_escolar);
```

### 5.3 Criar tabela `historico_escolar_aluno_dado`

```sql
CREATE TABLE IF NOT EXISTS public.historico_escolar_aluno_dado (
    id_historico_escolar_aluno_dado uuid DEFAULT gen_random_uuid() NOT NULL,
    id_historico_escolar uuid NOT NULL,
    nome_aluno varchar(150) NOT NULL,
    rg_rnm varchar(80) NOT NULL,
    ra varchar(80) NOT NULL,
    nascimento_municipio varchar(100) NOT NULL,
    nascimento_estado varchar(100) NOT NULL,
    nascimento_pais varchar(100) NOT NULL,
    data_nascimento date NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,

    CONSTRAINT pk_historico_escolar_aluno_dado
        PRIMARY KEY (id_historico_escolar_aluno_dado),

    CONSTRAINT fk_historico_aluno_dado_historico
        FOREIGN KEY (id_historico_escolar)
        REFERENCES public.historico_escolar(id_historico_escolar)
        ON DELETE CASCADE
);
```

### 5.4 Um bloco de aluno por histórico

```sql
CREATE UNIQUE INDEX IF NOT EXISTS uk_historico_escolar_aluno_dado
ON public.historico_escolar_aluno_dado (id_historico_escolar);
```

---

## 6. Períodos/anos do formulário

A tela trabalha com colunas de anos/séries, por exemplo:

- 1º Ano;
- 2º Ano;
- 3º Ano;
- até 9º Ano.

A tabela `historico_escolar_item` atual guarda `ano_letivo` e `serie_descricao`, mas para o formulário é melhor ter uma tabela controlando as colunas.

### 6.1 Criar tabela `historico_escolar_periodo`

```sql
CREATE TABLE IF NOT EXISTS public.historico_escolar_periodo (
    id_historico_escolar_periodo uuid DEFAULT gen_random_uuid() NOT NULL,
    id_historico_escolar uuid NOT NULL,
    ordem integer NOT NULL,
    serie_numero integer,
    serie_ano varchar(80) NOT NULL,
    equivalencia varchar(80),
    ano_letivo integer,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,

    CONSTRAINT pk_historico_escolar_periodo
        PRIMARY KEY (id_historico_escolar_periodo),

    CONSTRAINT fk_historico_periodo_historico
        FOREIGN KEY (id_historico_escolar)
        REFERENCES public.historico_escolar(id_historico_escolar)
        ON DELETE CASCADE,

    CONSTRAINT ck_historico_periodo_ordem
        CHECK (ordem BETWEEN 1 AND 9),

    CONSTRAINT ck_historico_periodo_serie_numero
        CHECK (serie_numero IS NULL OR serie_numero BETWEEN 1 AND 9)
);
```

### 6.2 Índices

```sql
CREATE UNIQUE INDEX IF NOT EXISTS uk_historico_periodo_ordem
ON public.historico_escolar_periodo (id_historico_escolar, ordem);
```

---

## 7. Componentes curriculares e notas

O formulário separa:

- Base Nacional Comum;
- Parte Diversificada;
- Eletivas;
- totais.

A tabela `historico_escolar_item` já existe e pode ser aproveitada. O ajuste recomendado é adicionar campos de classificação e ordem.

### 7.1 Ajustes em `historico_escolar_item`

```sql
ALTER TABLE public.historico_escolar_item
ADD COLUMN IF NOT EXISTS id_historico_escolar_periodo uuid,
ADD COLUMN IF NOT EXISTS grupo_curricular varchar(40),
ADD COLUMN IF NOT EXISTS tipo_registro varchar(40) DEFAULT 'COMPONENTE' NOT NULL,
ADD COLUMN IF NOT EXISTS ordem_exibicao integer,
ADD COLUMN IF NOT EXISTS updated_at timestamp without time zone;
```

### 7.2 Constraints

```sql
ALTER TABLE public.historico_escolar_item
ADD CONSTRAINT ck_historico_item_grupo_curricular
CHECK (
    grupo_curricular IS NULL OR
    grupo_curricular IN ('BASE_COMUM', 'PARTE_DIVERSIFICADA', 'ELETIVA')
);

ALTER TABLE public.historico_escolar_item
ADD CONSTRAINT ck_historico_item_tipo_registro
CHECK (
    tipo_registro IN (
        'COMPONENTE',
        'TOTAL_BASE_COMUM',
        'TOTAL_PARTE_DIVERSIFICADA',
        'TOTAL_AULAS_ANUAIS',
        'TOTAL_CARGA_HORARIA'
    )
);
```

### 7.3 FK para período

```sql
ALTER TABLE public.historico_escolar_item
ADD CONSTRAINT fk_historico_item_periodo
FOREIGN KEY (id_historico_escolar_periodo)
REFERENCES public.historico_escolar_periodo(id_historico_escolar_periodo)
ON DELETE CASCADE;
```

### 7.4 Índices

```sql
CREATE INDEX IF NOT EXISTS idx_historico_item_historico_grupo
ON public.historico_escolar_item (id_historico_escolar, grupo_curricular, ordem_exibicao);

CREATE INDEX IF NOT EXISTS idx_historico_item_periodo
ON public.historico_escolar_item (id_historico_escolar_periodo);
```

### 7.5 Regra de preenchimento

Para cada componente preenchido na tela, salvar um registro em `historico_escolar_item`.

Exemplo:

| Campo | Valor |
|---|---|
| `grupo_curricular` | `BASE_COMUM` |
| `tipo_registro` | `COMPONENTE` |
| `componente_curricular` | `Língua Portuguesa` |
| `id_historico_escolar_periodo` | período do 5º ano |
| `nota_conceito` | `8,0` ou conceito |
| `total_aulas` | total do componente |
| `carga_horaria` | carga horária |
| `resultado` | aprovado/reprovado/etc. |

---

## 8. Estudos realizados

A imagem oficial possui a seção:

```text
Série/Ano | Ano | Estabelecimento de Ensino | Município | UF
```

Para evitar gambiarra com `historico_escolar_item`, criar tabela própria.

```sql
CREATE TABLE IF NOT EXISTS public.historico_escolar_estudo_realizado (
    id_historico_escolar_estudo_realizado uuid DEFAULT gen_random_uuid() NOT NULL,
    id_historico_escolar uuid NOT NULL,
    ordem integer NOT NULL,
    serie_ano varchar(80) NOT NULL,
    ano integer,
    estabelecimento_ensino varchar(150),
    municipio varchar(100),
    uf char(2),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,

    CONSTRAINT pk_historico_escolar_estudo_realizado
        PRIMARY KEY (id_historico_escolar_estudo_realizado),

    CONSTRAINT fk_hist_estudo_historico
        FOREIGN KEY (id_historico_escolar)
        REFERENCES public.historico_escolar(id_historico_escolar)
        ON DELETE CASCADE,

    CONSTRAINT ck_hist_estudo_ordem
        CHECK (ordem BETWEEN 1 AND 9)
);
```

### Índice

```sql
CREATE UNIQUE INDEX IF NOT EXISTS uk_hist_estudo_ordem
ON public.historico_escolar_estudo_realizado (id_historico_escolar, ordem);
```

---

## 9. Pendências do histórico

A tela precisa salvar o histórico mesmo incompleto, mas sempre mostrar avisos.

As pendências podem ser calculadas em tempo real pelo backend. Porém, para auditoria e rastreabilidade, é útil armazenar as pendências no momento do salvamento.

```sql
CREATE TABLE IF NOT EXISTS public.historico_escolar_pendencia (
    id_historico_escolar_pendencia uuid DEFAULT gen_random_uuid() NOT NULL,
    id_historico_escolar uuid NOT NULL,
    codigo varchar(80) NOT NULL,
    descricao text NOT NULL,
    aba varchar(80),
    obrigatoria boolean DEFAULT false NOT NULL,
    resolvida boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    resolved_at timestamp without time zone,

    CONSTRAINT pk_historico_escolar_pendencia
        PRIMARY KEY (id_historico_escolar_pendencia),

    CONSTRAINT fk_hist_pendencia_historico
        FOREIGN KEY (id_historico_escolar)
        REFERENCES public.historico_escolar(id_historico_escolar)
        ON DELETE CASCADE
);
```

### Índices

```sql
CREATE INDEX IF NOT EXISTS idx_hist_pendencia_aberta
ON public.historico_escolar_pendencia (id_historico_escolar, resolvida);

CREATE UNIQUE INDEX IF NOT EXISTS uk_hist_pendencia_codigo_aberta
ON public.historico_escolar_pendencia (id_historico_escolar, codigo)
WHERE resolvida = false;
```

### Exemplos de códigos de pendência

```text
CABECALHO_INCOMPLETO
ALUNO_INCOMPLETO
ANOS_COMPONENTES_PENDENTE
ESTUDOS_REALIZADOS_PENDENTE
CERTIFICADO_PENDENTE
HISTORICO_SERIE_ANTERIOR_INCOMPLETO
```

---

## 10. Certificado

A tabela `historico_escolar` já tem campos de certificado:

- `ano_conclusao`
- `ensino_concluido`
- `diretor_nome`
- `diretor_rg`
- `gerente_organizacao_nome`
- `gerente_organizacao_rg`
- `doe_numero`
- `doe_data`
- `doe_volume`
- `doe_pagina`

Para fechar com a tela, adicionar:

```sql
ALTER TABLE public.historico_escolar
ADD COLUMN IF NOT EXISTS certificado_serie_concluida integer,
ADD COLUMN IF NOT EXISTS certificado_texto_ensino varchar(150),
ADD COLUMN IF NOT EXISTS certificado_data date;
```

### Constraints

```sql
ALTER TABLE public.historico_escolar
ADD CONSTRAINT ck_historico_certificado_serie
CHECK (certificado_serie_concluida IS NULL OR certificado_serie_concluida BETWEEN 1 AND 9);
```

### Regra

- Se o aluno veio transferido após concluir a 5ª série, `certificado_serie_concluida = 5`.
- Se futuramente ele concluir outra série na nova escola e o histórico for atualizado, este campo pode ser alterado.
- O PDF usa esses dados para montar o texto do certificado.
- Assinatura continua manual no papel/PDF impresso.

---

## 11. Relação com o payload do Angular

### 11.1 `contexto`

| Payload | Banco sugerido |
|---|---|
| `idHistoricoEscolar` | `historico_escolar.id_historico_escolar` |
| `idAluno` | `historico_escolar.id_aluno` |
| `idMatricula` | `historico_escolar.id_matricula` |
| `modo` | calculado pelo backend: `CADASTRO` ou `EDICAO` |
| `status` | `historico_escolar.status` |
| `serieMatriculaAtual` | `historico_escolar.serie_matricula_atual` ou `serie.ordem` da matrícula |
| `serieConcluidaOrigem` | `historico_escolar.serie_concluida_origem` |
| `escolaOrigem` | `historico_escolar.escola_origem_nome` ou `transferencia_aluno` |
| `dataTransferencia` | `historico_escolar.data_transferencia` |
| `bloqueado` | `historico_escolar.bloqueado` |

### 11.2 `cabecalho`

| Payload | Banco sugerido |
|---|---|
| `governo` | `historico_escolar_cabecalho.governo` |
| `secretaria` | `historico_escolar_cabecalho.secretaria` |
| `diretoria` | `historico_escolar_cabecalho.diretoria_ensino` |
| `escola` | `historico_escolar_cabecalho.escola_nome` |
| `atoLegalCriacao` | `historico_escolar_cabecalho.ato_legal_criacao` |
| `endereco` | `historico_escolar_cabecalho.endereco` |
| `numero` | `historico_escolar_cabecalho.numero` |
| `bairro` | `historico_escolar_cabecalho.bairro` |
| `municipio` | `historico_escolar_cabecalho.municipio` |
| `cep` | `historico_escolar_cabecalho.cep` |
| `telefone` | `historico_escolar_cabecalho.telefone` |
| `email` | `historico_escolar_cabecalho.email` |

### 11.3 `aluno`

| Payload | Banco sugerido |
|---|---|
| `nome` | `historico_escolar_aluno_dado.nome_aluno` |
| `rg` | `historico_escolar_aluno_dado.rg_rnm` |
| `ra` | `historico_escolar_aluno_dado.ra` |
| `nascimentoMunicipio` | `historico_escolar_aluno_dado.nascimento_municipio` |
| `nascimentoEstado` | `historico_escolar_aluno_dado.nascimento_estado` |
| `nascimentoPais` | `historico_escolar_aluno_dado.nascimento_pais` |
| `nascimentoData` | `historico_escolar_aluno_dado.data_nascimento` |

### 11.4 `periodos`

| Payload | Banco sugerido |
|---|---|
| `ordem` | `historico_escolar_periodo.ordem` |
| `serie` | `historico_escolar_periodo.serie_ano` |
| `equivalencia` | `historico_escolar_periodo.equivalencia` |
| `anoLetivo` | `historico_escolar_periodo.ano_letivo` |

### 11.5 `baseComum` e `parteDiversificada`

Cada célula preenchida vira um registro em `historico_escolar_item`, associado ao período correto.

| Payload | Banco sugerido |
|---|---|
| nome do componente | `historico_escolar_item.componente_curricular` |
| grupo | `historico_escolar_item.grupo_curricular` |
| valor por ano | `historico_escolar_item.nota_conceito` |
| ordem | `historico_escolar_item.ordem_exibicao` |
| período/coluna | `historico_escolar_item.id_historico_escolar_periodo` |

### 11.6 `totais`

Salvar em `historico_escolar_item.tipo_registro`, usando:

```text
TOTAL_BASE_COMUM
TOTAL_PARTE_DIVERSIFICADA
TOTAL_AULAS_ANUAIS
TOTAL_CARGA_HORARIA
```

### 11.7 `estudosRealizados`

Salvar em `historico_escolar_estudo_realizado`.

### 11.8 `observacoes`

Salvar em:

```text
historico_escolar.observacoes
```

### 11.9 `certificado`

Salvar em:

```text
historico_escolar.certificado_serie_concluida
historico_escolar.certificado_texto_ensino
historico_escolar.certificado_data
historico_escolar.ano_conclusao
historico_escolar.ensino_concluido
historico_escolar.diretor_nome
historico_escolar.diretor_rg
historico_escolar.gerente_organizacao_nome
historico_escolar.gerente_organizacao_rg
historico_escolar.doe_numero
historico_escolar.doe_data
historico_escolar.doe_volume
historico_escolar.doe_pagina
```

---

## 12. Fluxo de backend recomendado

### 12.1 Abrir cadastro novo

```http
GET /api/historicos-escolares/novo?idAluno={idAluno}&idMatricula={idMatricula}&modo=CADASTRO
```

Backend deve:

1. Buscar aluno, pessoa, matrícula atual, turma e série.
2. Buscar escola atual e endereço.
3. Buscar transferência, se existir.
4. Retornar formulário vazio ou pré-preenchido conforme regra da escola.
5. Retornar `status = RASCUNHO`.
6. Retornar `pendencias` calculadas.

### 12.2 Salvar cadastro novo

```http
POST /api/historicos-escolares
```

Backend deve validar:

1. Cabeçalho completo.
2. Dados do aluno completos.
3. Criar `historico_escolar`.
4. Criar `historico_escolar_cabecalho`.
5. Criar `historico_escolar_aluno_dado`.
6. Criar períodos, itens, estudos e certificado quando enviados.
7. Calcular pendências.
8. Retornar o histórico salvo com `status = PENDENTE` ou `COMPLETO`.

### 12.3 Editar histórico existente

```http
PUT /api/historicos-escolares/{idHistoricoEscolar}
```

Backend deve:

1. Atualizar cabeçalho e dados do aluno.
2. Atualizar períodos.
3. Atualizar componentes e totais.
4. Atualizar estudos realizados.
5. Atualizar observações e certificado.
6. Recalcular pendências.
7. Atualizar status:
   - `PENDENTE`, se houver pendências;
   - `COMPLETO`, se não houver pendências;
   - `BLOQUEADO`, se o histórico for fechado pela secretaria/direção.

---

## 13. Regra de pendência por série

A regra principal é:

```text
serieObrigatoriaAte = serieMatriculaAtual - 1
```

Exemplo:

```text
Aluno matriculado na 6ª série
Precisa ter histórico preenchido até a 5ª série
```

Backend deve verificar:

1. Existem períodos de 1 até `serieObrigatoriaAte`?
2. Cada período obrigatório tem ano letivo?
3. Cada período obrigatório tem componentes mínimos preenchidos?
4. Cada período obrigatório tem estudos realizados?
5. O certificado indica a série concluída na origem?

Se faltar alguma informação, retornar pendência, por exemplo:

```json
{
  "codigo": "HISTORICO_SERIE_ANTERIOR_INCOMPLETO",
  "descricao": "Aluno matriculado na 6ª série. Histórico precisa estar preenchido até a 5ª série.",
  "aba": "Anos e componentes",
  "obrigatoria": false
}
```

---

## 14. Script consolidado sugerido

Este bloco pode virar uma migration Flyway, por exemplo:

```text
Vxxx__ajustes_historico_escolar_backend.sql
```

Ordem recomendada:

1. `ALTER TABLE escola`
2. `ALTER TABLE historico_escolar`
3. criar `historico_escolar_cabecalho`
4. criar `historico_escolar_aluno_dado`
5. criar `historico_escolar_periodo`
6. alterar `historico_escolar_item`
7. criar `historico_escolar_estudo_realizado`
8. criar `historico_escolar_pendencia`
9. criar índices
10. criar constraints

---

## 15. Observação importante para o Java

No backend Java, evite expor diretamente as entidades JPA para o Angular.

Use DTOs específicos:

```text
HistoricoEscolarResponse
HistoricoEscolarSaveRequest
HistoricoCabecalhoDto
HistoricoAlunoDto
HistoricoPeriodoDto
HistoricoComponenteDto
HistoricoEstudoRealizadoDto
HistoricoCertificadoDto
HistoricoPendenciaDto
```

A tela Angular já está preparada para trabalhar com esse conceito de payload.

---

## 16. Resultado esperado

Com essas alterações, o backend conseguirá:

1. abrir um Histórico Escolar novo vazio para cadastro inicial;
2. exigir somente os dois blocos iniciais para o primeiro salvamento;
3. salvar histórico incompleto com status `PENDENTE`;
4. retornar pendências para a tela;
5. permitir edição posterior;
6. validar preenchimento obrigatório conforme a série atual da matrícula;
7. controlar certificado conforme série concluída na escola de origem;
8. gerar PDF oficial com dados salvos;
9. manter assinatura manual;
10. preservar os dados oficiais do histórico mesmo que o cadastro da escola/aluno mude no futuro.
