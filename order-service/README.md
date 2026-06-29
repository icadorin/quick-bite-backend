# Order Service

Microsserviço responsável por criação e gestão do ciclo de vida dos pedidos.

**Porta:** 8084

## Funcionalidades

- Criação de pedidos com validação de restaurante e produtos
- Integração com Product Service via Feign Client
- Transições de status controladas
- Histórico de mudanças de status
- Listagem de pedidos por usuário, restaurante e pesquisa
- Estatísticas de pedidos por status

## Endpoints

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | /api/v1/orders | Pedidos do usuário |
| GET | /api/v1/orders/{id} | Detalhes do pedido |
| GET | /api/v1/orders/restaurant/{restaurantId} | Pedidos do restaurante |
| GET | /api/v1/orders/search | Pesquisar pedidos |
| GET | /api/v1/orders/restaurant/{restaurantId}/stats | Estatísticas |
| POST | /api/v1/orders | Criar pedido |
| PATCH | /api/v1/orders/{id}/status | Atualizar status |
| POST | /api/v1/orders/{id}/cancel | Cancelar pedido |

## Transições de Status

PENDING -> CONFIRMED -> PREPARING -> READY_FOR_PICKUP -> OUT_FOR_DELIVERY -> DELIVERED

Cancelamento permitido: PENDING e CONFIRMED apenas.

## Autorização

| Papel | Permissão |
|-------|-----------|
| CUSTOMER | Vê apenas seus pedidos |
| RESTAURANT_OWNER | Gerencia pedidos do seu restaurante |
| ADMIN | Acesso total |

## Integração com Product Service

```bash
    @FeignClient(name = "product-service")
    public interface ProductServiceClient {
        ProductResponse getProduct(Long id);
        boolean validateRestaurant(Long id);
    }
```


## Entidades

- Order: userId, restaurantId, status, totalAmount, deliveryAddress, items, statusHistory
- OrderItem: productId, productName, quantity, unitPrice, totalPrice
- OrderStatusHistory: status, notes
- DeliveryAddress: street, number, city, state, zipCode, complement

## Dependências

- Spring Boot Web
- Spring Boot Validation
- Spring Security
- Spring Data JPA
- Spring Boot Actuator
- Spring Cloud OpenFeign
- JJWT (API, Impl, Jackson)
- Lombok
- MapStruct
- QuickBite Core
- PostgreSQL (runtime)
- H2 (testes)

## Execução

```bash
    cd order-service
    mvn spring-boot:run
```