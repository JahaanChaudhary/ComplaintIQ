\# ComplaintIQ



AI-powered customer complaint management system with role-based access, real-time updates, and intelligent categorization.



\## Architecture



This is a monorepo containing both backend and frontend:



\- \*\*`backend/`\*\* — Spring Boot 3.2.3 REST API (Java 17, PostgreSQL, JWT, WebSocket)

\- \*\*`frontend/`\*\* — React 18 + Vite SPA (TailwindCSS, Axios, STOMP)



\## Tech Stack



\### Backend

\- Java 17 (Temurin)

\- Spring Boot 3.2.3 (Web, Security, Data JPA, WebSocket, Mail)

\- PostgreSQL 18

\- JWT Authentication (JJWT)

\- Hibernate ORM 6.4

\- HikariCP (connection pool)

\- Lombok, MapStruct

\- Google Gemini 2.5 Flash (AI categorization)

\- Gmail SMTP



\### Frontend

\- React 18

\- Vite 5

\- TailwindCSS

\- React Router 6

\- Axios

\- SockJS + STOMP.js (real-time)



\## Features



\- Four role types: \*\*Customer\*\*, \*\*Agent\*\*, \*\*Team Lead\*\*, \*\*Admin\*\*

\- JWT-based authentication with role-based authorization

\- Real-time complaint updates via WebSocket

\- AI-powered complaint categorization (Gemini)

\- SLA tracking with breach detection

\- Email notifications via Gmail SMTP

\- Audit trail and complaint history



\## Setup



\### Prerequisites



\- Java 17

\- Node.js 18+

\- PostgreSQL 18 (running on `localhost:5432`)

\- Gmail App Password

\- Gemini API Key



\### Backend Setup



```bash

cd backend



\# Create database in PostgreSQL

\# CREATE DATABASE complaintiq\_db;



\# Copy local config template

cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties



\# Edit application-local.properties with your credentials



\# Run

./mvnw spring-boot:run

```



Backend runs at `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui.html`



\### Frontend Setup



```bash

cd frontend

npm install

npm run dev

```



Frontend runs at `http://localhost:5173`



\## Default Seeded Users



| Role       | Email                          | Password     |

|------------|--------------------------------|--------------|

| Admin      | admin@complaintiq.com          | Admin@123    |

| Manager    | manager1@complaintiq.com       | Manager@123  |

| Team Lead  | priya.sharma@complaintiq.com   | Agent@123    |

| Customer   | aarav.shah@gmail.com           | Customer@123 |



\## License



MIT

