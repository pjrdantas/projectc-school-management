# Implementação de Banco de Dados para Diário de Classe

Este documento descreve **somente os ajustes necessários para a tela de Diário de Classe** do projeto `diario-classe-material`.

A proposta considera a base atual já existente, principalmente as tabelas:

- `escola`
- `periodo_letivo`
- `turma`
- `turma_disciplina`
- `professor_turma_disciplina`
- `matricula`
- `aluno`
- `pessoa`
- `disciplina`
- `planejamento_bimestral`
- `planejamento_bimestral_aula`
- `planejamento_bimestral_avaliacao`
- `aula`
- `frequencia_aluno`
- `situacao_frequencia`
- `avaliacao`

A ideia é **não refazer o modelo escolar inteiro**, mas acrescentar apenas o que falta para o backend Java conseguir alimentar e salvar corretamente o payload da tela do Diário de Classe.

---

## 1. Regra funcional da tela

Quando o professor abrir o Diário de Classe:

1. O cabeçalho deve vir preenchido pelo backend.
2. A data do diário deve ser a data corrente.
3. O mês exibido deve ser o mês corrente.
4. A aba **Frequência** deve carregar todos os alunos matriculados na turma/disciplina.
5. Somente o dia corrente pode receber lançamento.
6. Dias anteriores ficam bloqueados.
7. Dias posteriores ficam bloqueados.
8. Finais de semana ficam bloqueados e podem ficar sem lançamento.
9. A frequência aceita somente:
   - `P` = presença;
   - `.` = presença;
   - `F` = falta.
10. Para salvar, todos os alunos precisam ter lançamento no dia corrente, quando o dia corrente for dia útil.
11. A aba **Conteúdo e observações** deve vir preenchida a partir do planejamento.
12. Se o professor alterar o período ou a descrição do conteúdo, a justificativa/observação passa a ser obrigatória.
13. A aba **Avaliações e assinatura** deve carregar avaliações já planejadas e bloqueadas para alteração.
14. A assinatura do professor e a data da assinatura são obrigatórias.
15. Após salvar, o lançamento do dia fica bloqueado.
16. O PDF deve ser gerado com os dados atuais carregados/salvos.

---

## 2. Campos do cabeçalho

A maior parte do cabeçalho já pode ser montada com as tabelas existentes.

| Campo da tela | Origem sugerida |
|---|---|
| Escola | `escola.nome` |
| Município | `endereco.cidade`, via `escola.id_endereco` |
| Ano letivo | `periodo_letivo.ano` |
| Turma/Série | `turma.nome`, `serie.nome` |
| Turno | `turno.nome` |
| Disciplina | `disciplina.nome` |
| Professor | `professor` + `pessoa.nome_completo` |
| Mês | data corrente do backend |
| Data atual | data corrente do backend |

### Ajuste necessário em `escola`

A tela possui campo de **Diretoria de Ensino**. Caso esse campo ainda não exista, adicionar:

```sql
ALTER TABLE public.escola
ADD COLUMN IF NOT EXISTS diretoria_ensino varchar(150);
```

---

## 3. Situação de frequência

A tabela `situacao_frequencia` já existe e pode continuar sendo usada.

Sugestão de registros mínimos:

```sql
INSERT INTO public.situacao_frequencia (codigo, descricao)
SELECT 'PRESENTE', 'Presente'
WHERE NOT EXISTS (
    SELECT 1 FROM public.situacao_frequencia WHERE codigo = 'PRESENTE'
);

INSERT INTO public.situacao_frequencia (codigo, descricao)
SELECT 'FALTA', 'Falta'
WHERE NOT EXISTS (
    SELECT 1 FROM public.situacao_frequencia WHERE codigo = 'FALTA'
);
```

Como a tela aceita `P` e `.` para presença, o backend deve tratar os dois como presença.

Porém, para preservar exatamente o que o professor digitou na tela e também no PDF, é recomendado gravar a marcação original.

