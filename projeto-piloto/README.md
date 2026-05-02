# 🚀 Projeto Piloto - Hexagonal Auth Skeleton

Este projeto é um esqueleto de autenticação e gestão de aplicativos desenvolvido com **Spring Boot 3**, utilizando os princípios da **Arquitetura Hexagonal (Ports & Adapters)** para garantir um sistema desacoplado, testável e de fácil manutenção.

---

** ✅ Objetivo do projeto **

Fornecer uma base sólida para sistemas que precisam de autenticação, autorização por perfis e separação clara entre domínio, aplicação e infraestrutura, com foco em:

- Evolução segura do domínio sem acoplamento à infraestrutura.
- Facilidade para adicionar novos adaptadores (REST, mensageria, persistência).
- Configuração flexível por ambientes (dev, docker, prod).

---

** 🧩 Principais funcionalidades **

- Autenticação JWT Stateless.
- Gerenciamento de usuários, perfis e permissões.
- Integração com PostgreSQL em todos os ambientes.
- Migrações versionadas com Flyway.
- Documentação automática via Swagger/OpenAPI.

---

** 🛠️ Tecnologias e Especificações **
- **Java:** 21
- **Spring Boot:** 4.x
- **Autenticação:** JWT Stateless (JSON Web Token)
- **Banco de Dados Local:** PostgreSQL
- **Banco de Dados Nuvem:** PostgreSQL (AWS RDS)
- **Migrações:** Flyway com suporte a baseline.
- **Segurança:** Spring Security com configuração de filtros customizados.

---

** 📂 Estrutura de Diretórios (Hierarquia Completa) **

A organização segue o padrão da arquitetura hexagonal, separando o domínio da infraestrutura e dos adaptadores de entrada e saída.

```text
C:.
├── 📄 deploy.bat                # Automação de build e deploy Docker local
├── 📄 Dockerfile                # Configuração da imagem do container
├── 📄 pom.xml                   # Gerenciador de dependências Maven
├── 📄 README.md                 # Documentação do projeto
├── 📁 .github/workflows/        # Pipeline de CI/CD (GitHub Actions)
│   └── ci-cd.yml
├── 📁 k8s/                      # Manifestos de Orquestração Kubernetes
│   ├── deployment.yaml
│   └── nginx-config.yaml
├── 📁 src/main/java/br/com/projeto/piloto/
│   ├── 🚀 ProjetoPilotoApplication.java
│   │
│   ├── 🔌 adapter/              # Camada de Adaptadores
│   │   ├── 📥 in/web/           # Entrada via REST API
│   │   │   ├── 🎮 controller/   # Aplicativos, Auth, Perfil, Permissao, Usuario
│   │   │   ├── 📦 dto/          # Requests, Responses e Resumos (Data Transfer Objects)
│   │   │   └── ⚠️ exception/    # ErrorResponse e GlobalExceptionHandler
│   │   └── 📤 out/jpa/          # Saída via Persistência (JPA)
│   │       ├── 💾 entity/       # Entidades mapeadas (Aplicativo, Perfil, etc.)
│   │       ├── 🗺️ mapper/       # Mapeadores MapStruct/ModelMapper
│   │       ├── 🏛️ repository/   # Interfaces Spring Data JPA
│   │       └── 🔌 AdapterImpl/   # Implementações dos Ports de saída
│   │
│   ├── 🏛️ application/         # Camada de Aplicação
│   │   └── ⚙️ usecase/          # Interactors (Implementação da lógica de negócio)
│   │
│   ├── 🏗️ domain/              # Camada de Domínio (O Coração do Sistema)
│   │   ├── ⚠️ exception/        # Exceções de Domínio (DomainException)
│   │   ├── 💎 model/           # Modelos de Domínio Puros
│   │   └── 🏁 port/             # Interfaces de Entrada (Inbound) e Saída (Outbound)
│   │
│   └── ⚙️ infrastructure/      # Configurações Técnicas
│       ├── 📝 config/           # AuthProperties, JwtProperties, SwaggerConfig
│       └── 🔒 security/         # JwtUtil, SecurityConfig, CustomUserDetailsService
│
├── 📁 src/main/resources/
│   ├── 📄 application.yml       # Configurações de Profiles (dev, docker, prod)
│   └── 📁 db/migration/         # Scripts Flyway (V1__create_tables.sql)
│
└── 📁 src/test/java/            # Testes Unitários e de Integração
    └── controller/              # Testes específicos para os endpoints Web

```

