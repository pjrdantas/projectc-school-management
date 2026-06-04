# Fase 17 - API admin de documentos exigidos da matricula

## Objetivo

Criar API administrativa para configurar documentos exigidos por tipo de matricula.

## Escopo realizado

- Criado endpoint administrativo em:
  - `GET /api/matriculas/catalogos/documentos-exigidos?tipoMatriculaId={id}`
  - `POST /api/matriculas/catalogos/documentos-exigidos`
  - `PUT /api/matriculas/catalogos/documentos-exigidos/{id}`
  - `DELETE /api/matriculas/catalogos/documentos-exigidos/{id}`
- Criado `MatriculaDocumentoExigidoAdminController`.
- Criado `MatriculaDocumentoExigidoAdminService`.
- Criado `MatriculaDocumentoExigidoRequest`.
- Criado `TipoDocumentoJpaRepository` para lookup do catalogo de tipos de documento.
- Adicionadas regras de validacao:
  - tipo de matricula deve existir;
  - tipo de documento deve existir;
  - nao permite duplicidade por tipo de matricula + tipo de documento.
- Adicionadas excecoes especificas para:
  - documento exigido nao encontrado;
  - documento exigido duplicado;
  - tipo de matricula nao encontrado;
  - tipo de documento nao encontrado.
- Ajustado tratamento global de erros para retornar `404` e `409` nos novos cenarios.
- Ampliado teste de integracao de matricula para validar criar, listar, atualizar, duplicidade e excluir configuracao.

## Arquivos principais

- `school-management-service/src/main/java/br/com/escola/matricula/adapter/in/web/MatriculaDocumentoExigidoAdminController.java`
- `school-management-service/src/main/java/br/com/escola/matricula/application/service/MatriculaDocumentoExigidoAdminService.java`
- `school-management-service/src/main/java/br/com/escola/matricula/adapter/in/web/MatriculaDocumentoExigidoRequest.java`
- `school-management-service/src/main/java/br/com/escola/documento/adapter/out/persistence/repository/TipoDocumentoJpaRepository.java`
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

- Definir transicao automatica de status da matricula quando todos os documentos obrigatorios forem entregues.
- Definir regra funcional de rematricula.
