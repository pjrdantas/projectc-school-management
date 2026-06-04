# Fase 9 - Revisao de duplicidades e responsabilidades

Data: 2026-06-03

## Objetivo

Revisar os pontos conceituais levantados apos a criacao da base estrutural JPA, separando:

- duplicidade real de entidade/tabela;
- apenas sobreposicao conceitual entre dominios;
- refatoracao recomendada para fase futura;
- pontos que nao devem ser alterados sem ampliar escopo de endpoints, services e DTOs.

## Resultado da revisao

Nao foi encontrada duplicidade fisica relevante para:

- `FuncionarioEntity`
- `NotaAlunoEntity`
- `FrequenciaAlunoEntity`

Essas entidades estao concentradas nos dominios mais adequados:

- `FuncionarioEntity` em `rh`
- `NotaAlunoEntity` em `avaliacao`
- `FrequenciaAlunoEntity` em `frequencia`

Os demais dominios podem consumir esses dados por relacionamento, consulta ou DTO, mas nao precisam duplicar a entidade fisica.

## Pontos revisados

### Funcionario

Tabela:

- `funcionario`

Entidade atual:

- `br.com.escola.rh.adapter.out.persistence.entity.FuncionarioEntity`

Decisao:

- Manter em `rh`.

Motivo:

- A tabela representa vinculo funcional da pessoa com a instituicao.
- O dominio `professor` pode referenciar professor como papel pedagogico, mas nao precisa duplicar funcionario.

Recomendacao futura:

- Quando houver caso de uso de professor/funcionario, manter o vinculo entre `ProfessorEntity`, `FuncionarioEntity` e `PessoaEntity` claro no service ou DTO, sem criar outra entidade para a mesma tabela.

### Nota do aluno

Tabela:

- `nota_aluno`

Entidade atual:

- `br.com.escola.avaliacao.adapter.out.persistence.entity.NotaAlunoEntity`

Decisao:

- Manter em `avaliacao`.

Motivo:

- Nota e resultado de uma avaliacao.
- O aluno e a matricula sao participantes do lancamento, mas a entidade fisica pertence ao processo avaliativo.

Recomendacao futura:

- Expor notas em telas de aluno por consulta ou DTO de leitura, nao por duplicacao da entidade no dominio `aluno`.

### Frequencia do aluno

Tabela:

- `frequencia_aluno`

Entidade atual:

- `br.com.escola.frequencia.adapter.out.persistence.entity.FrequenciaAlunoEntity`

Decisao:

- Manter em `frequencia`.

Motivo:

- Frequencia e um processo proprio de presenca/ausencia.
- Aluno e matricula devem ser referencias, nao donos da entidade fisica.

Recomendacao futura:

- Historicos de presenca por aluno devem ser DTOs ou views de leitura sobre o dominio `frequencia`.

### Escola e EscolaOrigem

Tabela atual mapeada:

- `escola`

Entidade atual:

- `br.com.escola.transferencia.adapter.out.persistence.entity.EscolaOrigemEntity`

Situacao:

- A tabela `escola` esta mapeada em `transferencia` com o nome `EscolaOrigemEntity`.
- Conceitualmente, `escola` pertence ao dominio `catalogo`.
- O service de transferencia usa a entidade atual para cadastrar/resolver escola de origem.

Decisao desta fase:

- Nao mover agora.

Motivo:

- Diferente de `DisciplinaEntity`, aqui ha fluxo de transferencia, controller, DTOs e service acoplados ao conceito de escola de origem.
- Mover sem redesenhar o caso de uso pode confundir a diferenca entre escola cadastrada no catalogo e escola de origem usada numa transferencia.

Recomendacao futura:

- Criar uma fase propria para separar:
  - `catalogo.EscolaEntity` como cadastro institucional da tabela `escola`;
  - contratos de transferencia que tratem "escola de origem" como papel/uso da escola, e nao como uma entidade fisica separada.

Possivel caminho:

1. Criar `br.com.escola.catalogo.adapter.out.persistence.entity.EscolaEntity`.
2. Criar `EscolaJpaRepository` em `catalogo`.
3. Ajustar `TransferenciaAlunoEntity` para referenciar `EscolaEntity`.
4. Preservar DTOs `EscolaOrigemRequest` e `EscolaOrigemResponse` como linguagem do endpoint.
5. Remover `EscolaOrigemEntity` apenas quando o fluxo estiver testado.

### HistoricoEscolar e HistoricoEscolarItem

Tabelas:

- `historico_escolar`
- `historico_escolar_item`

Entidades atuais:

- `HistoricoEscolar`
- `HistoricoEscolarItem`

Situacao:

- As classes nao seguem o sufixo `Entity`.
- Ainda assim, estao corretamente anotadas com `@Entity` e mapeiam as tabelas esperadas.
- Existem repositories, mapper, service e controller usando esses nomes.

Decisao desta fase:

- Nao renomear agora.

Motivo:

- Renomear para `HistoricoEscolarEntity` e `HistoricoEscolarItemEntity` e uma padronizacao valida, mas nao corrige bug funcional neste momento.
- A alteracao atravessaria repository, mapper, service, controller e testes.

Recomendacao futura:

- Renomear apenas em fase de padronizacao de historico, junto com revisao do contrato de historico escolar.

## Decisao consolidada

| Tema | Decisao |
| --- | --- |
| `FuncionarioEntity` | Manter em `rh` |
| `NotaAlunoEntity` | Manter em `avaliacao` |
| `FrequenciaAlunoEntity` | Manter em `frequencia` |
| `DisciplinaEntity` | Ja consolidada em `catalogo` na Fase 7 |
| `EscolaOrigemEntity` | Manter por enquanto; refatorar em fase propria |
| `HistoricoEscolar` e `HistoricoEscolarItem` | Manter por enquanto; renomear apenas em fase propria |
| Flyway | Nao mapear como dominio |

## Proxima fase recomendada

Escolher um fluxo MVP para implementar de ponta a ponta.

Ordem recomendada:

1. Catalogo academico.
2. Aluno e responsavel.
3. Matricula.
4. Professor, turma e disciplina.
5. Frequencia e avaliacao.
6. Dashboard.
