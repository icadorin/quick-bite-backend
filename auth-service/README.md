# Auth Service

Microsserviço responsável por autenticação, registro e gestão de usuários.

**Porta:** 8082

## Funcionalidades

- Registro de novos usuários com validação
- Login com geração de JWT (access + refresh token)
- Renovação de token
- Logout (invalidação de refresh token)
- CRUD de usuários com segurança por papel
- Perfil de usuário com informações adicionais

## Endpoints

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | /api/auth/register | Registro de usuário |
| POST | /api/auth/login | Login |
| POST | /api/auth/refresh-token | Renovar token |
| POST | /api/auth/logout | Logout |
| GET | /api/users/{id} | Buscar usuário |
| PUT | /api/users/{id} | Atualizar usuário |
| GET | /api/users | Listar usuários (ADMIN) |

## Entidades

- User: email, passwordHash, fullName, role, status
- UserProfile: phone, address, avatarUrl, preferências
- RefreshToken: token, expiresAt, revoked

## Dependências

- Spring Boot Web
- Spring Security
- Spring Data JPA
- Spring Boot Validation
- Spring Boot Actuator
- JJWT (API, Impl, Jackson)
- Lombok
- MapStruct
- QuickBite Core
- PostgreSQL (runtime)
- H2 (testes)
- Testcontainers (testes)

## Execução
```bash
    cd auth-service
    mvn spring-boot:run
```
