## Fase 43E - Migracao do dominio aluno para o microfrontend

Esta fase migra as telas de aluno para o microfrontend, mantendo o host como shell de seguranca e roteador federado.

### Escopo migrado

Foram movidas do host para o microfrontend as telas:

- listagem de alunos;
- cadastro/edicao de aluno;
- detalhe de aluno.

Diretorio de origem:

```text
school-management-web/host/src/app/aluno/pages
```

Diretorio de destino:

```text
school-management-web/microfrontend/src/app/aluno/pages
```

### Dependencias levadas para o microfrontend

Para manter as telas de aluno funcionais no remoto, foram adicionados:

- `aluno/models`;
- `aluno/services`;
- `historico`;
- `compartilhado`;
- dependencias ja existentes no remoto: `responsavel`, `documento` e `aluno/utils/cpf-validator.ts`.

O modulo `historico` foi levado como dependencia do painel escolar usado nas telas de aluno. A migracao funcional completa de historico como rota propria ainda deve ser tratada em fase separada.

### Escopo preservado no host

O host ainda mantem temporariamente:

```text
school-management-web/host/src/app/aluno/models
school-management-web/host/src/app/aluno/services
school-management-web/host/src/app/aluno/utils
```

Motivo: a tela de matricula ainda consome `StudentsService`, `student.model` e rotas de retorno para aluno. Esses vinculos devem ser removidos quando matricula for migrada ou quando for criado um contrato especifico.

### Ajustes no microfrontend

O `StudentsService` do microfrontend passou a usar `ShellContextService` para obter:

- `apiBaseUrl`;
- token de acesso.

O `StudentRecordsService` e o painel de historico copiados para o microfrontend tambem foram adaptados ao contrato de shell.

### Exposicao federada

O `federation.config.js` do microfrontend passou a expor:

```text
./AlunoList
./AlunoNew
./AlunoDetail
```

As rotas publicas no host foram preservadas:

```text
students
students/new
students/:id
students/:id/edit
```

Porem agora carregam componentes remotos do `mfe1`.

### Validacao

- `npm run build` em `school-management-web/microfrontend` executado com sucesso.
- `npm run build` em `school-management-web/host` executado com sucesso.
- `remoteEntry.json` gerado no build do microfrontend contem os exports `AlunoList`, `AlunoNew` e `AlunoDetail`.

### Proxima fase sugerida

Fase 43F - migrar `matricula` para o microfrontend.

Com catalogo, responsavel e aluno ja remotos, matricula passa a ser o proximo dominio natural, pois depende diretamente desses tres contextos.
