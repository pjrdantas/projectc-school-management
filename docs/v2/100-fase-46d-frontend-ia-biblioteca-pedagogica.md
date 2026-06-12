# Fase 46D - Frontend de IA e biblioteca pedagógica

## Objetivo

Expor no frontend o fluxo inicial de IA pedagógica implementado na Fase 46C, ainda em modo simulado/local, e permitir o reaproveitamento de conteúdos aprovados da biblioteca pedagógica.

## Escopo implementado

- Seção de IA no detalhe do planejamento bimestral.
- Geração de conteúdo pedagógico a partir de prompt do professor.
- Listagem de conteúdos gerados e interações de IA do planejamento.
- Listagem de versões de conteúdo gerado.
- Criação de nova versão revisada.
- Aprovação de versão.
- Aprovação com publicação direta na biblioteca.
- Publicação manual de conteúdo já aprovado.
- Consulta de conteúdos compatíveis da biblioteca no contexto do planejamento.
- Reaproveitamento de conteúdo da biblioteca no campo de conteúdo final aprovado do planejamento.
- Tela federada de biblioteca pedagógica com filtros por tema e tipo de conteúdo.
- Rota e menu no host para acesso à biblioteca pedagógica.

## Arquitetura adotada

A implementação ficou no microfrontend existente, dentro do domínio de professor/planejamento, porque a separação para um microfrontend próprio de planejamento/IA está prevista como evolução futura no roadmap pós-MVP.

Foi mantido o contrato atual do backend monolítico, sem BFF e sem serviço independente de IA.

## Rotas

- `/planning/:id`
  - Detalhe do planejamento com a nova seção de IA e biblioteca.

- `/planning-library`
  - Biblioteca pedagógica federada.

## Fora do escopo

- Integração real com provedor de IA.
- Streaming de respostas.
- Editor rico de conteúdo.
- Permissões refinadas por papel na biblioteca.
- Novo microfrontend dedicado de planejamento/IA.
- BFF ou serviços independentes.

## Validação

- Microfrontend:
  - `npm run build`
  - Resultado: sucesso.

- Host:
  - `npm run build`
  - Resultado: sucesso.

## Próxima fase recomendada

Fase 46E - Polimento funcional e contratos de integração do planejamento com IA:

- Revisar pontos de experiência entre planejamento, conteúdo final aprovado e biblioteca.
- Definir contratos para futura integração real com IA.
- Preparar critérios para permissões e escopo multi-escola sem implementar ainda a separação de serviços.
