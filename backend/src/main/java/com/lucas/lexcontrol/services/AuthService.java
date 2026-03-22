package com.lucas.lexcontrol.services;

import com.lucas.lexcontrol.common.ApiException;
import com.lucas.lexcontrol.common.ApiErrorCode;
import com.lucas.lexcontrol.common.InputSanitizer;
import com.lucas.lexcontrol.dto.auth.LoginRequest;
import com.lucas.lexcontrol.dto.auth.RegisterRequest;
import com.lucas.lexcontrol.entities.User;
import com.lucas.lexcontrol.repositories.UserRepository;
import com.lucas.lexcontrol.security.PasswordHasher;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class);

    @Inject
    UserRepository userRepository;

    @Inject
    PasswordHasher passwordHasher;

    @Inject
    TokenService tokenService;

    @Inject
    InputSanitizer sanitizer;

    @Transactional
    public boolean register(RegisterRequest request) {
        String email = sanitizer.clean(request.email).toLowerCase();
        
        if (userRepository.findByEmail(email).isPresent()) {
            LOG.warnf("Registration attempt with existing email: %s", email);
            throw new ApiException(ApiErrorCode.EMAIL_ALREADY_REGISTERED, "Email already registered");
        }

        User user = new User();
        user.name = sanitizer.clean(request.name);
        user.email = email;
        user.passwordHash = passwordHasher.hash(request.password);

        userRepository.persist(user);
        LOG.infof("New user registered: %s", email);
        return true;
    }

    public User authenticate(LoginRequest request) {
        String email = sanitizer.clean(request.email).toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOG.warnf("Login attempt with non-existent email: %s", email);
                    return new ApiException(ApiErrorCode.INVALID_CREDENTIALS, "Invalid credentials");
                });

        if (!passwordHasher.verify(request.password, user.passwordHash)) {
            LOG.warnf("Failed login attempt for user: %s", email);
            throw new ApiException(ApiErrorCode.INVALID_CREDENTIALS, "Invalid credentials");
        }

        LOG.infof("User logged in: %s", email);
        return user;
    }
}
