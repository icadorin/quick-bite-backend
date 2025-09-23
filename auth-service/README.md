# 🔐 Auth Service

Microsserviço responsável pela autenticação e autorização de usuários na plataforma QuickBite.

## 🚀 Funcionalidades

-   Registro de novos usuários
-   Login e geração de tokens JWT
-   Validação de tokens e rotas protegidas

## 🛠️ Dependências

-   `Spring Boot DevTools`
-   `Spring Web`
-   `Spring Data JPA`
-   `Eureka Discovery Client`
-   `PostgreSQL Driver`
-   `Lombok`
-   `Spring Security`

## ⚙️ Configuração

**Porta:** `8082`  
**Banco de Dados:** PostgreSQL (Neon.tech)  
**Service Discovery:** Eureka Server (`8761`)

## 🏃‍♂️ Como Executar

```bash
cd services/auth-service
mvn spring-boot:run
