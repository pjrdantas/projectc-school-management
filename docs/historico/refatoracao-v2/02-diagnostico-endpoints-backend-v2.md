# Diagnostico de endpoints backend v2

Data: 2026-05-26

## Objetivo

Estabilizar o contrato minimo do backend v2 antes da reforma do frontend.

A base oficial `gestao_escolar` nao deve ser alterada. A correcao deve acontecer no backend, respeitando a modelagem definitiva.

## Problema encontrado

Durante a validacao real, varios endpoints de listagem retornavam `404` quando a tabela estava vazia:

- `GET /api/alunos`;
- `GET /api/responsaveis`;
- `GET /api/periodos-letivos`;
- `GET /api/series`;
- `GET /api/turmas`.

Esse comportamento bloqueia telas iniciais do frontend, porque lista vazia e recurso inexistente sao cenarios diferentes.

## Contrato adotado

- Listagens sem registros devem retornar `200` com `[]`.
- Busca por id inexistente deve continuar retornando `404`.
- Erros de regra de negocio continuam usando o status apropriado.

## Correcao aplicada

Foram removidas as excecoes `ResponseStatusException(HttpStatus.NOT_FOUND, ...)` das listagens vazias nos controllers:

- `AlunoController`;
- `ResponsavelController`;
- `PeriodoLetivoController`;
- `SerieController`;
- `TurmaController`.

Nenhuma tabela, coluna, constraint ou migration foi alterada.

## Validacao automatizada

Comando executado:

```powershell
.\mvnw.cmd test
```

Resultado:

```text
Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Validacao real contra PostgreSQL

Backend iniciado com:

```powershell
$env:SPRING_PROFILES_ACTIVE='local'
$env:SERVER_PORT='18080'
.\mvnw.cmd spring-boot:run
```

Base usada:

```text
jdbc:postgresql://localhost:5432/gestao_escolar
```

Resultado das chamadas:

| Metodo | Endpoint | Status | Registros |
| --- | --- | ---: | ---: |
| POST | `/api/auth/login` | 200 | - |
| GET | `/api/usuarios` | 200 | 2 |
| GET | `/api/perfis` | 200 | 4 |
| GET | `/api/permissoes` | 200 | 6 |
| GET | `/api/alunos` | 200 | 0 |
| GET | `/api/responsaveis` | 200 | 0 |
| GET | `/api/periodos-letivos` | 200 | 0 |
| GET | `/api/series` | 200 | 0 |
| GET | `/api/turmas` | 200 | 0 |
| GET | `/api/matriculas` | 200 | 0 |
| GET | `/api/pessoas/catalogos/tipos-pessoa` | 200 | 4 |
| GET | `/api/academico/catalogos/niveis-ensino` | 200 | 3 |
| GET | `/api/matriculas/catalogos/tipos` | 200 | 4 |

## Estado final

- Backend compila e passa na suite atual.
- Endpoints principais deixam o frontend carregar telas vazias.
- Dados artificiais de diagnostico foram conferidos/removidos.
- Schema oficial permanece imutavel.

## Complemento: student records

Validacao adicional executada em 2026-05-26 contra `jdbc:postgresql://localhost:5432/gestao_escolar`.

Correcoes aplicadas no backend:

- `disciplina` passou a usar a coluna oficial `ativo` no lugar do campo legado `status`.
- `historico_escolar` e `historico_escolar_item` passaram a usar os IDs oficiais `id_historico_escolar` e `id_historico_escolar_item`.
- Transferencias passaram a usar a tabela oficial `escola` no relacionamento de origem e os catalogos `tipo_transferencia` e `status_transferencia`.
- Documentos de aluno passaram a usar `documento` + `pessoa_documento`, removendo a dependencia da tabela legada `documento_aluno`.
- O handler global passou a registrar stack trace de erro interno para impedir 500 silencioso durante a reforma.

Validacao automatizada:

```text
Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Validacao real:

| Metodo | Endpoint | Status |
| --- | --- | ---: |
| GET | `/api/disciplinas` | 200 |
| GET | `/api/historicos-escolares` | 200 |
| POST | `/api/alunos` | 201 |
| GET | `/api/transferencias/alunos/{idAluno}` | 200 |
| GET | `/api/documentos-alunos/alunos/{idAluno}` | 200 |
| POST | `/api/documentos-alunos` | 201 |
| GET | `/api/documentos-alunos/{idDocumento}` | 200 |
| DELETE | `/api/documentos-alunos/{idDocumento}` | 204 |
| POST | `/api/escolas-origem` | 201 |
| POST | `/api/transferencias` | 201 |
| DELETE | `/api/alunos/{idAluno}` | 204 |
