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
---

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

### 1. Prerequisites
- Kubernetes cluster (local like Minikube, Docker Desktop, or cloud-based)
- `kubectl CLI` tool configured to access your cluster
- Docker Hub access (to pull application images)

### 2. Clone the Repository if you haven't already cloned it
```bash
git clone https://github.com/AashaniSamarakoon/Online_Food_Ordering.git
cd Online_Food_Ordering
```

Before deploying the application, you need to create the necessary secrets:

```bash
apiVersion: v1
kind: Secret
metadata:
  name: neon-db-secrets
type: Opaque
stringData:
  # Base64 encoding is handled automatically when using stringData
  postgres-password: "your-postgres-password"
  postgres-user: "your-postgres-user"
  postgres-database: "your-database-name"
  postgres-host: "your-neon-db-host.provider.com"
  postgres-port: "5432"
```
```bash
apiVersion: v1
kind: Secret
metadata:
  name: mongodb-atlas-credentials
type: Opaque
stringData:
  # Replace with your MongoDB Atlas connection string
  mongo-uri: "mongodb+srv://USERNAME:PASSWORD@cluster.example.mongodb.net/tracking_db?retryWrites=true&w=majority"
```
```bash
apiVersion: v1
kind: Secret
metadata:
  name: redis-credentials
type: Opaque
stringData:
  redis-username: "username"
  redis-password: "password"
  redis-host: "redis-service"
  redis-port: "6379"
```

### 3. Apply the secrets to your cluster:
```bash
kubectl apply -f bootstrap/redis-credentials.yaml
kubectl apply -f bootstrap/mongo-db-secrets.yaml
kubectl apply -f bootstrap/neon-db-secrets.yaml
```


### 4. Deploy to Kubernetes

Inside your `k8s/` folder, just run
```bash
kubectl apply -R -f .
```
`-R` tells `kubectl` to recursively go into subfolders and only apply files with `.yaml/.yml/.json`.

Or Deploy supporting services:
```bash
kubectl apply -f k8s/services/redis/
kubectl apply -f services/zipkin/
# Repeat for all services
```
And Deploy backend microservices:
```bash
kubectl apply -f bootstrap/driver-auth/
kubectl apply -f bootstrap/driver-service/
# Repeat for all services
```

### 5. Deploy the API Gateway (Nginx Ingress):
```bash
# Install Nginx Ingress Controller if not already installed
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml
```
```bash
# Wait for ingress controller to be ready
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s
```
```bash
# Apply API Gateway configuration
kubectl apply -f services/api-gateway/
```

## Common Troubleshooting
| Issue | Resolution |
|-------|------------|
| 502 Bad Gateway errors | 1. Check if the target service has active endpoints: `kubectl get endpoints <service-name>`<br>2. Check service logs: `kubectl logs -l app=<service-name>` |
| Image Pull Issues | Force pull the latest image: `kubectl rollout restart deployment <deployment-name>` |
| Debug Ingress Issues | Check ingress controller logs: `kubectl logs -n ingress-nginx deploy/ingress-nginx-controller` |
| Kubernetes pod not starting | Run `kubectl describe pod <pod-name>` for more details |
| Monitor RabbitMQ | View queues with: `rabbitmqctl list_queues` |
| RabbitMQ connection timeout | Check `RABBITMQ_URL` in environment variables |

---

## External Access
After deployment, you can access the services through the ingress controller:

```bash
http://localhost/api/auth/login
```
For local development with port forwarding:
```bash
kubectl port-forward svc/tracking-service 8086:80
```
## Monitoring
Check the status of your deployments:
```bash
kubectl get all

Or

kubectl get pods
kubectl get deployments
kubectl get services
kubectl get ingress
```
Logs for specific pods
```bash
kubectl logs <pod-name>
```

## Contributing
Please read our [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to contribute to this project.

---

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
