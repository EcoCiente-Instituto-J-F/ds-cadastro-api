<p align="center">
  <img src="assets/card-readme.png" alt="Card do repositório" width="100%">
</p>

# ds-cadastro-api

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-wrapper-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-JDBC-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-jjwt_0.12.6-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Swagger](https://img.shields.io/badge/OpenAPI-springdoc_2.8.9-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-manifests-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)
![GitHub repo size](https://img.shields.io/github/repo-size/EcoCiente-Instituto-J-F/ds-cadastro-api?style=for-the-badge)
![GitHub last commit](https://img.shields.io/github/last-commit/EcoCiente-Instituto-J-F/ds-cadastro-api?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)

> API responsável por cadastrar usuários no sistema da EcoCiente: usuários comuns, moradores de condomínios e cooperativas.

## Sobre o projeto

O `ds-cadastro-api` expõe endpoints REST para:

- cadastrar **usuário comum**, **morador** (vinculado a um condomínio) e **cooperativa**;
- validar o **CEP** pela [ViaCEP](https://viacep.com.br) e o **CNPJ** da cooperativa pela [BrasilAPI](https://brasilapi.com.br);
- permitir que o usuário autenticado (JWT) **atualize seus dados e endereço** e o endereço de um condomínio.

A senha é armazenada como hash BCrypt. O código segue arquitetura hexagonal (`core`, `entrypoint`, `dataprovaider`). A documentação interativa (Swagger UI) fica em `/swagger-ui/index.html`.

### Endpoints

| Método | Rota | Autenticação | Descrição |
| --- | --- | --- | --- |
| POST | `/api/cadastro/comum` | Não | Cadastra usuário comum |
| POST | `/api/cadastro/morador` | Não | Cadastra morador de condomínio |
| POST | `/api/cadastro/cooperativa` | Não | Cadastra cooperativa (valida CNPJ) |
| PATCH | `/api/usuarios/me` | JWT | Atualiza dados do usuário autenticado |
| PATCH | `/api/usuarios/me/endereco` | JWT | Atualiza o endereço do usuário autenticado |
| PATCH | `/api/condominios/{id}/endereco` | JWT | Atualiza o endereço de um condomínio |
| PATCH | `/api/condominios/{id}` | JWT | Atualiza nome, tipo e foto (`urlFoto`) do condomínio; só o síndico responsável |

O JWT é validado com o segredo `JWT_SECRET` e deve conter o claim `usuarioId` e expiração. Envie-o no header `Authorization: Bearer <token>`.

## Pré-requisitos

- Java 21 (toolchain definida no `build.gradle`)
- PostgreSQL acessível (o schema é apenas validado: `ddl-auto=validate`)
- Docker (para `docker compose` e para os testes de integração, que usam Testcontainers)

## Instalando

```bash
git clone https://github.com/EcoCiente-Instituto-J-F/ds-cadastro-api.git
cd ds-cadastro-api
cp .env.example .env     # Windows (PowerShell): Copy-Item .env.example .env
```

Edite o `.env` com os dados do seu banco. A aplicação lê o arquivo `.env` na raiz (`spring.config.import`).

| Variável | Descrição | Padrão |
| --- | --- | --- |
| `APP_PORT` | Porta publicada pelo Docker Compose | `9090` |
| `DB_URL` | URL JDBC do PostgreSQL | — |
| `DB_USER` | Usuário do banco | — |
| `DB_PASSWORD` | Senha do banco | — |
| `JWT_SECRET` | Segredo HMAC usado para validar os JWT (obrigatório; ainda não consta no `.env.example`) | — |
| `SPRING_DATASOURCE_HIKARI_*` | Ajustes do pool de conexões (`MAXIMUMPOOLSIZE`, `MINIMUMIDLE`, `CONNECTIONTIMEOUT`, `IDLETIMEOUT`, `MAXLIFETIME`) | veja `.env.example` |

## Usando

Localmente:

```bash
./gradlew bootRun        # Windows: gradlew.bat bootRun
```

Com Docker Compose:

```bash
docker compose up --build
```

A API sobe em `http://localhost:9090`. Há também manifestos Kubernetes em `k8s/` (namespace, configmap, secret de exemplo, deployment e service).

## Testes

Testes unitários e build (comando executado pelo CI):

```bash
./gradlew clean build --no-daemon
```

Testes de integração com PostgreSQL (exigem Docker):

```bash
./gradlew integrationTest
```

## Estrutura do projeto

```text
.
├── src/main/java/br/com/ecociente/cadastro
│   ├── config/          # Segurança, JWT e Swagger
│   ├── core/            # Domínio, casos de uso, serviços, gateways e exceções
│   ├── dataprovaider/   # Entidades JPA, repositórios, mappers e clientes (ViaCEP, BrasilAPI)
│   └── entrypoint/      # Controllers, DTOs, mappers e tratamento global de erros
├── src/main/resources/  # application.properties
├── src/test/            # Testes unitários e de integração
├── k8s/                 # Manifestos Kubernetes
├── scripts/pr-bot/      # Bot que gera a descrição das Pull Requests
├── .github/workflows/   # CI e PR bot
├── Dockerfile
└── docker-compose.yml
```

## Contribuindo

Leia o [CONTRIBUTING.md](CONTRIBUTING.md) antes de abrir uma Pull Request. Ao participar, você concorda com o [Código de Conduta](CODE_OF_CONDUCT.md).

## Segurança

Encontrou uma vulnerabilidade? Siga o [SECURITY.md](SECURITY.md). **Não abra issue pública.**

## Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE).
