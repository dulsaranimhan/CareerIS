package com.careeris.api.blackbox;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import com.careeris.api.auth.LoginRequest;
import com.careeris.api.auth.RegisterRequest;
import com.careeris.api.auth.UserRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * EC4206 Section 3.3 - Black-box testing of the Registration form (Equivalence Partitioning + BVA).
 * Tests only the specified input rules, not the code inside (black-box).
 *
 * Spec (from RegisterRequest / README):
 *   password : 8..72 characters          name : 1..120 characters (not blank)
 *   email    : valid e-mail format         role : STUDENT or COMPANY (required)
 *
 * Owner: EG/2023/5664 - Jayasuriya J.A.D.N
 */
@DisplayName("Black-box: Registration EP + BVA")
class RegistrationEquivalenceBoundaryTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static RegisterRequest request(String name, String email, String password) {
        return new RegisterRequest(UserRole.STUDENT, name, null, email, password);
    }

    private static Set<ConstraintViolation<RegisterRequest>> violationsOn(RegisterRequest request, String field) {
        return Set.copyOf(validator.validate(request).stream()
                .filter(v -> v.getPropertyPath().toString().equals(field)).toList());
    }

    // ---------- BVA: password length (valid range 8..72) ----------
    @ParameterizedTest(name = "TC-BB-PW-{index}: password length {0} -> valid={1}")
    @CsvSource({
            "0,  false",   // empty (invalid partition)
            "7,  false",   // min - 1
            "8,  true",    // min
            "9,  true",    // min + 1
            "40, true",    // nominal
            "71, true",    // max - 1
            "72, true",    // max
            "73, false"    // max + 1
    })
    void passwordLengthBoundaries(int length, boolean expectedValid) {
        RegisterRequest request = request("Nimal Perera", "nimal@example.com", "a".repeat(length));
        assertThat(violationsOn(request, "password").isEmpty()).isEqualTo(expectedValid);
    }

    // ---------- BVA: name length (valid range 1..120, not blank) ----------
    @ParameterizedTest(name = "TC-BB-NM-{index}: name length {0} -> valid={1}")
    @CsvSource({"0, false", "1, true", "2, true", "119, true", "120, true", "121, false"})
    void nameLengthBoundaries(int length, boolean expectedValid) {
        RegisterRequest request = request("N".repeat(length), "nimal@example.com", "Password123");
        assertThat(violationsOn(request, "name").isEmpty()).isEqualTo(expectedValid);
    }

    // ---------- EP: e-mail ----------
    @ParameterizedTest(name = "TC-BB-EM-V{index}: valid e-mail class ''{0}''")
    @ValueSource(strings = {"student@example.com", "first.last@uni.ac.lk", "a+tag@mail.co"})
    void validEmailPartition(String email) {
        assertThat(violationsOn(request("Nimal", email, "Password123"), "email")).isEmpty();
    }

    @ParameterizedTest(name = "TC-BB-EM-I{index}: invalid e-mail class ''{0}''")
    @ValueSource(strings = {"", "   ", "plainaddress", "@example.com", "student@", "student@@example.com", "stu dent@example.com"})
    void invalidEmailPartition(String email) {
        assertThat(violationsOn(request("Nimal", email, "Password123"), "email")).isNotEmpty();
    }

    @Test
    @DisplayName("TC-BB-EM-OBS: e-mail without a domain suffix (user@localhost) is ACCEPTED - see BUG-05")
    void emailWithoutTopLevelDomainIsAccepted() {
        // Observation: Bean Validation @Email accepts "nimal@localhost". Real users can't receive mail there.
        assertThat(violationsOn(request("Nimal", "nimal@localhost", "Password123"), "email")).isEmpty();
    }

    // ---------- EP: role ----------
    @Test
    @DisplayName("TC-BB-RL-01: role missing (null) is rejected")
    void roleIsRequired() {
        RegisterRequest request = new RegisterRequest(null, "Nimal", null, "nimal@example.com", "Password123");
        assertThat(violationsOn(request, "role")).isNotEmpty();
    }

    // ---------- Login form uses the same password partition ----------
    @ParameterizedTest(name = "TC-BB-LG-{index}: login password length {0} -> valid={1}")
    @CsvSource({"7, false", "8, true", "72, true", "73, false"})
    void loginPasswordBoundaries(int length, boolean expectedValid) {
        LoginRequest request = new LoginRequest(UserRole.STUDENT, "nimal@example.com", "x".repeat(length));
        boolean valid = validator.validate(request).stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("password"));
        assertThat(valid).isEqualTo(expectedValid);
    }
}
