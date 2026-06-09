# Fase 45A - Frontend administrativo de configuracao de dashboards

## Objetivo

Criar a tela administrativa para o ADMIN configurar publicos, dashboards e widgets ja suportados pela API da Fase 34.

## Escopo realizado

- Criado dominio frontend de configuracao de dashboards no microfrontend.
- Criada tela `Configuração de dashboards` com tres areas:
  - publicos;
  - dashboards do publico selecionado;
  - widgets do dashboard selecionado.
- Criados modais para cadastro/edicao de publico, dashboard e widget.
- Adicionadas acoes de exclusao com confirmacao.
- Integrado com:
  - `GET /api/dashboard/configuracoes/publicos`;
  - `POST /api/dashboard/configuracoes/publicos`;
  - `PUT /api/dashboard/configuracoes/publicos/{id}`;
  - `DELETE /api/dashboard/configuracoes/publicos/{id}`;
  - `GET /api/dashboard/configuracoes/dashboards`;
  - `POST /api/dashboard/configuracoes/dashboards`;
  - `PUT /api/dashboard/configuracoes/dashboards/{id}`;
  - `DELETE /api/dashboard/configuracoes/dashboards/{id}`;
  - `GET /api/dashboard/configuracoes/dashboards/{dashboardId}/widgets`;
  - `POST /api/dashboard/configuracoes/widgets`;
  - `PUT /api/dashboard/configuracoes/widgets/{id}`;
  - `DELETE /api/dashboard/configuracoes/widgets/{id}`.
- Exposta rota federada `dashboard/config`.
- Adicionado item de menu `Dashboards` para perfil `ADMIN`.

## Decisoes

- UUIDs de publicos, dashboards e widgets sao usados apenas internamente nas chamadas e selecoes; nao sao exibidos na interface.
- Combos em modais usam `select matNativeControl`.
- A tela ficou restrita ao ADMIN nesta fase.
- Nao houve alteracao backend, Maven, Flyway ou schema.

## Validacao

- `npm run build` em `school-management-web/microfrontend`.
- `npm run build` em `school-management-web/host`.
- Verificacao no navegador de `http://localhost:4200/dashboard/config`, redirecionando para login sem erros de console quando sem sessao.

## Proxima fase apos concluir

Avancar para a personalizacao de dashboards por usuario, permitindo que cada usuario configure visibilidade, ordem e preferencias dos widgets do proprio dashboard.
