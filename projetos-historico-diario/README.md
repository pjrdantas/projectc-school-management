# Projetos Angular Material - Diário de Classe e Histórico Escolar

Esta versão contém dois projetos Angular independentes:

- `diario-classe-material`
- `historico-escolar-material`

## Ajustes da versão v21

### Diário de Classe

- Preparado para conexão com backend Java/Spring Boot.
- Criados DTOs de request/response em `src/app/core/models/diario-classe-api.models.ts`.
- Criado service em `src/app/core/services/diario-classe-api.service.ts`.
- Criado mock local em `src/app/core/mock/diario-classe.mock.ts`.
- Cabeçalho vem preenchido e somente leitura.
- Data do diário vem como data corrente.
- Frequência carrega todos os alunos da classe.
- Dias futuros ficam bloqueados.
- O botão **Salvar** só libera quando:
  - todos os alunos tiverem `P` ou `F` até a data corrente;
  - o lançamento do dia atual estiver preenchido, quando aplicável;
  - alterações no planejamento tiverem justificativa em observações;
  - assinatura do professor e data da assinatura estiverem preenchidas.
- Conteúdo vem pré-carregado pelo planejamento.
- Avaliações vêm pré-carregadas e não podem ser alteradas.
- Contratos da API documentados em `diario-classe-material/CONTRATOS_BACKEND_DIARIO.md`.

### Histórico Escolar

- Não foi alterado nesta versão.

## Como executar

```bash
cd angular-material-projetos/diario-classe-material
npm install
npm start -- --port 4200
```

Em outro terminal:

```bash
cd angular-material-projetos/historico-escolar-material
npm install
npm start -- --port 4201
```

Depois acesse:

- Diário: http://localhost:4200
- Histórico: http://localhost:4201

## PDF

Os botões de PDF usam a impressão do navegador. Na janela de impressão, escolha **Salvar como PDF**.
