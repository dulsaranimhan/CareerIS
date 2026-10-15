package com.careeris.api.vacancy;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface VacancyRepository extends MongoRepository<Vacancy, String> {
    List<Vacancy> findByCompanyIdOrderByCreatedAtDesc(String companyId);
    List<Vacancy> findByStatusOrderByCreatedAtDesc(VacancyStatus status);
    java.util.Optional<Vacancy> findByIdAndStatus(String id, VacancyStatus status);
}
