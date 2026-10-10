package com.eduproof.security;

import com.eduproof.model.User;
import com.eduproof.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class RequestAuth {
    private final SessionStore sessionStore;
    private final UserRepository userRepository;

    public RequestAuth(SessionStore sessionStore, UserRepository userRepository) {
        this.sessionStore = sessionStore;
        this.userRepository = userRepository;
    }

    public User currentUser() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        Long userId = sessionStore.getUserId(header.substring(7).trim());
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }

    public boolean isAdmin() {
        User user = currentUser();
        if (user == null || user.getRole() == null) {
            return false;
        }
        String role = user.getRole();
        return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
    }
}
