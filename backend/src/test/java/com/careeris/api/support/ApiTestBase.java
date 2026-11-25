package com.careeris.api.support;

import static io.restassured.RestAssured.given;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Shared base class for API-level tests (REST Assured).
 * Starts the real Spring Boot API on a random port against the careeris_test MongoDB database
 * and wipes every collection before each test so tests never depend on each other.
 *
 * Owner: EG/2023/5712 - Madhusanka E.M.P (test automation)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class ApiTestBase {
    protected static final String PASSWORD = "Password123";

    @LocalServerPort
    protected int port;

    @Autowired
    protected MongoTemplate mongo;

    @BeforeEach
    void resetDatabaseAndClient() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        TestData.clean(mongo);
    }

    protected String registerAndLogin(String role, String email, String name) {
        TestData.register(role, email, name, PASSWORD);
        return TestData.login(role, email, PASSWORD);
    }

    protected String createVacancy(String companyToken, String title) {
        return TestData.createVacancy(companyToken, title, LocalDate.now().plusDays(30));
    }

    protected String apply(String studentToken, String vacancyId) {
        return given().auth().oauth2(studentToken).contentType(ContentType.JSON)
                .body(Map.of("vacancyId", vacancyId, "coverLetter", "I would like to join."))
                .when().post("/student/applications")
                .then().statusCode(201).extract().path("id");
    }

    /** Small static helpers shared with the Cucumber step definitions. */
    public static final class TestData {
        private TestData() { }

        public static void clean(MongoTemplate mongo) {
            for (String name : mongo.getCollectionNames()) {
                if (!name.startsWith("system.")) {
                    mongo.getCollection(name).deleteMany(new Document());
                }
            }
        }

        public static void register(String role, String email, String name, String password) {
            Map<String, Object> body = new java.util.HashMap<>(Map.of(
                    "role", role, "name", name, "email", email, "password", password));
            if ("COMPANY".equals(role)) {
                body.put("contactName", "Hiring Manager");
            }
            given().contentType(ContentType.JSON).body(body)
                    .when().post("/auth/register")
                    .then().statusCode(201);
        }

        public static String login(String role, String email, String password) {
            return given().contentType(ContentType.JSON)
                    .body(Map.of("role", role, "email", email, "password", password))
                    .when().post("/auth/login")
                    .then().statusCode(200).extract().path("accessToken");
        }

        public static Map<String, Object> vacancyBody(String title, LocalDate deadline) {
            return Map.of(
                    "title", title,
                    "description", "Work with our engineering team on real projects.",
                    "skills", List.of("Java", "Testing"),
                    "location", "Colombo",
                    "workMode", "HYBRID",
                    "deadline", deadline.toString());
        }

        public static String createVacancy(String companyToken, String title, LocalDate deadline) {
            return given().auth().oauth2(companyToken).contentType(ContentType.JSON)
                    .body(vacancyBody(title, deadline))
                    .when().post("/company/vacancies")
                    .then().statusCode(201).extract().path("id");
        }
    }
}
