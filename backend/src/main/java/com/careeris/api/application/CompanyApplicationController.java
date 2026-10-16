package com.careeris.api.application;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/company/applications")
public class CompanyApplicationController {
    private final ApplicationService applicationService;

    public CompanyApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public List<ApplicationResponse> list(@RequestParam(required = false) String vacancyId, Authentication authentication) {
        return applicationService.listForCompany(authentication.getName(), vacancyId);
    }

    @PatchMapping("/{id}/status")
    public ApplicationResponse updateStatus(@PathVariable String id, @Valid @RequestBody ApplicationStatusRequest request,
            Authentication authentication) {
        return applicationService.updateStatus(authentication.getName(), id, request.status());
    }
}
