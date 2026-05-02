# Documento Descritivo — Projeto Piloto Microfrontend Angular

> **Data de referência:** Abril de 2026
> **Versão Angular:** 20.x | **Angular Material:** 20.x | **Native Federation:** 21.x

---

## 1. Visão Geral

O **Projeto Piloto** é uma aplicação web corporativa construída com **Angular 20** seguindo a arquitetura de **Microfrontend com Native Federation**. O projeto está organizado como um monorepo com dois módulos independentes:

| Módulo | Tipo | Porta padrão |
|---|---|---|
| `host` | Shell (aplicação principal) | 4200 |
| `microfrontend` | Remote (aplicação remota / MFE) | 4201 |

A aplicação host é responsável pela autenticação, roteamento global, navegação e consumo das funcionalidades expostas pelo microfrontend. O microfrontend expõe componentes que são carregados dinamicamente pelo host em tempo de execução, sem necessidade de recompilação conjunta.

---

## 2. Stack Tecnológica

| Tecnologia | Versão | Finalidade |
|---|---|---|
| Angular | ^20.3.0 | Framework principal |
| Angular Material / CDK | ^20.2.x | Componentes de UI |
| @angular-architects/native-federation | ^21.0.3 | Arquitetura Microfrontend |
| RxJS | ~7.8.0 | Programação reativa |
| TypeScript | — | Linguagem principal |
| Angular ESLint | 21.0.1 | Linting |
| Karma / Jasmine | 6.4 / 5.9 | Testes unitários |
| Prettier | — | Formatação de código |

---

## 3. Arquitetura Microfrontend (Native Federation)

### 3.1 Funcionamento

O projeto utiliza **Native Federation** (baseado em ES Modules nativos do navegador), diferente do Webpack Module Federation tradicional. Cada aplicação gera um arquivo `remoteEntry.json` que descreve os módulos expostos.

```
host (shell)  ──────────────────────────────────────────────────────────┐
│  Carrega manifest: /public/federation.manifest.json                   │
│  { "mfe1": "http://localhost:4201/remoteEntry.json" }                 │
│                                                                       │
│  Rota /app/microfrontend ──► loadRemoteModule('mfe1', './Component') ─►  HomeComponent (MFE)
└───────────────────────────────────────────────────────────────────────┘
```

### 3.2 Configuração do Host (`host/federation.config.js`)

- **Nome:** `host`
- **Modo:** consumidor (sem exposes)
- Compartilha todas as dependências como singletons (`shareAll`), incluindo `@angular/material` e `@angular/cdk`

### 3.3 Configuração do Microfrontend (`microfrontend/federation.config.js`)

- **Nome:** `microfrontend`
- **Expõe:** `./Component` → `src/app/pages/home/home.component.ts`
- Compartilha as mesmas dependências como singletons para evitar duplicação de bundles

---

## 4. Estrutura de Diretórios

### 4.1 Host (`host/src/app/`)

```
app/
├── app.config.ts               # Configuração global do provedor Angular
├── app.routes.ts               # Roteamento principal
├── app.ts                      # Componente raiz
│
├── core/
│   ├── auth/
│   │   ├── auth.service.ts             # Login, refresh token, validação
│   │   ├── auth-state.service.ts       # Estado reativo da autenticação
│   │   ├── auth-bootstrap.service.ts   # Restaura sessão na inicialização
│   │   └── session-validator.service.ts # Validação periódica de sessão
│   ├── interceptors/
│   │   ├── auth.interceptor.ts         # Injeção de JWT + refresh automático
│   │   └── http-error.interceptor.ts   # Tratamento global de erros HTTP
│   └── logger/
│       └── logger.service.ts           # Serviço de log
│
├── guards/
│   └── auth.guard.ts           # Guard de rotas privadas
│
├── layouts/
│   ├── private-layout.component.ts     # Layout autenticado (inclui menu)
│   └── public-layout.component.ts      # Layout público (login)
│
├── models/                     # Interfaces e modelos de dados
│
├── pages/
│   ├── login/                  # Tela de login
│   ├── home/                   # Página inicial
│   ├── menu/                   # Barra de navegação lateral/topo
│   ├── aplicativos/            # CRUD de Aplicativos
│   ├── perfis/                 # Gestão de Perfis
│   ├── permissoes/             # Gestão de Permissões
│   └── usuarios/               # Gestão de Usuários
│
├── services/                   # Serviços de comunicação com a API
│   ├── aplicativos.service.ts
│   ├── config.service.ts
│   ├── permissao.service.ts
│   ├── role.service.ts
│   └── usuario.service.ts
│
├── shared/
│   ├── dialog-confirmacao/     # Dialog reutilizável de confirmação
│   └── directives/
│       └── has-permission.directive.ts  # Diretiva de controle de acesso
│
└── validators/
    └── url-validator.ts        # Validador customizado de URL
```

