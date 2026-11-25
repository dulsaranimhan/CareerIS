# EC4206 Section 3.5 - BDD with Gherkin + Cucumber
# Owner: EG/2023/5712 - Madhusanka E.M.P
Feature: Company schedules an interview
  As a company recruiter
  I want to invite applicants and schedule interviews
  So that students know when and where to attend

  Background:
    Given a company "Acme Software" has published an open vacancy "QA Intern"
    And a student "Nimal Perera" is signed in
    And the student has already applied to "QA Intern"

  Scenario: Company invites the student and schedules an interview
    When the company changes the application status to "INTERVIEW"
    And the company schedules the interview 5 days from today at "10:30" at "https://meet.example/qa"
    Then the student sees the interview at "10:30" at "https://meet.example/qa"

  Scenario: Company cannot schedule an interview before inviting the student
    When the company schedules the interview 5 days from today at "10:30" at "Room 4"
    Then the request is rejected with HTTP status 409

  Scenario Outline: Company can only use allowed status changes
    Given the company changes the application status to "<first>"
    When the company changes the application status to "<second>"
    Then the request is answered with HTTP status <code>

    Examples:
      | first        | second       | code |
      | UNDER_REVIEW | INTERVIEW    | 200  |
      | INTERVIEW    | REJECTED     | 200  |
      | REJECTED     | INTERVIEW    | 409  |
      | INTERVIEW    | UNDER_REVIEW | 409  |
