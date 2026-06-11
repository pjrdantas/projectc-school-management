# Fase 45H - Revisao da experiencia de configuracao de dashboards

## Objetivo

Melhorar a leitura operacional da tela administrativa de configuracao de
dashboards, mantendo o foco no uso do ADMIN para revisar publicos, dashboards e
widgets oficiais.

## Escopo implementado

- Frontend do microfrontend academico.
- Tela `Configuracao de dashboards`.
- O painel de widgets passou a exibir um resumo compacto com:
  - widgets oficiais previstos;
  - widgets oficiais criados;
  - widgets oficiais faltantes;
  - widgets oficiais divergentes;
  - widgets customizados.
- O botao de atualizacao de metadados passou de `Atualizar` para
  `Sincronizar`.
- O botao de criacao dos widgets padrao passou de `Padrao` para
  `Criar faltantes`.
- A revisao de widgets oficiais foi compactada para reduzir ruido visual e
  facilitar a leitura do estado de cada widget.

## Decisoes

- Sem alteracao backend, Maven, Flyway ou schema.
- Sem mudanca na regra de negocio de provisionamento ou sincronizacao.
- Widgets customizados seguem fora das rotinas oficiais e agora aparecem apenas
  no resumo operacional.

## Validacao

- `npm run build` em `school-management-web/microfrontend`.

## Proxima fase sugerida

Fase 45I: iniciar a administracao de snapshots de dashboard no microfrontend,
permitindo ao ADMIN consultar snapshots por publico e data antes de avançar para
edicoes manuais.
