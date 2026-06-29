# Product Service

Microsserviço responsável por restaurantes, produtos e categorias.

**Porta:** 8083

## Funcionalidades

- CRUD de restaurantes
- CRUD de categorias
- CRUD de produtos
- Filtros por restaurante, categoria, preço e disponibilidade
- Produtos em destaque (featured)
- Validação de existência de restaurantes
- Contagem de produtos por restaurante/categoria
- Busca por nome, cozinha, avaliação

## Endpoints

### Restaurantes

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | /api/v1/restaurants | Listar restaurantes |
| GET | /api/v1/restaurants/{id} | Buscar restaurante |
| GET | /api/v1/restaurants/owner/{ownerId} | Restaurantes por dono |
| GET | /api/v1/restaurants/search | Buscar por nome |
| GET | /api/v1/restaurants/{id}/exists | Validar existência |
| GET | /api/v1/restaurants/cuisine/{cuisineType} | Buscar por cozinha |
| GET | /api/v1/restaurants/rating | Buscar por avaliação mínima |
| POST | /api/v1/restaurants | Criar restaurante |
| PUT | /api/v1/restaurants/{id} | Atualizar |
| DELETE | /api/v1/restaurants/{id} | Deletar |

### Categorias

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | /api/v1/categories | Listar categorias |
| POST | /api/v1/categories | Criar (ADMIN) |
| PUT | /api/v1/categories/{id} | Atualizar (ADMIN) |
| DELETE | /api/v1/categories/{id} | Deletar (ADMIN) |

### Produtos

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | /api/v1/products | Listar produtos |
| GET | /api/v1/products/{id} | Buscar produto |
| GET | /api/v1/products/featured | Produtos em destaque |
| GET | /api/v1/products/by-restaurant/{restaurantId}/count | Contar produtos |
| POST | /api/v1/products | Criar |
| PUT | /api/v1/products/{id} | Atualizar |
| DELETE | /api/v1/products/{id} | Deletar |

## Entidades

- Restaurant: ownerId, name, description, address, phone, email, cuisineType, rating
- Category: name, description, imageUrl, sortOrder, isActive
- Product: restaurant, category, name, price, comparePrice, costPrice, isAvailable, isFeatured

## Dependências

- Spring Boot Web
- Spring Data JPA
- Spring Boot Validation
- Spring Security
- Spring Boot Actuator
- JJWT (API, Impl, Jackson)
- Lombok
- MapStruct
- QuickBite Core
- PostgreSQL (runtime)
- H2 (testes)

## Execução

```bash
    cd product-service
    mvn spring-boot:run
```