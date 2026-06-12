# Fase 46E - Polimento funcional e contratos de integração do planejamento com IA

## Objetivo

Estabilizar o fluxo funcional entre planejamento bimestral, conteúdo gerado por IA, versões aprovadas e biblioteca pedagógica, deixando explícitos os contratos para uma futura integração real com provedor de IA.

## Escopo implementado

- O diálogo de nova versão de conteúdo IA passa a abrir preenchido com o conteúdo atual, reduzindo retrabalho do professor.
- Conteúdos IA aprovados podem ser aplicados diretamente como `conteudoFinalAprovado` do planejamento bimestral.
- A ação de aplicar conteúdo ao planejamento fica restrita a conteúdo aprovado pelo professor.
- O reaproveitamento de conteúdo da biblioteca continua atualizando o conteúdo final aprovado do planejamento sem nova chamada de IA.

## Contrato funcional atual

### Geração de conteúdo

Endpoint atual:

- `POST /api/planejamentos-bimestrais/{planejamentoId}/ia/conteudos`

Entrada:

- `promptProfessor`: texto livre obrigatório.
- `tipoConteudo`: código de tipo de conteúdo pedagógico.
- `titulo`: título opcional.
- `reutilizavel`: indica se o conteúdo pode compor biblioteca.

Saída funcional:

- conteúdo gerado;
- vínculo com planejamento e interação;
- status inicial `GERADO`;
- versão inicial `1`;
- hash do conteúdo;
- metadados do modelo usado.

### Versionamento

Endpoint atual:

- `POST /api/ia/conteudos/{conteudoId}/versoes`

Entrada:

- `conteudo`: conteúdo revisado obrigatório.
- `motivoAlteracao`: justificativa opcional.

Regra:

- nova versão coloca o conteúdo em `EM_EDICAO`;
- a versão aprovada ainda precisa ser escolhida explicitamente.

### Aprovação

Endpoint atual:

- `PATCH /api/ia/conteudos/{conteudoId}/aprovar-versao`

Entrada:

- `numeroVersao`: versão a aprovar.
- `publicarBiblioteca`: quando verdadeiro, publica a versão aprovada na biblioteca.

Regra:

- aprovação copia a versão para o conteúdo principal;
- recalcula hash;
- marca `aprovadoPeloProfessor=true`;
- status passa para `APROVADO`.

### Biblioteca pedagógica

Endpoint atual:

- `GET /api/biblioteca-conteudos-pedagogicos`

Filtros:

- `professorId`;
- `disciplinaId`;
- `tipoConteudo`;
- `tema`.

Regra:

- publicação exige conteúdo aprovado;
- reutilização no planejamento não dispara nova geração de IA.

## Contrato de integração futura com provedor real

O ponto de extensão técnico permanece em `GeradorConteudoPedagogicoGateway`.

Implementações futuras devem preservar:

- entrada já normalizada pelo comando de geração;
- retorno com `modelo`, `conteudo`, `tokensEntrada` e `tokensSaida`;
- persistência da interação antes do conteúdo gerado;
- fluxo de revisão e aprovação pelo professor;
- ausência de publicação automática sem aprovação.

Campos esperados para evolução futura:

- identificador de provedor;
- identificador externo da requisição;
- status técnico da chamada;
- motivo de falha técnica;
- custo estimado real;
- política de rate limit;
- contexto de escola quando a preparação multi-escola for iniciada.

## Fora do escopo

- Integração real com provedor de IA.
- Streaming de geração.
- Editor rico.
- Auditoria avançada de prompt/resposta.
- Separação em BFF, serviço independente, Kafka, MongoDB ou Redis.
- Regras finais de isolamento por escola.

## Validação

- Microfrontend:
  - `npm run build`
  - Resultado: sucesso.

## Próxima fase recomendada

Fase 47A - Diagnóstico multi-escola:

- Mapear entidades que devem ser escopadas por escola.
- Separar dados globais de dados por escola.
- Preparar o desenho antes de qualquer mudança estrutural no banco ou serviços.
