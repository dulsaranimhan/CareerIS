package com.careeris.api.profile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.careeris.api.application.InternshipApplication;
import com.careeris.api.application.InternshipApplicationRepository;
import com.careeris.api.auth.UserAccount;
import com.careeris.api.auth.UserAccountRepository;
import com.careeris.api.auth.UserRole;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentProfileService {
    private static final long MAX_CV_BYTES = 5 * 1024 * 1024;
    private final UserAccountRepository users;
    private final InternshipApplicationRepository applications;
    private final GridFsTemplate files;

    public StudentProfileService(UserAccountRepository users, InternshipApplicationRepository applications, GridFsTemplate files) {
        this.users = users;
        this.applications = applications;
        this.files = files;
    }

    public StudentProfileResponse getOwnProfile(String studentId) {
        return StudentProfileResponse.from(findStudent(studentId));
    }

    public StudentProfileResponse saveProfile(String studentId, StudentProfileRequest request) {
        UserAccount student = findStudent(studentId);
        student.setEducation(clean(request.education()));
        student.setQualification(clean(request.qualification()));
        student.setProfileSummary(clean(request.profileSummary()));
        List<String> skills = request.skills() == null ? List.of() : request.skills().stream()
                .filter(skill -> skill != null && !skill.isBlank()).map(String::trim).distinct().toList();
        student.setSkills(skills);
        return StudentProfileResponse.from(users.save(student));
    }

    public StudentProfileResponse uploadCv(String studentId, MultipartFile file) {
        UserAccount student = findStudent(studentId);
        if (file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a CV file to upload.");
        if (file.getSize() > MAX_CV_BYTES) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CV files must be 5 MB or smaller.");
        String filename = file.getOriginalFilename() == null ? "cv" : file.getOriginalFilename();
        String extension = filename.toLowerCase(Locale.ROOT);
        if (!(extension.endsWith(".pdf") || extension.endsWith(".doc") || extension.endsWith(".docx"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload your CV as a PDF, DOC, or DOCX file.");
        }
        try {
            ObjectId id = files.store(file.getInputStream(), filename, safeContentType(file.getContentType()));
            String previousId = student.getCvFileId();
            student.setCvFileId(id.toHexString());
            student.setCvFileName(filename);
            student.setCvContentType(safeContentType(file.getContentType()));
            UserAccount saved = users.save(student);
            if (previousId != null) files.delete(new Query(Criteria.where("_id").is(new ObjectId(previousId))));
            return StudentProfileResponse.from(saved);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save the CV file.");
        }
    }

    public CvDownload downloadForCompany(String companyId, String applicationId) {
        InternshipApplication application = applications.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found."));
        if (!application.getCompanyId().equals(companyId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view this applicant.");
        return download(findStudent(application.getStudentId()));
    }

    public StudentProfileResponse profileForCompany(String companyId, String applicationId) {
        InternshipApplication application = applications.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found."));
        if (!application.getCompanyId().equals(companyId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view this applicant.");
        return StudentProfileResponse.from(findStudent(application.getStudentId()));
    }

    public CvDownload downloadOwn(String studentId) { return download(findStudent(studentId)); }

    private CvDownload download(UserAccount student) {
        if (student.getCvFileId() == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This student has not uploaded a CV.");
        GridFSFile file = files.findOne(new Query(Criteria.where("_id").is(new ObjectId(student.getCvFileId()))));
        if (file == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The CV file could not be found.");
        GridFsResource resource = files.getResource(file);
        return new CvDownload(resource, student.getCvFileName(), student.getCvContentType());
    }

    private UserAccount findStudent(String id) {
        UserAccount student = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student account not found."));
        if (student.getRole() != UserRole.STUDENT) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This profile is only available to students.");
        return student;
    }

    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String safeContentType(String contentType) { return contentType == null ? "application/octet-stream" : contentType; }

    public record CvDownload(GridFsResource resource, String filename, String contentType) { }
}
