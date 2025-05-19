# deploy-food-ordering-system.ps1

# Set working directory
$workDir = "D:\Projects\Online_Food_Ordering\k8s"
cd $workDir

Write-Host "🔧 Installing Ingress NGINX Controller..." -ForegroundColor Cyan
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml
Write-Host "⏳ Waiting for Ingress Controller to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 45
Write-Host "✅ Ingress NGINX Controller installed successfully" -ForegroundColor Green

Write-Host "🔑 Creating Secrets..." -ForegroundColor Cyan
kubectl apply -f bootstrap/neon-db-secrets.yaml
kubectl apply -f bootstrap/mongo-db-secrets.yaml
kubectl apply -f bootstrap/redis-credentials.yaml
Write-Host "✅ Secrets created successfully" -ForegroundColor Green

Write-Host "🔄 Deploying Infrastructure Services..." -ForegroundColor Cyan
# Deploy Redis
kubectl apply -f services/redis/

# Deploy RabbitMQ
kubectl apply -f services/rabbitmq/
Write-Host "✅ Infrastructure services deployed successfully" -ForegroundColor Green

Write-Host "⏳ Waiting for infrastructure services to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host "🚀 Deploying Backend Services..." -ForegroundColor Cyan
# Deploy Authentication Services
kubectl apply -f bootstrap/driver-auth/

kubectl apply -f bootstrap/restaurant-auth/

kubectl apply -f bootstrap/user-service/

# Deploy Core Services
kubectl apply -f bootstrap/driver-service/

kubectl apply -f bootstrap/real-time-tracking/

kubectl apply -f bootstrap/order-assignment/

kubectl apply -f bootstrap/restaurant-service/

kubectl apply -f bootstrap/banking-service/

kubectl apply -f bootstrap/order-service/

kubectl apply -f bootstrap/payment-service/

Write-Host "✅ Backend services deployed successfully" -ForegroundColor Green

Write-Host "⏳ Waiting for backend services to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 40

Write-Host "🌐 Deploying API Gateway..." -ForegroundColor Cyan
# Apply API Gateway last
kubectl apply -f services/api-gateway/

Write-Host "✅ API Gateway deployed successfully" -ForegroundColor Green

Write-Host "📊 System Status:" -ForegroundColor Magenta
kubectl get pods

Write-Host "🎉 Deployment Complete! Your food ordering system should be accessible at http://localhost/api/" -ForegroundColor Green