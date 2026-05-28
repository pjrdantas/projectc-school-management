# Sprint 0

## Objetivo

Preparar o projeto real de gestao escolar para iniciar a implementacao do primeiro fluxo ponta a ponta
com escopo validado, backlog inicial organizado e arquitetura tecnica minimamente estabilizada.

## Resultado Esperado da Sprint 0

Ao final da Sprint 0, o projeto deve ter:
- escopo inicial validado;
- MVP definido;
- epicos e historias iniciais revisados;
- decisoes arquiteturais basicas fechadas;
- arquitetura inicial documentada;
- repositorio do projeto real preparado;
- backlog tecnico inicial pronto para a Sprint 1.

## Entradas da Sprint 0

Os documentos base para esta sprint sao:
- [00-gestao-escolar-base.md](00-gestao-escolar-base.md)
- [01-mvp-gestao-escolar.md](01-mvp-gestao-escolar.md)
- [02-epicos-e-historias.md](02-epicos-e-historias.md)
- [03-decisoes-arquiteturais.md](03-decisoes-arquiteturais.md)
- [04-arquitetura-inicial.md](04-arquitetura-inicial.md)

## Escopo da Sprint 0

### 1. Validacao do escopo inicial com o cliente

Objetivo:
confirmar o que entra e o que fica fora do MVP.

Atividades:
- revisar o documento do MVP;
- validar prioridades de negocio;
- identificar modulos obrigatorios da primeira entrega;
- registrar exclusoes explicitas do MVP.

Entregavel:
- `01-mvp-gestao-escolar.md` revisado.

### 2. Refinamento inicial de epicos e historias

Objetivo:
transformar o escopo validado em backlog funcional inicial.

Atividades:
- revisar epicos existentes;
- ajustar historias ao contexto real do cliente;
- remover historias fora do MVP;
- adicionar historias faltantes;
- ordenar as historias por prioridade.

Entregavel:
- `02-epicos-e-historias.md` revisado.

### 3. Fechamento das decisoes arquiteturais iniciais

Objetivo:
evitar ambiguidade tecnica antes do inicio da implementacao.

Atividades:
- confirmar o uso de monolito modular;
- confirmar stack inicial;
- definir posicao sobre eventos no MVP;
- confirmar estrategia de autenticacao;
- definir direcao para versionamento de banco e contratos.

Entregavel:
- `03-decisoes-arquiteturais.md` revisado.

### 4. Consolidacao da arquitetura inicial

Objetivo:
traduzir as decisoes tecnicas em estrutura de solucao.

Atividades:
- revisar os modulos iniciais;
- confirmar ownership de responsabilidades;
- definir organizacao do backend;
- definir organizacao inicial do frontend;
- registrar pontos de evolucao futura.

Entregavel:
- `04-arquitetura-inicial.md` revisado.

### 5. Preparacao do projeto real

Objetivo:
deixar o repositorio pronto para iniciar a Sprint 1.

Atividades:
- criar o repositorio do sistema real, se ainda nao existir;
- definir estrutura inicial de pastas;
- definir padrao de versionamento;
- configurar qualidade minima do projeto;
- definir estrategia inicial de ambientes.

Entregaveis:
- repositorio criado;
- estrutura inicial do projeto definida;
- convencoes tecnicas registradas.

### 6. Planejamento da Sprint 1

Objetivo:
converter os artefatos da Sprint 0 em execucao.

Atividades:
- escolher o primeiro fluxo ponta a ponta;
- quebrar historias em tarefas tecnicas;
- definir prioridade da implementacao;
- separar atividades de backend e frontend;
- mapear dependencias e riscos.

Entregavel:
- backlog tecnico da Sprint 1.

## Primeiro Fluxo Recomendado para a Sprint 1

Fluxo sugerido:
- autenticacao basica;
- cadastro de aluno;
- cadastro de turma e periodo;
- matricula;
- consulta de matricula.

## Tarefas da Sprint 0

### Bloco A. Produto e negocio

- revisar o MVP com o cliente;
- ajustar epicos e historias;
- definir o que explicitamente nao entra na primeira entrega;
- validar usuarios e jornadas prioritarias.

### Bloco B. Arquitetura e engenharia

- consolidar decisoes arquiteturais;
- definir estrutura de modulos do backend;
- definir estrutura do frontend Angular;
- definir estrategia de banco, migracoes e autenticacao;
- definir padrao de tratamento de erros e observabilidade.

### Bloco C. Preparacao operacional

- criar o repositorio do produto real;
- configurar padrao de branch;
- configurar pipeline inicial, se couber;
- preparar base de documentacao;
- planejar a Sprint 1.

## Responsabilidades Sugeridas

### Produto

Responsavel por:
- validar MVP;
- revisar historias;
- priorizar backlog.

### Arquitetura e backend

Responsavel por:
- consolidar decisoes tecnicas;
- definir modulos;
- estruturar backend inicial.

### Frontend

Responsavel por:
- validar jornadas;
- organizar estrutura Angular;
- preparar base visual e navegacao inicial.

## Riscos da Sprint 0

- escopo amplo demais para a primeira entrega;
- historias ainda muito genericas;
- falta de decisao sobre autenticacao e perfis;
- tentativa de antecipar complexidade distribuida sem necessidade;
- iniciar frontend ou backend sem backlog funcional suficientemente claro.

## Criterios de Conclusao da Sprint 0

A Sprint 0 estara concluida quando:
- o MVP estiver validado;
- os epicos e historias estiverem ajustados;
- as decisoes arquiteturais iniciais estiverem registradas;
- a arquitetura inicial estiver coerente com o MVP;
- o projeto real estiver pronto para comecar a implementacao;
- houver backlog inicial da Sprint 1.

## Proximo Passo Imediato

Apos concluir esta Sprint 0, o proximo artefato a ser produzido deve ser um backlog tecnico detalhado
da Sprint 1, separado por backend e frontend, a partir do primeiro fluxo escolhido.