### Ajuste necessário em `frequencia_aluno`

```sql
ALTER TABLE public.frequencia_aluno
ADD COLUMN IF NOT EXISTS marcacao_diario char(1),
ADD COLUMN IF NOT EXISTS updated_at timestamp without time zone;

ALTER TABLE public.frequencia_aluno
ADD CONSTRAINT ck_frequencia_aluno_marcacao_diario
CHECK (marcacao_diario IS NULL OR marcacao_diario IN ('P', '.', 'F'));
```

### Restrição para evitar duplicidade de frequência do aluno na mesma aula

```sql
CREATE UNIQUE INDEX IF NOT EXISTS uk_frequencia_aluno_aula_matricula
ON public.frequencia_aluno (id_aula, id_matricula);
```

---

## 4. Controle de lançamento diário

A tabela `aula` registra a aula em si, mas a tela do Diário precisa de um controle claro de:

- data do lançamento;
- professor/turma/disciplina;
- assinatura;
- data da assinatura;
- status do lançamento;
- bloqueio após salvar.

Para isso, criar uma tabela específica de lançamento do Diário.

```sql
CREATE TABLE IF NOT EXISTS public.diario_classe_lancamento (
    id_diario_classe_lancamento uuid DEFAULT gen_random_uuid() NOT NULL,
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

    CONSTRAINT pk_diario_classe_lancamento
        PRIMARY KEY (id_diario_classe_lancamento),

    CONSTRAINT fk_diario_lancamento_prof_turma_disc
        FOREIGN KEY (id_professor_turma_disciplina)
        REFERENCES public.professor_turma_disciplina(id_professor_turma_disciplina),

    CONSTRAINT ck_diario_lancamento_mes
        CHECK (mes BETWEEN 1 AND 12),

    CONSTRAINT ck_diario_lancamento_status
        CHECK (status IN ('EM_PREENCHIMENTO', 'SALVO', 'BLOQUEADO'))
);
```

### Um lançamento por professor/turma/disciplina/data

```sql
CREATE UNIQUE INDEX IF NOT EXISTS uk_diario_lancamento_prof_turma_disc_data
ON public.diario_classe_lancamento (
    id_professor_turma_disciplina,
    data_lancamento
);
```

### Relação opcional entre `aula` e `diario_classe_lancamento`

Para facilitar rastreabilidade, adicionar em `aula`:

```sql
ALTER TABLE public.aula
ADD COLUMN IF NOT EXISTS id_diario_classe_lancamento uuid;

ALTER TABLE public.aula
ADD CONSTRAINT fk_aula_diario_classe_lancamento
FOREIGN KEY (id_diario_classe_lancamento)
REFERENCES public.diario_classe_lancamento(id_diario_classe_lancamento);
```

### Índice para buscar a aula do dia

```sql
CREATE INDEX IF NOT EXISTS idx_aula_prof_turma_disc_data
ON public.aula (id_professor_turma_disciplina, data_aula);
```

---

## 5. Conteúdo planejado versus conteúdo lançado

A tela carrega o conteúdo a partir do planejamento, mas permite alteração do período e da descrição.

Quando houver alteração, a justificativa é obrigatória.

A tabela `aula` possui `conteudo_ministrado` e `observacao`, mas ela não guarda bem o comparativo entre planejado e lançado.

Por isso, criar uma tabela própria para os conteúdos lançados no Diário.

