# Roadmap pos-MVP - planejamento, IA, multi-escola, BFF e servicos

## Objetivo

Organizar as proximas etapas de codificacao em blocos pequenos, executaveis em
chats separados, sem iniciar revisoes amplas antes da conclusao funcional.

O sistema atual atende o cenario de uma escola. O objetivo futuro e evoluir para
atender varias escolas, com separacao arquitetural por BFF, servicos e
microfrontends federados por dominio. Essa evolucao deve ser planejada, mas nao
deve bloquear a entrega das proximas funcionalidades.

## Estado atual considerado

- Branch `develop` igualada com `Master`.
- Host Angular permanece como shell de autenticacao, sessao, menu e rotas
  federadas.
- Microfrontend academico atual concentra varias funcionalidades escolares.
- Backend atual ainda e uma aplicacao unica, com dominios internos organizados.
- Fluxos principais ja implementados/validados:
  - catalogos academicos;
  - alunos;
  - responsaveis;
  - matriculas;
  - transferencias;
  - documentos;
  - historico escolar;
  - professores;
  - aulas;
  - avaliacoes/notas;
  - dashboards e snapshots.
- Ainda nao e momento de revisao ampla de permissoes, seguranca granular ou
  refatoracao estrutural. Revisoes devem ser pontuais e iniciadas quando houver
  demanda explicita.

## Diretriz de trabalho

- Fazer uma fase por vez.
- Cada fase deve ter escopo funcional claro.
- Evitar misturar codificacao funcional com refatoracao arquitetural grande.
- Backend tocado: validar com `.\mvnw.cmd test` em
  `school-management-service`.
- Frontend tocado: validar com `npm run build` no microfrontend afetado.
- Host tocado: validar build do host.
- Ao final de cada fase:
  - documentar o que foi feito;
  - indicar backend, frontend ou ambos;
  - informar comandos de validacao;
  - indicar a proxima fase.

## Bloco 46 - Planejamento pedagogico e IA

Este e o proximo bloco funcional recomendado. Ele completa a etapa original de
planejamento bimestral e prepara a futura assistencia por IA.

### Fase 46A - Backend de planejamento bimestral

Objetivo:

- Criar API funcional para planejamento bimestral.

Escopo:

- Criar DTOs de request/response.
- Criar service de planejamento bimestral.
- Criar controller REST.
- Listar planejamentos por professor, turma, disciplina e periodo.
- Criar planejamento bimestral.
- Atualizar planejamento bimestral.
- Adicionar aulas previstas.
- Adicionar avaliacoes previstas.
- Alterar status do planejamento:
  - rascunho;
  - em analise;
  - aprovado;
  - reprovado.
- Criar testes de integracao.

Fora do escopo:

- Chamada real de IA.
- Frontend.
- BFF.
- Separacao em servicos.
- Permissoes granulares por perfil.

Validacao:

- `.\mvnw.cmd test` em `school-management-service`.

Proxima fase:

- Fase 46B - Frontend de planejamento bimestral.

### Fase 46B - Frontend de planejamento bimestral

Objetivo:

- Criar tela operacional de planejamento bimestral no microfrontend atual.

Escopo:

- Criar rota federada para planejamento.
- Criar item de menu.
- Criar lista com filtros.
- Criar cadastro/edicao em tela ou modal.
- Criar detalhe do planejamento.
- Exibir aulas previstas.
- Exibir avaliacoes previstas.
- Permitir alterar status.
- Usar selects nativos quando combo em modal apresentar problema.

Fora do escopo:

- IA.
- Separacao em microfrontend proprio.
- Regras finais de acesso por professor logado.

Validacao:

- `npm run build` no microfrontend.
- Build do host se houver alteracao de menu/rota federada.

Proxima fase:

- Fase 46C - Backend de IA e biblioteca pedagogica.

### Fase 46C - Backend de IA e conteudo pedagogico

Objetivo:

- Criar o backend funcional para registrar sugestoes de IA, versoes e biblioteca
  de conteudo pedagogico.

Escopo:

- Criar DTOs, services e controllers para:
  - interacao de IA;
  - conteudo gerado;
  - versoes de conteudo;
  - aprovacao de versao;
  - biblioteca de conteudo pedagogico.
- Registrar prompt enviado e resposta recebida.
- Salvar conteudo gerado vinculado ao planejamento bimestral.
- Versionar conteudo.
- Aprovar uma versao.
- Publicar versao aprovada na biblioteca pedagogica.
- Permitir reuso de conteudo aprovado sem nova chamada de IA.

