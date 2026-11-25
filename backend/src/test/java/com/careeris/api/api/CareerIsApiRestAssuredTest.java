package com.careeris.api.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.not;

import java.time.LocalDate;
import java.util.Map;

import com.careeris.api.support.ApiTestBase;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * EC4206 Section 3.6 - API test automation with REST Assured.
 * Each test drives the real HTTP API (Spring Boot on a random port + MongoDB test database).
 *
 * Owner: EG/2023/5712 - Madhusanka E.M.P
 */
@DisplayName("Automation: REST Assured API tests")
class CareerIsApiRestAssuredTest extends ApiTestBase {

    @Test
    @DisplayName("API-01: GET /api/health returns ok in under 2 s")
    void healthCheck() {
        given().when().get("/health")
                .then().statusCode(200).body("status", equalTo("ok")).time(lessThan(2000L));
    }

    @Test
    @DisplayName("API-02: register student -> 201 with id and role, duplicate e-mail -> 409")
    void registerAndDuplicate() {
        Map<String, String> body = Map.of("role", "STUDENT", "name", "Nimal", "email", "nimal@example.com", "password", PASSWORD);
        given().contentType(ContentType.JSON).body(body).when().post("/auth/register")
                .then().statusCode(201).body("id", notNullValue()).body("role", equalTo("STUDENT"));
        given().contentType(ContentType.JSON).body(Map.of("role", "STUDENT", "name", "Nimal 2", "email", "NIMAL@example.com", "password", PASSWORD))
                .when().post("/auth/register")
                .then().statusCode(409).body("message", equalTo("An account with this email already exists."));
    }

    @Test
    @DisplayName("API-03: login returns a Bearer JWT; wrong password -> 401")
    void login() {
        TestData.register("STUDENT", "nimal@example.com", "Nimal", PASSWORD);
        given().contentType(ContentType.JSON).body(Map.of("role", "STUDENT", "email", "nimal@example.com", "password", PASSWORD))
                .when().post("/auth/login")
                .then().statusCode(200).body("tokenType", equalTo("Bearer")).body("accessToken", notNullValue())
                .body("user.email", equalTo("nimal@example.com"));
        given().contentType(ContentType.JSON).body(Map.of("role", "STUDENT", "email", "nimal@example.com", "password", "Wrong12345"))
                .when().post("/auth/login").then().statusCode(401);
    }

    @Test
    @DisplayName("API-04: invalid registration body -> 400 with field message")
    void validationError() {
        given().contentType(ContentType.JSON).body(Map.of("role", "STUDENT", "name", "Nimal", "email", "nimal@example.com", "password", "short"))
                .when().post("/auth/register")
                .then().statusCode(400).body("message", equalTo("password: size must be between 8 and 72"));
    }

    @Test
    @DisplayName("API-05: company publishes vacancy -> student can browse and open it")
    void publishAndBrowseVacancy() {
        String company = registerAndLogin("COMPANY", "hr@acme.com", "Acme");
        String student = registerAndLogin("STUDENT", "nimal@example.com", "Nimal");
        String vacancyId = createVacancy(company, "Java Intern");

        given().auth().oauth2(student).when().get("/vacancies")
                .then().statusCode(200).body("title", hasItem("Java Intern")).body("[0].companyName", equalTo("Acme"));
        given().auth().oauth2(student).when().get("/vacancies/" + vacancyId)
                .then().statusCode(200).body("skills", hasItem("Java")).body("status", equalTo("OPEN"));
    }

    @Test
    @DisplayName("API-06: closed vacancy disappears from browse list and cannot be applied to")
    void closedVacancy() {
        String company = registerAndLogin("COMPANY", "hr@acme.com", "Acme");
        String student = registerAndLogin("STUDENT", "nimal@example.com", "Nimal");
        String vacancyId = createVacancy(company, "Closed Intern");
        given().auth().oauth2(company).contentType(ContentType.JSON).body(Map.of("status", "CLOSED"))
                .when().patch("/company/vacancies/" + vacancyId + "/status").then().statusCode(200).body("status", equalTo("CLOSED"));

        given().auth().oauth2(student).when().get("/vacancies").then().statusCode(200).body("$", hasSize(0));
        given().auth().oauth2(student).contentType(ContentType.JSON).body(Map.of("vacancyId", vacancyId))
                .when().post("/student/applications").then().statusCode(404);
    }