```sql
CREATE TABLE IF NOT EXISTS public.diario_classe_conteudo_lancamento (
    id_diario_classe_conteudo_lancamento uuid DEFAULT gen_random_uuid() NOT NULL,
    id_diario_classe_lancamento uuid NOT NULL,
    id_planejamento_bimestral_aula uuid,
    periodo_planejado varchar(40),
    periodo_lancado varchar(40) NOT NULL,
    conteudo_planejado text,
    conteudo_lancado text NOT NULL,
    alterado boolean DEFAULT false NOT NULL,
    justificativa_alteracao text,
    ordem integer NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,

    CONSTRAINT pk_diario_classe_conteudo_lancamento
        PRIMARY KEY (id_diario_classe_conteudo_lancamento),

    CONSTRAINT fk_diario_conteudo_lancamento
        FOREIGN KEY (id_diario_classe_lancamento)
        REFERENCES public.diario_classe_lancamento(id_diario_classe_lancamento),

    CONSTRAINT fk_diario_conteudo_planejamento_bim_aula
        FOREIGN KEY (id_planejamento_bimestral_aula)
        REFERENCES public.planejamento_bimestral_aula(id_planejamento_bimestral_aula),

    CONSTRAINT ck_diario_conteudo_justificativa
        CHECK (
            alterado = false
            OR justificativa_alteracao IS NOT NULL
            AND length(trim(justificativa_alteracao)) > 0
        )
);
```

### Evitar duplicidade do mesmo item planejado no mesmo lançamento

```sql
CREATE UNIQUE INDEX IF NOT EXISTS uk_diario_conteudo_lancamento_planejamento
ON public.diario_classe_conteudo_lancamento (
    id_diario_classe_lancamento,
    id_planejamento_bimestral_aula
)
WHERE id_planejamento_bimestral_aula IS NOT NULL;
```

---

## 6. Observações do Diário

A tela possui observações ligadas ao lançamento do dia.

Criar uma tabela simples para permitir uma ou mais observações.

```sql
CREATE TABLE IF NOT EXISTS public.diario_classe_observacao (
    id_diario_classe_observacao uuid DEFAULT gen_random_uuid() NOT NULL,
    id_diario_classe_lancamento uuid NOT NULL,
    observacao text NOT NULL,
    ordem integer DEFAULT 1 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,

    CONSTRAINT pk_diario_classe_observacao
        PRIMARY KEY (id_diario_classe_observacao),

    CONSTRAINT fk_diario_observacao_lancamento
        FOREIGN KEY (id_diario_classe_lancamento)
        REFERENCES public.diario_classe_lancamento(id_diario_classe_lancamento)
);
```

---

## 7. Avaliações

As avaliações da tela devem vir pré-carregadas e não podem ser alteradas pelo professor nessa tela.

Usar preferencialmente as tabelas existentes:

- `planejamento_bimestral_avaliacao`, quando a avaliação ainda é planejada;
- `avaliacao`, quando ela já foi efetivada no sistema.

A tabela `avaliacao` já possui `id_planejamento_bimestral_avaliacao`, então ela pode ser usada para manter o vínculo com o planejamento.

Nenhuma tabela nova é obrigatória para avaliações, desde que o backend consiga buscar as avaliações por `id_professor_turma_disciplina`, mês e ano.

Sugestão de índice:

```sql
CREATE INDEX IF NOT EXISTS idx_avaliacao_prof_turma_disc_data
ON public.avaliacao (id_professor_turma_disciplina, data_aplicacao);
```

---

## 8. Consulta de alunos da turma

A aba Frequência precisa trazer todos os alunos da classe.

O backend deve buscar alunos por matrícula ativa na turma.

Sugestão de índice:

```sql
CREATE INDEX IF NOT EXISTS idx_matricula_turma_status
ON public.matricula (id_turma, id_status_matricula);
```

Caso o sistema use campo de matrícula ativa por status, o backend deve filtrar somente matrículas ativas/cursando.

---

## 9. Payload de carregamento da tela

Endpoint sugerido:

```http
GET /api/diarios-classe?idProfessor={idProfessor}&idTurma={idTurma}&idDisciplina={idDisciplina}&anoLetivo={anoLetivo}&mes={mes}&dataReferencia={yyyy-MM-dd}
```

Response esperado pelo Angular:

```json
{
  "cabecalho": {
    "idDiarioClasse": "uuid-do-lancamento-ou-chave-logica",
    "idEscola": "uuid-escola",
    "escola": "EMEF Professora Helena Duarte",
    "diretoriaEnsino": "Diretoria de Ensino Centro",
    "municipio": "São Paulo",
    "anoLetivo": 2026,
    "mes": 6,
    "dataAtual": "2026-06-26",
    "idTurma": "uuid-turma",
    "turmaSerie": "8º A",
    "turno": "Manhã",
    "idDisciplina": "uuid-disciplina",
    "disciplina": "Matemática",
    "idProfessor": "uuid-professor",
    "professor": "Marina Albuquerque"
  },
  "alunos": [
    {
      "idAluno": "uuid-aluno",
      "numeroChamada": 1,
      "nome": "Ana Clara Santos",
      "frequencias": {
        "1": "P",
        "2": ".",
        "3": "F",
        "26": ""
      }
    }
  ],
  "conteudosPlanejados": [
    {
      "idPlanejamentoAula": "uuid-planejamento-bimestral-aula",
      "periodo": "24 a 28",
      "descricao": "Frações: conceito, representação e exercícios"
    }
  ],
  "observacoes": [
    ""
  ],
  "avaliacoes": [
    {
      "idAvaliacao": "uuid-avaliacao",
      "data": "28/06",
      "descricao": "Atividade avaliativa de frações",
      "turma": "8º A",
      "valor": "0 a 10"
    }
  ],
  "assinatura": {
    "nomeProfessor": "",
    "dataAssinatura": "26/06/2026"
  },
  "bloqueado": false
}
```

### Observação sobre `idDiarioClasse`

No frontend atual, o nome do campo é `idDiarioClasse`.

No banco sugerido, ele corresponde a `id_diario_classe_lancamento` para a data corrente.

O backend pode manter o nome `idDiarioClasse` no DTO para não precisar alterar o Angular.

---

## 10. Payload de salvamento

Endpoint sugerido:

```http
PUT /api/diarios-classe/{idDiarioClasse}
```

Request esperado pelo backend:

```json
{
  "idDiarioClasse": "uuid-lancamento",
  "dataLancamento": "2026-06-26",
  "frequencias": [
    {
      "idAluno": "uuid-aluno-001",
      "data": "2026-06-26",
      "dia": 26,
      "status": "P"
    },
    {
      "idAluno": "uuid-aluno-002",
      "data": "2026-06-26",
      "dia": 26,
      "status": "."
    },
    {
      "idAluno": "uuid-aluno-003",
      "data": "2026-06-26",
      "dia": 26,
      "status": "F"
    }
  ],
  "conteudos": [
    {
      "idPlanejamentoAula": "uuid-planejamento-bimestral-aula",
      "periodo": "24 a 28",
      "descricao": "Frações: conceito, representação e exercícios",
      "alterado": false
    },
    {
      "idPlanejamentoAula": "uuid-planejamento-bimestral-aula-2",
      "periodo": "24 a 28",
      "descricao": "Revisão adicional de frações equivalentes",
      "alterado": true,
      "observacaoJustificativa": "A turma precisou de reforço antes da avaliação."
    }
  ],
  "observacoes": [
    "A turma precisou de reforço antes da avaliação."
  ],
  "assinatura": {
    "nomeProfessor": "Marina Albuquerque",
    "dataAssinatura": "26/06/2026"
  }
}
```

Response esperado:

```json
{
  "idDiarioClasse": "uuid-lancamento",
  "status": "SALVO",
  "mensagem": "Diário de Classe salvo com sucesso.",
  "salvoEm": "2026-06-26T17:10:00",
  "bloqueado": true
}
```

---

## 11. Regras obrigatórias no backend

Mesmo que o frontend já valide, o backend precisa validar novamente.

### 11.1 Data corrente

O backend deve rejeitar salvamento se `dataLancamento` for diferente da data corrente do servidor.

```text
Regra: dataLancamento == current_date
```

### 11.2 Final de semana

