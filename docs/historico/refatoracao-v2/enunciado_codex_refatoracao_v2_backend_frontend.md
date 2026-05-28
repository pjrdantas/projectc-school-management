# Enunciado para o Codex — Refatoração e modernização da solução de gestão escolar

## Objetivo geral
Refatorar o projeto existente de gestão escolar usando o novo modelo SQL normalizado. A refatoração deve ser feita em duas grandes etapas obrigatórias:

1. **Etapa 1 — Backend Java/Spring Boot**
2. **Etapa 2 — Frontend Angular**

Não iniciar a refatoração do frontend antes de estabilizar o backend, os contratos REST, DTOs, regras de negócio, autenticação/autorização e migrations da base.

## Contexto
O projeto atual já possui uma estrutura inicial com alunos, responsáveis, matrícula, turma, série, disciplina, documentos, transferência, histórico escolar, usuário, perfil e permissão. O novo modelo normaliza a base e amplia o sistema para gestão escolar completa, incluindo:

- pessoas, alunos, responsáveis, professores e funcionários;
- matrícula por tipo, status e etapas;
- documentos exigidos e documentos entregues;
- upload e armazenamento local de arquivos;
- transferência de entrada e saída;
- histórico escolar interno e externo;
- turmas, séries, disciplinas e período letivo;
- aulas, frequência de aluno e professor;
- avaliações, provas, notas e boletim;
- planejamento pedagógico e planejamento bimestral;
- apoio de IA ao planejamento do professor;
- armazenamento do conteúdo gerado por IA para reuso futuro;
- dashboards para professor, secretaria e diretor;
- exclusão lógica de aluno com autorização administrativa.

## Diretriz principal
A solução atual deve ser **refatorada**, não simplesmente remendada. Porém, não abandonar o projeto automaticamente. Primeiro reaproveitar o que fizer sentido da estrutura Java/Spring e Angular existente. Só recriar módulos do zero quando a estrutura atual estiver muito acoplada, desorganizada ou incompatível com o novo modelo.

---

# Etapa 1 — Refatoração do Backend Java/Spring Boot

## 1. Banco de dados e migrations

Usar Flyway para versionar o novo modelo SQL.

Regras:

1. Todos os IDs devem ser UUID.
2. Usar `gen_random_uuid()` no PostgreSQL.
3. Não usar entidade com ID numérico incremental.
4. Criar migrations organizadas por domínio.
5. Separar dados pessoais, documentos, endereço, matrícula, acadêmico, planejamento, dashboard e segurança.
6. Não manter tabelas antigas sem uso no modelo final.
7. Não criar tabelas backup como `aluno_old`, `matricula_backup`, etc.
8. Usar exclusão lógica para dados sensíveis e históricos.

## 2. Upload e armazenamento de documentos

O sistema deve permitir upload de documentos e armazenar inicialmente os arquivos em pasta local no servidor.

Importante:

- Não gravar o binário do arquivo dentro do banco.
- O banco deve armazenar apenas os metadados do arquivo.
- O arquivo físico deve ficar em diretório configurável.
- A solução deve permitir troca futura para S3, MinIO ou outro storage sem alterar a regra de negócio.

A tabela `documento` deve guardar informações como:

- tipo do documento;
- nome original do arquivo;
- nome físico gerado pelo sistema;
- provider de armazenamento, inicialmente `LOCAL`;
- diretório base;
- caminho relativo;
- caminho absoluto, se necessário;
- URL de acesso, se houver;
- content type;
- extensão;
- tamanho em bytes;
- hash SHA-256;
- data de upload;
- usuário responsável pelo upload;
- status ativo/inativo.

Organização sugerida para armazenamento local:

```text
/uploads/escola/{idEscola}/alunos/{idAluno}/matriculas/{idMatricula}/documentos/{idDocumento}/arquivo.pdf
```

Exemplo de configuração no `application.yml`:

```yaml
app:
  storage:
    provider: LOCAL
    local-base-path: C:/gestao-escolar/uploads
```

Criar serviço de storage:

- `StorageService`
- `LocalStorageService`
- futuramente `S3StorageService` ou `MinioStorageService`

Endpoints esperados:

- `POST /api/documentos/upload`
- `GET /api/documentos/{id}/download`
- `GET /api/documentos/{id}/metadata`
- `DELETE /api/documentos/{id}` para exclusão lógica

O upload deve validar:

- tamanho máximo;
- extensão permitida;
- content type;
- tipo de documento permitido;
- vínculo com pessoa, matrícula ou histórico escolar.

