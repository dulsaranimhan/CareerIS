package com.careeris.api.profile;

import java.io.IOException;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/student/profile")
public class StudentProfileController {
    private final StudentProfileService profiles;
    public StudentProfileController(StudentProfileService profiles) { this.profiles = profiles; }

    @GetMapping
    public StudentProfileResponse get(Authentication auth) { return profiles.getOwnProfile(auth.getName()); }

    @PutMapping
    public StudentProfileResponse save(@Valid @RequestBody StudentProfileRequest request, Authentication auth) {
        return profiles.saveProfile(auth.getName(), request);
    }

    @PostMapping("/cv")
    public StudentProfileResponse upload(@RequestParam("file") MultipartFile file, Authentication auth) {
        return profiles.uploadCv(auth.getName(), file);
    }

    @GetMapping("/cv")
    public ResponseEntity<Resource> download(Authentication auth) throws IOException {
        return response(profiles.downloadOwn(auth.getName()));
    }

    static ResponseEntity<Resource> response(StudentProfileService.CvDownload cv) throws IOException {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(cv.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(cv.filename()).build().toString())
                .body(cv.resource());
    }
}
