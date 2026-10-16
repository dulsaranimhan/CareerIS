package com.careeris.api.auth;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class UserAccount {
    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String displayName;
    private String contactName;
    private String passwordHash;
    private UserRole role;
    private Instant createdAt;
    private String education;
    private String qualification;
    private String profileSummary;
    private List<String> skills = new ArrayList<>();
    private String cvFileId;
    private String cvFileName;
    private String cvContentType;

    protected UserAccount() {
    }

    public UserAccount(String email, String displayName, String contactName, String passwordHash, UserRole role) {
        this.email = email;
        this.displayName = displayName;
        this.contactName = contactName;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getContactName() {
        return contactName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public String getProfileSummary() { return profileSummary; }
    public void setProfileSummary(String profileSummary) { this.profileSummary = profileSummary; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills == null ? new ArrayList<>() : new ArrayList<>(skills); }
    public String getCvFileId() { return cvFileId; }
    public void setCvFileId(String cvFileId) { this.cvFileId = cvFileId; }
    public String getCvFileName() { return cvFileName; }
    public void setCvFileName(String cvFileName) { this.cvFileName = cvFileName; }
    public String getCvContentType() { return cvContentType; }
    public void setCvContentType(String cvContentType) { this.cvContentType = cvContentType; }
}
