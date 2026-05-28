# Definition of Structure Done (DoSD)

## Objetivo

Definir um checklist objetivo para validar se um incremento estrutural do projeto respeita a base arquitetural e as convencoes do MVP.

## Quando usar

Aplicar este checklist em:

- PRs que criam ou alteram modulo backend;
- PRs que criam ou alteram feature frontend;
- PRs de fundacao tecnica (estrutura, padroes, convencoes);
- revisoes de inicio de sprint.

## Checklist minimo (PR de estrutura)

### 1. Ownership claro por contexto

- [ ] O artefato novo possui modulo/feature dono explicito.
- [ ] Nao ha sobreposicao de ownership entre modulos sem contrato.

### 2. Separacao por camada no backend

- [ ] `domain` contem regra de negocio e entidades do contexto.
- [ ] `application` contem casos de uso e orquestracao.
- [ ] `adapter.in` contem entrada HTTP (controllers/DTOs).
- [ ] `adapter.out` contem persistencia/integracoes concretas.
- [ ] `infrastructure` contem configuracao tecnica transversal.

### 3. Regra de fronteira entre camadas

- [ ] Controller nao concentra regra de negocio principal.
- [ ] Caso de uso nao depende de detalhe HTTP.
- [ ] Dominio nao depende de repository JPA concreto.

### 4. Convencoes de nomenclatura

- [ ] Base de pacote em `br.com.escola`.
- [ ] Pacotes em minusculo, sem hifen e sem underscore.
- [ ] Classes com nomes coerentes com papel da camada.

### 5. Convencoes de dados e migration

- [ ] Tabelas e colunas novas seguem `snake_case`.
- [ ] Migration nova usa versionamento incremental (`V00x__descricao.sql`).
- [ ] Migration ja aplicada nao foi alterada.

### 6. Coerencia com backlog do MVP

- [ ] A alteracao mapeia para historia/epico da sprint atual.
- [ ] O modulo/feature alterado bate com a correspondencia backlog x estrutura.

### 7. Critico de acoplamento

- [ ] Nao ha dependencia circular entre modulos.
- [ ] Nao ha uso indevido de `shared` como deposito generico.

### 8. Evidencias de validacao

- [ ] PR descreve claramente o que foi criado/alterado na estrutura.
- [ ] PR informa comandos de validacao executados localmente.

## Resultado esperado

Um PR estrutural e considerado aprovado quando todos os itens obrigatorios estao atendidos ou quando excecoes estao justificadas de forma explicita no proprio PR.
