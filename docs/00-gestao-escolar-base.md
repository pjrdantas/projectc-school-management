# Base Inicial do Sistema de Gestao Escolar

## Objetivo Deste Documento

Este documento registra a direcao inicial para o desenvolvimento do sistema real de gestao escolar.
Ele consolida as decisoes de produto, arquitetura e implementacao do repositorio
`projectc-school-management`.

O objetivo e preservar os aprendizados tecnicos, reduzir retrabalho e manter um ponto de referencia
consistente para futuras conversas, decisoes arquiteturais e planejamento de implementacao.

## Premissas

- O projeto evoluiu a partir de uma prova de conceito para um MVP operacional.
- O sistema real deve nascer com fronteiras de dominio mais claras.
- O sistema inicial deve priorizar organizacao e clareza de responsabilidades antes de sofisticacao excessiva.
- A recomendacao inicial e um monolito modular, preparado para futura extracao de servicos.
- Angular 20 e uma opcao adequada para a interface web administrativa.
- Mobile nao entra no MVP, salvo necessidade de negocio muito clara.

## Epicos do Sistema de Gestao Escolar

### 1. Autenticacao e Controle de Acesso

Objetivo:
Permitir acesso seguro ao sistema com perfis basicos. A matriz granular de autorizacao/permissoes sera refinada no final do projeto, apos estabilizacao das funcionalidades principais.

Escopo inicial:
- login;
- logout;
- perfis basicos de usuario;
- recuperacao de senha;
- auditoria basica de acesso.

### 2. Cadastro Academico

Objetivo:
Manter os dados mestres usados pelos processos escolares.

Escopo inicial:
- periodos letivos;
- cursos;
- series;
- turmas;
- disciplinas;
- carga horaria;
- calendario academico basico.

### 3. Gestao de Alunos e Responsaveis

Objetivo:
Centralizar o cadastro das pessoas envolvidas na vida academica do aluno.

Escopo inicial:
- cadastro de aluno;
- cadastro de responsaveis;
- enderecos e contatos;
- documentos;
- vinculo entre aluno e responsaveis;
- situacao cadastral.

### 4. Gestao de Matricula

Objetivo:
Permitir matricula, rematricula, cancelamento e acompanhamento do ciclo academico do aluno.

Escopo inicial:
- criar matricula;
- vincular aluno a turma e periodo;
- consultar matricula;
- cancelar matricula;
- manter historico de status.

### 5. Financeiro Escolar

Objetivo:
Registrar cobrancas e acompanhar a situacao financeira relacionada a matricula.

Escopo inicial:
- gerar cobranca;
- registrar pagamento;
- acompanhar inadimplencia;
- aplicar descontos ou bolsas;
- integrar com meios de pagamento, se necessario.

### 6. Comunicacao e Notificacoes

Objetivo:
Informar eventos importantes para usuarios internos e externos.

Escopo inicial:
- notificacao de matricula realizada;
- lembretes financeiros;
- comunicados academicos;
- confirmacoes por email ou outros canais.

### 7. Portal Operacional e Consultas

Objetivo:
Permitir busca, acompanhamento e operacao diaria dos registros escolares.

Escopo inicial:
- consulta de alunos;
- consulta de turmas;
- consulta de matriculas;
- filtros por periodo e status;
- historico operacional.

### 8. Relatorios e Auditoria

Objetivo:
Oferecer visibilidade gerencial e rastreabilidade de operacoes criticas.

Escopo inicial:
- relatorios de matriculas;
- relatorios financeiros basicos;
- trilha de auditoria;
- exportacoes simples.

## MVP Proposto

O MVP deve ser enxuto e operacional. A primeira versao deve priorizar a entrada em producao com o minimo
de modulos necessarios para suportar a operacao inicial da escola.

### Modulos sugeridos para o MVP

- autenticacao e controle de acesso;
- cadastro de alunos e responsaveis;
- cadastro academico basico;
- matricula;
- consulta operacional de matriculas.

### Modulos que podem ficar para a fase seguinte

- financeiro mais completo;
- notificacoes multicanal;
- relatorios avancados;
- app mobile;
- integracoes externas mais sofisticadas.

## Primeiras Historias do MVP

