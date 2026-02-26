## RAG Chat Storage Microservice

Java 17 Spring Boot microservice for storing chat sessions and messages from a RAG-based chatbot, with API key auth, rate limiting, logging, health checks, Swagger docs, and Dockerized Postgres + Adminer.

### Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3 (Web, Data JPA, Validation, Actuator, Security)
- **Database**: PostgreSQL
- **Build**: Maven
- **Docs**: springdoc-openapi (Swagger UI)
- **Other**: API key auth, in-memory rate limiting (Bucket4j), CORS, Docker, docker-compose, Adminer

### Setup

1. **Prerequisites**
   - Docker & Docker Compose

2. **Configure environment**
   - Copy `.env.example` to `.env` and adjust values:

     - **`DB_NAME`**: Database name (default `rag_chat`)
     - **`DB_USER`** / **`DB_PASSWORD`**: DB credentials
     - **`DB_PORT`**: Host port for Postgres (default `5432`)
     - **`API_KEY`**: API key required for all protected APIs
     - **`RATE_LIMIT_RPM`**: Requests per minute per IP (default `60`)
     - **`CORS_ALLOWED_ORIGINS`**: Comma-separated allowed origins (e.g. `http://localhost:3000`)

3. **Run with Docker**

   ```bash
   cd rag-chat-service
   docker compose up --build
   ```

   - App: `http://localhost:8080`
   *** Check hostname if in wsl window using - "hostname -I" if that comes with  - 172.29.239.238:8080 then
   - Swagger UI: http://172.29.239.238:8080/swagger-ui/index.html
   - Health: `http://localhost:8080/actuator/health`
   - Adminer: http://172.29.239.238:8081/ (server: `postgres`, DB/user/pass from `.env`)

4. **Run tests locally (optional)**

   ```bash
   mvn test
   ```

---

# 🔐 Security Design

## API Key Authentication

All APIs are secured using API key authentication as required by the case study specification.

### How It Works

- Each request must include:
  X-API-KEY: <your-api-key>
- API key is read from environment variables
- Stateless authentication (no HTTP sessions)
- Invalid/missing keys return 401 Unauthorized

---

## 🔒 Environment-Based Secret Management

The API key is configured via environment variable:

API_KEY=your-secret-key

Benefits:

- No hardcoded secrets
- Supports Dockerized deployments

---

# 🚦 Rate Limiting

To prevent API abuse, rate limiting is implemented using Bucket4j.

### Current Implementation

- Algorithm: Token Bucket
- Scope: Per API Key
- Storage: In-memory (ConcurrentHashMap)
- Configurable via:
  rate-limiting.requests-per-minute

If the rate limit is exceeded:
429 Too Many Requests

------------------------------------------------------------------------

## ⚠ Scalability Consideration

Current rate limiting is in-memory and instance-specific.

In horizontally scaled deployments: - Each instance maintains its own
bucket - Effective rate limit increases with instance count

------------------------------------------------------------------------

## 🚀 Production Upgrade Path

For distributed systems, rate limiting can be upgraded to:

-   Redis-backed Bucket4j
-   API Gateway-based throttling
-   Spring Cloud Gateway rate limiter

This enables:

-   Centralized rate control
-   Horizontal scalability
-   Consistent global limits

------------------------------------------------------------------------

### CORS

- Configured via `CORS_ALLOWED_ORIGINS` env var.
- Set to `*` for development, or a list like `http://localhost:3000,http://localhost:5173`.

---

# 🗄 Database Choice

## Current Implementation

This service currently uses **PostgreSQL (Relational Database)** for persistent storage.

The domain model consists of:

- Chat Sessions
- Chat Messages
- One-to-many relationship between sessions and messages
- Structured schema with pagination and filtering support

A relational database fits naturally for the current structured data model and transactional consistency requirements.

---

## Future Extensibility

The architecture is designed in a layered manner (Controller → Service → Repository), which allows flexibility in the persistence layer.

If future requirements evolve, the system can be adapted to use:

- A NoSQL document database (e.g., MongoDB)
- A distributed data store
- A hybrid SQL + NoSQL approach

NoSQL could be considered if:

- Message schema becomes highly flexible or dynamic
- Horizontal scalability requirements increase significantly
- Large volumes of unstructured or semi-structured data are introduced

