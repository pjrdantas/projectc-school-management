# Fase 22 - Avaliacoes e lancamento de notas

## Objetivo

Completar o recorte MVP de execucao pedagogica iniciado na fase 21, adicionando avaliacoes e notas por aluno.

O recorte desta fase cobre:

- criacao de avaliacao a partir de uma alocacao professor/turma/disciplina;
- consulta de avaliacoes por alocacao ou turma;
- lancamento de nota por matricula;
- consulta de notas por avaliacao;
- consulta de notas por matricula.

## Regra implementada

Uma avaliacao pode ser criada quando:

- o `professorTurmaDisciplinaId` informado existe;
- o tipo de avaliacao existe no catalogo (`PROVA`, `TRABALHO`);
- `valorMaximo` e `peso` sao maiores que zero.

Uma nota pode ser lancada quando:

- a avaliacao existe;
- a matricula existe;
- a matricula pertence a mesma turma da avaliacao;
- a nota nao ultrapassa o valor maximo da avaliacao;
- ainda nao existe nota para a mesma matricula na avaliacao.

## Endpoints adicionados

- `POST /api/avaliacoes`
- `GET /api/avaliacoes`
- `GET /api/avaliacoes/{id}`
- `POST /api/avaliacoes/{id}/notas`
- `GET /api/avaliacoes/{id}/notas`
- `GET /api/matriculas/{matriculaId}/notas`

## Escopo realizado

- Criado controller REST de avaliacoes.
- Criado controller de consulta de notas por matricula.
- Criados DTOs de avaliacao e nota.
- Criado `AvaliacaoService` com regras de negocio.
- Ampliados repositories de avaliacao e nota para consultas por turma, avaliacao, matricula e duplicidade.
- Criadas excecoes de dominio para:
  - avaliacao nao encontrada;
  - tipo de avaliacao nao encontrado;
  - nota duplicada;
  - nota acima do valor maximo;
  - matricula de aluno em turma diferente da avaliacao.
- Adicionado tratamento HTTP para os novos erros.
- Criado teste de integracao do fluxo MVP.

## Arquivos principais

- `school-management-service/src/main/java/br/com/escola/avaliacao/adapter/in/web/AvaliacaoController.java`
- `school-management-service/src/main/java/br/com/escola/avaliacao/adapter/in/web/NotaAlunoController.java`
- `school-management-service/src/main/java/br/com/escola/avaliacao/application/service/AvaliacaoService.java`
- `school-management-service/src/main/java/br/com/escola/avaliacao/adapter/out/persistence/repository/AvaliacaoJpaRepository.java`
- `school-management-service/src/main/java/br/com/escola/avaliacao/adapter/out/persistence/repository/NotaAlunoJpaRepository.java`
- `school-management-service/src/test/java/br/com/escola/avaliacao/adapter/in/web/AvaliacaoControllerIntegrationTest.java`

## Resultado parcial dos testes

Comando focado:

```powershell
.\mvnw.cmd -Dtest=AvaliacaoControllerIntegrationTest test
```

Resultado:

- 2 testes executados.
- 0 falhas.
- 0 erros.

## Pendencias para fases futuras

- Criar edicao/correcao de notas com auditoria.
- Criar consulta consolidada de notas e frequencia por matricula.
- Integrar notas e frequencia ao boletim.
- Definir regras de fechamento de periodo avaliativo.
