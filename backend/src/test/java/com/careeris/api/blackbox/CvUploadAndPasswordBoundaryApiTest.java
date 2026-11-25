package com.careeris.api.blackbox;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

import java.util.Map;

import com.careeris.api.support.ApiTestBase;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * EC4206 Section 3.3 - Boundary Value Analysis executed through the real HTTP API.
 *   - CV upload size: requirement says "maximum 5 MB" (README) => 5,242,880 bytes must be accepted.
 *   - Password length in BYTES: bcrypt only supports 72 bytes, the form allows 72 characters.
 *
 * Tests tagged "known-bug" document defects that are still open; they are excluded from the CI
 * build and run with:  mvn test -Dgroups=known-bug -DexcludedTestGroups=none
 *
 * Owner: EG/2023/5664 - Jayasuriya J.A.D.N
 */
@DisplayName("Black-box (API): CV size & password byte boundaries")
class CvUploadAndPasswordBoundaryApiTest extends ApiTestBase {

    @ParameterizedTest(name = "TC-BB-CV-{index}: CV of {0} bytes -> HTTP {1}")
    @CsvSource({
            "0,       400",   // empty file (invalid)
            "1,       200",   // min valid
            "1048576, 200",   // nominal 1 MB
            "5242879, 200",   // max - 1 byte
            "5242880, 200",   // max (exactly 5 MB)  -> was BUG-01, fixed
            "5242881, 413"    // max + 1 byte (rejected)
    })
    void cvSizeBoundaries(int bytes, int expectedStatus) {
        String student = registerAndLogin("STUDENT", "cv-" + bytes + "@example.com", "CV Tester");
        given().auth().oauth2(student)
                .multiPart("file", "cv.pdf", new byte[bytes], "application/pdf")
                .when().post("/student/profile/cv")
                .then().statusCode(expectedStatus);
    }

    @ParameterizedTest(name = "TC-BB-CV-EXT-{index}: file ''{0}'' -> HTTP {1}")
    @CsvSource({"cv.pdf, 200", "cv.DOCX, 200", "cv.doc, 200", "cv.txt, 400", "cv.exe, 400", "cv.pdf.exe, 400"})
    void cvExtensionPartitions(String fileName, int expectedStatus) {
        String student = registerAndLogin("STUDENT", "ext@example.com", "CV Tester");
        given().auth().oauth2(student)
                .multiPart("file", fileName, "dummy".getBytes(), "application/octet-stream")
                .when().post("/student/profile/cv")
                .then().statusCode(expectedStatus);
    }

    @Test
    @DisplayName("TC-BB-PW-MB: characters after the 72nd byte of a password must still matter")
    @Tag("known-bug") // BUG-03
    void passwordCharactersBeyond72BytesAreNotIgnored() {
        // 30 Sinhala characters = 90 bytes. The form allows it (<= 72 characters).
        String realPassword = "අ".repeat(24) + "secret";
        String attackerGuess = "අ".repeat(24) + "WRONG!";  // same first 72 bytes, different ending
        given().contentType(ContentType.JSON)
                .body(Map.of("role", "STUDENT", "name", "Sinhala User", "email", "si@example.com", "password", realPassword))
                .when().post("/auth/register")
                .then().statusCode(201);
        given().contentType(ContentType.JSON)
                .body(Map.of("role", "STUDENT", "email", "si@example.com", "password", attackerGuess))
                .when().post("/auth/login")
                .then().statusCode(401); // a different password must be rejected
    }

    @Test
    @DisplayName("TC-BB-PW-MB-24: 24 Sinhala characters (72 bytes) is the real byte boundary and works")
    void multiBytePasswordAtByteLimit() {
        String sinhala24 = "අ".repeat(24); // 72 bytes
        given().contentType(ContentType.JSON)
                .body(Map.of("role", "STUDENT", "name", "Sinhala User", "email", "si24@example.com", "password", sinhala24))
                .when().post("/auth/register")
                .then().statusCode(201).body("role", equalTo("STUDENT"));
        given().contentType(ContentType.JSON)
                .body(Map.of("role", "STUDENT", "email", "si24@example.com", "password", sinhala24))
                .when().post("/auth/login")
                .then().statusCode(200).time(lessThan(5000L));
    }
}