Decisao tecnica inicial:

- Nao acoplar ainda a um provedor externo obrigatorio.
- Implementar contrato interno para geracao.
- Permitir modo inicial simulado/controlado para testes locais.
- Deixar ponto de extensao para provedor real depois.

Fora do escopo:

- Integracao real obrigatoria com provedor de IA.
- Kafka.
- MongoDB.
- Redis.
- BFF.

Validacao:

- `.\mvnw.cmd test` em `school-management-service`.

Proxima fase:

- Fase 46D - Frontend de IA e biblioteca.

### Fase 46D - Frontend de IA e biblioteca

Objetivo:

- Criar a interface para professor/direcao trabalharem com sugestoes, versoes e
  conteudos aprovados.

Escopo:

- Tela ou secao dentro do planejamento bimestral para:
  - solicitar sugestao;
  - visualizar conteudo gerado;
  - comparar versoes;
  - aprovar uma versao;
  - reaproveitar conteudo da biblioteca.
- Tela de biblioteca pedagogica.
- Busca por tema, disciplina, serie e tipo de conteudo.

Fora do escopo:

- Microfrontend separado.
- Regras finais de isolamento por professor.

Validacao:

- `npm run build` no microfrontend.

Proxima fase:

- Fase 47 - Preparacao multi-escola.

## Bloco 47 - Preparacao multi-escola

O sistema atual esta preparado para uma escola. Antes de separar servicos ou
criar BFF definitivo, o modelo precisa reconhecer escola/tenant como fronteira
funcional.

### Fase 47A - Diagnostico multi-escola

Objetivo:

- Mapear quais entidades precisam ser escopadas por escola.

Escopo:

- Levantar tabelas que devem carregar `escola_id` ou equivalente.
- Separar entidades globais e entidades por escola.

Entidades provavelmente globais:

- perfis tecnicos padrao;
- permissoes tecnicas;
- tipos/catalogos nacionais quando aplicavel.

Entidades provavelmente por escola:

- alunos;
- responsaveis;
- professores;
- funcionarios;
- periodos letivos;
- series;
- turmas;
- disciplinas, se customizadas por escola;
- matriculas;
- aulas;
- frequencias;
- avaliacoes;
- boletins;
- documentos;
- historicos;
- dashboards/configuracoes;
- planejamentos;
- biblioteca pedagogica, conforme politica.

Entrega:

- Documento de diagnostico.
- Sem migration ainda.

### Fase 47B - Modelo base de escola/tenant

Objetivo:

- Introduzir a entidade escola/tenant e o vinculo minimo necessario.

Escopo:

- Definir entidade `Escola`.
- Definir relacao entre usuario e escola.
- Definir escola ativa no contexto da sessao.
- Definir estrategia para dados existentes de escola unica.

Fora do escopo:

- Isolamento completo.
- BFF.
- Separacao de banco por escola.

Validacao:

- `.\mvnw.cmd test`.

### Fase 47C - Aplicacao gradual do escopo por escola

Objetivo:

- Aplicar o filtro de escola nas principais consultas e criacoes.

Ordem sugerida:

1. Catalogos academicos.
2. Pessoas, alunos e responsaveis.
3. Professores e funcionarios.
4. Matriculas.
5. Aulas, frequencia e avaliacoes.
6. Planejamento e IA.
7. Dashboards.

Observacao:

- Esta fase deve ser dividida em subfases menores. Nao executar tudo de uma vez.

## Bloco 48 - BFF e separacao de servicos

Este bloco deve vir depois de estabilizar funcionalidades e multi-escola basico.

### Fase 48A - Desenho dos BFFs

Objetivo:

- Definir BFFs por experiencia de uso, nao por tabela.

Possiveis BFFs:

- BFF Admin/Shell.
- BFF Secretaria.
- BFF Professor.
- BFF Diretor.
- BFF Responsavel/Aluno, se houver portal externo.

Responsabilidades:

- Compor dados de multiplos servicos.
- Adaptar respostas para frontend.
- Reduzir acoplamento do frontend com servicos internos.
- Aplicar contexto de usuario, escola e perfil.

Fora do escopo:

- Criar microservicos imediatamente.

### Fase 48B - Separacao inicial de servicos

Objetivo:

