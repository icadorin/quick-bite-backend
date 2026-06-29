# API Gateway

Gateway para roteamento de requisições para os microsserviços.

**Porta:** 8081

## Funcionalidades

- Roteamento dinâmico baseado no path da URL
- Proxy de requisições HTTP via WebClient
- Tratamento de erros dos serviços backend
- Logging de requisições e respostas

## Endpoints Roteados

| Prefixo | Serviço | Porta |
|---------|---------|-------|
| /api/v1/auth/** | Auth Service | 8082 |
| /api/v1/products/** | Product Service | 8083 |
| /api/v1/restaurants/** | Product Service | 8083 |
| /api/v1/categories/** | Product Service | 8083 |
| /api/v1/orders/** | Order Service | 8084 |

## Dependências

- Spring Boot WebFlux
- Spring Cloud Gateway Server WebFlux
- Spring Cloud LoadBalancer
- Spring Boot Actuator
- Lombok
- QuickBite Core

## Execução

```bash
    cd api-gateway
    mvn spring-boot:run
```