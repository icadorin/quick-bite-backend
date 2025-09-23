
# 💳 Payment Service

Microsserviço responsável pelo processamento de pagamentos e integração com gateways de pagamento.

## 🚀 Funcionalidades

-   Processamento de pagamentos
-   Integração com gateways (simulado)
-   Gestão de transações
-   Confirmação de pagamentos

## 🛠️ Dependências

-   `Spring Boot DevTools`
-   `Spring Web`
-   `Eureka Discovery Client`
-   `Lombok`

## ⚙️ Configuração

**Porta:** `8085`  
**Service Discovery:** Eureka Server (`8761`)

## 🏃‍♂️ Como Executar

```bash
cd services/payment-service
mvn spring-boot:run