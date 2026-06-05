# Fase 43G - Migracao de historico/disciplinas e limpeza do host

## Objetivo

Remover do host a ultima tela escolar local ainda ativa, `academic/disciplines`, e deixar o host mais proximo do papel definido para a arquitetura federada: shell, seguranca, menu e administracao tecnica.

## Alteracoes realizadas

- Exposto o componente remoto `./HistoricoDisciplines` no microfrontend.
- Adicionada a rota direta `academic/disciplines` no microfrontend.
- Atualizada a rota do host `academic/disciplines` para carregar `DisciplinesComponent` via Native Federation.
- Removidos do host os pacotes escolares locais que ficaram sem dependencia funcional:
  - `historico`;
  - `catalogo`;
  - `aluno`;
  - `responsavel`;
  - `documento`.

## Contrato de shell

O modulo de historico no microfrontend ja usa `ShellContextService` para obter API base e token. Foi validado que nao ha dependencia de `AuthStateService` ou `API_BASE_URL` internos do host em `microfrontend/src/app/historico`.

## Rotas preservadas

- `academic/disciplines`

Essa rota continua existindo no host, mas agora resolve o componente pelo remoto `./HistoricoDisciplines`.

## Validacao

Validacoes previstas para a fase:

```bash
cd school-management-web/microfrontend
npm run build
```

```bash
cd school-management-web/host
npm run build
```

Tambem deve ser validado que nao restaram importacoes locais para `historico`, `catalogo`, `aluno`, `responsavel` ou `documento` no host.

## Estado final esperado

- `host`: mantem login, menu, home, seguranca, usuarios, perfis, permissoes e carregamento de remotos.
- `microfrontend`: concentra catalogo, responsavel, aluno, matricula e historico/disciplinas.

## Proxima fase sugerida

Auditar o host para separar o que ainda e infraestrutura de shell do que ainda pode ser movido ou limpo, antes de iniciar novas telas de negocio ou dashboard no microfrontend.
