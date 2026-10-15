package com.careeris.api.vacancy;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vacancies")
public class VacancyController {
    private final VacancyService vacancyService;

    public VacancyController(VacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    @GetMapping
    public List<VacancyResponse> listOpen() {
        return vacancyService.listOpen();
    }

    @GetMapping("/{id}")
    public VacancyResponse getOpen(@PathVariable String id) {
        return vacancyService.getOpen(id);
    }
}
