package com.careeris.api.vacancy;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/company/vacancies")
public class CompanyVacancyController {
    private final VacancyService vacancyService;

    public CompanyVacancyController(VacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    @GetMapping
    public List<VacancyResponse> list(Authentication authentication) {
        return vacancyService.listForCompany(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VacancyResponse create(@Valid @RequestBody VacancyRequest request, Authentication authentication) {
        return vacancyService.create(authentication.getName(), request);
    }

    @PutMapping("/{id}")
    public VacancyResponse update(@PathVariable String id, @Valid @RequestBody VacancyRequest request, Authentication authentication) {
        return vacancyService.update(authentication.getName(), id, request);
    }

    @PatchMapping("/{id}/status")
    public VacancyResponse updateStatus(@PathVariable String id, @Valid @RequestBody VacancyStatusRequest request, Authentication authentication) {
        return vacancyService.updateStatus(authentication.getName(), id, request.status());
    }
}
