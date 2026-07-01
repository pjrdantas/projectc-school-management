# Contratos de backend - Diário de Classe

Esta tela está preparada para ligar em um backend Java/Spring Boot.
Enquanto a API não existir, o arquivo `src/app/core/services/diario-classe-api.service.ts` usa mock local.
Para ligar no backend, altere `usarMock` para `false` e ajuste `apiBaseUrl`.

## 1. Carregar Diário de Classe

O frontend sempre solicita o diário do **mês corrente**, usando a data de referência do dia.

### Request

```http
GET /api/diarios-classe?idProfessor=prof-001&idTurma=turma-8a-2026&idDisciplina=disc-matematica&anoLetivo=2026&mes=6&dataReferencia=2026-06-26
```

### Response

```json
{
  "cabecalho": {
    "idDiarioClasse": "diario-2026-06-8a-matematica",
    "idEscola": "esc-001",
    "escola": "EMEF Professora Helena Duarte",
    "diretoriaEnsino": "Centro-Oeste",
    "municipio": "São Paulo",
    "anoLetivo": 2026,
    "mes": 6,
    "dataAtual": "2026-06-26",
    "idTurma": "turma-8a-2026",
    "turmaSerie": "8º A",
    "turno": "Manhã",
    "idDisciplina": "disc-matematica",
    "disciplina": "Matemática",
    "idProfessor": "prof-001",
    "professor": "Marina Albuquerque"
  },
  "alunos": [
    {
      "idAluno": "aluno-001",
      "numeroChamada": 1,
      "nome": "Ana Clara Santos",
      "frequencias": {
        "1": "P",
        "2": ".",
        "3": "F",
        "6": ""
      }
    }
  ],
  "conteudosPlanejados": [
    {
      "idPlanejamentoAula": "plan-001",
      "periodo": "01 a 05",
      "descricao": "Números Naturais e Operações"
    }
  ],
  "observacoes": [
    ""
  ],
  "avaliacoes": [
    {
      "idAvaliacao": "aval-001",
      "data": "10/06",
      "descricao": "Prova Mensal - Números Naturais",
      "turma": "8º A",
      "valor": "0 a 10"
    }
  ],
  "assinatura": {
    "nomeProfessor": "",
    "dataAssinatura": ""
  }
}
```

## 2. Salvar Diário de Classe

O frontend envia somente o lançamento do **dia corrente**, quando o dia corrente é dia útil. Dias anteriores, dias posteriores e finais de semana ficam bloqueados na tela.

### Request

```http
PUT /api/diarios-classe/{idDiarioClasse}
```

```json
{
  "idDiarioClasse": "diario-2026-06-8a-matematica",
  "dataLancamento": "2026-06-26",
  "frequencias": [
    {
      "idAluno": "aluno-001",
      "data": "2026-06-26",
      "dia": 26,
      "status": "P"
    },
    {
      "idAluno": "aluno-002",
      "data": "2026-06-26",
      "dia": 26,
      "status": "."
    },
    {
      "idAluno": "aluno-003",
      "data": "2026-06-26",
      "dia": 26,
      "status": "F"
    }
  ],
  "conteudos": [
    {
      "idPlanejamentoAula": "plan-001",
      "periodo": "01 a 05",
      "descricao": "Números Naturais e Operações",
      "alterado": false
    },
    {
      "idPlanejamentoAula": "plan-002",
      "periodo": "08 a 13",
      "descricao": "Frações: conceito, representação e exercícios",
      "alterado": true,
      "observacaoJustificativa": "A turma precisou de uma aula adicional para revisão."
    }
  ],
  "observacoes": [
    "A turma precisou de uma aula adicional para revisão."
  ],
  "assinatura": {
    "nomeProfessor": "Marina Albuquerque",
    "dataAssinatura": "26/06/2026"
  }
}
```

### Response

```json
{
  "idDiarioClasse": "diario-2026-06-8a-matematica",
  "status": "SALVO",
  "mensagem": "Diário de Classe salvo com sucesso.",
  "salvoEm": "2026-06-26T17:10:00"
}
```

## Regras implementadas no frontend

1. Cabeçalho vem preenchido pelo backend e fica somente leitura.
2. Data do diário é a data corrente.
3. O mês do diário obedece ao mês corrente.
4. Frequência vem carregada com todos os alunos da classe.
5. Os dias anteriores ao dia corrente ficam bloqueados.
6. Os dias posteriores ao dia corrente ficam bloqueados.
7. Finais de semana ficam bloqueados e podem ficar sem lançamento.
8. Somente o dia corrente fica liberado para lançamento, quando for dia útil.
9. A frequência aceita apenas `P`, `.` ou `F`.
10. `P` e `.` representam presença.
11. `F` representa falta e é destacada em vermelho.
12. Para salvar em dia útil, todos os alunos precisam ter lançamento no dia corrente.
13. Conteúdo vem pré-carregado pelo planejamento.
14. Se período ou descrição do conteúdo for alterado, uma observação/justificativa passa a ser obrigatória.
15. Avaliações vêm pré-carregadas e ficam somente leitura.
16. Assinatura do professor e data da assinatura são obrigatórias.
17. O botão `Salvar` só libera quando todas as regras forem atendidas.


## Ajustes de tela v23

- O cabeçalho visual da página mostra somente a data corrente; o mês e o ano continuam disponíveis nos campos do formulário e no PDF.
- O campo de observações mantém o foco durante a digitação, inclusive quando a validação de justificativa está ativa.
- A data da assinatura vem preenchida com a data corrente quando o backend não retornar uma data salva.
- O campo de data da assinatura usa `mat-datepicker`, permitindo digitação no padrão `dd/mm/aaaa` ou seleção pelo ícone de calendário.


## Bloqueio após salvar

Depois que o backend confirmar o salvamento do diário, a tela deve considerar o formulário bloqueado para novos lançamentos.

No mock, o `PUT /api/diarios-classe/{id}` retorna:

```json
{
  "status": "SALVO",
  "bloqueado": true
}
```

Quando `bloqueado` for `true`, a tela bloqueia frequência, conteúdo, observações, assinatura e data. Os botões de impressão/exportação permanecem disponíveis.

O carregamento inicial também aceita o campo opcional `bloqueado` no response do diário.
