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
![Architecture Diagram](path/to/architecture-diagram.png) <!-- Optional -->

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

### 3. Build Docker Images
```bash
cd auth-service && docker build -t quick-serve/auth:v1 .
cd ../restaurant-service && docker build -t quick-serve/restaurant:v1 .
# Repeat for all services
```

### 4. Deploy to Kubernetes
```bash
kubectl apply -f k8s/auth-deployment.yaml
kubectl apply -f k8s/restaurant-deployment.yaml
kubectl apply -f k8s/rabbitmq-deployment.yaml
# Repeat for all services
```

### 5. Frontend Setup
- **Web App (Restaurant & Admin Dashboards):**
  ```bash
  cd frontend/web && npm install
  REACT_APP_API_URL=http://api.quickserve.com npm run build
  docker build -t quick-serve/web:v1 .
  ```
- **Mobile Apps (Customer & Driver):**
  ```bash
  cd frontend/mobile && expo build:android
  ```

---

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
