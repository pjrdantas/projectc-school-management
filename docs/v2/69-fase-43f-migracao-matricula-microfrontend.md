# Fase 43F - Migracao de matricula para o microfrontend

## Objetivo

Migrar a funcionalidade de matricula do host para o microfrontend, preservando a rota publica `/enrollment` e mantendo o host como shell de seguranca, menu e administracao tecnica.

## Alteracoes realizadas

- Movida a tela `matricula/pages/new` do host para o microfrontend.
- Copiados `matricula/models` e `matricula/services` para o microfrontend.
- Removido o pacote `matricula` restante do host, pois nao havia mais dependencia local apos a rota federada.
- Adaptado `EnrollmentService` no microfrontend para consumir `ShellContextService`.
- Mantidos os endpoints REST existentes:
  - `POST /api/matriculas`
  - `GET /api/matriculas`
  - `PATCH /api/matriculas/{id}/status`
  - `DELETE /api/matriculas/{id}`
- Exposto o componente remoto `./Matricula` no Native Federation.
- Atualizada a rota local do microfrontend `enrollment`.
- Atualizada a rota do host `/enrollment` para carregar `EnrollmentNewComponent` via remoto.

## Contrato de shell

O microfrontend de matricula passou a obter:

- URL base da API por `ShellContextService.getApiBaseUrl()`;
- token de acesso por `ShellContextService.getToken()`.

Com isso, matricula nao depende mais de `AuthStateService` nem de `API_BASE_URL` internos do host.

## Validacao

Comandos executados:

```bash
cd school-management-web/microfrontend
npm run build
```

Resultado: build concluido com sucesso e chunk `enrollment-new-component` gerado.

```bash
cd school-management-web/host
npm run build
```

Resultado: build concluido com sucesso apos remocao do pacote local de matricula.

Tambem foi validado por busca que nao restaram referencias de `AuthStateService` ou `API_BASE_URL` em `microfrontend/src/app/matricula`.

## Estado final

- `host`: carrega `/enrollment` via remoto `./Matricula`.
- `microfrontend`: possui a implementacao funcional de matricula.
- rota publica preservada: `/enrollment`.

## Proxima fase sugerida

Migrar a tela restante de disciplinas/historico academico ainda residente no host ou fazer uma fase curta de limpeza das dependencias escolares que ficaram no host apos as migracoes de catalogo, responsavel, aluno e matricula.
