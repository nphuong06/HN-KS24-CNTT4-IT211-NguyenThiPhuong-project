package org.example.project.security;

import org.example.project.entity.User;
import org.example.project.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails details)) {
            throw new ApiException("Chưa xác thực", HttpStatus.UNAUTHORIZED);
        }
        return details;
    }

    public static User getCurrentUser() {
        return getCurrentUserDetails().getUser();
    }
}
