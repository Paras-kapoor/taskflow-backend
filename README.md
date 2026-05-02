# TaskFlow — Team Task Manager

A full-stack task management app with role-based access control.

## Live Demo
Frontend: https://taskflow-frontend-production.up.railway.app
Backend API: https://taskflow-backend-production.up.railway.app/api

## Tech Stack
- **Frontend:** React.js, Vite, Axios, React Router
- **Backend:** Java 17, Spring Boot, Spring Security, JWT
- **Database:** PostgreSQL
- **Deployment:** Railway

## Features
- JWT Authentication (Register/Login)
- Create and manage projects
- Kanban task board (To Do / In Progress / Done)
- Role-based access (Admin / Member)
- Dashboard with stats and overdue tracking

## Running Locally

### Backend
```bash
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

## Environment Variables

### Backend
| Variable | Description |
|---|---|
| SPRING_DATASOURCE_URL | PostgreSQL connection URL |
| SPRING_DATASOURCE_USERNAME | DB username |
| SPRING_DATASOURCE_PASSWORD | DB password |
| JWT_SECRET | Secret key for JWT signing |
| JWT_EXPIRATION | Token expiry in milliseconds |

### Frontend
| Variable | Description |
|---|---|
| VITE_API_URL | Backend API base URL |
