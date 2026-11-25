package com.careeris.api.security;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import com.careeris.api.support.ApiTestBase;
import io.restassured.http.ContentType;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * EC4206 Section 3.7 - Manual-style OWASP Top 10 (2021) checks, automated so they can be re-run.
 *   A01 Broken Access Control       - role checks, IDOR between companies, unauthenticated access
 *   A02 Cryptographic Failures      - passwords stored as bcrypt hashes
 *   A03 Injection                   - NoSQL operator injection in the login body
 *   A04 Insecure Design             - CV content type is trusted (open finding BUG-07)
 *   A07 Identification & Auth Fail. - forged / tampered JWT, brute-force (open finding BUG-04)
 *
 * Owner: EG/2023/5664 - Jayasuriya J.A.D.N
 */
@DisplayName("Security: OWASP Top 10 checks")
class OwaspSecurityTest extends ApiTestBase {

    @Nested
    @DisplayName("A01 Broken Access Control")
    class BrokenAccessControl {
        @Test
        @DisplayName("SEC-01: no token -> 401 on protected route")
        void noTokenIsRejected() {
            given().when().get("/vacancies").then().statusCode(401);
        }

        @Test
        @DisplayName("SEC-02: student token cannot call company routes (vertical privilege escalation)")
        void studentCannotUseCompanyRoutes() {
            String student = registerAndLogin("STUDENT", "s@example.com", "Student");
            given().auth().oauth2(student).when().get("/company/applications").then().statusCode(403);
            given().auth().oauth2(student).contentType(ContentType.JSON)
                    .body(TestData.vacancyBody("Fake", java.time.LocalDate.now().plusDays(3)))
                    .when().post("/company/vacancies").then().statusCode(403);
        }

        @Test
        @DisplayName("SEC-03: company cannot call student routes")
        void companyCannotUseStudentRoutes() {
            String company = registerAndLogin("COMPANY", "c@example.com", "Acme");
            given().auth().oauth2(company).when().get("/student/profile").then().statusCode(403);
        }

        @Test
        @DisplayName("SEC-04: Company B cannot edit/close Company A's vacancy (IDOR)")
        void companyCannotTouchAnotherCompanysVacancy() {
            String companyA = registerAndLogin("COMPANY", "a@example.com", "Company A");
            String companyB = registerAndLogin("COMPANY", "b@example.com", "Company B");
            String vacancyId = createVacancy(companyA, "A's internship");

            given().auth().oauth2(companyB).contentType(ContentType.JSON).body(Map.of("status", "CLOSED"))
                    .when().patch("/company/vacancies/" + vacancyId + "/status").then().statusCode(403);
            given().auth().oauth2(companyB).contentType(ContentType.JSON)
                    .body(TestData.vacancyBody("Hijacked", java.time.LocalDate.now().plusDays(3)))
                    .when().put("/company/vacancies/" + vacancyId).then().statusCode(403);
        }

        @Test
        @DisplayName("SEC-05: Company B cannot read applicant profile/CV of Company A's application (IDOR)")
        void companyCannotReadAnotherCompanysApplicant() {
            String companyA = registerAndLogin("COMPANY", "a@example.com", "Company A");
            String companyB = registerAndLogin("COMPANY", "b@example.com", "Company B");
            String student = registerAndLogin("STUDENT", "s@example.com", "Student");
            String applicationId = apply(student, createVacancy(companyA, "A's internship"));

            given().auth().oauth2(companyB).when().get("/company/applications/" + applicationId + "/student-profile").then().statusCode(403);
            given().auth().oauth2(companyB).when().get("/company/applications/" + applicationId + "/cv").then().statusCode(403);
            given().auth().oauth2(companyB).contentType(ContentType.JSON).body(Map.of("status", "REJECTED"))
                    .when().patch("/company/applications/" + applicationId + "/status").then().statusCode(403);
        }
    }

    @Nested
    @DisplayName("A02 Cryptographic Failures")
    class CryptographicFailures {
        @Test
        @DisplayName("SEC-06: password is stored as a bcrypt hash, never in plain text")
        void passwordIsHashed() {
            registerAndLogin("STUDENT", "hash@example.com", "Hash Check");
            Document user = mongo.getCollection("users").find(new Document("email", "hash@example.com")).first();
            assertThat(user).isNotNull();
            assertThat(user.getString("passwordHash")).startsWith("$2a$10$").doesNotContain(PASSWORD);
        }