## 3. Modelagem de pessoas

Criar/refatorar entidades:

- `Pessoa`
- `Aluno`
- `Responsavel`
- `Professor`
- `Funcionario`
- `PessoaEndereco`
- `PessoaDocumento`

A tabela `pessoa` deve conter dados comuns. As especializações devem referenciar `pessoa`.

## 4. Matrícula por etapas

Criar/refatorar:

- `Matricula`
- `TipoMatricula`
- `StatusMatricula`
- `EtapaMatriculaModelo`
- `MatriculaEtapa`
- `StatusEtapaMatricula`
- `MatriculaDocumentoExigido`
- `MatriculaDocumentoEntregue`

Fluxos obrigatórios:

### Transferência de entrada
1. Pedido/declaracão de transferência.
2. Documentos do aluno.
3. Documentos do responsável, quando aluno for menor de idade e não emancipado.
4. Comprovante de residência.
5. Histórico escolar.
6. Efetivação da matrícula.

### Primeira matrícula
1. Documentos do aluno.
2. Documentos do responsável, quando necessário.
3. Comprovante de residência.
4. Efetivação da matrícula.

### Renovação
1. Atualização cadastral.
2. Comprovante de residência, se exigido pela escola.
3. Efetivação da renovação.

## 5. Transferência e histórico escolar

Criar/refatorar:

- `TransferenciaAluno`
- `HistoricoEscolar`
- `HistoricoEscolarItem`
- `AlunoHistoricoEvento`

O histórico escolar deve suportar:

- histórico interno gerado pelo sistema;
- histórico externo recebido de outra escola;
- emissão ao concluir estudos;
- emissão ao solicitar transferência;
- registro de eventos importantes na linha do tempo do aluno.

## 6. Acadêmico

Criar/refatorar:

- `PeriodoLetivo`
- `Serie`
- `Disciplina`
- `Turma`
- `TurmaDisciplina`
- `ProfessorTurmaDisciplina`
- `Aula`
- `FrequenciaAluno`
- `FrequenciaProfessor`
- `Avaliacao`
- `NotaAluno`
- `Boletim`
- `BoletimItem`

A avaliação deve representar prova, trabalho, atividade, recuperação ou outro tipo avaliativo.

## 7. Planejamento pedagógico com IA

Criar/refatorar:

- `PlanejamentoProfessor`
- `PlanejamentoAula`
- `PlanejamentoBimestral`
- `PlanejamentoBimestralAula`
- `PlanejamentoBimestralAvaliacao`
- `PlanejamentoIaInteracao`
- `PlanejamentoIaConteudoGerado`
- `PlanejamentoIaVersao`
- `BibliotecaPedagogica`

O professor deve poder:

1. Criar um planejamento bimestral.
2. Informar tema e descrição inicial.
3. Acionar IA para sugerir conteúdo.
4. Refinar o conteúdo com novas interações.
5. Aprovar uma versão final.
6. Dividir o tema em aulas.
7. Planejar avaliações/provas.
8. Salvar o conteúdo gerado para reutilização futura.
9. Reaproveitar conteúdos já aprovados sem chamar novamente a IA.

## 8. Dashboards

Criar suporte backend para dashboards:

- dashboard do professor;
- dashboard da secretaria;
- dashboard do diretor.

Estruturas:

- `Dashboard`
- `DashboardWidget`
- `DashboardUsuarioConfiguracao`
- `DashboardIndicadorSnapshot`
- views de resumo para professor, secretaria e diretor.

O professor deve visualizar:

- turmas vinculadas;
- aulas planejadas;
- aulas realizadas;
- frequência pendente;
- avaliações pendentes;
- boletins/notas pendentes;
- planejamentos bimestrais.

A secretaria deve visualizar:

- matrículas solicitadas;
- matrículas em andamento;
- documentos pendentes;
- históricos pendentes;
- transferências;
- solicitações de exclusão.

O diretor deve visualizar:

- visão geral da escola;
- alunos ativos;
- professores ativos;
- turmas ativas;
- matrículas efetivadas;
- aulas registradas;
- avaliações registradas;
- históricos emitidos;
- indicadores administrativos e acadêmicos.

## 9. Segurança e autorização

Manter/refatorar:

- `Usuario`
- `Perfil`
- `Permissao`
- `UsuarioPerfil`
- `PerfilPermissao`
- `SessaoAutenticacao`

Garantir perfis como:

- ADMIN
- DIRETOR
- SUPERVISOR
- SECRETARIA
- PROFESSOR
- RESPONSAVEL
- ALUNO, se necessário

