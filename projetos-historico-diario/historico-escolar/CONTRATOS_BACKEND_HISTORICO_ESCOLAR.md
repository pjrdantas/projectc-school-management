# Contratos Backend - Histórico Escolar

Este documento descreve o que o backend Java deve entregar e receber para alimentar a tela de **Histórico Escolar**.

## 1. Objetivo da tela

A tela atende dois estados principais:

1. **Cadastro inicial**: usado quando a escola recebe um aluno transferido de outra escola. O formulário pode vir vazio, pois será o primeiro cadastramento do histórico na nova escola.
2. **Edição**: usado para atualizar o histórico conforme o aluno evolui nas séries ou conforme a secretaria complete informações pendentes.

A assinatura continua sendo manual no PDF impresso. O sistema salva os dados do histórico, mas não precisa capturar assinatura digital nesta etapa.

## 2. Regras funcionais implementadas no Angular

### 2.1 Cadastro mínimo obrigatório

Para liberar o botão **Salvar**, é obrigatório preencher os dois blocos iniciais da tela:

- dados da escola/cabeçalho;
- dados do aluno/nascimento.

Campos obrigatórios do cabeçalho:

- Governo;
- Secretaria;
- Diretoria de Ensino;
- Escola;
- Ato legal de criação;
- Endereço;
- Número;
- Bairro;
- Município;
- CEP;
- Telefone;
- E-mail.

Campos obrigatórios do aluno:

- Nome do aluno;
- RG/RNM;
- RA;
- Município de nascimento;
- Estado de nascimento;
- País de nascimento;
- Data de nascimento.

### 2.2 Abas que podem ficar pendentes

As abas abaixo podem ser preenchidas depois:

- Anos e componentes;
- Estudos realizados;
- Observações e certificado.

Mesmo assim, após salvar, a tela exibe avisos de pendência.

### 2.3 Regra da série de matrícula atual

O backend deve informar a série atual da matrícula.

Exemplo:

```text
Aluno matriculado na 6ª série / 6º ano
```

Neste caso, o histórico deve estar preenchido pelo menos até a 5ª série / 5º ano.

A tela valida e mostra pendência quando faltam informações anteriores à série atual.

### 2.4 Certificado

O certificado representa a série concluída na escola de origem.

Exemplo:

```text
Aluno veio transferido após concluir a 5ª série.
Certificado deve indicar a 5ª série como concluída.
```

Esse campo pode ser atualizado futuramente, pois o aluno pode evoluir para outras séries.

## 3. Endpoint para carregar o Histórico Escolar

### Cadastro novo

```http
GET /api/historicos-escolares/novo?idAluno={idAluno}&idMatricula={idMatricula}&modo=CADASTRO
```

### Edição

```http
GET /api/historicos-escolares/{idHistoricoEscolar}
```

## 4. Response para carregar a tela

```json
{
  "contexto": {
    "idHistoricoEscolar": null,
    "idAluno": "uuid-aluno",
    "idMatricula": "uuid-matricula",
    "modo": "CADASTRO",
    "status": "RASCUNHO",
    "serieMatriculaAtual": 6,
    "serieConcluidaOrigem": 5,
    "escolaOrigem": "",
    "dataTransferencia": "",
    "bloqueado": false
  },
  "cabecalho": {
    "governo": "",
    "secretaria": "",
    "diretoria": "",
    "escola": "",
    "atoLegal": "Ato Legal de criação:",
    "atoLegalCriacao": "",
    "endereco": "",
    "numero": "",
    "bairro": "",
    "municipio": "",
    "cep": "",
    "telefone": "",
    "email": ""
  },
  "aluno": {
    "nome": "",
    "rg": "",
    "ra": "",
    "nascimentoMunicipio": "",
    "nascimentoEstado": "",
    "nascimentoPais": "",
    "nascimentoData": ""
  },
  "periodos": [
    {
      "ordem": 1,
      "anoLetivo": "",
      "serie": "1º Ano",
      "equivalencia": "1ª série"
    }
  ],
  "baseComum": [
    {
      "ordem": 1,
      "nome": "Língua Portuguesa",
      "valores": ["", "", "", "", "", "", "", "", ""]
    }
  ],
  "parteDiversificada": [
    {
      "ordem": 1,
      "nome": "Projeto de Vida",
      "valores": ["", "", "", "", "", "", "", "", ""]
    }
  ],
  "totais": {
    "totalBaseComum": ["", "", "", "", "", "", "", "", ""],
    "totalParteDiversificada": ["", "", "", "", "", "", "", "", ""],
    "totalAulasAnuais": ["", "", "", "", "", "", "", "", ""],
    "totalCargaHoraria": ["", "", "", "", "", "", "", "", ""]
  },
  "estudosRealizados": [
    {
      "ordem": 1,
      "serieAno": "1º Ano",
      "ano": "",
      "escola": "",
      "municipio": "",
      "uf": ""
    }
  ],
  "observacoes": "",
  "certificado": {
    "serieConcluida": 5,
    "diretor": "",
    "escola": "",
    "rgAluno": "",
    "ano": "",
    "publicacao": "",
    "data": "",
    "gerenteNome": "",
    "gerenteRg": "",
    "diretorNome": "",
    "diretorRg": ""
  },
  "pendencias": []
}
```

