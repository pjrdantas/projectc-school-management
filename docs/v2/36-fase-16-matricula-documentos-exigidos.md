# Fase 16 - Matricula documentos exigidos

## Objetivo

Expor a leitura de documentos exigidos por matricula, usando a tabela transacional/catalogo `matricula_documento_exigido`.

## Escopo realizado

- Adicionado endpoint:
  - `GET /api/matriculas/{id}/documentos-exigidos`
- A resposta retorna os documentos exigidos para o tipo da matricula.
- A resposta indica se cada documento exigido ja foi entregue na matricula.
- A entrega e identificada por compatibilidade entre o tipo do documento exigido e o tipo do documento entregue.
- Adicionado DTO `MatriculaDocumentoExigidoResponse`.
- Estendido `MatriculaFluxoService` para montar a visao consolidada exigido/entregue.
- Ajustado `TipoDocumentoEntity` para expor getters usados na resposta.
- Ajustado repositorio de documentos exigidos para consulta por tipo de matricula ordenada por `ordem`.
- Ampliado teste de integracao de matricula para validar:
  - exigido ainda nao entregue;
  - registro de documento entregue;
  - exigido marcado como entregue apos o registro.

## Arquivos principais

- `school-management-service/src/main/java/br/com/escola/matricula/adapter/in/web/MatriculaDocumentoExigidoResponse.java`
- `school-management-service/src/main/java/br/com/escola/matricula/adapter/in/web/MatriculaController.java`
- `school-management-service/src/main/java/br/com/escola/matricula/application/service/MatriculaFluxoService.java`
- `school-management-service/src/main/java/br/com/escola/matricula/adapter/out/persistence/repository/MatriculaDocumentoExigidoJpaRepository.java`
- `school-management-service/src/main/java/br/com/escola/documento/adapter/out/persistence/entity/TipoDocumentoEntity.java`
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

- Criar API administrativa para configurar documentos exigidos por tipo de matricula.
- Definir regra de transicao automatica de status quando todos os documentos obrigatorios forem entregues.
- Definir regra funcional de rematricula.
