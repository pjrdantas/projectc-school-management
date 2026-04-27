# Plano Frontend Angular 20 + Angular Material + Microfrontend Federado

## Objetivo

Definir a base tecnica do frontend para iniciar a implementacao da Sprint 1 com:

- Angular 20;
- Angular Material;
- arquitetura por features;
- roteamento desde o inicio;
- preparacao para microfrontend federado.

Este documento foi pensado para ser migrado para o novo repositorio/branch do frontend, iniciando limpo e com direcao unica.

## Contexto de decisao

A evolucao de um frontend monolitico para microfrontend costuma gerar retrabalho de:

- fronteiras de modulo;
- roteamento;
- compartilhamento de bibliotecas;
- governance de design system;
- pipeline e deploy.

Por isso, a decisao e iniciar ja com base federada.

## Princípios

1. Organizar por feature de negocio.
2. Manter componente de tela separado de service de dados.
3. Definir contratos de API por feature.
4. Centralizar autenticacao/erro/telemetria em `core`.
5. Colocar somente reutilizaveis reais em `shared`.
6. Manter baixo acoplamento entre microfrontends.

---

## Stack recomendada

- Angular 20 (standalone components);
- Angular Material;
- RxJS;
- Angular Router;
- Module Federation (Native Federation ou Webpack Module Federation, conforme padrao escolhido no bootstrap do projeto).

---

## Estrutura base do projeto

```text
src/app
  core/
    auth/
    http/
    guards/
    interceptors/
    layout/
    config/
  shared/
    ui/
    components/
    directives/
    pipes/
    models/
    utils/
  features/
    auth/
      pages/
      components/
      services/
      models/
      routes/
    students/
      pages/
      components/
      services/
      models/
      routes/
    academic/
      pages/
      components/
      services/
      models/
      routes/
    enrollment/
      pages/
      components/
      services/
      models/
      routes/
```

### Regras de responsabilidade

### Componentes de tela (`.ts`, `.html`, `.scss`)

Devem conter:

- estrutura visual;
- bindings;
- interacao de formulario;
- delegacao para services.

Nao devem conter:

- regra de negocio de integracao;
- montagem manual de endpoint;
- tratamento global de erro.

### Services de feature

Devem conter:

- chamadas HTTP;
- transformacao de dados da feature;
- estado local de tela/fluxo (quando aplicavel).

Nao devem conter:

- regra de navegacao global;
- regra de autenticacao transversal.

### Core

Deve conter:

- interceptors;
- guardas de rota;
- sessao/autenticacao;
- configuracao de ambiente;
- shell/layout principal.

### Shared

Deve conter:

- componentes reutilizaveis;
- modelos e utilitarios compartilhados;
- estilos/token visuais comuns.

---

## Roteamento (base)

## Rotas de shell

```text
/                -> redireciona para /auth/login ou /students
/auth/login      -> tela de login
/students/new    -> cadastro de aluno
/academic/periods/new -> cadastro de periodo letivo
/academic/classes/new -> cadastro de turma
/enrollment/new  -> tela de matricula
/enrollment/search -> consulta operacional de matriculas
```

## Estrategia de guards

- `authGuard`: protege rotas internas autenticadas;
- rotas publicas restritas ao `auth`;
- fallback para `login` em sessao invalida.

## Lazy loading

- carregar cada feature sob demanda;
- manter bundle inicial pequeno;
- preparar separacao natural para remotes federados.

---

## Modelo de microfrontend federado (recomendado)

## Opcao inicial (mais simples)

- 1 app `shell` (host);
- 4 remotes funcionais:
  - `mf-auth`
  - `mf-students`
  - `mf-academic`
  - `mf-enrollment`

## Responsabilidade

- `shell`: layout, navegacao, auth state global, roteamento raiz, observabilidade basica;
- `remotes`: paginas, components e services da feature;
- `shared lib`: design tokens, componentes base e tipos comuns.

## Contratos entre shell e remotes

- evitar compartilhar estado mutavel complexo;
- trafegar dados por contratos claros (inputs/events/services de interface);
- versionar contratos de integracao.

## Dependencias compartilhadas (singleton)

- Angular packages;
- Angular Material;
- RxJS.

Objetivo: evitar duplicidade de runtime no browser.

---

## Contratos de API por feature (MVP)

## Auth

- login;
- logout;
- estado de sessao.

## Students

- `POST /api/alunos`
- `GET /api/alunos/{id}`

## Academic

- `POST /api/periodos-letivos`
- `GET /api/periodos-letivos/{id}`
- `POST /api/turmas`
- `GET /api/turmas/{id}`

## Enrollment

- `POST /api/matriculas`
- `GET /api/matriculas?alunoId=&turmaId=&periodoLetivoId=&status=`

---

## Plano de execucao (alinhado ao Kanban)

1. `KAN-27` - Estruturar projeto Angular + Material + base de federacao;
2. configurar `core`, `shared` e rotas base do shell;
3. implementar `auth` (login + guard + logout);
4. implementar `students` (tela + validacao + integracao);
5. implementar `academic` (periodo/turma + integracao);
6. implementar `enrollment` (matricula + consulta + filtros);
7. hardening: erro padronizado, loading state, testes de integracao de fluxos criticos.

---

## Definition of Done (frontend sprint 1)

- projeto sobe localmente com shell e pelo menos 1 remote conectado;
- rotas principais acessiveis;
- guard de autenticacao funcional;
- telas da sprint criadas e integradas;
- tratamento de sucesso/erro consistente;
- consulta de matricula com filtros funcionando;
- documentacao de setup e comandos atualizada.

---

## Riscos e mitigacoes

## Risco 1: Complexidade inicial da federacao
Mitigacao: iniciar com shell + 1 remote e expandir progressivamente.

## Risco 2: Acoplamento entre remotes
Mitigacao: contratos de interface simples e versionados.

## Risco 3: Divergencia visual
Mitigacao: uso precoce de Angular Material + componentes compartilhados.

## Risco 4: Divergencia com backend
Mitigacao: DTOs por feature + validacao de contrato a cada integracao.

---

## Checklist de inicio rapido

- [ ] Criar workspace Angular 20
- [ ] Adicionar Angular Material
- [ ] Definir shell e primeiro remote
- [ ] Configurar roteamento base
- [ ] Configurar guard e interceptor
- [ ] Criar estrutura de features
- [ ] Conectar endpoints da Sprint 1
- [ ] Registrar comandos de setup no README do frontend
