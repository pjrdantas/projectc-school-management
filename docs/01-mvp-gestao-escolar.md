# MVP Gestao Escolar

## Objetivo

Este documento define o recorte inicial recomendado para o MVP do sistema de gestao escolar.

## Diretriz do MVP

O MVP deve priorizar o minimo conjunto de funcionalidades necessario para operar o fluxo academico inicial
com seguranca, organizacao e capacidade real de uso.

O objetivo nao e cobrir toda a gestao escolar de uma vez.

## Objetivos do MVP

- permitir acesso controlado ao sistema;
- manter cadastro basico de alunos e responsaveis;
- manter cadastro academico minimo;
- realizar matriculas;
- consultar matriculas de forma operacional.

## Escopo do MVP

### 1. Autenticacao e Controle de Acesso

Inclui:
- login;
- perfis de acesso;
- permissao por tipo de usuario.

### 2. Cadastro de Alunos e Responsaveis

Inclui:
- cadastro de aluno;
- cadastro de responsavel;
- vinculo aluno-responsavel;
- consulta cadastral basica.

### 3. Cadastro Academico Basico

Inclui:
- periodo letivo;
- turma;
- disciplina;
- organizacao minima para matricula.

### 4. Gestao de Matricula

Inclui:
- criacao de matricula;
- consulta de matricula;
- cancelamento de matricula;
- historico basico de status.

### 5. Consulta Operacional

Inclui:
- busca por aluno;
- busca por turma;
- busca por periodo;
- filtro por status de matricula.

## Fora do MVP Inicial

- aplicativo mobile;
- notificacoes multicanal completas;
- financeiro avancado;
- boletim e diario escolar;
- relatorios gerenciais avancados;
- integracoes externas complexas.

## Usuarios do MVP

- administrador;
- usuario operacional;
- administrador academico;
- atendente.

## Primeiro Fluxo Completo Recomendado

O primeiro fluxo ponta a ponta do MVP deve ser:

1. autenticacao basica;
2. cadastro de aluno;
3. cadastro de turma e periodo;
4. matricula;
5. consulta da matricula.

## Valor do Primeiro Fluxo

Esse fluxo permite validar:
- modelo de dominio;
- persistencia;
- contratos entre backend e frontend;
- fluxo real de operacao;
- experiencia do usuario;
- base do sistema para expansao futura.

## Stack Inicial Recomendada

### Backend

- Java 21;
- Spring Boot;
- PostgreSQL.

### Frontend

- Angular 20.

## Estrategia de Entrega

O MVP nao deve ser construido como um bloco unico.
Ele deve ser entregue por incrementos funcionais.

Sequencia sugerida:
- autenticacao;
- cadastro de alunos e responsaveis;
- cadastro academico minimo;
- matricula;
- consultas operacionais.

## Criterio de Sucesso do MVP

O MVP sera considerado bem sucedido quando permitir:
- acesso seguro por usuario autorizado;
- cadastro de alunos e dados academicos essenciais;
- realizacao de matricula sem improviso manual;
- consulta operacional confiavel dos registros principais.
