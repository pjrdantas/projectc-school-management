# Roteiro de Continuidade — KAN-33 (Cadastro de Aluno)

## Objetivo deste documento

Consolidar o plano para continuidade do desenvolvimento em um novo chat, com foco no card **KAN-33** e na priorização do MVP.

## Contexto do projeto

- Projeto: `school-management-web` (host Angular + microfrontend).
- Priorização atual: fluxo de **aluno** no MVP.
- Cards de login/autenticação ficam para etapa posterior (não bloqueadores do MVP atual).
- Evidência funcional de backend de aluno: persistência já validada em `public.aluno` com registros salvos.

## Priorização de cards

1. **KAN-33** — Frontend: criar tela de cadastro de aluno.
2. **KAN-34** — Frontend: validações do formulário.
3. **KAN-15** — Frontend: integração do cadastro com backend.

## Escopo do KAN-33

Criar a tela inicial de cadastro de aluno com formulário base e layout pronto para integração.

### Critérios de aceite do KAN-33

- Tela disponível para uso.
- Formulário com campos essenciais.
- Layout pronto para integração.

### Campos essenciais sugeridos (MVP)

- Nome completo (obrigatório)
- CPF (obrigatório)
- Data de nascimento (obrigatório)
- E-mail (obrigatório)
- Telefone (opcional)

### Ações da tela

- Botão **Salvar** (estrutura de submit pronta, mesmo sem POST final nesta etapa).
- Botão **Cancelar** (retornar para lista/menu).

## Sequência sugerida de implementação

1. Mapear rotas e menu atuais para incluir acesso ao cadastro de aluno.
2. Criar/ajustar página e componente de cadastro de aluno.
3. Montar formulário com campos essenciais.
4. Preparar layout responsivo básico para desktop/notebook.
5. Garantir estrutura pronta para integração (payload e método de submit).
6. Executar checks/build disponíveis no ambiente.

## Checklist de homologação (KAN-33)

- [ ] Tela abre via menu.
- [ ] Tela abre por URL/rota direta.
- [ ] Formulário renderiza todos os campos essenciais.
- [ ] Botões Salvar/Cancelar presentes e funcionais.
- [ ] Layout sem quebras visuais relevantes.
- [ ] Sem erros de console ao abrir/interagir.
- [ ] Estrutura pronta para integração no KAN-15.

## Definition of Done (DoD) — KAN-33

O card só pode ir para **Concluído** quando:

- Todos os critérios de aceite estiverem atendidos.
- Checklist de homologação estiver 100% validado.
- A tela estiver navegável e estável no fluxo principal.

## Comentários prontos para Jira

### 1) Transição para "Em andamento"

```
Iniciamos o KAN-33 com foco no MVP de cadastro de aluno.

Escopo desta etapa:
- disponibilizar a tela de cadastro de aluno no frontend;
- montar formulário com campos essenciais;
- deixar layout pronto para integração com backend.

Observação:
- neste card o objetivo é estrutura de tela/formulário;
- validações avançadas e integração serão tratadas nos cards seguintes (KAN-34 e KAN-15).
```

### 2) Transição para "Concluído"

```
KAN-33 concluído.

Entregas realizadas:
- tela de cadastro de aluno disponível para uso;
- formulário inicial com campos essenciais implementado;
- layout pronto para integração com backend.

Próximos passos:
- KAN-34: validações de formulário;
- KAN-15: integração de cadastro com API backend.
```

### 3) Atualização de roadmap

```
Priorização confirmada: cards de login ficam para etapa posterior, pois não são bloqueadores do MVP atual.
Fluxo ativo da sprint: KAN-33 -> KAN-34 -> KAN-15.
```

## Prompt curto para iniciar novo chat

```
Contexto: priorizar MVP de aluno no school-management-web.
Card atual: KAN-33 (tela de cadastro de aluno).
Objetivo: implementar tela + formulário base + layout pronto para integração.
Depois: KAN-34 (validações) e KAN-15 (integração backend).
Favor executar com checklist de homologação e saída pronta para atualização no Jira.
```
