# Arquitetura Inicial

## Objetivo

Este documento define a arquitetura inicial recomendada para o sistema real de gestao escolar,
usando o projeto `academic-core-service` como referencia conceitual, sem assumir reutilizacao direta
do codigo como produto final.

## Principios

- comecar simples, mas com estrutura clara;
- organizar por contexto de negocio, nao por tecnologia;
- evitar microservicos prematuros;
- permitir evolucao futura com baixo retrabalho;
- manter boa rastreabilidade e observabilidade desde o inicio.

## Estilo Arquitetural Recomendado

A recomendacao inicial e um monolito modular.

Motivos:
- menor custo operacional no inicio;
- maior velocidade de entrega;
- menos complexidade de deploy e integracao;
- melhor foco no dominio e no MVP;
- possibilidade de extracao futura de modulos para servicos separados.

## Modulos Iniciais Sugeridos

### 1. `access-control`

Responsavel por:
- autenticacao;
- autorizacao;
- perfis;
- usuarios;
- recuperacao de acesso.

### 2. `student-management`

Responsavel por:
- cadastro de alunos;
- cadastro de responsaveis;
- contatos;
- documentos;
- situacao cadastral.

### 3. `academic-catalog`

Responsavel por:
- periodos letivos;
- cursos;
- series;
- turmas;
- disciplinas;
- organizacao academica.

### 4. `enrollment`

Responsavel por:
- matricula;
- rematricula;
- cancelamento;
- historico de status;
- regras academicas ligadas ao ingresso do aluno.

### 5. `finance`

Responsavel por:
- cobrancas;
- pagamentos;
- descontos;
- inadimplencia.

Pode entrar de forma reduzida no inicio ou ser postergado conforme o MVP.

### 6. `notification`

Responsavel por:
- notificacoes operacionais;
- comunicacoes automaticas;
- envios por canais externos.

Pode ser introduzido depois do MVP inicial.

### 7. `shared-kernel`

Reservado apenas para:
- tipos compartilhados realmente genericos;
- convencoes tecnicas;
- componentes transversais.

Evitar colocar regra de negocio aqui.

## Organizacao Interna do Backend

Cada modulo deve conter, quando fizer sentido:
- dominio;
- casos de uso;
- contratos;
- persistencia;
- adaptadores de entrada e saida.

Exemplo conceitual:

```text
modulo
  domain
  application
  adapter/in
  adapter/out
  infrastructure
```

## Persistencia

Recomendacao inicial:
- PostgreSQL;
- ownership logico das tabelas por modulo;
- migracoes controladas com Flyway ou Liquibase.

Mesmo com um unico banco, cada modulo deve ser tratado como dono dos seus dados.

## Integracao Entre Modulos

No inicio, a comunicacao principal pode ser interna, via chamadas de aplicacao e transacoes locais.

Eventos devem ser usados quando houver necessidade real de:
- desacoplamento;
- processamento assincrono;
- integracao com sistemas externos;
- auditoria orientada a eventos.

## Uso de Kafka, Saga e Outbox

O projeto atual mostrou que esses padroes funcionam e sao valiosos.
Mas para o sistema real, a recomendacao e aplicacao seletiva.

Usar quando:
- houver integracao assincrona entre contextos;
- existir dependencia de servico externo critico;
- o fluxo exigir resiliencia distribuida.

Nao usar por padrao em todo fluxo logo no inicio.

## Observabilidade

O sistema deve nascer com:
- logs estruturados;
- correlacao de requisicoes;
- tratamento consistente de erros;
- auditoria para operacoes criticas;
- endpoints de healthcheck e monitoramento.

## Stack Recomendada

### Backend

- Java 21;
- Spring Boot;
- Spring Web;
- Spring Data JPA;
- Spring Security;
- PostgreSQL;
- Flyway ou Liquibase;
- OpenAPI;
- JUnit e testes de integracao.

### Frontend

- Angular 20;
- arquitetura por features;
- formularios reativos;
- componentes reutilizaveis;
- consumo de APIs REST.

### Mobile

Nao iniciar no MVP, salvo demanda objetiva de negocio.

## Evolucao Futura

Os candidatos mais fortes para extracao futura em servicos separados sao:
- `enrollment`;
- `finance`;
- `notification`;
- `access-control`, dependendo da estrategia geral da plataforma.

Essa extracao deve acontecer apenas quando houver necessidade concreta de autonomia, escala ou ciclo de mudanca independente.