    @Test
    @DisplayName("API-07: past deadline in vacancy form -> 400")
    void pastDeadlineRejected() {
        String company = registerAndLogin("COMPANY", "hr@acme.com", "Acme");
        given().auth().oauth2(company).contentType(ContentType.JSON)
                .body(TestData.vacancyBody("Old", LocalDate.now().minusDays(1)))
                .when().post("/company/vacancies").then().statusCode(400);
    }

    @Test
    @DisplayName("API-08: full flow - apply, duplicate apply 409, invite, schedule, student sees interview")
    void endToEndApplicationFlow() {
        String company = registerAndLogin("COMPANY", "hr@acme.com", "Acme");
        String student = registerAndLogin("STUDENT", "nimal@example.com", "Nimal");
        String vacancyId = createVacancy(company, "QA Intern");
        String applicationId = apply(student, vacancyId);

        given().auth().oauth2(student).contentType(ContentType.JSON).body(Map.of("vacancyId", vacancyId))
                .when().post("/student/applications").then().statusCode(409);

        given().auth().oauth2(company).queryParam("vacancyId", vacancyId).when().get("/company/applications")
                .then().statusCode(200).body("$", hasSize(1)).body("[0].studentEmail", equalTo("nimal@example.com"));

        given().auth().oauth2(company).contentType(ContentType.JSON).body(Map.of("status", "INTERVIEW"))
                .when().patch("/company/applications/" + applicationId + "/status").then().statusCode(200);

        String date = LocalDate.now().plusDays(5).toString();
        given().auth().oauth2(company).contentType(ContentType.JSON)
                .body(Map.of("date", date, "time", "10:30", "location", "https://meet.example/abc", "notes", "Bring your NIC"))
                .when().patch("/company/applications/" + applicationId + "/interview").then().statusCode(200);

        given().auth().oauth2(student).when().get("/student/applications")
                .then().statusCode(200)
                .body("[0].status", equalTo("INTERVIEW"))
                .body("[0].interviewDate", equalTo(date))
                .body("[0].interviewTime", equalTo("10:30:00"))
                .body("[0].interviewNotes", equalTo("Bring your NIC"));
    }

    @Test
    @DisplayName("API-09: rejected application cannot be re-invited (BUG-02 regression test)")
    void rejectedCannotBeReinvited() {
        String company = registerAndLogin("COMPANY", "hr@acme.com", "Acme");
        String student = registerAndLogin("STUDENT", "nimal@example.com", "Nimal");
        String applicationId = apply(student, createVacancy(company, "QA Intern"));
        given().auth().oauth2(company).contentType(ContentType.JSON).body(Map.of("status", "REJECTED"))
                .when().patch("/company/applications/" + applicationId + "/status").then().statusCode(200);
        given().auth().oauth2(company).contentType(ContentType.JSON).body(Map.of("status", "INTERVIEW"))
                .when().patch("/company/applications/" + applicationId + "/status").then().statusCode(409);
    }

    @Test
    @DisplayName("API-10: student profile update + CV upload/download round trip")
    void profileAndCv() {
        String student = registerAndLogin("STUDENT", "nimal@example.com", "Nimal");
        given().auth().oauth2(student).contentType(ContentType.JSON)
                .body(Map.of("education", "BSc Eng (Ruhuna)", "qualification", "Java OCA", "profileSummary", "Keen tester",
                        "skills", java.util.List.of("Java", " Java ", "Selenium")))
                .when().put("/student/profile")
                .then().statusCode(200).body("skills", hasSize(2)).body("education", equalTo("BSc Eng (Ruhuna)"));

        byte[] pdf = "%PDF-1.4 test cv".getBytes();
        given().auth().oauth2(student).multiPart("file", "nimal-cv.pdf", pdf, "application/pdf")
                .when().post("/student/profile/cv").then().statusCode(200).body("cvFileName", equalTo("nimal-cv.pdf"));
        given().auth().oauth2(student).when().get("/student/profile/cv")
                .then().statusCode(200).header("Content-Disposition", equalTo("attachment; filename=\"nimal-cv.pdf\""))
                .body(not(equalTo("")));
    }
}
