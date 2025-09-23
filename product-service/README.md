# 🍕 Product Service

Microsserviço responsável pelo catálogo de produtos, cardápios e gestão de itens dos restaurantes.

## 🚀 Funcionalidades

-   CRUD completo de produtos
-   Gestão de categorias
-   Controle de disponibilidade
-   Busca e filtros de produtos

## 🛠️ Dependências

-   `Spring Boot DevTools`
-   `Spring Web`
-   `Spring Data JPA`
-   `Eureka Discovery Client`
-   `PostgreSQL Driver`
-   `Lombok`

## ⚙️ Configuração

**Porta:** `8083`  
**Banco de Dados:** PostgreSQL (Neon.tech)  
**Service Discovery:** Eureka Server (`8761`)

## 🏃‍♂️ Como Executar

```bash
cd services/product-service
mvn spring-boot:run