Se a data corrente for sábado ou domingo, o backend não deve exigir frequência.

Pode retornar a tela bloqueada para lançamento de frequência.

### 11.3 Lançamento duplicado

Se já existir `diario_classe_lancamento` com:

- mesmo `id_professor_turma_disciplina`;
- mesma `data_lancamento`;
- `bloqueado = true`;

então o backend não deve permitir novo salvamento.

### 11.4 Frequência obrigatória em dia útil

Em dia útil, todos os alunos ativos da turma devem ter lançamento no dia corrente.

Valores aceitos:

```text
P
.
F
```

Qualquer outro valor deve retornar erro de validação.

### 11.5 Mapeamento de frequência

| Valor recebido | Gravar em `marcacao_diario` | Situação |
|---|---|---|
| `P` | `P` | PRESENTE |
| `.` | `.` | PRESENTE |
| `F` | `F` | FALTA |

### 11.6 Justificativa quando conteúdo mudar

Se `alterado = true`, então `observacaoJustificativa` é obrigatória.

Também é recomendado o backend recalcular se houve alteração, comparando:

- período planejado x período lançado;
- conteúdo planejado x conteúdo lançado.

### 11.7 Avaliações bloqueadas

O backend não deve aceitar alteração de avaliação por esse endpoint.

As avaliações devem ser somente consulta no carregamento da tela.

### 11.8 Assinatura obrigatória

Para salvar:

- `assinatura.nomeProfessor` obrigatório;
- `assinatura.dataAssinatura` obrigatória.

### 11.9 Bloqueio após salvar

Após salvar com sucesso:

```sql
status = 'SALVO'
bloqueado = true
salvo_em = current_timestamp
```

No próximo carregamento da tela para a mesma data, o backend deve retornar:

```json
"bloqueado": true
```

---

## 12. Sugestão de fluxo de persistência no backend

Ao receber o `PUT /api/diarios-classe/{id}`:

1. Validar professor/turma/disciplina.
2. Validar se a data do lançamento é a data corrente.
3. Validar se o dia é útil.
4. Buscar ou criar `diario_classe_lancamento`.
5. Verificar se já está bloqueado.
6. Criar ou atualizar `aula` do dia.
7. Para cada aluno:
   - localizar matrícula ativa;
   - localizar situação `PRESENTE` ou `FALTA`;
   - gravar `frequencia_aluno`;
   - gravar `marcacao_diario`.
8. Gravar `diario_classe_conteudo_lancamento`.
9. Gravar `diario_classe_observacao`.
10. Gravar assinatura e data assinatura em `diario_classe_lancamento`.
11. Marcar `status = 'SALVO'` e `bloqueado = true`.
12. Retornar response com `bloqueado = true`.

---

## 13. Resumo das implementações necessárias

### Alterações em tabelas existentes

```sql
ALTER TABLE public.escola
ADD COLUMN IF NOT EXISTS diretoria_ensino varchar(150);

ALTER TABLE public.frequencia_aluno
ADD COLUMN IF NOT EXISTS marcacao_diario char(1),
ADD COLUMN IF NOT EXISTS updated_at timestamp without time zone;

ALTER TABLE public.aula
ADD COLUMN IF NOT EXISTS id_diario_classe_lancamento uuid;
```

### Novas tabelas

```text
public.diario_classe_lancamento
public.diario_classe_conteudo_lancamento
public.diario_classe_observacao
```

### Índices/constraints importantes

```text
uk_diario_lancamento_prof_turma_disc_data
uk_frequencia_aluno_aula_matricula
ck_frequencia_aluno_marcacao_diario
ck_diario_conteudo_justificativa
idx_aula_prof_turma_disc_data
idx_avaliacao_prof_turma_disc_data
idx_matricula_turma_status
```

---

## 14. Observação importante

Esta proposta é voltada apenas para o **Diário de Classe**.

Não contempla alterações para Histórico Escolar, matrícula, transferência, boletim ou documentos.
