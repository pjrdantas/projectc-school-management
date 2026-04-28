# 18 - Roteiro de Migração BIGINT para UUID (Banco -> Backend -> Frontend)

## 1. Objetivo
Migrar os identificadores principais do projeto de `BIGINT` para `UUID` de forma **segura**, **incremental** e **sem perda de dados**, seguindo a ordem:

1. Banco de dados (Flyway)
2. Backend Java (Spring Boot)
3. Frontend Angular

---

## 2. Contexto atual
Atualmente as tabelas centrais (`aluno`, `periodo_letivo`, `turma`, `matricula`) usam PK/FK `BIGINT`.

A migração para UUID visa:
- reduzir previsibilidade de IDs;
- facilitar integração distribuída;
- padronizar estratégia de identificação da aplicação.

---

## 3. Princípios da migração
- Não usar "drop e recriar" em produção.
- Executar com compatibilidade temporária entre colunas antigas e novas.
- Manter rollback operacional por etapa (quando possível).
- Validar build/testes ao fim de cada etapa.
- Versionar em commits separados por camada.

---

## 4. Etapa 1 — Banco de dados (Flyway)

## 4.1 Escopo
Aplicar migração UUID nas tabelas:
- `aluno`
- `periodo_letivo`
- `turma`
- `matricula`

## 4.2 Estratégia recomendada
1. Habilitar extensão UUID no PostgreSQL (`pgcrypto`) caso necessário.
2. Adicionar colunas UUID novas (`id_uuid`, `*_id_uuid`) mantendo BIGINT original.
3. Popular UUID para registros existentes.
4. Preencher FKs UUID via `UPDATE ... FROM` com join entre tabelas.
5. Criar PK/FK novas em UUID.
6. Renomear UUID para nomes oficiais (`id`, `aluno_id`, etc.).
7. Manter BIGINT antigo como coluna legado temporária.
8. Em migration posterior, remover colunas BIGINT legadas.

## 4.3 Critérios de aceite (DB)
- Integridade referencial preservada.
- Dados existentes mantidos.
- Constraints e índices operando com UUID.
- Flyway executa em base limpa e base já populada.

---

## 5. Etapa 2 — Backend Java (Spring Boot)

## 5.1 Escopo
Trocar tipos de ID de `Long` para `UUID` em:
- entidades JPA;
- DTOs de entrada/saída;
- controllers (`@PathVariable UUID id`);
- use cases;
- ports;
- gateways/repositories (`JpaRepository<..., UUID>`).

## 5.2 Ajustes técnicos esperados
- Atualizar mapeamentos de entidades e relacionamentos.
- Ajustar conversões e serialização de UUID nos endpoints.
- Revisar validação/exceções para ID inválido.
- Atualizar testes de integração e testes unitários com UUID.

## 5.3 Critérios de aceite (Backend)
- Endpoints funcionam com UUID sem regressão funcional.
- Testes automatizados do backend passam.
- Persistência e leitura corretas com novas chaves UUID.

---

## 6. Etapa 3 — Frontend Angular

## 6.1 Escopo
- Garantir `id` como `string` nos models e serviços.
- Ajustar chamadas HTTP para IDs UUID.
- Validar rotas e telas de listagem/detalhe/edição/exclusão.

## 6.2 Ajustes técnicos esperados
- Revisar tipagem de models e formulários.
- Revisar componentes que comparam IDs.
- Garantir funcionamento de paginação, busca e navegação com UUID.

## 6.3 Critérios de aceite (Frontend)
- Fluxo CRUD de aluno opera com UUID.
- Build Angular sem erros.
- Navegação entre telas e ações via UUID funcionando.

---

## 7. Ordem de execução do próximo chat
1. Criar migration Flyway UUID completa (etapa banco).
2. Aplicar e validar schema/dados.
3. Refatorar backend para UUID.
4. Rodar testes backend.
5. Refatorar frontend para UUID.
6. Rodar build/testes frontend.
7. Entregar checklist final com evidências.

---

## 8. Prompt base para abrir o próximo chat
```text
Vamos executar a migração de IDs BIGINT para UUID no projeto school-management.

Ordem obrigatória:
1) Banco (Flyway)
2) Backend Java (Spring Boot)
3) Frontend Angular

Requisitos:
- Sem perda de dados
- Migração incremental e segura
- Commits separados por etapa
- Testes/build ao fim de cada etapa
- Resumo final com comandos executados e resultados
```

---

## 9. Checklist de conclusão
- [ ] Migration Flyway criada e aplicada em ambiente de teste
- [ ] PK/FK principais operando com UUID
- [ ] Backend convertido para UUID
- [ ] Testes backend aprovados
- [ ] Frontend ajustado para UUID
- [ ] Build/testes frontend aprovados
- [ ] Documento de rollback e riscos atualizado
