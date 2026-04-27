# Decisoes Arquiteturais

## Objetivo

Este documento registra as principais decisoes arquiteturais iniciais do projeto,
para que elas possam ser revisitadas, refinadas ou substituidas com contexto.

## DA-001 - Usar o projeto atual como referencia, nao como produto final

### Decisao

O `academic-core-service` sera tratado como referencia de aprendizado, arquitetura e fluxo,
mas nao como base obrigatoria do sistema real.

### Justificativa

- o projeto atual cumpriu papel de laboratorio tecnico;
- ele ja validou conceitos importantes;
- evitar mexer em algo estavel reduz risco;
- o produto real precisa nascer com fronteiras mais claras de dominio.

### Consequencia

- reaproveitamos aprendizados;
- evitamos carregar limitacoes do experimento para o produto;
- mantemos liberdade para desenhar melhor o sistema real.

## DA-002 - Iniciar com monolito modular

### Decisao

O sistema real deve comecar como monolito modular.

### Justificativa

- reduz complexidade operacional;
- acelera o desenvolvimento inicial;
- favorece entendimento do dominio;
- evita o custo prematuro de uma arquitetura distribuida.

### Consequencia

- o codigo deve nascer com modulos bem definidos;
- nao ha necessidade imediata de multiplas APIs;
- a extracao futura continua possivel se os limites forem respeitados.

## DA-003 - Separar modulos por contexto de negocio

### Decisao

A organizacao principal do backend sera por contexto de negocio, e nao por camada tecnica global.

### Justificativa

- melhora clareza de responsabilidade;
- reduz acoplamento indevido;
- facilita evolucao orientada ao dominio;
- prepara o sistema para crescer sem virar um bloco unico confuso.

### Consequencia

Modulos iniciais previstos:
- `access-control`;
- `student-management`;
- `academic-catalog`;
- `enrollment`;
- `finance`;
- `notification`.

## DA-004 - Priorizar MVP operacional antes de modulos avancados

### Decisao

O primeiro foco sera um MVP enxuto e operacional.

### Justificativa

- sistema escolar tem escopo naturalmente grande;
- priorizacao protege prazo e qualidade;
- funcionalidades secundarias podem ser adicionadas depois.

### Consequencia

Entram primeiro:
- autenticacao;
- cadastro de alunos e responsaveis;
- cadastro academico basico;
- matricula;
- consultas operacionais.

Ficam para depois:
- mobile;
- notificacoes mais completas;
- financeiro avancado;
- relatorios mais sofisticados.

## DA-005 - Prototipar frontend cedo, sem comecar o projeto por telas isoladas

### Decisao

O frontend deve ser prototipado cedo, mas o projeto nao deve comecar por implementacao visual isolada.

### Justificativa

- telas ajudam a validar jornada com o cliente;
- regras de negocio precisam estar claras antes da implementacao pesada;
- evita desalinhamento entre UX e dominio.

### Consequencia

- primeiro vem descoberta do produto e definicao do MVP;
- depois backend e frontend andam em paralelo;
- Angular 20 sera a base do frontend web administrativo.

## DA-006 - Adiar mobile ate existir necessidade real

### Decisao

O projeto nao iniciara com app mobile no MVP.

### Justificativa

- aumenta custo e superficie de manutencao;
- pode ser desnecessario no inicio;
- web administrativo costuma atender bem a primeira fase operacional.

### Consequencia

- foco no frontend web;
- reavaliacao do mobile apos validacao do MVP;
- mobile so entra com caso de uso real e priorizado.

## DA-007 - Usar eventos de forma seletiva

### Decisao

Kafka, saga, outbox e retry nao devem ser adotados por padrao em todos os modulos desde o dia 1.

### Justificativa

- esses padroes agregam robustez, mas tambem complexidade;
- o MVP pode nao precisar de arquitetura assincrona completa em todos os fluxos;
- o uso deve ser guiado por necessidade de negocio e integracao.

### Consequencia

- o sistema pode iniciar com fluxos transacionais locais;
- eventos entram quando houver motivacao clara;
- o projeto atual continua como referencia de como aplicar esses padroes quando preciso.

## DA-008 - Documentacao do projeto deve morar no repositorio

### Decisao

As decisoes, arquitetura e backlog base devem ser mantidos em arquivos versionados no repositorio.

### Justificativa

- conversas de chat nao sao memoria garantida;
- arquivos no repositorio sao rastreaveis;
- melhora continuidade entre sessoes e colaboradores.

### Consequencia

Os documentos iniciais ficam em `docs/` e servem como fonte principal de referencia para futuras iteracoes.
