## Fase 43D - Migracao do dominio responsavel para o microfrontend

Esta fase migra as telas de responsavel para o microfrontend, mantendo o host como shell de seguranca e roteador federado.

### Escopo migrado

Foram movidas do host para o microfrontend as telas:

- listagem de responsaveis;
- cadastro/edicao de responsavel;
- detalhe de responsavel.

Diretorio de origem:

```text
school-management-web/host/src/app/responsavel/pages
```

Diretorio de destino:

```text
school-management-web/microfrontend/src/app/responsavel/pages
```

### Dependencias levadas para o microfrontend

Para manter as telas funcionais no remoto, foram adicionados:

- `responsavel/models`;
- `responsavel/services`;
- `documento`;
- `aluno/utils/cpf-validator.ts`.

O painel de documentos foi levado como dependencia de tela, mas o dominio `documento` ainda nao foi tratado como migracao funcional completa.

### Escopo preservado no host

O host ainda mantem temporariamente:

```text
school-management-web/host/src/app/responsavel/models
school-management-web/host/src/app/responsavel/services
```

Motivo: telas de aluno ainda consomem `ResponsiblesService` e `responsible.model` para vincular/desvincular responsaveis.

### Ajustes no microfrontend

O `ResponsiblesService` do microfrontend passou a usar `ShellContextService` para obter:

- `apiBaseUrl`;
- token de acesso.

O `DocumentsService` copiado para o microfrontend tambem foi adaptado ao contrato de shell.

### Exposicao federada

O `federation.config.js` do microfrontend passou a expor:

```text
./ResponsavelList
./ResponsavelNew
./ResponsavelDetail
```

As rotas publicas no host foram preservadas:

```text
responsibles
responsibles/new
responsibles/:id
responsibles/:id/edit
```

Porem agora carregam componentes remotos do `mfe1`.

### Validacao

- `npm run build` em `school-management-web/microfrontend` executado com sucesso.
- `npm run build` em `school-management-web/host` executado com sucesso.
- `remoteEntry.json` gerado no build do microfrontend contem os exports `ResponsavelList`, `ResponsavelNew` e `ResponsavelDetail`.

### Proxima fase sugerida

Fase 43E - preparar a migracao de `aluno` para o microfrontend.

Recomendacao: migrar `aluno` antes de `matricula`, porque matricula depende de aluno, responsavel e catalogo.
