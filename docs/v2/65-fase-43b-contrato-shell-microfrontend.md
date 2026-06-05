## Fase 43B - Contrato de shell entre host e microfrontend

Esta fase cria um contrato explicito para o microfrontend consumir contexto de seguranca publicado pelo host, sem duplicar autenticacao.

### Objetivo

Permitir que funcionalidades escolares migradas para o microfrontend consumam:

- URL base da API;
- token de acesso atual;
- usuario autenticado;
- perfis;
- permissoes.

### Contrato publicado pelo host

O host publica o contexto no `localStorage` usando a chave:

```text
school-management.shell.context.v1
```

Tambem dispara o evento de browser:

```text
school-management:shell-context-changed
```

Formato do contrato:

```ts
interface ShellContext {
  version: 1;
  apiBaseUrl: string;
  accessToken: string | null;
  usuario: {
    usuario: string;
    nome: string;
    perfis: string[];
    permissoes: string[];
  } | null;
}
```

### Implementacao no host

- `school-management-web/host/src/app/core/shell/shell-context.model.ts`
- `school-management-web/host/src/app/core/shell/shell-context-publisher.service.ts`

O publisher e inicializado no bootstrap do host e republica o contrato quando token ou usuario mudam.

### Implementacao no microfrontend

- `school-management-web/microfrontend/src/app/core/shell/shell-context.model.ts`
- `school-management-web/microfrontend/src/app/core/shell/shell-context.service.ts`
- `school-management-web/microfrontend/src/app/core/shell/shell-auth.interceptor.ts`

O microfrontend le o contexto inicial do `localStorage`, escuta o evento de alteracao e usa o token no interceptor HTTP.

### Limites desta fase

- Nao houve migracao de funcionalidade escolar.
- Nao houve alteracao de rotas publicas.
- Nao houve duplicacao de login no microfrontend.
- Refresh token continua sob responsabilidade do host.

### Validacao

- `npm run build` em `school-management-web/host` executado com sucesso.
- `npm run build` em `school-management-web/microfrontend` executado com sucesso.

### Proxima fase sugerida

Fase 43C - migrar o primeiro dominio piloto para o microfrontend.

Dominio recomendado: `catalogo`, por ser mais isolado que aluno, responsavel e matricula.
