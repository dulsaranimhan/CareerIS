# BUG-03: Password characters after the 72nd byte are ignored - a different password can sign in

| Field | Value |
|---|---|
| Module | Authentication |
| Severity / Priority | Critical / Highest |
| Status | Open |
| Reported by | Jayasuriya J.A.D.N (EG/2023/5664) |
| Assigned to | Wanasinghe W.M.C.O.B (EG/2023/5941) |
| Found with | BVA on bytes vs characters (TC-BB-PW-MB) |
| Jira | CIS-16 |

## Steps to reproduce
1. Register with password = 24 Sinhala letters + 'secret' (30 chars, 90 bytes).
2. Log in with 24 Sinhala letters + 'WRONG!'.

## Expected result
HTTP 401 - wrong password.

## Actual result
HTTP 200 - login succeeds and a JWT is issued.

## Root cause
BCrypt only uses the first 72 BYTES of a password. Validation (@Size max=72) counts CHARACTERS. Sinhala/Tamil letters are 3 bytes in UTF-8, so a 30-letter password is 90 bytes and the last 18 bytes are silently dropped.

## Fix
Proposed: validate UTF-8 byte length <= 72 (custom validator) or pre-hash with SHA-256 before bcrypt / use Argon2PasswordEncoder.
