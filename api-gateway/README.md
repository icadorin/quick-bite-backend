# 🚪 API Gateway

Gateway principal para todas as requisições da API QuickBite. Responsável por rotear as requisições para os microsserviços corretos usando roteamento estático.

## 🚀 Funcionalidades

-   Roteamento estático para microsserviços
-   Proxy de requisições HTTP
-   Ponto único de entrada para a API

## 🛠️ Dependências

-   `Spring Boot DevTools`
-   `Spring Web`
-   `WebFlux` (para WebClient)

## ⚙️ Configuração

**Porta:** `8081`  
**Roteamento:** Configuração estática via WebClient  
**Auth Service:** `http://localhost:8082`

## 🏃‍♂️ Como Executar

```bash
cd services/api-gateway
mvn spring-boot:run