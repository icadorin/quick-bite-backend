# 📋 Order Service

Microsserviço responsável por todo o ciclo de vida dos pedidos, desde a criação até a entrega.

## 🚀 Funcionalidades

-   Criação de novos pedidos
-   Acompanhamento de status
-   Histórico de pedidos
-   Gestão de carrinho de compras

## 🛠️ Dependências

-   `Spring Boot DevTools`
-   `Spring Web`
-   `Spring Data JPA`
-   `Eureka Discovery Client`
-   `PostgreSQL Driver`
-   `Lombok`

## ⚙️ Configuração

**Porta:** `8084`  
**Banco de Dados:** PostgreSQL (Neon.tech)  
**Service Discovery:** Eureka Server (`8761`)

## 🏃‍♂️ Como Executar

```bash
cd services/order-service
mvn spring-boot:run