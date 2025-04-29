# Quick Serve - Food Delivery System

Quick Serve is a distributed food ordering and delivery management platform built using a microservices architecture. It connects restaurants, customers, and delivery drivers seamlessly, ensuring scalability, reliability, and real-time coordination.

---

## Key Features

✅ Multi-Role Applications:
- **Restaurant Management Portal** (React)
- **Customer App** (React)
- **Driver Mobile App** (React Native)
- **Super Admin Dashboard** (React)

✅ Core Functionalities:
- Real-time order tracking (WebSocket + Mapbox/Here Maps)
- Automated driver assignment (Redis Geospatial + RabbitMQ)
- Secure authentication (JWT + OTP)
- Multi-payment integration (PayHere, FriMi, Stripe)
- Dynamic menu & order management

✅ Tech Stack:
- **Backend:** Spring Boot, PostgreSQL, RabbitMQ, Redis
- **Frontend:** React, React Native
- **Maps:** Mapbox, Here Maps
- **Infra:** Docker, Kubernetes (Minikube/EKS/GKE)

---

## Architecture
[Architecture Diagram](path/to/architecture-diagram.png) <!-- Optional -->

---

## Prerequisites

Ensure you have the following installed:
- **Docker** 20.10+
- **Kubernetes** (Minikube/Kind/EKS/GKE)
- **PostgreSQL** 14+
- **Redis** 7+
- **RabbitMQ** 3.10+
- **Node.js** 18+ (for frontend)

---

## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/AashaniSamarakoon/Online_Food_Ordering.git
cd Online_Food_Ordering
```

### 2. Configure Environment Variables
- Create a `.env` file for each service (e.g., `auth-service/.env`) with the following:
  ```
  JWT_SECRET=your_secret_key
  POSTGRES_URL=jdbc:postgresql://postgres-service:5432/quickserve
  RABBITMQ_URL=amqp://rabbitmq-service:5672
  REDIS_URL=redis://redis-service:6379
  ```

## 🐳 Docker Setup
To run this project seamlessly using Docker Compose:

### 1. Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) Installed

### 2. Clone the Repository if you haven't already cloned it
```bash
git clone https://github.com/AashaniSamarakoon/Online_Food_Ordering.git
cd Online_Food_Ordering
```

### 3. Run with Docker Compose
```bash
docker-compose up --build
```
✅ This will spin up PostgreSQL, MongoDB, Redis, RabbitMQ, PgAdmin, and all application services automatically.

The application will be available at:
```bash
http://localhost:YOUR_PORT
```

### 🛠️ Services Overview
| Service Name            | Port(s) Exposed | Description |
|--------------------------|-----------------|-------------|
| **PostgreSQL**            | `5432`           | Primary relational database |
| **MongoDB**               | `27017`          | NoSQL database for tracking service |
| **Redis**                 | `6379`           | Caching layer for tracking |
| **RabbitMQ**              | `5672`, `15672`  | Message broker for microservice communication |
| **PgAdmin**               | `5050`           | DB management UI for PostgreSQL |
| **Driver Auth Service**   | `8086`           | Authentication microservice for drivers |
| **Driver Service**        | `8087`           | Microservice for driver operations |
| **Order Assignment Service** | `8088`        | Handles order-to-driver assignments |
| **Tracking Service**      | `8089`           | Real-time tracking of orders and drivers |


### 📦 Volumes Created
| Volume Name          | Purpose                         |
|-----------------------|---------------------------------|
| `pgdata`              | PostgreSQL persistent storage  |
| `mongodb_data`        | MongoDB persistent storage     |
| `redis_data`          | Redis persistent storage       |
| `driver-auth-uploads` | Uploads folder for auth service |

### 🧹 Tear Down
```bash
docker-compose down -v
```
-v flag ensures volumes are also deleted (clean fresh start).

### If you change init-db.sh, restart Postgres by doing:
```bash
docker-compose down
docker-compose up --build
```
- If your port 5432, 8086-8089, 5050, etc., are busy, change them in docker-compose.yml.
- Always check container health (`docker ps` and `docker inspect`) before blaming the code 😉

### 1. Prerequisites

```bash
cd auth-service && docker build -t quick-serve/auth:v1 .
cd ../restaurant-service && docker build -t quick-serve/restaurant:v1 .
# Repeat for all services
```

---

## ⚓ Kubernetes Setup

### 4. Deploy to Kubernetes
```bash
kubectl apply -f k8s/auth-deployment.yaml
kubectl apply -f k8s/restaurant-deployment.yaml
kubectl apply -f k8s/rabbitmq-deployment.yaml
# Repeat for all services
```

## Running the Application

- Access the application via the Ingress URL (e.g., `http://quickserve.local`).
- Key endpoints:
  - **Auth Service:** `POST /api/auth/register` (Register users)
  - **Order Service:** `GET /api/orders` (Fetch orders)
  - **WebSocket Tracking:** `ws://<ingress-url>/ws/tracking`

---

## Monitoring & Logs

### View Kubernetes Logs
```bash
kubectl logs -f deployment/auth-service
```

### Monitor RabbitMQ Events
```bash
rabbitmqctl list_queues
```

---

## Troubleshooting

| Issue                        | Resolution                                                                 |
|------------------------------|---------------------------------------------------------------------------|
| JWT validation fails         | Verify `JWT_SECRET` matches across all services.                         |
| RabbitMQ connection timeout  | Check `RABBITMQ_URL` in environment variables.                           |
| Map not loading              | Confirm Mapbox token is valid.                                           |
| Kubernetes pod not starting  | Run `kubectl describe pod <pod-name>` for more details.                  |

---

## Contributing
Please read our [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to contribute to this project.

---

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