- Definir fronteiras de servicos antes de extrair codigo.

Possiveis servicos:

- identidade/acesso;
- pessoas;
- academico/catalogos;
- matriculas/documentos;
- pedagogico/professores/aulas/avaliacoes;
- planejamento/IA;
- dashboards/analytics;
- notificacoes.

Regra:

- Extrair um servico somente quando o contrato estiver estavel o suficiente.

## Bloco 49 - Microfrontends federados por dominio

O microfrontend academico atual deve ser quebrado futuramente.

### Fase 49A - Plano de federacao por dominio

Objetivo:

- Definir os remotos finais sem quebrar a operacao atual.

Possiveis remotos:

- `mfe-dashboard`;
- `mfe-alunos`;
- `mfe-responsaveis`;
- `mfe-matriculas`;
- `mfe-catalogo-academico`;
- `mfe-professores`;
- `mfe-aulas-avaliacoes`;
- `mfe-planejamento-ia`;
- `mfe-documentos`;
- `mfe-admin`.

Regra:

- Separar por dominio funcional e ciclo de evolucao.
- Manter o host como shell.
- Evitar duplicar autenticacao.

### Fase 49B - Extracao piloto

Objetivo:

- Extrair um dominio pequeno primeiro.

Candidato recomendado:

- Dashboard, por ter fronteira mais clara e menos edicao transacional.

Validacao:

- Build do remoto extraido.
- Build do host.
- Navegacao local.

## Bloco 50 - Kafka, MongoDB e Redis

Estas tecnologias devem entrar por necessidade concreta, nao por antecipacao.

### Kafka

Usar quando houver eventos entre servicos ou processamento assincrono relevante.

Candidatos:

- matricula criada;
- documento enviado;
- transferencia solicitada;
- aula realizada;
- avaliacao publicada;
- nota lancada;
- planejamento aprovado;
- conteudo IA aprovado;
- snapshot de dashboard solicitado.

Possiveis topicos:

- `school.enrollment.created`;
- `school.document.uploaded`;
- `school.transfer.requested`;
- `school.lesson.completed`;
- `school.assessment.published`;
- `school.grade.recorded`;
- `school.planning.approved`;
- `school.ai-content.approved`;
- `school.dashboard.snapshot.requested`.

### MongoDB

Usar para documentos flexiveis, historicos ricos ou conteudo sem schema rigido.

Candidatos:

- respostas completas de IA;
- versoes de conteudo pedagogico;
- logs de interacao com IA;
- snapshots historicos detalhados de dashboard;
- auditoria rica de alteracoes.

Regra:

- Dados transacionais oficiais continuam em PostgreSQL.
- MongoDB entra para conteudo flexivel e consulta documental.

### Redis

Usar para cache, sessao curta, locks e processamento rapido.

Candidatos:

- cache de dashboards;
- cache de catalogos;
- cache de contexto usuario/escola/perfil;
- rate limit de chamadas IA;
- lock de geracao de snapshot;
- fila curta/temporaria quando Kafka for excessivo.

Regra:

- Redis nao deve virar fonte oficial de dados.

## Ordem recomendada para proximos chats

1. Chat 1: Fase 46A - Backend de planejamento bimestral.
2. Chat 2: Fase 46B - Frontend de planejamento bimestral.
3. Chat 3: Fase 46C - Backend de IA e biblioteca pedagogica.
4. Chat 4: Fase 46D - Frontend de IA e biblioteca.
5. Chat 5: Fase 47A - Diagnostico multi-escola.
6. Chat 6: Fase 47B - Modelo base de escola/tenant.
7. Chat 7+: Fase 47C em subfases por dominio.
8. Depois: BFF, servicos, microfrontends separados e tecnologias de suporte.

## Primeiro prompt sugerido para o proximo chat

```text
Projeto: C:\Projeto\git\projectc-school-management
Branch: develop

Leia docs/v2/96-roadmap-pos-mvp-planejamento-ia-multiescola-bff-servicos.md
e inicie a Fase 46A - Backend de planejamento bimestral.

Regras:
- Considere develop como fonte da verdade.
- Nao faca revisao ampla de permissoes.
- Nao inicie BFF, Kafka, MongoDB, Redis ou separacao de microservicos agora.
- Implemente somente o backend funcional de planejamento bimestral.
- Se tocar backend, valide com .\mvnw.cmd test em school-management-service.
- Ao final, documente a fase e indique a proxima.
```
