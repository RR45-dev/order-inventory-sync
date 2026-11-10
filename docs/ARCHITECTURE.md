# Architecture

`order-service` writes new orders to Postgres and emits `OrderCreatedEvent`
on Kafka. `inventory-service` consumes that event, decrements stock (cached
in Redis), and emits `InventoryUpdatedEvent`. `notification-service`
consumes inventory updates to alert on low stock. The React `frontend`
talks to `order-service` and `inventory-service` directly.
