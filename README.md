# 📈 Stock Trading Platform

A full-stack stock trading platform built using **Java, Spring Boot Microservices, and modern distributed system technologies**.

The platform simulates the core functionality of a real-world stock trading application, including user authentication, portfolio management, order execution, market data processing, event-driven communication, and real-time stock price updates.

> 🚧 **Work in Progress**
>
> This project is actively under development. Backend microservices are currently being implemented and tested. Frontend development and AI-powered features are planned for future releases.

---

## 🚀 Tech Stack

### Backend
- Java 25
- Spring Boot 4
- Spring Cloud Gateway
- Spring Security
- Spring Data JPA
- JWT Authentication
- OpenFeign

### Database
- MySQL
- Hibernate (ORM)

### Messaging & Real-Time Technologies
- Apache Kafka
- Redis
- WebSocket

### Development Tools
- Maven
- IntelliJ IDEA
- Git & GitHub
- Docker

### Planned Technologies
- React
- Python
- Machine Learning

---

## 🏗️ Architecture

The application follows a **Microservices Architecture**, where each service is responsible for a specific business domain.

```text
                    ┌─────────────────────┐
                    │   Frontend (React)  │
                    │      Planned        │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │     API Gateway     │
                    │       :8080         │
                    │ JWT Authentication  │
                    └──────────┬──────────┘
                               │
          ┌────────────────────┼────────────────────┐
          │                    │                    │
          ▼                    ▼                    ▼

 ┌────────────────┐  ┌────────────────┐  ┌────────────────┐
 │  User Service  │  │ Market Data    │  │ Order Service  │
 │     :8081      │  │ Service :8082  │  │     :8083      │
 └────────────────┘  └────────────────┘  └────────────────┘
          │                    │                    │
          ▼                    ▼                    ▼

 ┌────────────────┐  ┌────────────────┐  ┌────────────────┐
 │     MySQL      │  │ MySQL + Redis  │  │     MySQL      │
 └────────────────┘  └────────────────┘  └────────────────┘
                               │
                    ┌──────────┴──────────┐
                    │                     │
                    ▼                     ▼

         ┌─────────────────┐  ┌────────────────┐
         │ Portfolio       │  │ Notification   │
         │ Service :8084   │  │ Service :8085  │
         └─────────────────┘  └────────────────┘
                    │                     │
                    └──────────┬──────────┘
                               │
                               ▼

                         Apache Kafka
```

---

## 🔧 Microservices

| Service | Port | Responsibility |
|----------|------|---------------|
| API Gateway | 8080 | Central routing and JWT authentication |
| User Service | 8081 | User management and authentication |
| Market Data Service | 8082 | Stock data, price updates, market APIs |
| Order Service | 8083 | Order placement and execution |
| Portfolio Service | 8084 | Portfolio and holdings management |
| Notification Service | 8085 | Event-driven notifications |

---

## ✅ Implemented Features

### User Service
- User Registration
- Login & Authentication
- BCrypt Password Hashing
- JWT Access Token Generation
- JWT Refresh Token Generation
- Account Management
- Fund Management

### API Gateway
- Centralized API Routing
- JWT Authentication Filter
- Public and Protected Endpoints
- Authorization Header Processing

### Market Data Service
- Stock Data Initialization
- Multi-Exchange Support
- Simulated Price Movement Engine
- Redis-Based Price Caching
- Real-Time WebSocket Updates
- Kafka Market Events Publishing

### Order Service
- Order APIs
- Persistence Layer
- Order Status Tracking
- Market Data Integration
- User Service Integration

### Portfolio Service
- Holdings Management
- Portfolio APIs
- Kafka Event Consumption
- Order Execution Processing

### Notification Service
- Kafka Event Consumer
- Order Success Notifications
- Failed Order Notifications
- User Registration Notifications

---

## 🔄 Service Communication

### Synchronous Communication
- REST APIs
- Spring Cloud Gateway
- OpenFeign

### Asynchronous Communication
- Apache Kafka
- Event-Driven Architecture

### Real-Time Communication
- WebSocket
- Live Stock Price Updates

---

## 📂 Project Structure

```text
stock-trading-platform/
│
├── backend/
│   ├── api-gateway/
│   ├── user-service/
│   ├── market-data-service/
│   ├── order-service/
│   ├── portfolio-service/
│   └── notification-service/
│
├── ai-service/
│   └── main.py
│
├── infrastructure/
├── MicroZips/
│
├── .gitignore
└── README.md
```

---

## 📋 Prerequisites

Before running the project, install:

- Java 25
- Maven
- MySQL
- Apache Kafka
- Redis
- Git

---

## ⚙️ Running the Backend

### 1. Clone the Repository

```bash
git clone https://github.com/PraloyG18/stock-trading-platform.git
cd stock-trading-platform
```

### 2. Configure Environment Variables

Example:

```env
DB_PASSWORD=your_mysql_password
JWT_SECRET=your_base64_jwt_secret
```

> ⚠️ Never commit secrets, passwords, or API keys to GitHub.

### 3. Start Infrastructure Services

- MySQL
- Kafka
- Redis

### 4. Start the Microservices

Run the services in the following order:

1. User Service
2. Market Data Service
3. Order Service
4. Portfolio Service
5. Notification Service
6. API Gateway

Each service can be started independently using IntelliJ IDEA or Maven.

---

## 🌐 Service URLs

| Service | URL |
|-----------|------|
| API Gateway | http://localhost:8080 |
| User Service | http://localhost:8081 |
| Market Data Service | http://localhost:8082 |
| Order Service | http://localhost:8083 |
| Portfolio Service | http://localhost:8084 |
| Notification Service | http://localhost:8085 |

### Health Endpoints

```text
http://localhost:8080/actuator/health
http://localhost:8081/actuator/health
http://localhost:8082/actuator/health
http://localhost:8083/actuator/health
http://localhost:8084/actuator/health
http://localhost:8085/actuator/health
```

---

## 🗺️ Development Roadmap

### Backend
- [x] Microservices Architecture
- [x] API Gateway
- [x] User Authentication
- [x] JWT Security
- [x] Market Data Service
- [x] Redis Integration
- [x] Kafka Integration
- [x] WebSocket Integration
- [x] Order Service
- [x] Portfolio Service
- [x] Notification Service
- [ ] Integration Testing
- [ ] Enhanced Order Matching Engine
- [ ] Transaction Management Improvements

### Frontend
- [ ] React Frontend
- [ ] Trading Dashboard
- [ ] Portfolio Dashboard
- [ ] Real-Time Stock Charts

### AI & Analytics
- [ ] AI Stock Analysis
- [ ] Machine Learning Models
- [ ] Stock Trend Prediction

### DevOps & Deployment
- [ ] Dockerized Deployment
- [ ] Kubernetes Support
- [ ] Cloud Deployment

---

## ⚠️ Disclaimer

This project is developed for **educational and software engineering purposes only**.

All stock prices, market data, and trading functionality are simulated and should not be considered real financial information or investment advice.

---

## 👨‍💻 Author

**Praloy Ghosh**

- GitHub: https://github.com/PraloyG18
- Project Repository: https://github.com/PraloyG18/stock-trading-platform

If you found this project helpful, consider giving it a ⭐ on GitHub.