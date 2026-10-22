# Política de Segurança

O `ds-cadastro-api` trata dados pessoais de cadastro: nome, e-mail, CPF, data de nascimento, endereço, senha (armazenada como hash) e, para cooperativas, CNPJ, e-mail e telefone.

## Versões suportadas

| Versão | Suportada |
| --- | --- |
| `main` (mais recente) | Sim |
| Demais branches e versões anteriores | Não |

## Reportando uma vulnerabilidade

**Não abra issue, Pull Request ou discussão pública.**

1. Acesse <https://github.com/EcoCiente-Instituto-J-F/ds-cadastro-api/security/advisories/new> (Security → Advisories → Report a vulnerability).
2. Descreva o problema incluindo:
   - o impacto;
   - os passos para reproduzir, **com dados falsos**;
   - o commit testado e a configuração usada;
   - se tiver, uma ideia de correção.

> Mantenedores: ative o *Private vulnerability reporting* em Settings → Code security.

O que acontece depois: confirmaremos o recebimento, corrigiremos de forma privada e publicaremos um advisory com crédito a quem reportou. Pedimos um prazo razoável para a correção antes de qualquer divulgação pública.

## Escopo

**Dentro do escopo**

- Acesso a dados de outro usuário (por exemplo, atualizar dados ou endereço de terceiros).
- Burla de autenticação ou autorização (JWT).
- Injeção SQL ou de comandos com exposição de dados.
- Vazamento de dados pessoais ou segredos em logs ou respostas de erro.
- Segredos commitados no repositório.

**Fora do escopo**

- Bugs sem impacto de segurança.
- Falhas em serviços de terceiros (ViaCEP, BrasilAPI).
- Credenciais de exemplo (`SEU_USUARIO`, `SUA_SENHA`) dos arquivos `.env.example` e `k8s/secret.example.yaml`.
- Negação de serviço no seu próprio deploy.

## Como o sistema protege os dados

| Camada | Onde | O que faz |
| --- | --- | --- |
| Senhas | `config/SecurityConfig.java` | Hash BCrypt antes de gravar (`senha_hash`) |
| Autenticação | `config/security/JwtAuthenticationFilter.java`, `JwtService.java` | Valida assinatura e expiração do JWT; sessão stateless |
| Autorização | `config/SecurityConfig.java` | `/api/usuarios/**` e `/api/condominios/**` exigem autenticação; o usuário vem do claim `usuarioId` do token |
| Validação de entrada | `entrypoint/dto/request/` | Bean Validation (e-mail, CPF, CEP, regra de senha) e rejeição de campos desconhecidos no JSON |
| Erros | `entrypoint/exception/GlobalExceptionHandler.java` | Respostas padronizadas sem expor stack trace |
| Schema | `application.properties` | `ddl-auto=validate` (a aplicação não altera o banco) |

## Checklist de deploy

- Defina `JWT_SECRET` forte e exclusivo por ambiente.
- Mantenha segredos (`DB_*`, `JWT_SECRET`) fora de ConfigMap, imagem e repositório; use Secret do Kubernetes.
- Use no banco uma role com o mínimo de permissões necessárias e conexão com `sslmode=require`.
- Troque todos os valores de exemplo (`SEU_*`, `SUA_*`).
- Considere restringir o acesso ao Swagger UI (`/swagger-ui/index.html`) e a `/api-docs` em produção.
