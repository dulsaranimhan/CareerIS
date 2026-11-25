# BUG-06: Vacancy deadline has no upper limit (year 9999 accepted)

| Field | Value |
|---|---|
| Module | Company / vacancies |
| Severity / Priority | Minor / Low |
| Status | Open |
| Reported by | Jayasuriya J.A.D.N (EG/2023/5664) |
| Assigned to | Madhusanka E.M.P (EG/2023/5712) |
| Found with | BVA on date (TC-BB-DL-OBS) |
| Jira | CIS-19 |

## Steps to reproduce
Create a vacancy with deadline 9999-12-31.

## Expected result
HTTP 400 (e.g. max 1 year ahead).

## Actual result
HTTP 201 - vacancy stays open forever.

## Root cause
Only @FutureOrPresent is applied; no maximum was specified.

## Fix
Proposed: custom validator - deadline <= today + 365 days.
