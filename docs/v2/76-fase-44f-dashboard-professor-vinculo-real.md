# Fase 44F - Dashboard do professor com vinculo real

## Objetivo

Tornar a Home do professor funcional quando o usuario autenticado estiver vinculado a um cadastro de professor.

## Decisao tecnica

O schema atual nao possuia vinculo direto entre `usuario` e `professor`.

Foi adicionada uma coluna opcional `professor.id_usuario`, com FK para `usuario` e unicidade para impedir que o mesmo usuario seja associado a mais de um professor.

Como compatibilidade operacional, o backend tambem consegue resolver o professor pelo e-mail da pessoa quando ainda nao houver `id_usuario` preenchido.

## Escopo realizado

- Adicionada migration incremental `V2__add_usuario_professor_link.sql`.
- Adicionado relacionamento opcional `ProfessorEntity.usuario`.
- Ajustado login/refresh para retornar `usuarioId` e `professorId`.
- Ajustado contrato do shell para publicar `usuarioId` e `professorId` ao microfrontend.
- Ajustado dashboard frontend para enviar `usuarioId` e, no perfil `PROFESSOR`, consumir `professorId` real.
- Mantida mensagem orientativa quando o usuario professor ainda nao tiver vinculo.

## Validacao esperada

- `.\mvnw.cmd test` em `school-management-service`.
- `npm run build` em `school-management-web/microfrontend`.
- `npm run build` em `school-management-web/host`.

## Proxima fase apos concluir

Avaliar personalizacao de dashboards por usuario ou telas administrativas de configuracao de dashboards.
