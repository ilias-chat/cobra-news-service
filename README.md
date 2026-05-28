# CORBA News Service (`corba-news-service`)

Standalone CORBA News subsystem for **Noticias de jugadores** with CORBA producer + REST gateway.

1. `news-producer`: CORBA producer + in-memory news store
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

| Workflow | File | Trigger |
|----------|------|---------|
| CI | `.github/workflows/ci.yml` | Every push / PR — `mvn clean package` |
| **CD (default)** | `.github/workflows/cd.yml` | Every push — Docker Hub → **Cloud Run** (all-in-one CORBA + REST gateway) |
| CD (GCE) | `.github/workflows/cd-gce.yml` | Manual only — needs Compute Engine API enabled |

### Required GitHub secrets (same project as DWSC-backend)

| Secret | Purpose |
|--------|---------|
| `DOCKERHUB_USERNAME` | Push image (copy from DWSC-backend repo secrets) |
| `DOCKERHUB_TOKEN` | Docker Hub access token |
| `GCP_SA_KEY` | Deploy to Cloud Run |
| `GCP_PROJECT_ID` | GCP project id (e.g. same as Spring/Cloud Run services) |

Optional: `GCP_REGION` (default `europe-west1`), `CORBA_CLOUD_RUN_SERVICE` (default `corba-news`), `DOCKERHUB_REPO_CORBA_NEWS` (default `corba-news` — create this repo on Docker Hub).

After a successful CD run, the workflow prints the URL. Set Ionic:

`standaloneCorbaNewsBaseUrl: 'https://corba-news-….run.app'` (HTTPS, no port)

### GCE (optional, syllabus / VM requirement)

Only if you need a VM: enable [Compute Engine API](https://console.cloud.google.com/apis/library/compute.googleapis.com), then run workflow **CORBA News CD (GCE)** manually.

## 6) Ionic integration

Ionic News tab should call the standalone gateway URL from environment config:

- `standaloneCorbaNewsBaseUrl`

The players/comments backend toggle remains unchanged and does not affect news.

## 7) Troubleshooting

- CD fails: `Set DOCKERHUB_USERNAME and DOCKERHUB_TOKEN`:
  - Add the same Docker Hub secrets used by `DWSC-backend` to this repo.
- CD fails at GCE with `Compute Engine API` disabled:
  - Use default `cd.yml` (Cloud Run), or enable the API and run `cd-gce.yml` manually.
- `503 CORBA news service unavailable` from gateway:
  - producer container not running
  - wrong `ORB_HOST`/`ORB_PORT` between gateway and producer
- Empty `/api/news`:
  - producer started without seeded data (check producer logs)
- Cannot reach VM endpoint:
  - firewall rule not allowing gateway TCP port
  - VM has no external IP