---

** 🚀 Como Executar a Aplicação **

** 1. Preparação do Ambiente **

Certifique-se de configurar o arquivo src/main/resources/application.yml com as credenciais corretas:

PostgreSQL Connection: Ajuste a URL, username e password.

JWT Secret: Defina uma chave forte no campo auth.jwt.secret.


> ⚠️ Se aparecer `FATAL: autenticação do tipo senha falhou para o usuário "postgres"` (SQLState `28P01`),
> a aplicação está usando credenciais diferentes das configuradas no seu PostgreSQL local.
> Defina as variáveis `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD` antes de subir o app.

> ℹ️ Para ambientes legados (`dev`/`docker`) com histórico Flyway antigo, a aplicação executa `flyway repair` automaticamente antes do `migrate`, corrigindo checksum divergente da migration V2.

Exemplo (Linux/macOS):
```bash
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=root123
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

Exemplo (Windows PowerShell):
```powershell
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="root123"
$env:SPRING_PROFILES_ACTIVE="dev"
mvn spring-boot:run
```



** 2. Execução via Maven **

Para rodar diretamente pela linha de comando ou IDE:

```bash
mvn spring-boot:run
```


** 3. Deploy Local via Docker **

Utilize o script automatizado que gerencia o build da imagem e a execução do container:

```bash
./deploy.bat

```

---

** ⚙️ Perfis e configurações **

O projeto utiliza o `application.yml` para gerenciar diferentes ambientes:

- **dev**: execução local com PostgreSQL.
- **docker**: execução em container com PostgreSQL.
- **prod**: execução em produção (PostgreSQL AWS RDS).

Para definir um perfil em execução local:

```bash
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

---

** 🔐 Variáveis importantes **

As principais propriedades do projeto (definidas no `application.yml`) incluem:

- `auth.jwt.secret`: chave de assinatura do JWT.
- `spring.datasource.url`, `username`, `password`: conexão com o banco.
- `spring.datasource.url`: use o banco `projeto_piloto` (ex.: `jdbc:postgresql://localhost:5432/projeto_piloto`).
- `flyway.baseline-on-migrate`: habilita baseline quando necessário.

Recomenda-se manter os segredos fora do repositório em ambientes produtivos, usando:

- variáveis de ambiente;
- gerenciadores de segredo (AWS Secrets Manager, Vault etc.).

---

** 🧪 Como executar testes **

```bash
mvn test
```

---

** 🚢 Deploy e infraestrutura **

O projeto possui suporte inicial para deploy via Docker e Kubernetes:

- `Dockerfile`: build da aplicação em container.
- `k8s/`: manifestos de deployment e configuração de nginx.

Para buildar uma imagem local:

```bash
docker build -t projeto-piloto .
```

---

** 4. Carga de Dados Inicial **

Crie os dados iniciais (Perfis, Permissões e Usuário Admin) utilizando os scripts SQL ou componentes de bootstrap da aplicação.

** 📖 Documentação da API (Endpoints) **

O projeto expõe a documentação interativa através do Swagger/OpenAPI:

```
👉 Swagger UI: http://localhost:8080/swagger-ui/index.html
👉 OpenAPI JSON: http://localhost:8080/v3/api-docs
👉 OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml

```

** 🔒 Segurança **
A API utiliza autenticação Stateless via JWT.

Os endpoints de /auth (login/register) são públicos.

Os demais endpoints exigem o Header Authorization: Bearer <TOKEN>.

O controle de acesso é baseado em perfis (Roles) carregados do banco de dados.

