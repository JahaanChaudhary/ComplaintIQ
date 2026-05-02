# ComplaintIQ — AI-Powered Customer Complaint Management System

**Now powered by Google Gemini (free tier)**

## Quick Start

### Prerequisites
- Java 17
- MySQL 8
- Maven 3.8+
- **Gemini API Key** (free — no credit card needed)

### Get a Gemini API Key
1. Go to https://aistudio.google.com/apikey
2. Sign in with your Google account
3. Click "Create API key"
4. Copy the key (looks like `AIzaSyXxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`)

**Free tier:** 15 requests/min, 1M tokens/day, 1,500 requests/day. More than enough for this project.

### Setup

1. **Create MySQL database:**
   ```sql
   CREATE DATABASE complaintiq_db;
   ```

2. **Fill in `src/main/resources/application.properties`:**
   ```properties
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   app.gemini.api-key=YOUR_GEMINI_KEY
   spring.mail.username=YOUR_GMAIL
   spring.mail.password=YOUR_GMAIL_APP_PASSWORD
   ```

3. **Run:**
   ```bash
   mvn spring-boot:run
   ```

4. **Open Swagger:** http://localhost:8080/swagger-ui.html

### Default Test Credentials (seeded on first run)
| Role     | Email                          | Password      |
|----------|--------------------------------|---------------|
| Admin    | admin@complaintiq.com          | Admin@123     |
| Manager  | manager1@complaintiq.com       | Manager@123   |
| Agent    | priya.sharma@complaintiq.com   | Agent@123     |
| Customer | aarav.shah@gmail.com           | Customer@123  |

## Project Structure
```
com.complaintiq/
├── config/        SecurityConfig, AsyncConfig, SwaggerConfig, GeminiConfig, DataSeeder
├── auth/          JWT auth, refresh tokens, user management
├── customer/      Customer profiles
├── complaint/     Core complaint lifecycle
├── ai/            Gemini AI integration (urgency, category, sentiment, intent)
├── assignment/    Auto-assignment engine (5-level waterfall)
├── sla/           SLA timer + auto-escalation scheduler
├── escalation/    Escalation tracking
├── resolution/    Resolution + feedback loop
├── notification/  Async email notifications
├── analytics/     Dashboard + reports
└── websocket/     Real-time status updates
```

## Architecture Notes
- **AI:** Gemini 1.5 Flash classifies urgency (4 levels), category (6 types), sentiment (5 levels), intent (5 types) via a single REST call with `responseMimeType: application/json` for guaranteed valid JSON.
- **Auto-Assignment:** 5-level waterfall — Critical/Legal → Senior; VIP → Senior; Normal → Least-loaded; Fallback → Dept head; Last resort → any available agent
- **SLA Engine:** @Scheduled task every 5 minutes. Warns at 75% of SLA, auto-escalates on breach using role chain (JUNIOR → SENIOR → TEAM_LEAD → MANAGER).
- **WebSocket:** STOMP over SockJS — live complaint status updates on `/topic/complaint/{ticketId}`
- **Feedback Loop:** Score ≤ 2 → complaint auto-reopens and notifies assigned agent.

## Key API Endpoints
- `POST /api/auth/register` — Register customer
- `POST /api/auth/login` — Login (returns JWT)
- `POST /api/complaints` — Submit complaint (triggers AI analysis + assignment)
- `GET /api/complaints/track/{ticketId}` — Track complaint (public)
- `PUT /api/complaints/{ticketId}/status` — Agent updates status
- `POST /api/complaints/{ticketId}/resolve` — Agent resolves
- `POST /api/complaints/{ticketId}/feedback` — Customer rates resolution
- `POST /api/complaints/{ticketId}/escalate` — Manual escalation
- `GET /api/analytics/dashboard` — Admin dashboard stats
- `GET /api/agents/{id}/dashboard` — Agent personal dashboard

## Switching AI Providers
If you want to switch to OpenAI, Claude, or any other LLM:
1. The AI call is isolated in `AIAnalysisService.java`'s `callGemini()` method
2. Replace the HTTP call with your provider's endpoint
3. Adjust the response extraction in `extractGeminiText()`
4. Everything else stays the same — the prompt, parsing, DB save, fallback logic all work with any LLM
