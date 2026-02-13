# Crypto Advisor (MVP PoC)

This is a modular monolith MVP for a crypto advisor platform. The application aggregates recommendations from multiple
third-party providers and exposes a unified API.

---

## Project Structure

- `crypto-advisor-app`: Spring Boot entry point
- `crypto-advisor-api`: REST API (OpenAPI-first)
- `crypto-advisor-service`: Core aggregation logic
- `crypto-advisor-model`: Domain models and ports
- `crypto-recommender-provider`: Provider adapters, OpenAPI clients
- `bruno`: Bruno API Suite
- `deployment`: Dockerfile
- `helm`:  Helm chart
- `.github`: GitHub Actions

---

## How to Run Locally (Dev Mode)

### Pre-requisites:

- Java 25
- Maven
- Docker (optional)
- (Optional) Helm 3+ for chart testing

---

### 1. Run with Spring Boot (from source)

```bash
cd crypto-recommender-app
mvn spring-boot:run
```

### 2. Build & Run with Docker

```bash
docker build -t crypto-advisor -f deployment/Dockerfile .
docker run -p 8080:8080 crypto-advisor
```

### 3. Helm

Install Helm (if needed):

```bash
brew install helm
```

Lint and render Helm chart locally:

```bash
cd helm/crypto-advisor
helm lint .
helm template crypto-advisor .
```

### API Documentation (ReDoc)

when the application is
launched, [recommendation-api.yaml](crypto-recommender-api/src/main/resources/recommendation-api.yaml) will be available at:

```url
http://{HOST}:{PORT}/docs
```