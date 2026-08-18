# Production Deployment Guide

This guide covers deploying LPMS to production environments.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Deployment Options](#deployment-options)
3. [Docker Compose Deployment](#docker-compose-deployment)
4. [Kubernetes Deployment](#kubernetes-deployment)
5. [Cloud Provider Guides](#cloud-provider-guides)
6. [Post-Deployment](#post-deployment)
7. [Rollback Procedures](#rollback-procedures)

---

## Prerequisites

### Infrastructure Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| CPU | 4 cores | 8+ cores |
| RAM | 8 GB | 16+ GB |
| Storage | 50 GB SSD | 100+ GB NVMe |
| Network | 100 Mbps | 1 Gbps |

### Required Services

- [ ] Domain name configured
- [ ] SSL/TLS certificates
- [ ] Database backup solution
- [ ] Monitoring stack (Prometheus/Grafana)
- [ ] Log aggregation (ELK/CloudWatch)
- [ ] CI/CD pipeline configured

---

## Deployment Options

| Option | Best For | Complexity | Cost |
|--------|----------|------------|------|
| Docker Compose | Small firms, < 100 users | Low | $ |
| Kubernetes | Medium-Large firms | Medium | $$ |
| Managed Cloud | Enterprise | Low-Medium | $$$ |

---

## Docker Compose Deployment

### 1. Server Preparation

```bash
# Install Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verify installation
docker --version
docker-compose --version
```

### 2. Configuration

```bash
# Create deployment directory
sudo mkdir -p /opt/lpms
sudo chown $USER:$USER /opt/lpms
cd /opt/lpms

# Copy project files
git clone <repository-url> .

# Configure environment
cp .env.example .env
nano .env  # Edit with production values
```

### 3. SSL Certificate (Let's Encrypt)

```bash
# Install certbot
sudo apt install certbot python3-certbot-nginx

# Obtain certificate
sudo certbot certonly --standalone -d yourdomain.com -d www.yourdomain.com

# Auto-renewal setup
sudo crontab -e
# Add: 0 3 * * * certbot renew --quiet
```

### 4. Deploy

```bash
# Build images
docker-compose -f docker-compose.prod.yml build

# Run migrations
docker-compose -f docker-compose.prod.yml exec backend \
  ./mvnw flyway:migrate -Dflyway.url=jdbc:mysql://mysql:3306/lpm_production

# Start services
docker-compose -f docker-compose.prod.yml up -d

# Verify deployment
docker-compose -f docker-compose.prod.yml ps
curl https://yourdomain.com/api/health
```

### 5. Nginx Configuration

```nginx
# /etc/nginx/sites-available/lpms
server {
    listen 80;
    server_name yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    # Frontend
    location / {
        proxy_pass http://localhost:5173;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Backend API
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_buffering off;
    }

    # Static assets with caching
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

---

## Kubernetes Deployment

### 1. Cluster Setup

```bash
# Option 1: Managed Kubernetes
# - GKE (Google Kubernetes Engine)
# - EKS (Amazon Elastic Kubernetes Service)
# - AKS (Azure Kubernetes Service)

# Option 2: Self-managed
# Using kubespray, rancher, or similar
```

### 2. Namespace Creation

```bash
kubectl create namespace lpms-production
```

### 3. Secrets Management

```bash
# Create secrets (do NOT commit these)
kubectl create secret generic lpms-secrets \
  --from-literal=mysql-root-password='CHANGE_ME' \
  --from-literal=mysql-password='CHANGE_ME' \
  --from-literal=jwt-secret='GENERATE_256_BIT_SECRET' \
  --from-literal=redis-password='CHANGE_ME' \
  --namespace=lpms-production

# Create database credentials secret
kubectl create secret generic lpms-db-credentials \
  --from-literal=username='lpms_user' \
  --from-literal=password='CHANGE_ME' \
  --namespace=lpms-production
```

### 4. Deploy Application

```bash
# Apply all manifests
kubectl apply -f k8s/ -n lpms-production

# Verify deployment
kubectl get all -n lpms-production
kubectl get pods -n lpms-production -w

# Check logs
kubectl logs -f deployment/lpms-backend -n lpms-production
```

### 5. Ingress Configuration

```bash
# Install ingress controller (if not using managed)
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml

# Apply ingress
kubectl apply -f k8s/ingress.yaml -n lpms-production
```

---

## Cloud Provider Guides

### AWS Deployment

```bash
# Infrastructure as Code with Terraform
# See terraform/ directory for complete setup

# Quick setup
terraform init
terraform plan -var-file=prod.tfvars
terraform apply

# Includes:
# - EKS cluster
# - RDS MySQL (Multi-AZ)
# - ElastiCache Redis
# - MSK (Managed Kafka)
# - ALB with ACM certificates
# - ECR for container registry
```

### Google Cloud Deployment

```bash
# Deploy to GKE
gcloud container clusters create lpms-cluster \
  --region us-central1 \
  --num-nodes 3 \
  --machine-type e2-standard-4

# Deploy Cloud SQL (MySQL)
gcloud sql instances create lpms-db \
  --database-version=MYSQL_8_0 \
  --tier=db-custom-4-15360 \
  --region=us-central1

# Deploy to Cloud Run (serverless option)
gcloud run deploy lpms-backend \
  --image gcr.io/PROJECT-ID/lpms-backend \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated
```

### Azure Deployment

```bash
# Azure AKS
az aks create \
  --resource-group lpms-rg \
  --name lpms-aks \
  --node-count 3 \
  --node-vm-size Standard_D4s_v3

# Azure Database for MySQL
az mysql server create \
  --resource-group lpms-rg \
  --name lpms-mysql \
  --location eastus \
  --admin-user lpmsadmin \
  --admin-password CHANGE_ME \
  --sku-name GP_Gen5_4

# Deploy with Helm
helm install lpms ./helm-chart -n lpms --create-namespace
```

---

## Post-Deployment

### 1. Health Checks

```bash
# Backend health
curl https://yourdomain.com/api/actuator/health

# Database connectivity
kubectl exec -it deployment/lpms-backend -n lpms-production -- \
  mysql -h mysql-service -u lpms_user -p

# Redis connectivity
kubectl exec -it deployment/lpms-backend -n lpms-production -- \
  redis-cli -h redis-service ping
```

### 2. Initial Setup

```bash
# Create initial admin user
docker-compose exec backend \
  java -jar app.jar --spring.profiles.active=prod \
  --command=create-admin \
  --email=admin@yourcompany.com \
  --password=SecurePassword123!

# Run Flyway migrations
docker-compose exec backend ./mvnw flyway:migrate

# Verify Kafka topics
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

### 3. Monitoring Setup

```bash
# Install Prometheus Operator
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm install prometheus prometheus-community/kube-prometheus-stack \
  -n monitoring --create-namespace

# Access Grafana
kubectl port-forward svc/prometheus-grafana 8080:80 -n monitoring
# Default credentials: admin/admin
```

---

## Rollback Procedures

### Docker Compose Rollback

```bash
# Stop current version
docker-compose -f docker-compose.prod.yml down

# Pull previous version
docker-compose -f docker-compose.prod.yml pull backend:previous-version

# Start previous version
docker-compose -f docker-compose.prod.yml up -d

# Verify
docker-compose logs backend | grep -i "started\|error"
```

### Kubernetes Rollback

```bash
# Rollback deployment
kubectl rollout undo deployment/lpms-backend -n lpms-production

# Rollback to specific revision
kubectl rollout undo deployment/lpms-backend --to-revision=2 -n lpms-production

# Verify rollback
kubectl rollout status deployment/lpms-backend -n lpms-production
```

---

## Backup & Recovery

### Database Backup

```bash
# Automated backup script
#!/bin/bash
# backup.sh

BACKUP_DIR="/backups/mysql"
DATE=$(date +%Y%m%d_%H%M%S)

docker-compose exec -T mysql mysqldump \
  -u root -p${MYSQL_ROOT_PASSWORD} \
  lpm_production | gzip > ${BACKUP_DIR}/backup_${DATE}.sql.gz

# Retention: Keep 7 days daily, 4 weeks weekly
find ${BACKUP_DIR} -name "*.sql.gz" -mtime +7 -delete
```

### Disaster Recovery

1. **RPO (Recovery Point Objective)**: 1 hour
2. **RTO (Recovery Time Objective)**: 4 hours

**Recovery Steps:**
1. Spin up new infrastructure
2. Restore database from latest backup
3. Deploy application
4. Verify data integrity
5. Update DNS if needed

---

## Performance Tuning

### JVM Tuning

```yaml
# docker-compose.prod.yml
environment:
  - JAVA_OPTS=-Xms2g -Xmx4g
  - JAVA_OPTS=-XX:+UseG1GC
  - JAVA_OPTS=-XX:MaxGCPauseMillis=200
  - JAVA_OPTS=-XX:+HeapDumpOnOutOfMemoryError
```

### Database Tuning

```sql
-- MySQL optimization
SET GLOBAL innodb_buffer_pool_size = 8589934592;  -- 8GB
SET GLOBAL innodb_log_file_size = 512M;
SET GLOBAL max_connections = 500;
SET GLOBAL query_cache_size = 134217728;  -- 128MB
```

### Redis Tuning

```bash
# redis.conf
maxmemory 4gb
maxmemory-policy allkeys-lru
tcp-keepalive 300
```

---

## Security Hardening Checklist

- [ ] All default passwords changed
- [ ] SSL/TLS certificates installed
- [ ] Firewall rules configured
- [ ] Database not publicly accessible
- [ ] Container images scanned for vulnerabilities
- [ ] Secrets managed securely
- [ ] Rate limiting enabled
- [ ] Logging and monitoring active
- [ ] Backups tested
- [ ] Security patches applied

---

## Support & Contact

- **Emergency**: emergency@yourcompany.com
- **Support**: support@yourcompany.com
- **Status Page**: https://status.yourcompany.com
