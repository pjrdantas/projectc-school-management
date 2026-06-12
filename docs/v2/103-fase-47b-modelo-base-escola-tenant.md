# Fase 47B - Modelo base de escola/tenant

## Objetivo

Introduzir o modelo mínimo de escola como tenant funcional sem aplicar ainda
isolamento completo nas consultas dos domínios escolares.

## Escopo implementado

- Criada entidade institucional `Escola`, mapeada para a tabela `escola`.
- Reaproveitada a tabela `escola` como fronteira de tenant/unidade escolar.
- Mantida compatibilidade com o fluxo de transferência, que já referenciava
  escola de origem pela mesma tabela.
- Criada escola padrão para dados existentes de escola única:
  - `00000000-0000-0000-0000-000000000047`;
  - nome `Escola padrão`.
- Criado serviço interno para resolver a escola ativa do usuário.
- Adicionado vínculo mínimo entre `usuario` e `escola`.
- Adicionado registro de escola ativa em `sessao_autenticacao`.
- Retornado `escolaId` e `escolaNome` no contrato de autenticação.
- Ajustado o cadastro de usuários para aceitar e retornar escola vinculada.
- Adicionada migration para regularizar `escola`, `usuario.id_escola` e
  `sessao_autenticacao.id_escola`.

## Decisões técnicas

- A escola padrão é criada pela migration em ambientes versionados.
- Em testes e ambientes com criação automática de schema, a escola padrão é
  criada sob demanda pelo backend.
- Usuários sem escola explícita são resolvidos para a escola padrão.
- A sessão guarda a escola ativa para preparar a futura troca de contexto e o
  futuro escopo por escola.
- `perfil`, `permissao` e demais catálogos globais continuam fora do escopo da
  fase.

## Fora do escopo

- Filtros obrigatórios de escola em todos os domínios.
- Troca de escola pelo usuário.
- Usuário multi-escola.
- Isolamento completo de dados.
- Separação de BFF, serviços, banco ou microfrontends.
- Kafka, MongoDB ou Redis.

## Validação esperada

Backend:

- `.\mvnw.cmd test` em `school-management-service`.

Frontend:

- Não houve alteração de frontend nesta fase.

## Próxima fase recomendada

Fase 47C - Aplicação gradual do escopo por escola, iniciando por subfase pequena
em catálogos acadêmicos:

- `periodo_letivo`;
- `serie`;
- `disciplina`;
- `turma`;
- `turma_disciplina`.

A próxima fase deve aplicar escopo por escola de forma incremental, sem tentar
isolar todos os domínios de uma vez.
