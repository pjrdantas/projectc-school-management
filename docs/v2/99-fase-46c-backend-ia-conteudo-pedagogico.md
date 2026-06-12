# Fase 46C - Backend de IA e conteúdo pedagógico

## Objetivo

Implementar o backend inicial para apoio pedagógico com IA no planejamento bimestral, sem integrar provedor externo neste momento.

Esta fase segue o roadmap pós-MVP e mantém o escopo restrito ao backend monolítico atual, preparando pontos de extensão para evolução futura com BFF, serviços independentes, Kafka, MongoDB e Redis.

## Escopo implementado

- Registro de interação de IA vinculada ao planejamento bimestral.
- Geração de conteúdo pedagógico em modo interno simulado e controlado.
- Persistência do conteúdo gerado com tipo, status, hash, versão atual e vínculo com a interação.
- Criação de versões editadas pelo professor.
- Aprovação de uma versão específica.
- Publicação opcional ou manual de conteúdo aprovado na biblioteca pedagógica.
- Consulta da biblioteca pedagógica com filtros por professor, disciplina, tipo de conteúdo e tema.

## Decisão técnica

Não foi incluída integração real com provedor de IA.

Foi criado um contrato interno de geração de conteúdo pedagógico, com implementação local simulada. Esse formato permite validar o fluxo funcional completo sem depender de chave, custo, latência, disponibilidade ou política de um fornecedor externo.

Quando a integração real for priorizada, o ponto de extensão é substituir ou complementar o gateway de geração, mantendo os contratos de aplicação e os endpoints atuais.

## Endpoints criados

- `POST /api/planejamentos-bimestrais/{planejamentoId}/ia/conteudos`
  - Gera conteúdo pedagógico em modo interno simulado.

- `GET /api/planejamentos-bimestrais/{planejamentoId}/ia/interacoes`
  - Lista prompts, respostas e metadados das interações do planejamento.

- `GET /api/planejamentos-bimestrais/{planejamentoId}/ia/conteudos`
  - Lista conteúdos gerados para o planejamento.

- `GET /api/ia/conteudos/{conteudoId}`
  - Consulta um conteúdo gerado.

- `POST /api/ia/conteudos/{conteudoId}/versoes`
  - Cria uma nova versão editada do conteúdo.

- `GET /api/ia/conteudos/{conteudoId}/versoes`
  - Lista versões de um conteúdo gerado.

- `PATCH /api/ia/conteudos/{conteudoId}/aprovar-versao`
  - Aprova uma versão específica e, opcionalmente, publica na biblioteca.

- `POST /api/ia/conteudos/{conteudoId}/publicar-biblioteca`
  - Publica na biblioteca somente conteúdo já aprovado.

- `GET /api/biblioteca-conteudos-pedagogicos`
  - Consulta conteúdos pedagógicos reutilizáveis.

## Regras implementadas

- Todo conteúdo gerado cria uma versão inicial.
- Nova versão coloca o conteúdo em edição.
- Aprovação copia o conteúdo da versão escolhida para o conteúdo principal e recalcula o hash.
- Publicação na biblioteca exige aprovação prévia pelo professor.
- Conteúdo publicado preserva professor, disciplina, tipo, tema, origem e flag de reutilização.

## Fora do escopo

- Integração com provedor real de IA.
- Streaming de resposta de IA.
- Controle financeiro real de tokens/custo.
- Kafka, MongoDB, Redis, BFF ou serviço independente de IA.
- Alterações de frontend para biblioteca ou geração assistida além do que já existe na fase anterior.

## Validação

- Teste focado executado:
  - `.\mvnw.cmd -Dtest=PlanejamentoIAControllerIntegrationTest test`
  - Resultado: sucesso.

- Teste completo do backend:
  - `.\mvnw.cmd test`
  - Resultado: sucesso, 95 testes executados.

## Próxima fase recomendada

Fase 46D - Frontend de IA e biblioteca pedagógica:

- Expor no microfrontend pedagógico o fluxo de geração simulada.
- Listar interações e conteúdos gerados por planejamento.
- Permitir criação de nova versão, aprovação e publicação na biblioteca.
- Consultar biblioteca pedagógica para reutilização de conteúdo aprovado.
- Validar o microfrontend afetado com `npm run build`.
