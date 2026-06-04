## Fase 41 - Alertas de indicadores criticos do dashboard

Esta fase adiciona uma API de alertas calculados a partir dos dashboards operacionais ja existentes.

### Endpoint

- `GET /api/dashboard/alertas?publicoCodigo={publicoCodigo}`
- `GET /api/dashboard/alertas?publicoCodigo=PROFESSOR&professorId={professorId}`

### Publicos suportados

- `ACADEMICO`
- `SECRETARIA`
- `DIRETOR`
- `PROFESSOR`

Para `PROFESSOR`, o parametro `professorId` e obrigatorio.

### Retorno

Cada alerta retorna:

- publico do dashboard;
- professor vinculado, quando aplicavel;
- codigo do alerta;
- severidade;
- titulo;
- mensagem;
- valor observado;
- limite configurado.

### Severidades

- `CRITICO`
- `ATENCAO`

Os alertas sao retornados ordenados por severidade e codigo.

### Regras iniciais

Academico:

- matriculas aguardando documentos;
- alunos reprovados.

Secretaria:

- matriculas aguardando documentos;
- matriculas aguardando historico escolar;
- matriculas com documentos pendentes;
- solicitacoes de exclusao pendentes.

Diretor:

- matriculas pendentes;
- turmas lotadas;
- avaliacoes com notas pendentes;
- matriculas com documentos pendentes.

Professor:

- frequencias pendentes;
- avaliacoes com notas pendentes;
- planejamentos bimestrais pendentes.

### Configuracao

O limite padrao e configuravel por:

- `DASHBOARD_ALERTAS_LIMITE_PADRAO`

Valor padrao: `0`. Assim, qualquer pendencia maior que zero gera alerta.

### Decisao tecnica

Nao houve alteracao de schema nesta fase. Os alertas sao calculados em tempo real com base nos services dos dashboards ja implementados. Persistencia, historico de leitura e regras configuraveis por perfil podem ser tratados em fases futuras.

### Validacao

- Testes unitarios das regras de alerta.
- Teste de ordenacao por severidade.
- Teste de obrigatoriedade do `professorId`.
- Teste de integracao do endpoint.
