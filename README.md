# CareerIS — Student Skills and Internship Matching System

CareerIS connects students looking for internships with companies that publish vacancies. Students create profiles, add qualifications, upload a CV, browse vacancies, and apply. Companies manage vacancies and review applications, including shortlisting candidates for interviews.

## Project scope

- Student and company registration and sign-in
- Student profile, qualifications, skills, and CV
- Company profile and vacancy management
- Vacancy browsing and applications
- Company applicant review and application status updates

No AI matching is included in this project scope.

## Technology

- Frontend: React, JavaScript, Vite, CSS
- Backend: Java, Spring Boot, Maven
- Database: MongoDB with Spring Data MongoDB
- API: REST using JSON
- Version control: Git and GitHub

## Repository layout

```text
frontend/   React application
backend/    Spring Boot application
```

## Account registration API

The backend provides `POST /api/auth/register` for student and company accounts. Send JSON with `role` (`STUDENT` or `COMPANY`), `name`, `email`, and `password`. Company registrations also require `contactName`. Passwords are hashed before saving, and email addresses are normalized and unique in the `users` collection.

Example student request:

```json
{
  "role": "STUDENT",
  "name": "Maya Perera",
  "email": "maya@example.com",
  "password": "a-secure-password"
}
```

Example company request:

```json
{
  "role": "COMPANY",
  "name": "Northstar Studio",
  "contactName": "Alex Fernando",
  "email": "alex@northstar.example",
  "password": "a-secure-password"
}
```

The API returns `201 Created` with the account ID, name, email, role, and creation time. Duplicate emails return `409 Conflict`.

`POST /api/auth/login` accepts a role, email, and password. On success it returns an eight-hour bearer token and the account details. The frontend keeps the token for the current browser session and opens the student or company dashboard based on the account role. Protected API routes require that token in an `Authorization: Bearer <token>` header. The dashboards are starter screens; profile, vacancy, and application features are still to be built.

## Run locally

Prerequisites: Node.js, Java 17 or newer, Maven, and MongoDB.

Start MongoDB locally, then run the backend with `mvn spring-boot:run` from `backend/`. For repeatable local sessions, set `JWT_SECRET` to a Base64-encoded key of at least 32 bytes before starting the backend (for example, generate one with `openssl rand -base64 32`). Without it, the backend creates a temporary key at startup and issued tokens stop working after a restart. Run the frontend with `npm install` followed by `npm run dev` from `frontend/`.

## Development plan

1. Project setup and API connection
2. Student and company accounts
3. Student profile and CV
4. Company profile and vacancy management
5. Vacancy browsing and applications
6. Company applicant review and statuses
7. Bug fixes, documentation, and demo

## Security note

Never commit passwords, database credentials, real CVs, or other private data. Use environment variables for local configuration.