### 4.2 Microfrontend (`microfrontend/src/app/`)

```
app/
├── core/
│   └── interceptors/
│       └── http-error.interceptor.ts
└── pages/
    └── home/
        ├── home.component.ts   # Componente exposto via Native Federation
        ├── home.component.html
        └── home.component.scss
```

---

## 5. Módulo de Autenticação

### 5.1 Fluxo de Autenticação

```
Usuário → Login → AuthService.login() → API POST /auth/login
                                              │
                             ┌────────────────┘
                             ↓
                  AuthStateService.setAuth()
                  ├── Armazena token (localStorage)
                  ├── Armazena refreshToken (localStorage)
                  └── Armazena usuarioObj (localStorage)
                             │
                             ↓
                  BehaviorSubjects notificam subscribers reativamente
```

### 5.2 Renovação Automática de Token

O `AuthInterceptor` intercepta todas as requisições HTTP:

1. Injeta o `Bearer Token` no header `Authorization`
2. Em caso de erro **401**, aciona `AuthService.refreshToken()`
3. Requisições pendentes aguardam o novo token via `refreshTokenSubject`
4. Após renovação, as requisições são reenviadas com o novo token
5. Em caso de falha no refresh, limpa o estado e redireciona para login

### 5.3 Validação Periódica de Sessão

O `SessionValidatorService` realiza validações automáticas a cada **10 segundos** chamando `POST /auth/validate-session`. Se a sessão for inválida, o estado é limpo automaticamente.

### 5.4 Bootstrap de Autenticação

O `AuthBootstrapService.init()` é chamado na inicialização da aplicação para restaurar o estado de autenticação a partir do `localStorage`, garantindo que o usuário permaneça autenticado após recarregar a página.

### 5.5 Armazenamento de Estado

| Chave (localStorage) | Conteúdo |
|---|---|
| `token` | JWT de acesso |
| `refreshToken` | Token de renovação |
| `usuarioObj` | Objeto JSON com `{ usuario, nome, perfis, permissoes }` |

---

## 6. Sistema de Permissões

### 6.1 Modelo de Permissões

Cada usuário autenticado possui um array de `permissoes` (strings). O controle de acesso é aplicado em duas camadas:

**Template (estrutural):**
```html
<button *appHasPermission="'PERMISSAO_NOME'">Ação restrita</button>
```
A diretiva `HasPermissionDirective` é reativa: atualiza automaticamente quando o estado do usuário muda.

**Programático:**
```typescript
this.authService.hasPermission('PERMISSAO_NOME')
```

### 6.2 Guard de Rotas

O `authGuard` protege todas as rotas sob o prefixo `/app/**`. Verifica a existência do token JWT; caso ausente, redireciona para `/login`.

---

## 7. Roteamento

```
/                    →  redireciona para /login
/login               →  LoginComponent          (layout público)
/app                 →  redireciona para /app/home  (requer autenticação)
/app/home            →  HomeComponent
/app/perfis          →  PerfisComponent          (lazy load)
/app/permissoes      →  PermissoesComponent      (lazy load)
/app/usuarios        →  UsuariosComponent        (lazy load)
/app/aplicativos     →  AplicativosComponent     (lazy load)
/app/microfrontend   →  HomeComponent (MFE)      (Native Federation)
**                   →  redireciona para /login
```

Todas as páginas privadas são carregadas com **lazy loading** (`loadComponent`), exceto `HomeComponent` que é importado diretamente. O carregamento do MFE utiliza `loadRemoteModule('mfe1', './Component')`.

