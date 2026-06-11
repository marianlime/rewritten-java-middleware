# Secure Middleware Java

A Java Spring Boot rewrite of a secure middleware platform, focused on API security, encryption, rate limiting, and request observability.

## Features

- AES-GCM encryption and decryption API
- Sliding-window rate limiter using thread-safe Java collections
- Request audit logging with correlation IDs
- REST API endpoints for crypto, rate-limit checks, and recent audit events
- Dockerised Spring Boot deployment

## Tech Stack

- Java 21
- Spring Boot
- Spring Security
- Maven
- Docker
- JUnit

## Architecture

The application exposes secure middleware-style APIs and processes requests through a Spring Boot backend.

Core components:

- `crypto` — AES-GCM encryption/decryption service
- `ratelimit` — sliding-window rate limiter using `ConcurrentHashMap` and timestamp queues
- `audit` — request logging filter with correlation IDs and recent event storage
- `config` — temporary security configuration for local API testing

## Running Locally

```bash
./mvnw spring-boot:run

