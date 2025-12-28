# 🍔 QuickBite — Plataforma Fullstack (Backend-First) de Delivery

QuickBite é uma **plataforma fullstack de delivery de comida**, desenvolvida com **arquitetura de microsserviços**, com foco principal em **backend e sistemas distribuídos**, simulando desafios reais encontrados em ambientes corporativos.

O frontend tem como objetivo **consumir e validar as APIs**, enquanto o backend concentra as decisões arquiteturais, regras de negócio e comunicação entre serviços.

O projeto foi construído com foco em:

- Arquitetura backend escalável  
- Separação de responsabilidades  
- Comunicação síncrona e assíncrona  
- Decisões arquiteturais documentadas  
- Boas práticas de desenvolvimento backend  
- Integração entre frontend e APIs REST  

📚 **Documentação**  
https://israelcadorin.vercel.app/quickbite

---

## 📌 Status das Implementações

- ✅ APIs REST com Spring Boot *(em evolução contínua)*  
- ✅ API Gateway  
- ✅ Autenticação JWT  
- ✅ Integração com PostgreSQL  
- 🧪 Kafka *(microserviço criado, integração em desenvolvimento)*  
- 🧪 Redis *(planejado para cache e otimização de performance)*  
- 🚧 Frontend simples para consumo das APIs *(em desenvolvimento)*  

---

## 🛠️ Stack Tecnológica

### 🔙 Backend (foco principal)
- **Java 21**
- **Spring Boot**
- **Spring Cloud**
- **Spring Security (JWT)**
- **PostgreSQL**
- **Kafka**
- **Redis**
- **Maven**

### 🎨 Frontend
- **React**
- **TypeScript**
- **Node.js**
- **Vite**
- **Axios / Fetch API**

### ☁️ Infraestrutura & DevOps
- **Git**
- **Neon.tech** *(PostgreSQL)*
- **Upstash** *(Kafka e Redis)*

---

## 🏗️ Arquitetura

O sistema é composto por múltiplos **microsserviços independentes**, desenvolvidos com **Spring Boot**, integrados via **Spring Cloud** e consumidos por um **frontend React**.

```text
Frontend → API Gateway (8081) → Microsserviços (8082–8086)
```

## ☁️ Infraestrutura na Nuvem

| Serviço       | Fornecedor   | Uso                  |
|---------------|--------------|----------------------|
| 🐘 **PostgreSQL** | [Neon.tech](https://neon.tech/) | Banco de dados |
| ⚡ **Kafka**      | [Upstash.com](https://upstash.com/) | Mensageria assíncrona |
| 🗃️ **Redis**     | [Upstash.com](https://upstash.com/) | Cache e sessões      |

## 📦 Microsserviços

| Serviço | Descrição | Porta Padrão |
| :--- | :--- | :--- |
| 🚪 **`api-gateway`** | 	API Gateway com roteamento estático | `8081` |
| 🔐 **`auth-service`** | Autenticação e autorização JWT | `8082` |
| 🍕 **`product-service`** | Catálogo de produtos e cardápios | `8083` |
| 📋 **`order-service`** | Gestão do ciclo de vida de pedidos | `8084` |
| 💳 **`payment-service`** | Processamento de pagamentos | `8085` |
| 📧 **`notification-service`** | Notificações por e-mail e SMS | `8086` |


## 🚀 Como Executar (Desenvolvimento Local)
📋 Pré-requisitos
Java 17 ou superior

Maven 3.6 ou superior

Contas nos serviços:

🌐 Neon.tech (PostgreSQL)

⚡ Upstash.com (Kafka + Redis)

## ▶️ Execução dos Serviços
Execute os serviços:

```bash
# Terminal 1 - API Gateway
cd services/api-gateway
mvn spring-boot:run

# Terminal 2 - Autenticação
cd services/auth-service
mvn spring-boot:run

# Terminal 3 - Produtos
cd services/product-service
mvn spring-boot:run

# Terminal 4 - Pedidos
cd services/order-service
mvn spring-boot:run

# Terminal 5 - Pagamentos
cd services/payment-service
mvn spring-boot:run

# Terminal 6 - Notificações
cd services/notification-service
mvn spring-boot:run
```

## ✅ Verificação

Acesse: http://localhost:8081/api/auth/test
