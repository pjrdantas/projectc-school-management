# Padroes Minimos de Estrutura e Responsabilidade

## Objetivo

Definir os padroes minimos de estrutura e responsabilidade do projeto para orientar a implementacao do MVP com clareza e consistencia.

## Escopo

Este documento define:

- responsabilidade de cada camada principal;
- responsabilidade de cada modulo inicial do backend;
- responsabilidade das features iniciais do frontend;
- regras minimas para evitar acoplamento indevido;
- distribuicao esperada dos artefatos da solucao.

## Principio Geral

Cada parte do sistema deve ter responsabilidade clara.

O projeto nao deve crescer com classes ou componentes que misturem:

- regra de negocio;
- acesso a banco;
- exposicao HTTP;
- integracao externa;
- formatacao de resposta;
- detalhes de interface.

## Backend

### Estrutura base

Cada modulo do backend deve seguir a estrutura:

```text
<modulo>
  domain
  application
  adapter
    in
    out
  infrastructure
```

## Responsabilidade por camada no backend

### `domain`

Responsavel por:

- entidades de negocio;
- regras centrais do dominio;
- objetos de valor, se houver;
- excecoes de negocio;
- estados e eventos de dominio, quando aplicavel.

Nao deve conter:

- controller;
- repository JPA;
- codigo HTTP;
- anotacoes de framework sem necessidade forte;
- acesso direto a banco;
- dependencias de interface web.

### `application`

Responsavel por:

- casos de uso;
- coordenacao do fluxo da aplicacao;
- orquestracao entre dominio e portas;
- definicao de contratos de entrada e saida da aplicacao.

Nao deve conter:

- detalhes de persistencia concreta;
- controller web;
- regra puramente visual;
- logica de framework espalhada sem necessidade.

### `adapter.in`

Responsavel por:

- entrada no sistema;
- controllers REST;
- requests e responses;
- validacao de entrada ligada ao transporte;
- traducao entre HTTP e aplicacao.

Nao deve conter:

- regra de negocio central;
- acesso direto ao banco;
- orquestracao complexa que pertence ao caso de uso.

### `adapter.out`

Responsavel por:

- adaptacao de saida para persistencia;
- adaptacao para mensageria;
- implementacao concreta de portas de saida;
- mapeamento entre dominio e infraestrutura.

Nao deve conter:

- controller;
- regra de tela;
- regra de negocio central.

### `infrastructure`

Responsavel por:

- configuracoes tecnicas;
- integracao com mensageria;
- observabilidade;
- agendamentos;
- suporte tecnico transversal.

Nao deve conter:

- regra de negocio do dominio;
- decisao funcional de caso de uso sem necessidade.

## Responsabilidade por modulo no backend

### `accesscontrol`

Deve concentrar:

- autenticacao;
- autorizacao;
- usuario;
- perfil;
- sessao ou token.

Nao deve concentrar:

- cadastro academico;
- matricula;
- regra de aluno fora do contexto de acesso.

### `studentmanagement`

Deve concentrar:

- aluno;
- responsavel;
- vinculo aluno-responsavel;
- consulta cadastral.

Nao deve concentrar:

- autenticacao;
- regra de turma;
- orquestracao de matricula fora do contexto cadastral.

### `academiccatalog`

Deve concentrar:

- periodo letivo;
- turma;
- disciplina;
- dados academicos estruturais do MVP.

Nao deve concentrar:

- autenticacao;
- cadastro detalhado de aluno;
- ciclo operacional da matricula.

### `enrollment`

Deve concentrar:

- matricula;
- status da matricula;
- cancelamento;
- consulta operacional de matriculas.

Nao deve concentrar:

- autenticacao;
- ownership do cadastro de aluno;
- ownership do cadastro academico estrutural.

## Frontend

### Estrutura base

As features iniciais do frontend devem ser organizadas por responsabilidade funcional:

```text
src/app
  auth
  students
  academic
  enrollment
  shared
```

## Responsabilidade por feature no frontend

### `auth`

Responsavel por:

- login;
- logout;
- sessao;
- guardas de rota;
- controle de autenticacao.

### `students`

Responsavel por:

- tela de cadastro de aluno;
- formulario de aluno;
- integracao com endpoints de aluno.

### `academic`

Responsavel por:

- tela de periodo letivo;
- tela de turma;
- integracao com endpoints academicos.

### `enrollment`

Responsavel por:

- tela de matricula;
- tela de consulta operacional;
- filtros e exibicao de status da matricula.

### `shared`

Responsavel por:

- componentes reutilizaveis;
- servicos comuns;
- modelos compartilhados;
- recursos transversais que nao pertencem a uma unica feature.

Nao deve virar:

- deposito de codigo sem dono;
- lugar para regra de negocio principal;
- pasta generica para qualquer coisa indefinida.

## Regras minimas de responsabilidade

### Regra 1

Controller nao implementa regra de negocio principal.

### Regra 2

Caso de uso nao conhece detalhes de HTTP.

### Regra 3

Dominio nao depende de repository JPA concreto.

### Regra 4

Feature de frontend nao deve acessar diretamente outra feature sem contrato claro.

### Regra 5

Codigo compartilhado so entra em `shared` quando houver reutilizacao real.

### Regra 6

Cada conceito principal do negocio deve ter um modulo ou feature dono.

## Distribuicao esperada dos artefatos

### Backend

- entidades de dominio em `domain`;
- casos de uso em `application`;
- controllers em `adapter.in`;
- repositories concretos, mappers e entities JPA em `adapter.out`;
- configuracoes e mensageria em `infrastructure`.

### Frontend

- telas e componentes por feature;
- servicos de acesso a API perto da feature dona quando o uso for local;
- componentes realmente compartilhados em `shared`;
- rotas organizadas por feature funcional.

## Sinais de problema

Os seguintes sinais indicam desvio estrutural:

- controller crescendo demais;
- classe `Service` fazendo tudo;
- regra de negocio espalhada em repository;
- componente frontend acessando dados de varias features sem limite claro;
- pasta `shared` recebendo tudo que nao se sabe onde colocar;
- mesmo conceito sendo nomeado ou tratado de formas diferentes entre camadas.

## Decisao Registrada

Para o MVP inicial:

- o backend seguira estrutura por modulo com separacao entre `domain`, `application`, `adapter` e `infrastructure`;
- o frontend seguira estrutura por feature funcional;
- cada modulo e cada feature tera ownership claro;
- regra de negocio, persistencia, transporte e interface nao devem ser misturados sem necessidade.
