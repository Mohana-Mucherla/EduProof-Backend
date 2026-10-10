package com.eduproof.controller;

import com.eduproof.dto.ApiError;
import com.eduproof.dto.StudentResponse;
import com.eduproof.model.Certificate;
import com.eduproof.model.Department;
import com.eduproof.model.Student;
import com.eduproof.model.User;
import com.eduproof.repository.CertificateRepository;
import com.eduproof.repository.DepartmentRepository;
import com.eduproof.repository.StudentRepository;
import com.eduproof.repository.UserRepository;
import com.eduproof.repository.VerificationLogRepository;
import com.eduproof.security.RequestAuth;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final CertificateRepository certificateRepository;
    private final VerificationLogRepository verificationLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final RequestAuth requestAuth;

    public StudentController(StudentRepository studentRepository,
                             DepartmentRepository departmentRepository,
                             UserRepository userRepository,
                             CertificateRepository certificateRepository,
                             VerificationLogRepository verificationLogRepository,
                             PasswordEncoder passwordEncoder,
                             RequestAuth requestAuth) {
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.certificateRepository = certificateRepository;
        this.verificationLogRepository = verificationLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.requestAuth = requestAuth;
    }

    @GetMapping
    public List<StudentResponse> list(@RequestParam(value = "search", required = false) String search) {
        List<Student> students = (search == null || search.isBlank())
                ? studentRepository.findAll()
                : studentRepository.findByNameContainingIgnoreCaseOrRollNumberContainingIgnoreCase(search, search);
        return students.stream().map(StudentResponse::from).toList();
    }

    @GetMapping("/profile/me")
    public ResponseEntity<?> me() {
        User user = requestAuth.currentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(new ApiError("Not authenticated."));
        }
        Student student = studentRepository.findByEmailIgnoreCase(user.getEmail()).orElse(null);
        if (student == null) {
            return ResponseEntity.status(404).body(new ApiError("Student profile not found."));
        }
        return ResponseEntity.ok(StudentResponse.from(student));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            return ResponseEntity.status(404).body(new ApiError("Student not found."));
        }
        return ResponseEntity.ok(StudentResponse.from(student));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body,
                                    @RequestParam(value = "initialPassword", defaultValue = "Student@123") String initialPassword) {
        String rollNumber = str(body.get("rollNumber"));
        String name = str(body.get("name"));
        String email = str(body.get("email"));
        if (rollNumber.isBlank() || name.isBlank() || email.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiError("Roll number, name and email are required."));
        }
        if (studentRepository.existsByRollNumberIgnoreCase(rollNumber) || studentRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.badRequest().body(new ApiError("A student with this roll number or email already exists."));
        }

        Student student = apply(new Student(), body);
        User account = userRepository.findByEmailIgnoreCase(email).orElseGet(User::new);
        account.setName(name);
        account.setEmail(email);
        account.setRole("STUDENT");
        if (account.getId() == null || account.getPassword() == null) {
            account.setPassword(passwordEncoder.encode(initialPassword));
        }
        student.setUser(userRepository.save(account));
        return ResponseEntity.ok(StudentResponse.from(studentRepository.save(student)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            return ResponseEntity.status(404).body(new ApiError("Student not found."));
        }
        return ResponseEntity.ok(StudentResponse.from(studentRepository.save(apply(student, body))));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            return ResponseEntity.status(404).body(new ApiError("Student not found."));
        }
        List<Certificate> certificates = certificateRepository.findByStudentOrderByIssueDateDesc(student);
        for (Certificate certificate : certificates) {
            verificationLogRepository.deleteByCertificate(certificate);
            certificateRepository.delete(certificate);
        }
        studentRepository.delete(student);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    private Student apply(Student student, Map<String, Object> body) {
        if (body.get("rollNumber") != null) student.setRollNumber(str(body.get("rollNumber")));
        if (body.get("name") != null) student.setName(str(body.get("name")));
        if (body.get("email") != null) student.setEmail(str(body.get("email")));
        if (body.get("phone") != null) student.setPhone(str(body.get("phone")));
        if (body.get("course") != null) student.setCourse(str(body.get("course")));
        if (body.get("year") != null) student.setYear(str(body.get("year")));
        if (body.get("status") != null) student.setStatus(str(body.get("status")));
        if (body.get("admissionYear") != null) student.setAdmissionYear(toInt(body.get("admissionYear")));
        if (body.get("graduationYear") != null) student.setGraduationYear(toInt(body.get("graduationYear")));
        if (body.get("cgpa") != null) student.setCgpa(toDouble(body.get("cgpa")));
        if (body.get("departmentId") != null) {
            Long deptId = toLong(body.get("departmentId"));
            Department dept = departmentRepository.findById(deptId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found."));
            student.setDepartment(dept);
        }
        if (student.getStatus() == null) student.setStatus("ACTIVE");
        return student;
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private Integer toInt(Object value) {
        if (value == null || str(value).isBlank()) return null;
        return Integer.parseInt(str(value));
    }

    private Long toLong(Object value) {
        return Long.parseLong(str(value));
    }

    private Double toDouble(Object value) {
        if (value == null || str(value).isBlank()) return null;
        return Double.parseDouble(str(value));
    }
}
