package com.careeris.api.vacancy;

import java.util.List;
import java.time.LocalDate;

import com.careeris.api.auth.UserAccount;
import com.careeris.api.auth.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VacancyService {
    private final VacancyRepository vacancies;
    private final UserAccountRepository users;

    public VacancyService(VacancyRepository vacancies, UserAccountRepository users) {
        this.vacancies = vacancies;
        this.users = users;
    }

    public List<VacancyResponse> listOpen() {
        LocalDate today = LocalDate.now();
        return vacancies.findByStatusOrderByCreatedAtDesc(VacancyStatus.OPEN).stream()
                .filter(vacancy -> !vacancy.getDeadline().isBefore(today))
                .map(VacancyResponse::from).toList();
    }

    public List<VacancyResponse> listForCompany(String companyId) {
        return vacancies.findByCompanyIdOrderByCreatedAtDesc(companyId).stream().map(VacancyResponse::from).toList();
    }

    public VacancyResponse getOpen(String vacancyId) {
        Vacancy vacancy = vacancies.findByIdAndStatus(vacancyId, VacancyStatus.OPEN)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Open vacancy not found."));
        if (vacancy.getDeadline().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Open vacancy not found.");
        }
        return VacancyResponse.from(vacancy);
    }

    public VacancyResponse create(String companyId, VacancyRequest request) {
        UserAccount company = users.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Company account no longer exists."));
        return VacancyResponse.from(vacancies.save(new Vacancy(companyId, company.getDisplayName(), request)));
    }

    public VacancyResponse update(String companyId, String vacancyId, VacancyRequest request) {
        Vacancy vacancy = findOwned(companyId, vacancyId);
        vacancy.updateFrom(request);
        return VacancyResponse.from(vacancies.save(vacancy));
    }

    public VacancyResponse updateStatus(String companyId, String vacancyId, VacancyStatus status) {
        Vacancy vacancy = findOwned(companyId, vacancyId);
        vacancy.setStatus(status);
        return VacancyResponse.from(vacancies.save(vacancy));
    }

    private Vacancy findOwned(String companyId, String vacancyId) {
        Vacancy vacancy = vacancies.findById(vacancyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vacancy not found."));
        if (!vacancy.getCompanyId().equals(companyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot manage another company's vacancy.");
        }
        return vacancy;
    }
}