As historias abaixo servem como base inicial. Elas podem ser refinadas depois com criterio de aceite e prioridade.

### Epic: Autenticacao e Controle de Acesso

Historia 1:
Como administrador, quero autenticar no sistema para acessar as funcionalidades administrativas com seguranca.

Historia 2:
Como administrador, quero manter perfis basicos de acesso para organizar a operacao inicial, deixando a matriz granular de permissoes para o final do projeto.

Historia 3:
Como usuario operacional, quero recuperar minha senha para voltar a acessar o sistema sem depender de suporte manual.

### Epic: Gestao de Alunos e Responsaveis

Historia 4:
Como atendente, quero cadastrar um aluno para que ele possa participar dos processos academicos.

Historia 5:
Como atendente, quero cadastrar os responsaveis de um aluno para manter os contatos e obrigacoes legais atualizados.

Historia 6:
Como atendente, quero consultar o cadastro de um aluno para confirmar seus dados antes de realizar a matricula.

### Epic: Cadastro Academico

Historia 7:
Como administrador academico, quero cadastrar um periodo letivo para organizar as matriculas por ciclo escolar.

Historia 8:
Como administrador academico, quero cadastrar turmas para disponibilizar vagas por periodo e organizacao escolar.

Historia 9:
Como administrador academico, quero vincular disciplinas a turmas para refletir a estrutura academica ofertada.

### Epic: Gestao de Matricula

Historia 10:
Como atendente, quero matricular um aluno em uma turma para formalizar sua vaga no periodo letivo.

Historia 11:
Como atendente, quero consultar o status de uma matricula para acompanhar sua situacao operacional.

Historia 12:
Como atendente, quero cancelar uma matricula para corrigir situacoes em que a vaga nao deve mais permanecer ativa.

Historia 13:
Como administrador academico, quero visualizar o historico de status da matricula para auditar alteracoes relevantes.

### Epic: Portal Operacional e Consultas

Historia 14:
Como usuario operacional, quero pesquisar matriculas por aluno, turma e periodo para localizar registros rapidamente.

Historia 15:
Como usuario operacional, quero filtrar matriculas por status para priorizar meu trabalho diario.

## Arquitetura Inicial Recomendada

## Visao Geral

A recomendacao inicial e desenvolver o sistema real como um monolito modular.
Isso oferece velocidade de entrega, simplicidade operacional e menor custo de coordenacao,
sem abrir mao de uma organizacao interna que facilite futura evolucao.

O projeto de referencia atual mostrou valor em pontos como rastreabilidade, separacao entre dominio e infraestrutura,
uso de eventos e fluxo de matricula. Esses aprendizados devem ser absorvidos, mas com aplicacao seletiva.

## Diretrizes Arquiteturais

### 1. Separacao por contexto de negocio

Organizar o backend por modulos de negocio, e nao por tecnologia.

Exemplo de modulos iniciais:
- `access-control`;
- `student-management`;
- `academic-catalog`;
- `enrollment`;
- `finance` em versao inicial ou futura;
- `notification` em fase posterior;
- `shared-kernel` apenas para elementos realmente compartilhados.

### 2. API unica no inicio, modulos internos bem definidos

No inicio, uma unica aplicacao backend pode expor os endpoints do MVP.
Internamente, cada modulo deve ter:
- casos de uso;
- entidades de dominio;
- contratos claros;
- repositorios por contexto;
- adaptadores de entrada e saida bem separados.

### 3. Persistencia clara e ownership por modulo

Mesmo em um unico banco, cada modulo deve ser dono logico das suas tabelas e regras.
Evitar compartilhamento acidental de entidades entre modulos.

### 4. Eventos como recurso de desacoplamento, nao como obrigacao

Nem todo fluxo precisa nascer com Kafka, saga, outbox e retry.
Esses mecanismos devem ser usados quando houver integracao assicrona real ou necessidade clara de resiliencia distribuida.

Sugestao:
- usar transacoes e chamadas internas para o MVP quando suficiente;
- reservar eventos para casos de integracao relevante ou processamento desacoplado.

### 5. Preparacao para extracao futura

Cada modulo deve ser desenhado de forma que possa, no futuro, virar servico proprio se houver motivo de negocio.

