# EC4206 Section 3.5 - BDD with Gherkin + Cucumber
# Owner: EG/2023/5712 - Madhusanka E.M.P
Feature: Student applies for an internship
  As a student looking for an internship
  I want to apply to open vacancies
  So that companies can review my application

  Background:
    Given a company "Acme Software" has published an open vacancy "Java Intern"
    And a student "Nimal Perera" is signed in

  Scenario: Student applies to an open vacancy
    When the student applies to "Java Intern" with the note "I love Java"
    Then the application is accepted
    And the student's application list shows "Java Intern" with status "SUBMITTED"

  Scenario: Student cannot apply to the same vacancy twice
    Given the student has already applied to "Java Intern"
    When the student applies to "Java Intern" with the note "Second try"
    Then the request is rejected with HTTP status 409

  Scenario: Student cannot apply after the company closes the vacancy
    Given the company closes the vacancy "Java Intern"
    When the student applies to "Java Intern" with the note "Please"
    Then the request is rejected with HTTP status 404
