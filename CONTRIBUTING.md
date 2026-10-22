# Contribuindo com o ds-cadastro-api

Ao participar, você concorda em seguir o [Código de Conduta](CODE_OF_CONDUCT.md). Para reportar um problema de segurança, **não** abra uma issue — siga o [SECURITY.md](SECURITY.md).

## Idioma

Issues, Pull Requests, commits e documentação em português do Brasil.

## Configurando o ambiente

1. Instale o Java 21 e o Docker.
2. Faça um fork (ou clone, se for mantenedor) e entre na pasta:

   ```bash
   git clone https://github.com/EcoCiente-Instituto-J-F/ds-cadastro-api.git
   cd ds-cadastro-api
   ```

3. Copie o arquivo de configuração e preencha com dados do **seu** banco de desenvolvimento (inclua também `JWT_SECRET`):

   ```bash
   cp .env.example .env     # Windows (PowerShell): Copy-Item .env.example .env
   ```

4. Rode a aplicação: `./gradlew bootRun` (Windows: `gradlew.bat bootRun`).

## Testando sua mudança

O CI executa:

```bash
./gradlew clean build --no-daemon
```

Os testes de integração (tag `integracao`, PostgreSQL via Testcontainers, exigem Docker) rodam à parte:

```bash
./gradlew integrationTest
```

Regras:

- Toda mudança de comportamento vem com um teste que falha sem ela.
- Testes não podem depender de rede, de API paga nem de dados de produção. Chamadas à ViaCEP e à BrasilAPI devem ser simuladas.

## Regras do projeto

- Nenhum segredo (senha, token, `JWT_SECRET`, `.env`) no repositório. Use apenas valores de exemplo, como em `.env.example` e `k8s/secret.example.yaml`.
- Nenhum dado pessoal real (CPF, CNPJ, e-mail, endereço) em testes ou exemplos.
- Nenhuma dependência nova sem discussão prévia em issue.
- Mantenha a arquitetura em camadas: regras de negócio em `core`, HTTP em `entrypoint`, persistência e clientes externos em `dataprovaider`.
- Senhas são sempre gravadas com hash (BCrypt); nunca registre senhas ou tokens em logs.
- Ao criar ou renomear uma variável de ambiente, atualize o `.env.example`.

## Pull requests

1. Crie a branch a partir da `main` (por exemplo `feat/...`, `fix/...`).
2. Faça commits focados, com prefixos `feat:`, `fix:`, `chore:`, `docs:`, `test:` ou `refactor:`.
3. Abra a PR para a `main`. A descrição é gerada pelo PR bot; revise-a.
4. O CI precisa passar.
5. Pelo menos um mantenedor revisa antes do merge.

## Reportando bugs e propondo funcionalidades

Abra uma issue descrevendo o comportamento esperado, o observado e, para bugs, os passos para reproduzir (com dados fictícios) e a versão/commit. Vulnerabilidades seguem o [SECURITY.md](SECURITY.md).
