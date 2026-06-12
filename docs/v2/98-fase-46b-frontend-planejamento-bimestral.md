# Fase 46B - Frontend de planejamento bimestral

## Objetivo

Criar a tela operacional de planejamento bimestral no microfrontend academico
atual, consumindo a API entregue na Fase 46A.

## Escopo realizado

- Criada rota do microfrontend para planejamento bimestral.
- Criados exposes de federacao para lista e detalhe de planejamento.
- Criadas rotas federadas no host.
- Criado item de menu `Planejamento`.
- Criados models e service HTTP para:
  - listar planejamentos;
  - buscar planejamento por ID;
  - criar planejamento;
  - atualizar planejamento;
  - adicionar aulas previstas;
  - adicionar avaliacoes previstas;
  - alterar status.
- Criada lista operacional com filtros por:
  - professor;
  - turma;
  - disciplina;
  - periodo avaliativo quando retornado pela API;
  - status.
- Criado modal de cadastro/edicao de planejamento.
- Criada tela de detalhe do planejamento.
- Criados modais para adicionar:
  - aulas previstas;
  - avaliacoes previstas.
- Criado controle de alteracao de status no detalhe.

## Rotas adicionadas

No microfrontend:

- `/planning`
- `/planning/:id`

No host:

- `/planning`
- `/planning/:id`

## Decisao tecnica pontual

O backend aceita `periodoAvaliativoId`, mas o frontend atual ainda nao possui
endpoint de catalogo de periodos avaliativos. Para evitar expor identificador
tecnico em campo manual para o usuario:

- o cadastro inicial envia periodo avaliativo como nulo;
- a edicao preserva o periodo avaliativo existente;
- a lista filtra por periodo apenas quando esse dado ja vem retornado pela API.

Um catalogo operacional de periodos avaliativos deve ser tratado em fase
posterior, se necessario.

## Fora do escopo

- IA.
- Microfrontend proprio de planejamento/IA.
- Regras finais de acesso por professor logado.
- BFF.
- Separacao de servicos.
- Kafka, MongoDB ou Redis.

## Validacao

Comandos executados:

```powershell
npm run build
```

Em `school-management-web/microfrontend`:

- build concluido com sucesso.

Em `school-management-web/host`:

- build concluido com sucesso.

## Proxima fase

Fase 46C - Backend de IA e conteudo pedagogico.
