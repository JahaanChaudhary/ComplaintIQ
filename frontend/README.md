# ComplaintIQ Frontend

React + Vite + Tailwind CSS frontend for the ComplaintIQ backend.

## Setup

```bash
npm install
npm run dev
```

Open http://localhost:5173

## Backend
Make sure the Spring Boot backend is running on port 8080.
The Vite proxy forwards /api and /ws to http://localhost:8080.

## Demo Credentials
| Role     | Email                          | Password      |
|----------|--------------------------------|---------------|
| Admin    | admin@complaintiq.com          | Admin@123     |
| Manager  | manager1@complaintiq.com       | Manager@123   |
| Agent    | priya.sharma@complaintiq.com   | Agent@123     |
| Customer | aarav.shah@gmail.com           | Customer@123  |

## Features
- Customer: Submit complaints, track by ticket ID, view history, submit feedback
- Agent: Personal dashboard, complaint management, resolve/reassign/escalate
- Admin/Manager: Full complaint management, agent management, analytics dashboard
- Real-time: WebSocket live status updates on tracking page
- AI: View AI classification results on each complaint
