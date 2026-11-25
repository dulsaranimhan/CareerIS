# BUG-05: E-mail without a domain suffix (nimal@localhost) is accepted at registration

| Field | Value |
|---|---|
| Module | Registration |
| Severity / Priority | Minor / Low |
| Status | Closed |
| Reported by | Jayasuriya J.A.D.N (EG/2023/5664) |
| Assigned to | Liyanarachchi L.A.E.E (EG/2023/5708) |
| Found with | Equivalence partitioning (TC-BB-EM-OBS) |
| Jira | CIS-18 |

## Steps to reproduce
Register with e-mail 'nimal@localhost'.

## Expected result
HTTP 400 - invalid e-mail.

## Actual result
HTTP 201 - account created.

## Root cause
Jakarta @Email follows RFC rules where a dot-less domain is legal.

## Fix
Fixed by Liyanarachchi: RegisterRequest.email uses @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$") with a clear message. Regression tests TC-BB-EM-05 and Selenium UI-06 pass.
