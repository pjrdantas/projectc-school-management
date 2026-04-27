# Roteiro para Próximo Chat — Continuidade do Projeto Escolar (KAN-3)

## 1) Objetivo deste documento
Este roteiro serve como **handoff** para iniciar um novo chat com todo o contexto necessário, sem perda de histórico de decisão.

---

## 2) Estado atual consolidado

### Concluído recentemente
- História **KAN-7** finalizada com fechamento dos cards **KAN-33, KAN-34 e KAN-15**.
- Fluxo de aluno está funcional com integração backend/frontend e testes de endpoint validados manualmente (Bruno) e por integração.

### Contexto de roadmap
- **NÃO iniciar agora**: `KAN-6 H1 — Autenticação básica`.
- A autenticação ficará para depois de finalizar frentes pendentes de:
  - **KAN-2 — Cadastro Acadêmico**
  - **KAN-4 — Gestão de Matrícula**
  - **KAN-5 — Portal Operacional e Consultas**

### Próximas histórias definidas para execução imediata
1. **KAN-3 H2 — Cadastro de Responsável**
2. **KAN-3 H3 — Vínculo Aluno-Responsável**
3. **KAN-3 H4 — Consulta Cadastral Básica (aluno + responsáveis vinculados)**

---

## 3) Diagnóstico técnico atual (backend)

A API já cobre os domínios de aluno, matrícula e catálogo acadêmico.

- Aluno com endpoints de criação, listagem, busca por id, atualização e exclusão.
- Matrícula com criação e consulta com filtros.
- Turma e período letivo com criação e consulta por id.

Ponto principal de gap para o épico KAN-3:
- Ainda não existe módulo de **Responsável** e nem relação **Aluno-Responsável** no banco.

---

## 4) Escopo de execução por história

## KAN-3 H2 — Cadastro de Responsável

### Objetivo
Permitir cadastro e manutenção básica de responsáveis para formar a base cadastral mínima de contato familiar.

### Critérios de aceite sugeridos
- Cadastrar responsável com campos obrigatórios.
- Bloquear CPF duplicado.
- Retornar mensagens claras para dados inválidos.
- Consultar responsável por ID.
- Listar responsáveis com filtro básico (nome/cpf).

### Subtasks sugeridas
1. **[BE] Criar domínio e casos de uso de Responsável**
   - Implementar DTOs, ports e use cases de create/get/list/update/delete.
2. **[BE] Criar migration e persistência de Responsável**
   - Tabela `responsavel` com `cpf` único e campos mínimos.
3. **[BE] Expor API REST de Responsável**
   - Endpoints CRUD com validações e códigos HTTP adequados.
4. **[BE] Exceções e handlers do domínio Responsável**
   - Conflito por CPF e not found com payload padrão de erro.
5. **[QA] Testes de integração de Responsável**
   - Cenários de sucesso, conflito, validação e não encontrado.
6. **[FE] Tela e serviço de Responsável**
   - Formulário, listagem e feedbacks de sucesso/erro.

---

## KAN-3 H3 — Vínculo Aluno-Responsável

### Objetivo
Associar um ou mais responsáveis a um aluno para suporte operacional e consistência cadastral.

### Critérios de aceite sugeridos
- Vincular responsável a aluno existente.
- Impedir vínculo duplicado entre mesmo par aluno-responsável.
- Exibir erro claro quando aluno/responsável não existir.
- Permitir listar e remover vínculos por aluno.

### Subtasks sugeridas
1. **[BE] Migration da tabela de vínculo**
   - Tabela `aluno_responsavel` com FKs e constraint única `(aluno_id, responsavel_id)`.
2. **[BE] Casos de uso de vínculo**
   - Vincular, desvincular, listar responsáveis por aluno.
3. **[BE] Endpoints REST de vínculo**
   - `POST/DELETE/GET` para gerenciamento de relação.
4. **[BE] Regras de negócio e exceções**
   - Duplicidade e inexistência de registros.
5. **[QA] Testes de integração do vínculo**
   - Fluxos felizes e cenários de erro.
6. **[FE] Gestão de vínculo na tela do aluno**
   - Componente para adicionar/remover e visualizar responsáveis vinculados.

---

## KAN-3 H4 — Consulta Cadastral Básica (aluno + responsáveis)

### Objetivo
Oferecer uma visão consolidada para consulta operacional do cadastro de aluno junto dos seus responsáveis vinculados.

### Critérios de aceite sugeridos
- Consultar aluno com responsáveis vinculados em resposta consolidada.
- Filtros básicos por nome/cpf (aluno e responsável).
- Tratamento claro de estados de vazio/erro.
- Resposta pronta para paginação quando volume crescer.

### Subtasks sugeridas
1. **[BE] Endpoint de consulta consolidada**
   - DTO agregador com aluno + coleção de responsáveis.
2. **[BE] Implementar query otimizada**
   - Evitar N+1 e preparar base para paginação.
3. **[BE] Documentar contratos de API**
   - Filtros e estrutura de payload de consulta.
4. **[QA] Testes de integração da consulta consolidada**
   - Com vínculo, sem vínculo e filtros.
5. **[FE] Tela de consulta cadastral básica**
   - Busca, visualização consolidada e estados de UX.

---

## 5) Ordem recomendada de implementação
1. KAN-3 H2
2. KAN-3 H3
3. KAN-3 H4
4. Depois retomar KAN-2, KAN-4 e KAN-5
5. Por fim KAN-6 H1 (autenticação)

---

## 6) Prompt base sugerido para iniciar o próximo chat

```text
Vamos continuar o projeto school-management.
Contexto:
- Fechamos KAN-7 (cards KAN-33, KAN-34, KAN-15).
- Agora vamos executar KAN-3 na ordem:
  1) KAN-3 H2 Cadastro de Responsável
  2) KAN-3 H3 Vínculo Aluno-Responsável
  3) KAN-3 H4 Consulta Cadastral Básica
- Não iniciar KAN-6 H1 (Autenticação) agora.

Objetivo do chat:
- Implementar KAN-3 H2 ponta a ponta (backend + testes + frontend básico),
  com migration, endpoints, validações e testes de integração.
- Ao final, listar comandos executados e evidências de teste.
```

---

## 7) Checklist de pronto por história (DoD enxuto)
- [ ] Migrations aplicadas e versionadas.
- [ ] Endpoints implementados com validações.
- [ ] Exceções de domínio mapeadas em erro de API.
- [ ] Testes de integração cobrindo sucesso e erros críticos.
- [ ] Frontend com feedback claro de sucesso/erro.
- [ ] Documentação mínima de contrato de API atualizada.

---

## 8) Observações finais
- Evitar iniciar autenticação antes de estabilizar os cadastros base do domínio.
- Priorizar consistência de regras de negócio e cobertura de integração no backend.
- Manter mensagens de erro em padrão uniforme para facilitar UX e suporte.
