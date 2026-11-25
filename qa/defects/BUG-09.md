# BUG-09: Public endpoints (login, health) return 401 when an expired/invalid Bearer token is sent

| Field | Value |
|---|---|
| Module | Security configuration |
| Severity / Priority | Minor / Low |
| Status | Open |
| Reported by | Madhusanka E.M.P (EG/2023/5712) |
| Assigned to | Madhusanka E.M.P (EG/2023/5712) |
| Found with | Found while building the JMeter script |
| Jira | CIS-22 |

## Steps to reproduce
POST /api/auth/login with header 'Authorization: Bearer expired.token.value'.

## Expected result
HTTP 200 - login is public.

## Actual result
HTTP 401 - API clients holding an old token cannot log in again.

## Root cause
Spring's BearerTokenAuthenticationFilter validates any Authorization header before permitAll() rules are applied.

## Fix
Proposed: ignore the bearer token on public paths (custom BearerTokenResolver).
