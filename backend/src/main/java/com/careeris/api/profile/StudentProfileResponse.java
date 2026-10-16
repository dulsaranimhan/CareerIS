package com.careeris.api.profile;

import java.util.List;

import com.careeris.api.auth.UserAccount;

public record StudentProfileResponse(
        String studentId,
        String name,
        String email,
        String education,
        String qualification,
        String profileSummary,
        List<String> skills,
        String cvFileName) {
    public static StudentProfileResponse from(UserAccount student) {
        return new StudentProfileResponse(student.getId(), student.getDisplayName(), student.getEmail(),
                student.getEducation(), student.getQualification(), student.getProfileSummary(),
                student.getSkills(), student.getCvFileName());
    }
}
