package com.careeris.api.bdd;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.careeris.api.support.ApiTestBase.TestData;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Step definitions for the CareerIS Gherkin features (glue code, REST Assured underneath).
 * Owner: EG/2023/5712 - Madhusanka E.M.P
 */
public class InternshipSteps {
    private static final String PASSWORD = "Password123";

    @LocalServerPort private int port;
    @Autowired private MongoTemplate mongo;

    private String companyToken;
    private String studentToken;
    private final Map<String, String> vacancyIds = new HashMap<>();
    private String applicationId;
    private Response lastResponse;

    @Before
    public void resetWorld() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        TestData.clean(mongo);
    }

    @Given("a company {string} has published an open vacancy {string}")
    public void companyPublishesVacancy(String companyName, String title) {
        TestData.register("COMPANY", "hr@acme.example", companyName, PASSWORD);
        companyToken = TestData.login("COMPANY", "hr@acme.example", PASSWORD);
        vacancyIds.put(title, TestData.createVacancy(companyToken, title, LocalDate.now().plusDays(20)));
    }

    @Given("a student {string} is signed in")
    public void studentSignedIn(String name) {
        TestData.register("STUDENT", "student@uni.example", name, PASSWORD);
        studentToken = TestData.login("STUDENT", "student@uni.example", PASSWORD);
    }

    @Given("the student has already applied to {string}")
    public void studentAlreadyApplied(String title) {
        studentApplies(title, "First application");
        lastResponse.then().statusCode(201);
        applicationId = lastResponse.path("id");
    }

    @Given("the company closes the vacancy {string}")
    public void companyClosesVacancy(String title) {
        given().auth().oauth2(companyToken).contentType(ContentType.JSON).body(Map.of("status", "CLOSED"))
                .when().patch("/company/vacancies/" + vacancyIds.get(title) + "/status").then().statusCode(200);
    }

    @When("the student applies to {string} with the note {string}")
    public void studentApplies(String title, String note) {
        lastResponse = given().auth().oauth2(studentToken).contentType(ContentType.JSON)
                .body(Map.of("vacancyId", vacancyIds.get(title), "coverLetter", note))
                .when().post("/student/applications");
    }

    @When("the company changes the application status to {string}")
    public void companyChangesStatus(String status) {
        lastResponse = given().auth().oauth2(companyToken).contentType(ContentType.JSON).body(Map.of("status", status))
                .when().patch("/company/applications/" + applicationId + "/status");
    }

    @When("the company schedules the interview {int} days from today at {string} at {string}")
    public void companySchedulesInterview(int days, String time, String location) {
        lastResponse = given().auth().oauth2(companyToken).contentType(ContentType.JSON)
                .body(Map.of("date", LocalDate.now().plusDays(days).toString(), "time", time, "location", location))
                .when().patch("/company/applications/" + applicationId + "/interview");
    }

    @Then("the application is accepted")
    public void applicationAccepted() {
        lastResponse.then().statusCode(201).body("status", equalTo("SUBMITTED"));
    }

    @Then("the student's application list shows {string} with status {string}")
    public void studentListShows(String title, String status) {
        given().auth().oauth2(studentToken).when().get("/student/applications")
                .then().statusCode(200).body("vacancyTitle", hasItem(title)).body("status", hasItem(status));
    }

    @Then("the request is rejected with HTTP status {int}")
    @Then("the request is answered with HTTP status {int}")
    public void requestAnsweredWith(int status) {
        lastResponse.then().statusCode(status);
    }

    @Then("the student sees the interview at {string} at {string}")
    public void studentSeesInterview(String time, String location) {
        given().auth().oauth2(studentToken).when().get("/student/applications")
                .then().statusCode(200)
                .body("[0].status", equalTo("INTERVIEW"))
                .body("[0].interviewTime", equalTo(time + ":00"))
                .body("[0].interviewLocation", equalTo(location));
    }
}
