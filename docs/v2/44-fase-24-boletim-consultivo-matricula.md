# Fase 24 - Boletim consultivo por matricula

## Objetivo

Criar o primeiro endpoint de boletim usando as notas e frequencias ja registradas nas fases anteriores.

Esta fase entrega um boletim consultivo, gerado em tempo real, sem persistir nas tabelas `boletim` e `boletim_item`.

## Decisao tecnica

O modelo atual de `boletim` possui `periodo_referencia` com unicidade global.

Persistir boletins nesta fase poderia bloquear boletins de alunos diferentes para o mesmo periodo de referencia. Por isso, a fase implementa apenas a consulta gerada a partir dos dados academicos reais.

Persistencia, fechamento oficial e revisao da chave unica ficam para fase propria.

## Endpoint adicionado

- `GET /api/matriculas/{matriculaId}/boletim`

## Regra aplicada

O boletim agrupa dados por disciplina.

Para cada disciplina:

- calcula a media simples das notas lancadas;
- calcula frequencia percentual com base nas presencas registradas;
- retorna `PENDENTE` quando nao ha nota ou frequencia;
- retorna `APROVADO` quando media >= 6 e frequencia >= 75%;
- retorna `REPROVADO` caso contrario.

O resultado geral:

- fica `PENDENTE` se algum item estiver pendente;
- caso contrario, aplica os mesmos criterios sobre media geral e frequencia geral.

## Escopo realizado

- Criado controller REST de boletim por matricula.
- Criados DTOs de boletim, item e indicadores.
- Criado `BoletimService`.
- Criado teste de integracao cobrindo:
  - matricula;
  - professor;
  - disciplina;
  - aula;
  - frequencia;
  - avaliacao;
  - nota;
  - consulta do boletim gerado.

## Arquivos principais

- `school-management-service/src/main/java/br/com/escola/historico/adapter/in/web/BoletimController.java`
- `school-management-service/src/main/java/br/com/escola/historico/application/service/BoletimService.java`
- `school-management-service/src/main/java/br/com/escola/historico/adapter/in/web/dto/BoletimResponse.java`
- `school-management-service/src/test/java/br/com/escola/historico/adapter/in/web/BoletimControllerIntegrationTest.java`

## Resultado parcial dos testes

Comando focado:

```powershell
.\mvnw.cmd -Dtest=BoletimControllerIntegrationTest test
```

Resultado:

- 1 teste executado.
- 0 falhas.
- 0 erros.

## Pendencias para fases futuras

- Revisar persistencia de `boletim` e `boletim_item`.
- Definir chave unica correta para boletim por matricula e periodo.
- Criar fechamento oficial de boletim.
- Definir formula institucional de media final.
- Diferenciar frequencia global e frequencia por disciplina em regra oficial.
