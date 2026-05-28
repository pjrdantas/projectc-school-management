# Guia Base Tecnica Para Implementacao do MVP

## Objetivo

Consolidar uma base tecnica inicial que oriente a implementacao do MVP de forma pratica,
coerente com a arquitetura definida e alinhada ao backlog da Sprint 1.

## Finalidade

Este documento deve servir como referencia rapida para iniciar a implementacao do produto sem ambiguidade sobre:

- o que construir primeiro;
- como estruturar backend e frontend;
- como conectar a arquitetura ao backlog funcional;
- quais decisoes devem ser respeitadas durante a execucao.

## Escopo do MVP

O MVP inicial cobre:

- autenticacao basica;
- cadastro de aluno;
- cadastro de periodo letivo;
- cadastro de turma;
- matricula;
- consulta operacional de matriculas.

Nao cobre nesta fase:

- mobile;
- financeiro avancado;
- notificacoes completas;
- relatorios avancados;
- integracoes externas complexas.

## Arquitetura de Implementacao

### Backend

O backend oficial sera:

- `school-management-service`

Modelo arquitetural:

- monolito modular;
- uma unica API;
- modulos internos por contexto de negocio.

Modulos iniciais:

- `accesscontrol`
- `studentmanagement`
- `academiccatalog`
- `enrollment`

Base de pacote recomendada:

- `br.com.escola`

### Frontend

O frontend oficial sera:

- `school-management-web`

Modelo inicial:

- aplicacao web administrativa;
- organizacao por features;
- integracao com API REST do backend.

Features iniciais:

- `auth`
- `students`
- `academic`
- `enrollment`
- `shared`

## Estrutura Tecnica Minima Esperada

### Backend

Cada modulo deve seguir a estrutura:

```text
<modulo>
  domain
  application
  adapter
    in
    out
  infrastructure
```

### Frontend

Estrutura funcional minima:

```text
src/app
  auth
  students
  academic
  enrollment
  shared
```

## Correspondencia Entre Backlog e Estrutura

| Historia | Backend | Frontend |
|---|---|---|
| H1 - Autenticacao basica | `accesscontrol` | `auth` |
| H2 - Cadastro de aluno | `studentmanagement` | `students` |
| H3 - Cadastro de periodo letivo e turma | `academiccatalog` | `academic` |
| H4 - Matricula de aluno | `enrollment` | `enrollment` |
| H5 - Consulta operacional de matricula | `enrollment` | `enrollment` |

## Ordem Recomendada de Implementacao

### Fase 1. Fundacao tecnica

Objetivo:
preparar a base para o desenvolvimento do MVP.

Entregas:

- convencoes tecnicas registradas;
- organizacao inicial da solucao registrada;
- padroes de estrutura e responsabilidade definidos;
- projeto backend estruturado;
- ambiente local e banco configurados;
- projeto frontend estruturado.

### Fase 2. Fluxo funcional do MVP

Objetivo:
implementar o primeiro fluxo ponta a ponta.

Sequencia recomendada:

1. autenticacao basica;
2. cadastro de aluno;
3. cadastro de periodo letivo e turma;
4. matricula;
5. consulta operacional de matriculas.

## Diretrizes de Implementacao

### Backend

- implementar por modulo e nao por camada global do sistema;
- manter controllers finos;
- colocar regra de negocio principal em dominio e casos de uso;
- isolar persistencia em adaptadores de saida;
- evitar dependencias desnecessarias entre modulos.

### Frontend

- construir por feature e por jornada;
- manter telas e formularios proximos da feature dona;
- colocar componentes realmente compartilhados em `shared`;
- evitar espalhar regra funcional entre varias features sem limite claro.

## Definicao de pronto tecnica para iniciar codificacao funcional

A base tecnica sera considerada suficiente quando:

- nomes oficiais de backend e frontend estiverem definidos;
- modulos e features iniciais estiverem registrados;
- convencoes de nomenclatura estiverem definidas;
- padroes de estrutura e responsabilidade estiverem definidos;
- ordem inicial de implementacao estiver clara.

## Artefatos de referencia

Este guia depende dos seguintes documentos:

- `docs/01-mvp-gestao-escolar.md`
- `docs/03-decisoes-arquiteturais.md`
- `docs/04-arquitetura-inicial.md`
- `docs/06-sprint-1-backlog.md`
- `docs/10-convencoes-tecnicas-iniciais.md`
- `docs/11-organizacao-inicial-da-solucao.md`
- `docs/12-padroes-minimos-de-estrutura-e-responsabilidade.md`
- `docs/14-definition-of-structure-done.md`

## Decisao Registrada

Para iniciar a implementacao do MVP:

- usar `school-management-service` como backend oficial;
- usar `school-management-web` como frontend oficial;
- implementar o backend como monolito modular;
- estruturar o frontend por feature;
- iniciar pelo fluxo de autenticacao, cadastro, matricula e consulta;
- usar este documento como referencia executiva para o inicio da codificacao.
