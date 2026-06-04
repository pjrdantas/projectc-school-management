# Fase 15 - Matricula etapas e documentos entregues

## Objetivo

Evoluir o fluxo de matricula para operar etapas da matricula e documentos entregues, reaproveitando as entidades transacionais ja mapeadas.

## Escopo realizado

- Adicionado servico `MatriculaFluxoService`.
- Expostos endpoints para etapas da matricula:
  - `GET /api/matriculas/{id}/etapas`
  - `PATCH /api/matriculas/{id}/etapas/{etapaId}/status`
- Expostos endpoints para documentos entregues:
  - `GET /api/matriculas/{id}/documentos-entregues`
  - `POST /api/matriculas/{id}/documentos-entregues`
- O documento entregue referencia um `DocumentoEntity` ja existente, preservando o modulo de documentos como responsavel por metadados/upload.
- Ajustada a exclusao de matricula para remover documentos entregues antes da remocao da matricula.
- Adicionadas excecoes especificas para etapa e documento nao encontrados no contexto da matricula.
- Ampliado o teste de integracao de matricula para validar:
  - listagem de etapas;
  - conclusao de etapa;
  - registro de documento entregue;
  - listagem de documentos entregues.

## Arquivos principais

- `school-management-service/src/main/java/br/com/escola/matricula/application/service/MatriculaFluxoService.java`
- `school-management-service/src/main/java/br/com/escola/matricula/adapter/in/web/MatriculaController.java`
- `school-management-service/src/main/java/br/com/escola/matricula/adapter/in/web/MatriculaEtapaStatusRequest.java`
- `school-management-service/src/main/java/br/com/escola/matricula/adapter/in/web/MatriculaDocumentoEntregueRequest.java`
- `school-management-service/src/main/java/br/com/escola/matricula/adapter/in/web/MatriculaDocumentoEntregueResponse.java`
- `school-management-service/src/test/java/br/com/escola/matricula/adapter/in/web/MatriculaControllerIntegrationTest.java`

## Resultado parcial dos testes

Comando focado:

```powershell
.\mvnw.cmd -Dtest=MatriculaControllerIntegrationTest test
```

Resultado:

- 3 testes executados.
- 0 falhas.
- 0 erros.

## Pendencias para fases futuras

- Expor documentos exigidos por tipo de matricula.
- Definir regra funcional para rematricula.
- Avaliar transicao automatica do status da matricula a partir da conclusao das etapas/documentos obrigatorios.
