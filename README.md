# Secure Middleware Java

A Java Spring Boot rewrite of a secure middleware platform focused on API security, encryption, request filtering, caching, gateway routing, observability, and risk scoring.

## Features

* AES-GCM encryption and decryption API
* JWT authentication endpoint
* Custom TTL/LRU cache service
* Sliding-window rate limiter using thread-safe Java collections
* Cached internal gateway proxy
* Request audit logging with correlation IDs
* Weighted request risk scoring engine
* Dockerised Spring Boot deployment
* JUnit tests for core security and middleware services

## Tech Stack

* Java 21
* Spring Boot
* Spring Security
* Maven
* Docker
* JUnit 5

## Architecture

The application is structured into independent middleware components:

* `auth` — issues and validates signed JWT tokens
* `crypto` — encrypts and decrypts data using AES-GCM
* `cache` — provides custom TTL/LRU caching
* `ratelimit` — applies sliding-window request limiting
* `gateway` — forwards requests to an internal service and caches responses
* `audit` — records request metadata with correlation IDs
* `risk` — evaluates suspicious API behaviour using weighted rules

## Running Locally

```bash
./mvnw spring-boot:run
```

## Running with Docker

```bash
docker build -t secure-middleware-java .
docker run -p 8080:8080 secure-middleware-java
```

## Example Requests

### Encrypt Data

```bash
curl -s -X POST http://localhost:8080/api/v1/crypto/encrypt \
  -H "Content-Type: application/json" \
  -d '{"plaintext":"hello banking security"}'
```

### Check Rate Limit

```bash
curl -s -X POST http://localhost:8080/api/v1/rate-limit/check \
  -H "Content-Type: application/json" \
  -d '{"clientId":"client-123"}'
```

### Evaluate Risk

```bash
curl -s -X POST http://localhost:8080/api/v1/risk/evaluate \
  -H "Content-Type: application/json" \
  -d '{"clientId":"client-123","method":"POST","path":"/api/v1/crypto/decrypt","rateLimitExceeded":true,"failedAuthAttempts":3}'
```

## Project Purpose

This project demonstrates Java backend engineering skills relevant to secure financial systems, including encryption, authentication, caching, request throttling, observability, gateway design, and suspicious behaviour detection.
