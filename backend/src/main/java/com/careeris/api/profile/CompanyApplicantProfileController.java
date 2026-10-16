package com.careeris.api.profile;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/company/applications")
public class CompanyApplicantProfileController {
    private final StudentProfileService profiles;
    public CompanyApplicantProfileController(StudentProfileService profiles) { this.profiles = profiles; }

    @GetMapping("/{applicationId}/student-profile")
    public StudentProfileResponse profile(@PathVariable String applicationId, Authentication auth) {
        return profiles.profileForCompany(auth.getName(), applicationId);
    }

    @GetMapping("/{applicationId}/cv")
    public ResponseEntity<Resource> cv(@PathVariable String applicationId, Authentication auth) throws IOException {
        return StudentProfileController.response(profiles.downloadForCompany(auth.getName(), applicationId));
    }
}
