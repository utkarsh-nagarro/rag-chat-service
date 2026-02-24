## RAG Chat Storage Microservice

Java 17 Spring Boot microservice for storing chat sessions and messages from a RAG-based chatbot, with API key auth, rate limiting, logging, health checks, Swagger docs, and Dockerized Postgres + Adminer.

### Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3 (Web, Data JPA, Validation, Actuator)
- **Database**: PostgreSQL
- **Build**: Maven
- **Docs**: springdoc-openapi (Swagger UI)
- **Other**: API key auth, in-memory rate limiting, CORS, Docker, docker-compose, Adminer

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

### API Authentication

- All `/api/**` endpoints are protected by an **API key**.
- Header name: **`X-API-KEY`** (configurable via `security.api-key-header`).
- Value: must match **`API_KEY`** environment property.
- Health and Swagger endpoints are publicly accessible.

### Rate Limiting

-   Implemented using Bucket4j
-   Configurable per API key
-   Returns 429 if exceeded

### CORS

- Configured via `CORS_ALLOWED_ORIGINS` env var.
- Set to `*` for development, or a list like `http://localhost:3000,http://localhost:5173`.

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

