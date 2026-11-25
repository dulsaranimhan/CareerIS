# BUG-01: CV upload of a file of exactly 5 MB (and 5 MB - 1 byte) is rejected with HTTP 413

| Field | Value |
|---|---|
| Module | Student profile / CV upload |
| Severity / Priority | Major / High |
| Status | Closed |
| Reported by | Jayasuriya J.A.D.N (EG/2023/5664) |
| Assigned to | Wanasinghe W.M.C.O.B (EG/2023/5941) |
| Found with | BVA (TC-BB-CV-04, TC-BB-CV-05) |
| Jira | CIS-14 |

## Steps to reproduce
1. Sign in as a student.
2. POST /api/student/profile/cv with a 5,242,880-byte PDF.

## Expected result
HTTP 200 - README says CVs up to 5 MB are allowed.

## Actual result
HTTP 413 Payload Too Large. Even 5,242,879 bytes fails.

## Root cause
spring.servlet.multipart.max-request-size=5MB limits the WHOLE multipart request. The request also contains boundaries and part headers (~200 bytes), so a file near 5 MB pushes the request over the limit before the file-size rule is checked.

## Fix
max-request-size raised to 6MB; max-file-size stays 5MB (application.properties). Regression test TC-BB-CV-01..06 now passes.