Criticos para futura extracao:
- matricula;
- financeiro;
- notificacoes;
- identidade e acesso, dependendo da estrategia da plataforma.

### 6. Observabilidade desde o inicio

Mesmo sem arquitetura distribuida completa, o sistema deve nascer com:
- logs estruturados;
- identificadores de correlacao;
- tratamento consistente de erros;
- auditoria de operacoes criticas.

## Stack Inicial Recomendada

### Backend

- Java 21;
- Spring Boot;
- Spring Web;
- Spring Data JPA;
- PostgreSQL;
- Spring Security;
- Flyway ou Liquibase para migracoes;
- OpenAPI para documentacao de contratos;
- testes unitarios e integracao.

### Frontend Web

- Angular 20;
- Angular Material ou outra biblioteca de componentes, se fizer sentido para acelerar o MVP;
- arquitetura por features;
- formularios reativos;
- consumo da API REST com contratos bem definidos.

### Mobile

- nao iniciar no MVP, salvo necessidade formal do cliente;
- reavaliar apos estabilizacao do frontend web e validacao dos fluxos principais.

## Ordem de Implementacao Entre Backend e Frontend

## Principio Geral

Nao comecar exclusivamente por backend ou exclusivamente por frontend.
O melhor caminho e conduzir o inicio do projeto em camadas de definicao, depois implementar backend e frontend em paralelo orientados por um mesmo fluxo prioritario.

## Sequencia Recomendada

### Etapa 1. Descoberta e alinhamento com o cliente

Definir:
- objetivos do sistema;
- usuarios;
- problemas a resolver;
- prioridades da primeira entrega;
- restricoes de prazo e operacao.

Entregaveis:
- escopo inicial;
- mapa de modulos;
- lista inicial de epicos.

### Etapa 2. Refinamento funcional

Transformar epicos em historias de usuario com regras de negocio e criterios de aceite.

Entregaveis:
- backlog inicial do MVP;
- definicao das jornadas prioritarias.

### Etapa 3. Modelagem do dominio e arquitetura

Definir:
- entidades principais;
- estados;
- regras;
- modulos do backend;
- contratos iniciais;
- convencoes tecnicas.

Entregaveis:
- documento de arquitetura;
- mapa de entidades e relacionamentos;
- contratos iniciais de API.

### Etapa 4. Prototipacao de frontend

Construir as jornadas de tela do MVP antes ou em paralelo a implementacao funcional.

Objetivo:
- validar fluxo com o cliente;
- reduzir retrabalho de UX;
- antecipar lacunas de regra de negocio.

Entregaveis:
- wireframes ou prototipos navegaveis;
- mapa de telas.

### Etapa 5. Implementacao do primeiro fluxo completo

Recomendacao:
implementar primeiro um fluxo fechado de ponta a ponta.

Fluxo sugerido:
- autenticacao basica;
- cadastro de aluno;
- cadastro de turma e periodo;
- matricula;
- consulta da matricula.

Assim o time valida:
- dominio;
- persistencia;
- contratos;
- UX;
- deploy;
- integracao entre camadas.

## Opiniao Objetiva Sobre Backend x Frontend

Se for preciso resumir em uma direcao pratica:

- comecar pelo produto e dominio;
- desenhar a arquitetura inicial do backend antes de codar forte;
- prototipar o frontend cedo;
- implementar backend e frontend juntos no primeiro fluxo do MVP;
- deixar mobile para depois.

## Como Usar Este Documento nas Proximas Conversas

Este arquivo deve funcionar como memoria operacional do projeto.

Quando quiser retomar em outro chat, basta dizer algo como:

"Considere o documento `docs/00-gestao-escolar-base.md` como referencia do projeto."

Ou:

"Leia `docs/00-gestao-escolar-base.md` e continue a partir dos epicos e da arquitetura definidos ali."

Isso permite que a conversa recomece com muito mais consistencia, mesmo fora deste chat.

## Proximos Passos Sugeridos

1. Revisar e ajustar os epicos com base no cliente real.
2. Priorizar quais historias entram de fato no MVP.
3. Criar um documento separado apenas para backlog detalhado.
4. Criar um documento de arquitetura tecnica com os modulos do backend.
5. Mapear as primeiras telas do frontend Angular.
