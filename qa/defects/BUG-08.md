# BUG-08: Deadline checks use the server time zone (UTC on Render) instead of Asia/Colombo

| Field | Value |
|---|---|
| Module | Vacancies / applications |
| Severity / Priority | Minor / Medium |
| Status | Open |
| Reported by | Wanasinghe W.M.C.O.B (EG/2023/5941) |
| Assigned to | Wanasinghe W.M.C.O.B (EG/2023/5941) |
| Found with | White-box code review (static) |
| Jira | CIS-21 |

## Steps to reproduce
Deploy on Render (UTC). Vacancy deadline = 24 Nov. At 25 Nov 02:00 Sri Lanka time, apply.

## Expected result
HTTP 409 - deadline passed in Sri Lanka.

## Actual result
Application accepted until 05:30 SL time because UTC date is still 24 Nov.

## Root cause
ApplicationService.apply() and VacancyService use LocalDate.now() (JVM zone) while scheduleInterview() uses Asia/Colombo - inconsistent design.

## Fix
Proposed: one Clock bean with ZoneId Asia/Colombo injected into all services.