The current design does not prevent migration or integration with NoSQL-based storage in the future.

---

### Domain Model

- **ChatSession**
  - `id` (UUID)
  - `userId` (string)
  - `title` (string)
  - `favorite` (boolean)
  - `createdAt`, `updatedAt`
  - One-to-many to `ChatMessage` (cascade delete)

- **ChatMessage**
  - `id` (UUID)
  - `session` (ChatSession)
  - `sender` (`USER` or `ASSISTANT`, free string)
  - `content` (text)
  - `context` (optional text/JSON)
  - `createdAt`

### REST APIs

Base path: `/api/v1`

All examples assume header: `X-API-KEY: <your-api-key>`.

- **Create session**
  - **POST** `/api/v1/sessions`
  - Body:
    ```json
    {
      "userId": "user-123",
      "title": "New RAG chat"
    }
    ```
  - Response `201 Created`:
    ```json
    {
      "id": "uuid",
      "userId": "user-123",
      "title": "New RAG chat",
      "favorite": false,
      "createdAt": "...",
      "updatedAt": "..."
    }
    ```

- **List sessions for user**
  - **GET** `/api/v1/sessions?userId=user-123`
  - Response `200 OK`: array of `ChatSessionResponse`, most recently updated first.

- **Rename session**
  - **PUT** `/api/v1/sessions/{sessionId}/rename`
  - Body:
    ```json
    { "title": "Renamed chat" }
    ```
  - Response `200 OK`: updated `ChatSessionResponse`.

- **Mark/unmark favorite**
  - **PUT** `/api/v1/sessions/{sessionId}/favorite`
  - Body:
    ```json
    { "favorite": true }
    ```
  - Response `200 OK`: updated `ChatSessionResponse`.

- **Delete session**
  - **DELETE** `/api/v1/sessions/{sessionId}`
  - Response `204 No Content`; deletes session and all associated messages.

- **Add message**

  Supports two modes:

  1) Add message to an existing session  
  2) Auto-create a session if sessionId is not provided  

  ---
  
  - **POST** `/api/v1/sessions/{sessionId}/messages`
  
    - Path Parameter:
      - `sessionId` (UUID) – Required
  
    - Body:
      ```json
      {
        "userId": "user-123",
        "sender": "USER",
        "content": "What is RAG?",
        "context": "Initial user query"
      }
      ```
  
    - Behavior:
      - Validates that session exists
      - Stores message under the given session
      - Updates session `updatedAt`
  
    - Response `201 Created`: `ChatMessageResponse`

  ---
  
  - **POST** `/api/v1/sessions/messages`
  
    - Path Parameter:
      - None
  
    - Body:
      ```json
      {
        "userId": "user-123",
        "sender": "USER",
        "content": "Explain Java 17 features",
        "context": "initial question"
      }
      ```
  
    - Behavior:
      - Automatically creates a new session
      - Uses first 30 characters of `content` as session title
      - Stores message in newly created session
      - Ensures no message loss
  
    - Response `201 Created`: `ChatMessageResponse`

  ---
  
  - Notes:
    - `userId` is mandatory in both cases
    - If `sessionId` is invalid → `404 Not Found`
    - If request validation fails → `400 Bad Request`
    - Requires header:
      ```
      X-API-KEY: <your-api-key>
      ```

- **Get messages (paginated)**
  - **GET** `/api/v1/sessions/{sessionId}/messages?page=0&size=20`
  - Response `200 OK`: Spring `Page` JSON with `content`, `totalElements`, `totalPages`, etc.

### Health & Observability

- **Health check**: `/actuator/health`
  - Used for Kubernetes/Docker health probes.
- **Logging**
  - Centralized Logback configuration (`logback-spring.xml`) with structured console output.
  - Application logs for controllers, services, auth, and rate limiting via SLF4J.

### Swagger / OpenAPI

- OpenAPI JSON: `/v3/api-docs`
- Swagger UI: `/swagger-ui/index.html`

### Notes & Potential Extensions

- Current rate limiting is in-memory (single-instance). For distributed deployments, replace with a shared store (Redis, etc.) or a library like Bucket4j with a shared backend.
- Message `context` is a free-form `TEXT` column, suitable for storing RAG retrieval metadata or serialized JSON.

