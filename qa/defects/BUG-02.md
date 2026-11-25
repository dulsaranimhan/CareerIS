# BUG-02: A REJECTED application can be moved back to INTERVIEW or UNDER_REVIEW

| Field | Value |
|---|---|
| Module | Company / application review |
| Severity / Priority | Major / High |
| Status | Closed |
| Reported by | Wanasinghe W.M.C.O.B (EG/2023/5941) |
| Assigned to | Wanasinghe W.M.C.O.B (EG/2023/5941) |
| Found with | State-transition analysis, fixed with TDD |
| Jira | CIS-15 |

## Steps to reproduce
1. Company rejects an application (PATCH /status REJECTED).
2. PATCH /status INTERVIEW on the same application.

## Expected result
HTTP 409 - a rejected student must not be re-invited by mistake.

## Actual result
HTTP 200 - status changed to INTERVIEW; the student sees an interview invitation after being rejected.

## Root cause
ApplicationService.updateStatus() only checked the TARGET status, never the CURRENT status. No state machine was designed for applications.

## Fix
New ApplicationStatusTransitions class written test-first (TDD) and called from updateStatus(); returns 409 for illegal moves. Regression tests API-09 and Cucumber outline.
