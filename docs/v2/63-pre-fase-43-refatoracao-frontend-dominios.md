## Pre-Fase 43 - Refatoracao estrutural do frontend por dominios

Esta fase reorganiza a estrutura do Angular host para aproximar os dominios de negocio da nomenclatura usada no backend, antes de iniciar a implementacao visual dos dashboards.

### Diretorios reorganizados

- `school-management-web/host/src/app/academic` para `school-management-web/host/src/app/catalogo`
- `school-management-web/host/src/app/auth` para `school-management-web/host/src/app/seguranca`
- `school-management-web/host/src/app/enrollment` para `school-management-web/host/src/app/matricula`
- `school-management-web/host/src/app/responsibles` para `school-management-web/host/src/app/responsavel`
- `school-management-web/host/src/app/student-records` para `school-management-web/host/src/app/historico`
- `school-management-web/host/src/app/students` para `school-management-web/host/src/app/aluno`
- `school-management-web/host/src/app/shared/documents` para `school-management-web/host/src/app/documento`
- `school-management-web/host/src/app/shared` para `school-management-web/host/src/app/compartilhado`

### Escopo preservado

- Rotas publicas continuam com os mesmos caminhos atuais, como `students`, `responsibles`, `academic`, `enrollment` e `auth`.
- Subpastas tecnicas Angular continuam em ingles, como `models`, `pages`, `services`, `components`, `guards`, `directives` e `dialogs`.
- Nomes de componentes, classes, seletores, arquivos e contratos HTTP nao foram traduzidos nesta fase.
- `core`, `home`, `menu`, `models` e `services` globais foram mantidos na estrutura atual.

### Ajustes realizados

- Atualizados imports relativos entre os novos dominios.
- Atualizados lazy imports de `app.routes.ts` para apontar aos novos diretorios.
- Ajustados imports do modulo `documento`, que ficou um nivel mais proximo de `core` apos sair de `shared/documents`.

### Validacao

- `npm run build` executado em `school-management-web/host` com sucesso.
- `npm start -- --port 4300` foi testado, mas nao publicou a porta local antes de encerrar/interromper o processo de validacao. A saida parou no preparo do Native Federation sem erro explicito de compilacao.

### Riscos e proximos cuidados

- Antes de seguir para as telas da Fase 43, vale revisar o fluxo de `ng serve`/Native Federation separadamente, sem misturar com alteracoes de dashboard.
- Como as URLs publicas foram preservadas, menus e links existentes nao devem exigir migracao nesta fase.
- Renomeacao de classes e arquivos para portugues deve ser tratada como fase propria caso seja desejada, porque aumenta o volume de diffs sem mudar comportamento.