        @Test
        @DisplayName("SEC-07: register/login responses never return the password hash")
        void responsesDoNotLeakHash() {
            given().contentType(ContentType.JSON)
                    .body(Map.of("role", "STUDENT", "name", "Leak", "email", "leak@example.com", "password", PASSWORD))
                    .when().post("/auth/register")
                    .then().statusCode(201).body(not(containsString("passwordHash"))).body(not(containsString("$2a$")));
        }
    }

    @Nested
    @DisplayName("A03 Injection")
    class Injection {
        @Test
        @DisplayName("SEC-08: NoSQL operator injection {\"$ne\": null} in login e-mail is rejected with 400")
        void noSqlOperatorInjectionRejected() {
            registerAndLogin("STUDENT", "victim@example.com", "Victim");
            String body = "{\"role\":\"STUDENT\",\"email\":{\"$ne\":null},\"password\":{\"$ne\":null}}";
            given().contentType(ContentType.JSON).body(body)
                    .when().post("/auth/login")
                    .then().statusCode(400).body("accessToken", equalTo(null));
        }

        @Test
        @DisplayName("SEC-09: regex-like e-mail string does not match other accounts")
        void regexStringDoesNotMatchOthers() {
            registerAndLogin("STUDENT", "victim@example.com", "Victim");
            given().contentType(ContentType.JSON)
                    .body(Map.of("role", "STUDENT", "email", ".*@example.com", "password", PASSWORD))
                    .when().post("/auth/login")
                    .then().statusCode(400); // invalid e-mail format, never reaches the database
        }
    }

    @Nested
    @DisplayName("A07 Identification and Authentication Failures")
    class AuthenticationFailures {
        @Test
        @DisplayName("SEC-10: JWT with tampered payload (role changed to COMPANY) is rejected")
        void tamperedJwtRejected() {
            String student = registerAndLogin("STUDENT", "s@example.com", "Student");
            String[] parts = student.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                    .replace("STUDENT", "COMPANY");
            String forged = parts[0] + "." + Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + parts[2];
            given().auth().oauth2(forged).when().get("/company/applications").then().statusCode(401);
        }

        @Test
        @DisplayName("SEC-11: unsigned JWT (alg=none) is rejected")
        void algNoneRejected() {
            String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"none\"}".getBytes());
            String payload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"sub\":\"x\",\"roles\":[\"COMPANY\"]}".getBytes());
            given().auth().oauth2(header + "." + payload + ".").when().get("/company/applications").then().statusCode(401);
        }

        @Test
        @DisplayName("SEC-12: wrong password gives a generic message (no user enumeration)")
        void genericLoginError() {
            registerAndLogin("STUDENT", "s@example.com", "Student");
            String existing = given().contentType(ContentType.JSON)
                    .body(Map.of("role", "STUDENT", "email", "s@example.com", "password", "WrongPass99"))
                    .when().post("/auth/login").then().statusCode(401).extract().path("message");
            String missing = given().contentType(ContentType.JSON)
                    .body(Map.of("role", "STUDENT", "email", "nobody@example.com", "password", "WrongPass99"))
                    .when().post("/auth/login").then().statusCode(401).extract().path("message");
            assertThat(existing).isEqualTo(missing);
        }

        @Test
        @Tag("known-bug") // BUG-04: no lockout / rate limiting
        @DisplayName("SEC-13: after 10 wrong passwords the account should be temporarily locked (HTTP 429)")
        void bruteForceShouldBeThrottled() {
            registerAndLogin("STUDENT", "brute@example.com", "Brute");
            for (int attempt = 1; attempt <= 10; attempt++) {
                given().contentType(ContentType.JSON)
                        .body(Map.of("role", "STUDENT", "email", "brute@example.com", "password", "guess" + attempt + "xyz"))
                        .when().post("/auth/login");
            }
            given().contentType(ContentType.JSON)
                    .body(Map.of("role", "STUDENT", "email", "brute@example.com", "password", "guess11xyz"))
                    .when().post("/auth/login").then().statusCode(429);
        }
    }

    @Nested
    @DisplayName("A04 Insecure Design")
    class InsecureDesign {
        @Test
        @Tag("known-bug") // BUG-07: content type taken from the client
        @DisplayName("SEC-14: an HTML file renamed to .pdf must not be served back as text/html")
        void spoofedCvContentType() {
            String student = registerAndLogin("STUDENT", "s@example.com", "Student");
            given().auth().oauth2(student)
                    .multiPart("file", "cv.pdf", "<script>alert(1)</script>".getBytes(), "text/html")
                    .when().post("/student/profile/cv").then().statusCode(400);
        }
    }
}
