package com.careeris.api.auth;

import java.time.Instant;

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
}
