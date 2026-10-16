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

`POST /api/auth/login` accepts a role, email, and password. On success it returns an eight-hour bearer token and the account details. The frontend keeps the token for the current browser session and opens the student or company dashboard based on the account role. Protected API routes require that token in an `Authorization: Bearer <token>` header.

## Company vacancies

Companies can create and manage their own vacancies in the company dashboard. A vacancy includes a title, description, comma-separated skills, location, work arrangement (`ON_SITE`, `HYBRID`, or `REMOTE`), and an application deadline. Vacancies are saved in MongoDB, appear in the company's list, and can be edited, closed, or reopened. Only open vacancies are returned by the student feed.

- `GET /api/company/vacancies` — list the signed-in company's vacancies
- `POST /api/company/vacancies` — create a vacancy
- `PUT /api/company/vacancies/{id}` — edit a vacancy owned by that company
- `PATCH /api/company/vacancies/{id}/status` — change status to `OPEN` or `CLOSED`
- `GET /api/vacancies` — list open vacancies for authenticated students and companies

The API derives the company ID from the signed-in token. A company cannot edit or close another company's vacancy.

## Student browsing and applications

The student dashboard loads open vacancies and the student's application history. Students can search by title, company, skill, and location, filter by work arrangement, inspect details, and apply with an optional note. Applications are saved with a `SUBMITTED` status and a snapshot of the student and vacancy details. Duplicate applications are prevented, including by a MongoDB unique index. Applications after a deadline or to a closed vacancy are rejected.

- `GET /api/vacancies` — list open vacancies with future deadlines
- `GET /api/vacancies/{id}` — get an open vacancy
- `GET /api/student/applications` — list the signed-in student's applications
- `POST /api/student/applications` — apply with `vacancyId` and optional `coverLetter`

Application status values include `SUBMITTED`, `UNDER_REVIEW`, `INTERVIEW`, `REJECTED`, and `WITHDRAWN`. Companies can view applicants to their own vacancies, read their contact email and optional note, and update status to Under review, Interview, or Rejected. Students can refresh their application list to see company updates. Company ownership is checked by the API before listing or updating applications.

- `GET /api/company/applications` — list applications for the signed-in company's vacancies
- `GET /api/company/applications?vacancyId={id}` — filter applicants to one vacancy
- `PATCH /api/company/applications/{id}/status` — update status to `UNDER_REVIEW`, `INTERVIEW`, or `REJECTED`

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
