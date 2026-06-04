# Fase 12 - Catalogo academico MVP

Data: 2026-06-03

## Objetivo

Avancar do mapeamento JPA para o primeiro fluxo MVP funcional: catalogo academico.

O criterio desta fase foi permitir criar uma turma completa com disciplinas vinculadas, preservando os endpoints ja existentes de periodo letivo, serie, turno e turma.

## Entregas

### Disciplina movida para o fluxo de catalogo

O endpoint `/api/disciplinas` foi preservado, mas sua implementacao passou a ficar no dominio `catalogo`.

Arquivos criados:

- `catalogo.adapter.in.web.controller.DisciplinaController`
- `catalogo.adapter.in.web.dto.DisciplinaRequest`
- `catalogo.adapter.in.web.dto.DisciplinaResponse`
- `catalogo.application.mapper.DisciplinaMapper`
- `catalogo.application.service.DisciplinaService`
- `catalogo.domain.exception.DisciplinaNaoEncontradaException`

Arquivos antigos removidos do dominio `historico`:

- `historico.adapter.in.web.DisciplinaController`
- `historico.adapter.in.web.dto.DisciplinaRequest`
- `historico.adapter.in.web.dto.DisciplinaResponse`
- `historico.application.service.DisciplinaService`
- `historico.application.mapper.DisciplinaMapper`
- `historico.domain.exception.DisciplinaNaoEncontradaException`

### Vinculo turma-disciplina

Foi criado o endpoint:

```text
POST /api/turmas/{turmaId}/disciplinas
GET  /api/turmas/{turmaId}/disciplinas
```

Arquivos criados:

- `catalogo.adapter.in.web.controller.TurmaDisciplinaController`
- `catalogo.adapter.in.web.dto.TurmaDisciplinaRequest`
- `catalogo.adapter.in.web.dto.TurmaDisciplinaResponse`
- `catalogo.application.service.TurmaDisciplinaService`
- `catalogo.domain.exception.TurmaDisciplinaJaCadastradaException`

Tambem foi adicionada consulta no repository:

- `TurmaDisciplinaJpaRepository.findByTurmaIdAndDisciplinaId`

## Tratamento de erros

O handler de catalogo passou a tratar:

- `DisciplinaNaoEncontradaException`
- `TurmaDisciplinaJaCadastradaException`

O handler de historico deixou de tratar disciplina, ficando restrito ao historico escolar.

## Testes adicionados

Foram adicionados cenarios ao teste de catalogo:

- cadastrar e consultar disciplina;
- vincular disciplina a turma;
- listar disciplinas vinculadas a turma.

Arquivo alterado:

- `AcademicCatalogControllerIntegrationTest`

## Validacao executada

Execucao focada:

```powershell
.\mvnw.cmd -Dtest=AcademicCatalogControllerIntegrationTest test
```

Resultado:

- Testes: 12
- Falhas: 0
- Erros: 0
- Build: sucesso

Execucao completa:

```powershell
.\mvnw.cmd test
```

Resultado:

- Testes: 36
- Falhas: 0
- Erros: 0
- Build: sucesso

## Resultado da fase

O catalogo academico agora cobre o fluxo minimo:

1. Criar periodo letivo.
2. Criar/consultar serie.
3. Criar/consultar turno.
4. Criar turma.
5. Criar disciplina.
6. Vincular disciplina a turma.
7. Listar disciplinas de uma turma.

## Proxima fase recomendada

Iniciar o fluxo `aluno + responsavel`, com foco em:

- cadastro de aluno com pessoa;
- cadastro de responsavel com pessoa;
- vinculo aluno-responsavel;
- consulta da ficha consolidada do aluno.