## 10. Exclusão de aluno com autorização

Criar/refatorar:

- `SolicitacaoExclusaoAluno`

Regra:

1. Secretaria solicita exclusão.
2. Supervisor ou diretor aprova/reprova.
3. Se aprovada, fazer exclusão lógica.
4. Registrar evento na linha do tempo do aluno.
5. Não apagar histórico, matrículas, documentos, notas ou frequência.

## 11. Arquitetura backend sugerida

Pacotes:

- `domain.model`
- `domain.catalog`
- `repository`
- `service`
- `controller`
- `dto.request`
- `dto.response`
- `mapper`
- `exception`
- `security`
- `storage`
- `dashboard`
- `ai`

Regras:

1. Usar DTOs para entrada e saída.
2. Não expor entidades JPA diretamente na API.
3. Criar services com regras de negócio.
4. Criar repositories Spring Data JPA.
5. Usar Bean Validation.
6. Criar exceptions específicas.
7. Criar endpoints REST organizados por contexto.
8. Usar paginação estável com DTO/PageResponse.
9. Evitar serializar `PageImpl` diretamente.
10. Criar testes básicos para fluxos principais.

---

# Etapa 2 — Refatoração do Frontend Angular

Executar somente depois que o backend estiver estável.

## 1. Objetivo

Refatorar o frontend existente para consumir a nova API REST e organizar a aplicação por módulos/contextos escolares.

## 2. Estrutura sugerida

Módulos ou features:

- pessoas
- alunos
- responsáveis
- professores
- funcionários
- matrículas
- documentos
- transferências
- histórico escolar
- turmas
- disciplinas
- aulas
- frequência
- avaliações
- boletim
- planejamento professor
- planejamento IA
- dashboards
- segurança/usuários/perfis

## 3. Telas principais

Criar/refatorar telas para:

- cadastro de aluno;
- cadastro de responsável;
- matrícula por etapas;
- upload de documentos;
- acompanhamento de pendências;
- transferência de entrada/saída;
- histórico escolar;
- diário de aula;
- frequência;
- lançamento de notas;
- boletim;
- planejamento bimestral;
- assistente IA para planejamento;
- biblioteca de conteúdos salvos;
- dashboard professor;
- dashboard secretaria;
- dashboard diretor;
- solicitação/aprovação de exclusão de aluno.

## 4. Upload no frontend

Criar componente de upload com:

- seleção de arquivo;
- progresso de upload;
- validação de tipo e tamanho;
- listagem dos documentos enviados;
- download;
- exclusão lógica;
- indicação de documento obrigatório/pendente/entregue.

## 5. UX de matrícula

A matrícula deve ser apresentada como wizard/stepper:

- etapa atual;
- documentos pendentes;
- documentos entregues;
- histórico escolar pendente;
- botão para efetivar matrícula somente quando as regras forem atendidas.

## 6. UX do planejamento com IA

O professor deve poder:

- criar tema bimestral;
- escrever descrição inicial;
- solicitar sugestão da IA;
- visualizar resposta;
- refinar com novas instruções;
- salvar versão aprovada;
- transformar versão aprovada em aulas;
- transformar versão aprovada em avaliações/provas;
- reutilizar conteúdo salvo.

## 7. Dashboards

Criar dashboards separados por perfil:

- professor;
- secretaria;
- diretor.

Cada dashboard deve consumir endpoints específicos do backend e exibir indicadores, cards, listas de pendências e atalhos de ação.

---

# Ordem recomendada de execução

1. Criar branch nova para a refatoração.
2. Aplicar migrations do banco.
3. Refatorar entidades JPA.
4. Criar repositories.
5. Criar DTOs e mappers.
6. Criar services por domínio.
7. Criar controllers REST.
8. Implementar storage local de documentos.
9. Implementar matrícula por etapas.
10. Implementar histórico escolar e transferência.
11. Implementar acadêmico: aula, frequência, avaliação, nota e boletim.
12. Implementar planejamento bimestral e IA.
13. Implementar dashboards.
14. Criar testes básicos.
15. Só depois refatorar o frontend Angular.
16. Ajustar telas para consumir a nova API.
17. Validar fluxo completo ponta a ponta.

## Observação final

Não abandonar automaticamente o projeto atual. Como já existe uma estrutura, a melhor abordagem é refatorar em cima dela quando possível. Porém, se algum módulo estiver muito incompatível com o novo modelo, recriar o módulo de forma limpa é melhor do que carregar dívida técnica.