---

## 8. Páginas e Funcionalidades

### 8.1 Login
- Formulário de autenticação com usuário e senha
- Integração com `AuthService`

### 8.2 Home
- Página inicial da área autenticada
- Ponto de entrada após o login

### 8.3 Menu (Navegação)
- Barra de navegação com links para todas as seções
- Exibe o nome do usuário logado
- Relógio em tempo real (atualizado a cada segundo, executado fora do `NgZone` por performance)
- Itens do menu controlados por `HasPermissionDirective`

### 8.4 Aplicativos
- Listagem de aplicativos com `MatTable` (ordenação + paginação)
- CRUD completo:
  - **Criar/Editar:** via `AplicativosDialogComponent`
  - **Visualizar detalhes:** via `AplicativosDetailsDialogComponent`
  - **Excluir:** via `ConfirmationDialogComponent`
- Controle de permissões por ação
- Atualização reativa da lista via `BehaviorSubject` + `switchMap`

### 8.5 Perfis
- Gerenciamento de perfis de usuário

### 8.6 Permissões
- Gerenciamento de permissões do sistema
- Dialog de criação/edição (`PermissaoDialogComponent`)

### 8.7 Usuários
- Gerenciamento de usuários do sistema

### 8.8 Microfrontend
- Renderiza o `HomeComponent` carregado dinamicamente do microfrontend remoto
- Demonstração da integração Native Federation em tempo de execução

---

## 9. Configuração de Ambiente

O `ConfigService` centraliza a URL base da API por ambiente:

| Ambiente | URL |
|---|---|
| `local` | `http://localhost:8080/api` |
| `dev` | `https://dev.api.suaempresa.com/api` |
| `hml` | `https://hml.api.suaempresa.com/api` |
| `prod` | `https://api.suaempresa.com/api` |

A variável `env` é trocada manualmente antes de cada deploy. Todos os serviços injetam o `ConfigService` para obter a URL base dinamicamente.

---

## 10. Componentes Compartilhados

### 10.1 ConfirmationDialogComponent
Dialog genérico de confirmação (`Sim` / `Não`) utilizado em operações de exclusão. Recebe `ConfirmationDialogData` com título e mensagem.

### 10.2 HasPermissionDirective
Diretiva estrutural standalone que:
- Renderiza ou remove o elemento do DOM com base nas permissões do usuário
- Reage automaticamente a mudanças no estado de autenticação via `usuario$` Observable
- Libera a subscription via `OnDestroy`

### 10.3 UrlValidator
Validador reativo customizado para campos de URL em formulários Angular.

---

## 11. Como Executar o Projeto

### Pré-requisitos
- Node.js (LTS)
- Angular CLI 20.x

### 1. Iniciar o Microfrontend (porta 4201)

```bash
cd microfrontend
npm install
ng serve
```

### 2. Iniciar o Host (porta 4200)

```bash
cd host
npm install
ng serve
```

Acesse a aplicação em: **http://localhost:4200**

> O host carrega o microfrontend automaticamente via manifest em `http://localhost:4201/remoteEntry.json`. Certifique-se de que o microfrontend esteja rodando antes de navegar para `/app/microfrontend`.

---

## 12. Scripts Disponíveis

| Comando | Descrição |
|---|---|
| `ng serve` | Inicia o servidor de desenvolvimento |
| `ng build` | Gera o build de produção |
| `ng test` | Executa testes unitários (Karma/Jasmine) |
| `ng lint` | Analisa o código com ESLint |
| `ng build --watch --configuration development` | Build em modo watch |

---

## 13. Padrões e Convenções

- **Componentes standalone:** todos os componentes utilizam a API standalone (sem NgModules)
- **Injeção de dependência moderna:** uso de `inject()` no lugar de construtor quando possível
- **Programação reativa:** uso de `Observable`, `BehaviorSubject` e operadores RxJS (`switchMap`, `tap`, `catchError`, `takeUntil`)
- **Lazy loading:** todas as páginas secundárias são carregadas sob demanda
- **Single quotes** e **printWidth 100** definidos via Prettier
- **Separação de responsabilidades:** lógica de negócio em services, estado em state services, apresentação em components