## 5. Endpoint para salvar

### Criar cadastro

```http
POST /api/historicos-escolares
```

### Atualizar cadastro existente

```http
PUT /api/historicos-escolares/{idHistoricoEscolar}
```

## 6. Request de salvamento

```json
{
  "idHistoricoEscolar": null,
  "idAluno": "uuid-aluno",
  "idMatricula": "uuid-matricula",
  "modo": "CADASTRO",
  "statusPretendido": "PENDENTE",
  "contexto": {
    "serieMatriculaAtual": 6,
    "serieConcluidaOrigem": 5,
    "escolaOrigem": "Escola Municipal Exemplo",
    "dataTransferencia": "2026-06-26"
  },
  "cabecalho": {},
  "aluno": {},
  "periodos": [],
  "baseComum": [],
  "parteDiversificada": [],
  "totais": {},
  "estudosRealizados": [],
  "observacoes": "",
  "certificado": {}
}
```

## 7. Response de salvamento

```json
{
  "idHistoricoEscolar": "uuid-historico",
  "status": "PENDENTE",
  "mensagem": "Histórico Escolar salvo com pendências. Complete as abas restantes quando possível.",
  "salvoEm": "2026-06-26T18:30:00-03:00",
  "pendencias": [
    {
      "codigo": "ANOS_COMPONENTES_PENDENTES",
      "severidade": "AVISO",
      "aba": "ANOS_COMPONENTES",
      "mensagem": "Há componentes curriculares ou anos letivos pendentes até a série anterior à matrícula."
    }
  ]
}
```

## 8. Status sugeridos

```text
RASCUNHO  - Histórico criado, mas ainda incompleto.
PENDENTE  - Cadastro mínimo salvo, porém existem pendências nas abas complementares.
COMPLETO  - Cadastro mínimo, anos/componentes, estudos e certificado preenchidos.
```

## 9. Tabelas/ajustes de banco sugeridos para o Histórico Escolar

A base já possui uma estrutura de histórico, mas para atender a tela com segurança o backend deve conseguir persistir:

- cabeçalho usado no documento;
- dados do aluno no momento da emissão;
- série atual da matrícula;
- série concluída na escola de origem;
- estudos realizados por série;
- componentes curriculares por grupo;
- totais por ano/série;
- certificado evolutivo.

Sugestões mínimas:

```sql
ALTER TABLE public.historico_escolar
ADD COLUMN IF NOT EXISTS id_matricula uuid,
ADD COLUMN IF NOT EXISTS status varchar(30) DEFAULT 'RASCUNHO' NOT NULL,
ADD COLUMN IF NOT EXISTS serie_matricula_atual integer,
ADD COLUMN IF NOT EXISTS serie_concluida_origem integer,
ADD COLUMN IF NOT EXISTS escola_origem varchar(150),
ADD COLUMN IF NOT EXISTS data_transferencia date,
ADD COLUMN IF NOT EXISTS governo varchar(150),
ADD COLUMN IF NOT EXISTS secretaria varchar(150),
ADD COLUMN IF NOT EXISTS diretoria_ensino varchar(150),
ADD COLUMN IF NOT EXISTS escola_nome_documento varchar(150),
ADD COLUMN IF NOT EXISTS ato_legal_criacao varchar(255),
ADD COLUMN IF NOT EXISTS endereco_documento varchar(255),
ADD COLUMN IF NOT EXISTS numero_documento varchar(20),
ADD COLUMN IF NOT EXISTS bairro_documento varchar(100),
ADD COLUMN IF NOT EXISTS municipio_documento varchar(100),
ADD COLUMN IF NOT EXISTS cep_documento varchar(20),
ADD COLUMN IF NOT EXISTS telefone_documento varchar(30),
ADD COLUMN IF NOT EXISTS email_documento varchar(120),
ADD COLUMN IF NOT EXISTS aluno_nome_documento varchar(150),
ADD COLUMN IF NOT EXISTS aluno_rg_rnm_documento varchar(30),
ADD COLUMN IF NOT EXISTS aluno_ra_documento varchar(30),
ADD COLUMN IF NOT EXISTS nascimento_municipio_documento varchar(100),
ADD COLUMN IF NOT EXISTS nascimento_estado_documento varchar(2),
ADD COLUMN IF NOT EXISTS nascimento_pais_documento varchar(80),
ADD COLUMN IF NOT EXISTS nascimento_data_documento date,
ADD COLUMN IF NOT EXISTS certificado_serie_concluida integer,
ADD COLUMN IF NOT EXISTS updated_at timestamp without time zone;
```

