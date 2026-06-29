# QuickBite - Backend (Monorepo)

## Visão Geral

Sistema de delivery de comida desenvolvido em Java 21 com arquitetura de microsserviços. Organizado em um monorepo gerenciado pelo Maven.

## Microsserviços

| Serviço | Porta | Descrição |
|---------|-------|-----------|
| API Gateway | 8081 | Roteamento de requisições |
| Auth Service | 8082 | Autenticação e gestão de usuários |
| Product Service | 8083 | Gestão de restaurantes, produtos e categorias |
| Order Service | 8084 | Ciclo de vida de pedidos |
| QuickBite Core | - | Componentes compartilhados |

## Tecnologias

- Java 21
- Spring Boot 4.0.6
- Spring Cloud 2025.1.1 (Gateway, Feign, LoadBalancer)
- Spring Security 7.0.5 (JWT)
- Spring Data JPA
- PostgreSQL
- H2 Database (testes)
- JJWT 0.12.6
- MapStruct 1.6.3
- Lombok 1.18.34
- Maven 3.9.11

## Estrutura

```
quick-bite-backend/
├── api-gateway/
├── auth-service/
├── product-service/
├── order-service/
├── quickbite-core/
├── pom.xml
└── .github/workflows/ci-backend.yml
```

## Módulo Core (quickbite-core)

Componentes reutilizáveis:

- `BaseEntity`: Classe base com createdAt e updatedAt
- `UserRole`: Papéis (CUSTOMER, RESTAURANT_OWNER, ADMIN)
- Exceções customizadas (ResourceNotFoundException, BusinessRuleViolationException, etc)
- `ErrorResponse` e `ApiError`: Padronização de respostas de erro
- `PatchMapperConfig`: Configuração para mapeamento parcial com MapStruct

## Serviços

### API Gateway

- Roteamento baseado no path da URL
- Mapeamento: `/api/v1/auth/**` → Auth Service, `/api/v1/products/**` → Product Service, `/api/v1/orders/**` → Order Service

### Auth Service

Endpoints:

- `POST /api/auth/register` - Registro
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh-token` - Renovação de token
- `POST /api/auth/logout` - Logout
- `GET /api/users/{id}` - Buscar usuário
- `PUT /api/users/{id}` - Atualizar usuário
- `GET /api/users` - Listar usuários (ADMIN)

### Product Service

Endpoints:

- **Restaurantes**: CRUD, busca por nome, cozinha, avaliação, validação de existência
- **Categorias**: CRUD
- **Produtos**: CRUD, filtros por restaurante, categoria, preço e disponibilidade

### Order Service

Endpoints:

- `POST /api/v1/orders` - Criar pedido
- `GET /api/v1/orders` - Listar pedidos do usuário
- `PATCH /api/v1/orders/{id}/status` - Atualizar status (RESTAURANT_OWNER)
- `POST /api/v1/orders/{id}/cancel` - Cancelar pedido
- `GET /api/v1/orders/{id}` - Detalhes do pedido
- `GET /api/v1/orders/restaurant/{restaurantId}` - Pedidos de um restaurante

Integração com Product Service via Feign Client.

Transições de status: PENDING → CONFIRMED → PREPARING → READY_FOR_PICKUP → OUT_FOR_DELIVERY → DELIVERED

## Segurança

Autenticação baseada em JWT.

Fluxo:

1. Auth Service gera accessToken e refreshToken
2. Cliente envia accessToken no header `Authorization: Bearer <token>`
3. Filtro JWT valida token e extrai userId, role e restaurantId
4. `@PreAuthorize` controla acesso baseado no papel

Papéis:

- CUSTOMER: Cria pedidos e visualiza próprios dados
- RESTAURANT_OWNER: Gerencia restaurantes e pedidos do seu restaurante
- ADMIN: Acesso total

## Execução

Pré-requisitos: Java 21, Maven 3.9+, PostgreSQL

```bash
git clone <URL>
cd quick-bite-backend

# Configurar datasources nos application.properties

mvn clean compile

# Executar serviços (terminais separados)
cd auth-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run

# Acessar
http://localhost:8081
http://localhost:8081/actuator/health
```

## Testes

```bash
mvn test
```

## CI/CD

Pipeline GitHub Actions em `.github/workflows/ci-backend.yml`.

Executa build e testes em push/pull request para branches main/master.

Ambiente de teste: H2 em memória.