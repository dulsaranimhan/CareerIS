package com.careeris.api.application;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface InternshipApplicationRepository extends MongoRepository<InternshipApplication, String> {
    List<InternshipApplication> findByStudentIdOrderByAppliedAtDesc(String studentId);
    List<InternshipApplication> findByCompanyIdOrderByAppliedAtDesc(String companyId);
    List<InternshipApplication> findByCompanyIdAndVacancyIdOrderByAppliedAtDesc(String companyId, String vacancyId);
    Optional<InternshipApplication> findByStudentIdAndVacancyId(String studentId, String vacancyId);
}
