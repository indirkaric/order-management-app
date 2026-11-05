# Development okruženje — Order Management System

Ovo okruženje podiže sve infrastrukturne komponente potrebne za `order-management-app`: PostgreSQL, Apache Kafka (KRaft, bez Zookeeper-a), Kafka UI, Keycloak (sa automatskim import-om realma), Elasticsearch (+ opciona Kibana), Redis i LocalStack (S3).

## Preduslovi

- Docker i Docker Compose v2
- Za pristup servisima preko `*.oms.local` alijasa sa host mašine: pokrenuti `hosts/setup-hosts.sh` (Linux/macOS, `sudo sh hosts/setup-hosts.sh`) ili `hosts/setup-hosts.ps1` (Windows, kao Administrator) **nakon** što je `docker compose up` prvi put pokrenut (da docker mreža postoji)

## Pokretanje

```bash
cp .env.example .env
docker compose up -d
```

Opcioni `observability` profil (Kibana):

```bash
docker compose --profile observability up -d
```

Servisi (`identity-api`, `order-api`, `audit-log-api`, `gateway-api`, `storage-api`) očekuju da su njihove Docker image-e već build-ovane (`IDENTITY_SERVICE_IMAGE` i slične varijable u `.env`). Dok se Java servisi ne build-uju, `docker compose up -d` i dalje podiže infrastrukturu (Postgres/Kafka/Keycloak/Elasticsearch/Redis/LocalStack) — java servise je moguće i pokretati lokalno sa `mvn spring-boot:run` dok se hosts alias-i koriste za povezivanje na infrastrukturu u kontejnerima.

## Mreža i aliasi

Svi kontejneri su na `oms` bridge mreži, svaki sa svojim `*.oms.local` alias-om (identičan obrazac kao postojeći `nsight.local` u glavnom `development/` repou):

| Alias | Servis | Port |
|---|---|---|
| `database.oms.local` | PostgreSQL | 5432 |
| `auth.oms.local` | Keycloak | 8080 |
| `kafka.oms.local` | Kafka | 9092 (internal), 9094 (host) |
| `kafka-ui.oms.local` | Kafka UI | 8090 |
| `elasticsearch.oms.local` | Elasticsearch | 9200 |
| `kibana.oms.local` | Kibana (profile `observability`) | 5601 |
| `redis.oms.local` | Redis | 6379 |
| `aws.oms.local` | LocalStack (S3) | 4566 |
| `identity.oms.local` | identity-service | 8081 |
| `order.oms.local` | order-service | 8082 |
| `audit-log.oms.local` | audit-log-service | 8083 |
| `gateway.oms.local` | oms-ui-service (gateway/BFF — jedina adresa koju frontend treba da zna) | 8084 |
| `storage.oms.local` | oms-storage-service (S3/LocalStack file storage) | 8085 |

## Keycloak

Realm `order-management` se automatski uvozi iz `docker-mounts/keycloak/oms-realm.json` pri pokretanju (`start-dev --import-realm`). Admin konzola: `http://auth.oms.local:8080` (admin/admin, Keycloak master realm).

### Test korisnici (uvijek prisutni, ne treba ih ručno dodavati)

| Username | Password | Grupa | Role |
|---|---|---|---|
| `admin` | `admin` | `OMS_ADMIN` | sve role |
| `manager.oms` | `manager.oms` | `OMS_MANAGER` | ORDER_*, USER_READ, PRODUCT_* |
| `customer.oms` | `customer.oms` | `OMS_CUSTOMER` | ORDER_READ, ORDER_WRITE, PRODUCT_READ |
| `auditor.oms` | `auditor.oms` | `OMS_AUDITOR` | AUDIT_READ, ORDER_READ, USER_READ, PRODUCT_READ |

Lozinke su dev-only (uvezene kao plain-text u realm JSON-u, Keycloak ih hashira pri importu) — **ne koristiti ovaj obrazac u produkciji**.

### Dobijanje tokena (password grant, klijent `oms-app`)

```bash
curl -X POST http://auth.oms.local:8080/realms/order-management/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password&client_id=oms-app&username=admin&password=admin"
```

## Baze podataka

`docker-mounts/postgres/init-scripts/01_create_databases.sql` kreira `identity_db`, `order_db` (+ `keycloak` bazu za Keycloak samog), svaku sa svojom app-rolom (`identity_app`, `order_app`) — database-per-service, servisi ne pristupaju tuđim bazama. `audit-log-service` nema relacionu bazu — čuva isključivo u Elasticsearch (indeks `oms-audit-logs`).

## LocalStack (S3)

`docker-mounts/localstack/01-init-s3-bucket.sh` kreira bucket `oms-product-files` pri pokretanju. `oms-storage-service` je jedini servis koji direktno govori sa LocalStack-om.

## Gašenje

```bash
docker compose down        # zadrži volumene (baze, ES indeksi)
docker compose down -v     # obriši i volumene
```
