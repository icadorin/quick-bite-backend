
# 📧 Notification Service

Microsserviço responsável pelo envio de notificações por e-mail e SMS de forma assíncrona.

## 🚀 Funcionalidades

-   Envio de e-mails de confirmação
-   Notificações de status de pedido
-   Comunicação assíncrona via eventos Kafka

## 🛠️ Dependências

-   `Spring Boot DevTools`
-   `Spring Web`
-   `Eureka Discovery Client`
-   `Spring for Apache Kafka`
-   `Lombok`

## ⚙️ Configuração

**Porta:** `8086`  
**Service Discovery:** Eureka Server (`8761`)  
**Mensageria:** Kafka (Upstash.com)

## 🏃‍♂️ Como Executar

```bash
cd services/notification-service
mvn spring-boot:run