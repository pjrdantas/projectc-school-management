# Roteiro de Continuidade — Próximo Chat (Frontend + Backend)

## Objetivo deste documento

Registrar o ponto atual do projeto e padronizar o início do próximo chat, agora com **frontend Angular (microfrontend)** e **backend Java 21 (hexagonal)** no mesmo contexto de evolução.

---

## Status atual (até este chat)

### Frontend (`school-management-web`)

Fluxo de aluno já disponível com CRUD local (temporário):

- listagem de alunos;
- cadastro de aluno;
- consulta por aluno;
- edição de aluno;
- exclusão de aluno;
- validação de CPF;
- validação/formatação de data (`dd/MM/aaaa`);
- validação/formatação de telefone;
- validação de e-mail em padrão clássico.

**Observação importante:** no estado atual, os dados estão em `localStorage` (sem integração HTTP real).

### Backend

- Backend ainda será adicionado no novo repositório (Java 21, arquitetura hexagonal).
- Próxima etapa: implementar API real de aluno e integrar no frontend.

---

## Meta do próximo chat

Substituir o CRUD local por integração real com backend, mantendo a UX atual do frontend.

---

## Escopo sugerido para o próximo chat

## 1) Backend — módulo de Aluno

Implementar o CRUD completo com os seguintes casos de uso:

- `CriarAluno`
- `ListarAlunos`
- `BuscarAlunoPorId`
- `AtualizarAluno`
- `ExcluirAluno`

### Campos iniciais de Aluno

- `id`
- `nomeCompleto`
- `cpf`
- `email`
- `dataNascimento`
- `createdAt`

### Sobre telefone

- telefone permanece no frontend por enquanto;
- o banco atual ainda não contempla telefone;
- planejar extensão de schema/migration em etapa posterior.

## 2) API REST (contrato inicial)

Endpoints sugeridos:

- `POST /api/alunos`
- `GET /api/alunos`
- `GET /api/alunos/{id}`
- `PUT /api/alunos/{id}`
- `DELETE /api/alunos/{id}`

Regras importantes:

- CPF único;
- retornar erros semânticos consistentes (400, 404, 409);
- payload padronizado para facilitar consumo no Angular.

## 3) Frontend — integração com backend

Trocar persistência local por chamadas HTTP:

- criar `StudentsApiService`;
- mapear DTOs de request/response;
- remover uso de `localStorage` do fluxo principal;
- manter telas atuais (lista, cadastro/edição, detalhe) com a mesma navegação.

## 4) Qualidade e validação

- validar fluxo ponta a ponta (criar, listar, consultar, editar, excluir);
- validar mensagens de erro de API no frontend;
- executar build/checks dos dois projetos.

---

## Critérios de pronto para o próximo ciclo

- frontend consumindo API real de aluno;
- CRUD funcionando sem `localStorage` como fonte primária;
- tratamento de erros de integração implementado;
- documentação técnica atualizada com endpoints e decisões tomadas.

---

## Riscos conhecidos

- divergência de formato de data entre frontend e backend;
- regra de e-mail/CNPJ/CPF diferente entre camadas;
- ausência inicial de telefone no schema persistido.

Mitigação:

- definir contrato explícito antes da codificação;
- validar exemplos reais de payload no início do próximo chat.

---

## Prompt sugerido para iniciar o próximo chat

```text
Contexto: agora temos frontend Angular (microfrontend) e backend Java 21 (hexagonal) no mesmo fluxo.
Objetivo: integrar o CRUD de aluno do frontend com API real no backend.

Estado atual:
- frontend já tem CRUD completo visual de aluno e validações;
- persistência atual ainda está em localStorage;
- backend será a nova fonte oficial de dados.

Solicitação:
1) implementar API de aluno (criar, listar, buscar por id, atualizar, excluir);
2) integrar frontend via HTTP mantendo telas atuais;
3) remover dependência de localStorage no fluxo principal;
4) entregar checklist final de homologação ponta a ponta.
```

---

## Checklist rápido para abertura do próximo chat

- [ ] Repositório com frontend e backend acessível;
- [ ] Instruções de execução local dos dois projetos;
- [ ] Banco configurado e migrations aplicáveis;
- [ ] Contrato inicial dos endpoints de aluno definido;
- [ ] Evidência de build/frontend e build/backend.
