# Fase 18 - Transicao automatica de status da matricula por documentos

## Objetivo

Adicionar transicao automatica de status da matricula quando todos os documentos obrigatorios configurados para seu tipo forem entregues.

## Regra implementada

Ao registrar um documento entregue em `POST /api/matriculas/{id}/documentos-entregues`:

- o sistema busca os documentos obrigatorios configurados para o tipo da matricula;
- se nao houver documentos obrigatorios configurados, nenhuma transicao automatica ocorre;
- se todos os documentos obrigatorios estiverem entregues;
- e se a matricula estiver em `AGUARDANDO_DOCUMENTOS`;
- o status da matricula e alterado automaticamente para `EM_ANDAMENTO`.

Esta fase nao efetiva matricula automaticamente. `EFETIVADA` continua dependendo de acao explicita no fluxo de matricula.

## Escopo realizado

- Estendido `MatriculaFluxoService` para reavaliar completude documental apos registrar documento entregue.
- Adicionado uso de `StatusMatriculaJpaRepository` no fluxo documental.
- Ampliado teste de integracao de matricula para validar:
  - matricula em `AGUARDANDO_DOCUMENTOS`;
  - entrega de documento obrigatorio;
  - documento exigido marcado como entregue;
  - status da matricula alterado para `EM_ANDAMENTO`.

## Arquivos principais

- `school-management-service/src/main/java/br/com/escola/matricula/application/service/MatriculaFluxoService.java`
- `school-management-service/src/test/java/br/com/escola/matricula/adapter/in/web/MatriculaControllerIntegrationTest.java`

## Resultado parcial dos testes

Comando focado:

```powershell
.\mvnw.cmd -Dtest=MatriculaControllerIntegrationTest test
```

Resultado:

- 4 testes executados.
- 0 falhas.
- 0 erros.

## Pendencias para fases futuras

- Definir se a conclusao de todas as etapas tambem deve influenciar transicao de status.
- Definir regra funcional de rematricula.
