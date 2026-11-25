# BUG-04: No brute-force protection: unlimited wrong-password attempts are allowed

| Field | Value |
|---|---|
| Module | Authentication |
| Severity / Priority | Major / High |
| Status | Open |
| Reported by | Jayasuriya J.A.D.N (EG/2023/5664) |
| Assigned to | Wanasinghe W.M.C.O.B (EG/2023/5941) |
| Found with | OWASP A07 check (SEC-13) |
| Jira | CIS-17 |

## Steps to reproduce
Send 11 wrong passwords for the same account in a row.

## Expected result
After 10 failures the account is throttled (HTTP 429) or locked for a short time.

## Actual result
Every attempt returns 401 and attempts are never limited.

## Root cause
No rate limiting or failed-attempt counter was designed into AuthService (security requirement missing in the original spec).

## Fix
Proposed: count failures per e-mail/IP (e.g. Bucket4j) and return 429 for 15 minutes after 10 failures.
