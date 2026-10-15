package com.careeris.api.auth;

import java.util.Locale;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccount register(RegisterRequest request) {
        if (request.role() == UserRole.COMPANY
                && (request.contactName() == null || request.contactName().isBlank())) {
            throw new IllegalArgumentException("A contact name is required for company accounts.");
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.findByEmailIgnoreCase(email).isPresent()) {
            throw new EmailAlreadyRegisteredException();
        }

        String displayName = request.name().trim();
        String contactName = request.contactName() == null ? null : request.contactName().trim();
        UserAccount account = new UserAccount(
                email,
                displayName,
                contactName,
                passwordEncoder.encode(request.password()),
                request.role());

        try {
            return users.save(account);
        } catch (DuplicateKeyException exception) {
            throw new EmailAlreadyRegisteredException();
        }
    }
}
