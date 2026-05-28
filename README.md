# CORBA News Service (`corba-news-service`)

Standalone CORBA News subsystem for **Noticias de jugadores** with two independent runtimes:

1. `news-producer`: CORBA producer + in-memory store
2. `news-gateway`: REST-to-CORBA adapter (`/api/news`)

This subsystem is isolated from Node/Mongo and Spring player/comment databases.

## 1) Module contents

- IDL contract: [`idl/NewsService.idl`](idl/NewsService.idl)
- Producer runtime:
  - `com.dwsc.corba.news.server.NewsProducerServer`
  - `com.dwsc.corba.news.server.NewsServiceImpl`
- Gateway runtime:
  - `com.dwsc.corba.news.gateway.NewsGatewayServer`
- CORBA consumer utility:
  - `com.dwsc.corba.news.client.CorbaNewsClient`
- Docker assets:
  - `docker/news-producer.Dockerfile`
  - `docker/news-gateway.Dockerfile`
  - `docker/start-producer.sh`
  - `docker/start-gateway.sh`

## 2) Build + IDL stubs

```bash
mvn -f corba-news-service/pom.xml clean package
```

Generated CORBA classes are created in:

`target/generated-sources/idl`

## 3) Run locally (two-process architecture)

### Process A: Producer (includes naming daemon startup in container script)

```bash
mvn -f corba-news-service/pom.xml exec:java -Dexec.mainClass=com.dwsc.corba.news.server.NewsProducerServer
```

Default env:

- `ORB_HOST=0.0.0.0`
- `ORB_PORT=1050`
- `CORBA_SERVICE_NAME=NewsService`

### Process B: Gateway

```bash
mvn -f corba-news-service/pom.xml exec:java -Dexec.mainClass=com.dwsc.corba.news.gateway.NewsGatewayServer
```

Default env:

- `ORB_HOST=127.0.0.1`
- `ORB_PORT=1050`
- `CORBA_SERVICE_NAME=NewsService`
- `GATEWAY_HTTP_PORT=8095`

### Verify

```bash
curl http://127.0.0.1:8095/health
curl http://127.0.0.1:8095/api/news
```

## 4) Docker build/run

```bash
docker build -f corba-news-service/docker/news-producer.Dockerfile -t corba-news-producer:local .
docker build -f corba-news-service/docker/news-gateway.Dockerfile -t corba-news-gateway:local .
```

Run with a shared Docker network:

```bash
docker network create corba-news-net
docker run -d --name corba-news-producer --network corba-news-net -e ORB_HOST=0.0.0.0 -e ORB_PORT=1050 corba-news-producer:local
docker run -d --name corba-news-gateway --network corba-news-net -p 8095:8095 -e ORB_HOST=corba-news-producer -e ORB_PORT=1050 -e GATEWAY_HTTP_PORT=8095 corba-news-gateway:local
```

## 5) CI/CD (GitHub Actions)

Workflows in this repository:

| Workflow | File | Trigger |
|----------|------|---------|
| CI | `.github/workflows/ci.yml` | Every push and PR — runs `mvn clean package` (IDL + compile) |
| CD | `.github/workflows/cd.yml` | Every push (any branch), or manual **Run workflow** — builds Docker images, deploys to GCE, health-checks gateway |

Required GitHub **secrets** (repository settings → Secrets and variables → Actions):

- `GCP_SA_KEY` — JSON key for a service account with Artifact Registry/GCR push, Compute Engine admin, and firewall admin
- `GCP_PROJECT_ID` — GCP project id (can be a **variable** instead)

Optional secrets or **variables**:

- `GCP_REGION` (default `europe-west1`)
- `GCP_ZONE` (default `europe-west1-b`)
- `GCE_CORBA_VM_NAME` (default `corba-news-vm`)
- `GCE_MACHINE_TYPE` (default `e2-medium`)
- `CORBA_NEWS_GATEWAY_PORT` (default `8095`)

After a successful CD run, set Ionic production env:

`standaloneCorbaNewsBaseUrl: 'http://<VM_EXTERNAL_IP>:8095'`

If this module lives inside the monorepo `DWSC-backend`, you can still use `DWSC-backend/.github/workflows/deploy-corba-news-gce.yml` (manual dispatch) instead of pushing from this standalone repo.

## 6) Ionic integration

Ionic News tab should call the standalone gateway URL from environment config:

- `standaloneCorbaNewsBaseUrl`

The players/comments backend toggle remains unchanged and does not affect news.

## 7) Troubleshooting

- `503 CORBA news service unavailable` from gateway:
  - producer container not running
  - wrong `ORB_HOST`/`ORB_PORT` between gateway and producer
- Empty `/api/news`:
  - producer started without seeded data (check producer logs)
- Cannot reach VM endpoint:
  - firewall rule not allowing gateway TCP port
  - VM has no external IP
