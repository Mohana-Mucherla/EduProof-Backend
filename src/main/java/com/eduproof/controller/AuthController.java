package com.eduproof.controller;

import com.eduproof.dto.ApiError;
import com.eduproof.dto.AuthResponse;
import com.eduproof.model.Student;
import com.eduproof.model.User;
import com.eduproof.repository.StudentRepository;
import com.eduproof.repository.UserRepository;
import com.eduproof.security.RequestAuth;
import com.eduproof.security.SessionStore;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionStore sessionStore;
    private final RequestAuth requestAuth;

    public AuthController(UserRepository userRepository,
                          StudentRepository studentRepository,
                          PasswordEncoder passwordEncoder,
                          SessionStore sessionStore,
                          RequestAuth requestAuth) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionStore = sessionStore;
        this.requestAuth = requestAuth;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String password = request.get("password");
        if (name == null || email == null || password == null
                || name.isBlank() || email.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiError("Name, email and password are required."));
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.badRequest().body(new ApiError("An account with this email already exists."));
        }
        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("STUDENT");
        
User savedUser = userRepository.save(user);
String token = sessionStore.create(savedUser.getId());
AuthResponse response = toAuthResponse(savedUser, null);
response.setToken(token);
return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        User user = userRepository.findByEmailIgnoreCase(email == null ? "" : email).orElse(null);
        if (user == null || !passwordEncoder.matches(password == null ? "" : password, user.getPassword())) {
            return ResponseEntity.status(401).body(new ApiError("Invalid credentials. Please check your email and password."));
        }
        String token = sessionStore.create(user.getId());
        String roll = studentRepository.findByEmailIgnoreCase(user.getEmail())
                .map(Student::getRollNumber)
                .orElse(null);
        AuthResponse response = toAuthResponse(user, roll);
        response.setToken(token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        User user = requestAuth.currentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(new ApiError("Not authenticated."));
        }
        String roll = studentRepository.findByEmailIgnoreCase(user.getEmail())
                .map(Student::getRollNumber)
                .orElse(null);
        return ResponseEntity.ok(toAuthResponse(user, roll));
    }

    private AuthResponse toAuthResponse(User user, String rollNumber) {
        AuthResponse response = new AuthResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setRollNumber(rollNumber);
        return response;
    }
}
