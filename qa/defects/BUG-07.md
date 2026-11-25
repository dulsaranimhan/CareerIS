# BUG-07: CV content type is trusted from the client - HTML renamed to .pdf is stored and served as text/html

| Field | Value |
|---|---|
| Module | Student profile / CV upload |
| Severity / Priority | Major / High |
| Status | Open |
| Reported by | Jayasuriya J.A.D.N (EG/2023/5664) |
| Assigned to | Wanasinghe W.M.C.O.B (EG/2023/5941) |
| Found with | OWASP A04 Insecure Design (SEC-14) |
| Jira | CIS-20 |

## Steps to reproduce
Upload 'cv.pdf' whose body is <script>alert(1)</script> with Content-Type text/html.

## Expected result
HTTP 400 - file is not a real PDF/DOC/DOCX.

## Actual result
HTTP 200 - file stored; download returns Content-Type text/html.

## Root cause
StudentProfileService only checks the file NAME extension and copies the client's Content-Type header.

## Fix
Proposed: check magic bytes (%PDF, D0CF11E0, PK) with Apache Tika and set the content type on the server.
