# StockSync — Real-Time Order & Inventory Sync Platform

## Business problem

Retailers selling across web, mobile, and marketplace channels often oversell items because each channel checks stock independently against a slightly stale copy of inventory. StockSync centralizes stock reservation behind a single event-driven pipeline so every channel sees a consistent, real-time view of what's actually available.

## Architecture

```
React frontend
     |
     v
[order-service] --(order-created, Kafka)--> [inventory-service] --(inventory-updated, Kafka)--> [notification-service]
     |                                              |
     v                                              v
 Postgres (orders)                     Postgres (stock) + Redis (read cache)
```

1. **order-service** (Spring Boot) accepts new orders over REST, persists them to Postgres, and publishes an `order-created` event to Kafka.
2. **inventory-service** (Spring Boot) consumes `order-created`, checks and decrements stock in Postgres inside a single transaction (rejecting the whole order if any line item is short — no oversells, no partial fulfillment), caches "quantity on hand" in Redis for fast reads, and publishes `inventory-updated`.
3. **notification-service** (Spring Boot) consumes `inventory-updated` and logs a customer notification (swap in an email/SMS provider in production).
4. **frontend** (React) lets you place an order and watch inventory and order status update as events flow through the system.

## Tech stack coverage

Java, Spring Boot, REST APIs, Microservices, Distributed Systems, System Design, Kafka, PostgreSQL, Redis, Docker, Kubernetes-ready, Terraform-ready, Git, CI/CD (GitHub Actions), JUnit + Mockito, Integration testing, React, SQL, AWS-ready (see `infra/` note below).

## Running it locally

```bash
docker compose up --build
```

- Frontend: http://localhost:3000
- order-service: http://localhost:8081/api/orders
- inventory-service: http://localhost:8082/api/inventory

Place an order for `SKU-001` with a quantity higher than what's on hand and watch it get rejected; place one within stock and watch the quantity decrement live.

## Running the tests

```bash
cd inventory-service && mvn test
```

## Taking it further (what to mention in an interview)

- Deploy to EKS with the included Dockerfiles + a Helm chart or raw manifests (add a `k8s/` folder with Deployment/Service/HPA definitions).
- Add a Terraform module provisioning MSK (managed Kafka), RDS Postgres, and ElastiCache Redis instead of the local containers.
- Add OpenTelemetry auto-instrumentation and ship traces to AWS X-Ray or Jaeger to visualize the order → inventory → notification trace end-to-end.
- Add a GitHub Actions workflow that runs `mvn test` on each service and builds/pushes Docker images on merge to main.