```sql
ALTER TABLE public.historico_escolar_item
ADD COLUMN IF NOT EXISTS grupo_curricular varchar(40),
ADD COLUMN IF NOT EXISTS ordem_exibicao integer,
ADD COLUMN IF NOT EXISTS serie_ordem integer,
ADD COLUMN IF NOT EXISTS valor_registro varchar(30);
```

```sql
CREATE TABLE IF NOT EXISTS public.historico_escolar_estudo_realizado (
    id_historico_escolar_estudo_realizado uuid DEFAULT gen_random_uuid() NOT NULL,
    id_historico_escolar uuid NOT NULL,
    serie_ordem integer NOT NULL,
    serie_ano varchar(80) NOT NULL,
    ano integer,
    estabelecimento_ensino varchar(150),
    municipio varchar(100),
    uf char(2),
    ordem integer NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,

    CONSTRAINT pk_historico_escolar_estudo_realizado
        PRIMARY KEY (id_historico_escolar_estudo_realizado),

    CONSTRAINT fk_hist_estudo_historico
        FOREIGN KEY (id_historico_escolar)
        REFERENCES public.historico_escolar(id_historico_escolar)
);
```

```sql
CREATE TABLE IF NOT EXISTS public.historico_escolar_pendencia (
    id_historico_escolar_pendencia uuid DEFAULT gen_random_uuid() NOT NULL,
    id_historico_escolar uuid NOT NULL,
    codigo varchar(80) NOT NULL,
    severidade varchar(20) NOT NULL,
    aba varchar(40),
    mensagem text NOT NULL,
    resolvida boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    resolved_at timestamp without time zone,

    CONSTRAINT pk_historico_escolar_pendencia
        PRIMARY KEY (id_historico_escolar_pendencia),

    CONSTRAINT fk_hist_pendencia_historico
        FOREIGN KEY (id_historico_escolar)
        REFERENCES public.historico_escolar(id_historico_escolar)
);
```

## 10. Regra de validação no backend

O frontend já valida para melhorar a experiência, mas o backend precisa validar novamente:

1. Recusar salvamento se os dados iniciais da escola/aluno estiverem incompletos.
2. Permitir salvar com status `PENDENTE` quando anos/componentes, estudos ou certificado estiverem incompletos.
3. Gerar pendências quando `serie_matricula_atual - 1` ainda não estiver completa.
4. Atualizar certificado conforme `serie_concluida_origem` ou conforme evolução do aluno.
5. Não depender apenas do frontend para status final.

## 11. Importação de PDF do Histórico Escolar

A tela possui o botão **Importar PDF**. O Angular envia o arquivo para o backend e recebe o Histórico Escolar estruturado para preencher a tela.

Endpoint sugerido:

```http
POST /api/historicos-escolares/importacao-pdf
Content-Type: multipart/form-data
```

Campo do arquivo:

```text
arquivo
```

Response:

```ts
interface HistoricoEscolarPdfImportResponse {
  historico: HistoricoEscolarResponse;
  nomeArquivo: string;
  confiancaGeral: number;
  avisos: string[];
  importadoEm: string;
}
```

Regra importante: a importação apenas pré-preenche a tela. O usuário deve conferir os dados e clicar em **Salvar** para persistir o Histórico Escolar.

Mais detalhes no arquivo:

```text
IMPORTACAO_PDF_HISTORICO_ESCOLAR.md
```
