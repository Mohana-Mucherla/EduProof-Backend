package com.eduproof.config;

import com.eduproof.model.Certificate;
import com.eduproof.model.CertificateType;
import com.eduproof.model.Department;
import com.eduproof.model.Student;
import com.eduproof.model.User;
import com.eduproof.repository.CertificateRepository;
import com.eduproof.repository.CertificateTypeRepository;
import com.eduproof.repository.DepartmentRepository;
import com.eduproof.repository.StudentRepository;
import com.eduproof.repository.UserRepository;
import com.eduproof.service.QrCodeService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CertificateTypeRepository certificateTypeRepository;
    private final StudentRepository studentRepository;
    private final CertificateRepository certificateRepository;
    private final PasswordEncoder passwordEncoder;
    private final QrCodeService qrCodeService;

    public DataSeeder(UserRepository userRepository,
                      DepartmentRepository departmentRepository,
                      CertificateTypeRepository certificateTypeRepository,
                      StudentRepository studentRepository,
                      CertificateRepository certificateRepository,
                      PasswordEncoder passwordEncoder,
                      QrCodeService qrCodeService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.certificateTypeRepository = certificateTypeRepository;
        this.studentRepository = studentRepository;
        this.certificateRepository = certificateRepository;
        this.passwordEncoder = passwordEncoder;
        this.qrCodeService = qrCodeService;
    }

    @Override
    public void run(ApplicationArguments args) {
        Department cse = department("CSE", "Computer Science & Engineering");
        department("ECE", "Electronics & Communication Engineering");
        department("MECH", "Mechanical Engineering");

        CertificateType degree = type("B.Tech Degree");
        type("Transcript");
        type("Bonafide");
        type("Merit Certificate");

        User recruiterUser = user("Recruiter John", "recruiter@college.edu", "Recruiter@123", "RECRUITER");
        User adminUser = user("Admin", "admin@college.edu", "Admin@123", "ADMIN");
        // Save recruiter if not exists (user method already handles existence)
        // No further action needed as user() saves it.
        // Existing admin already seeded above.
        // Ensure role prefix handling is consistent.

        User rahulUser = user("Rahul Kumar", "rahul.kumar@college.edu", "Student@123", "STUDENT");
        User priyaUser = user("Priya Sharma", "priya.sharma@college.edu", "Student@123", "STUDENT");
        User vikramUser = user("Vikram Singh", "vikram.singh@college.edu", "Student@123", "STUDENT");

        Student rahul = student("21AG1A0501", "Rahul Kumar", "rahul.kumar@college.edu",
                "+91 9876543210", cse, "B.Tech", "4th Year", 2021, 2025, 9.42, rahulUser);
        Student priya = student("21AG1A0502", "Priya Sharma", "priya.sharma@college.edu",
                "+91 9876543211", cse, "B.Tech", "4th Year", 2021, 2025, 9.18, priyaUser);
        Student vikram = student("20AG1A0544", "Vikram Singh", "vikram.singh@college.edu",
                "+91 9876543212", cse, "B.Tech", "Alumni", 2020, 2024, 7.85, vikramUser);

        certificate("EDU-CERT-2026-00001", rahul, degree,
                "BACHELOR OF TECHNOLOGY IN COMPUTER SCIENCE",
                LocalDate.of(2026, 5, 12), 9.42, "VALID",
                "Awarded First Class with Distinction.");
        certificate("EDU-CERT-2026-00002", priya, degree,
                "BACHELOR OF TECHNOLOGY IN COMPUTER SCIENCE",
                LocalDate.of(2026, 5, 12), 9.18, "VALID",
                "Awarded First Class with Distinction.");
        certificate("EDU-CERT-2025-00003", vikram, degree,
                "BACHELOR OF TECHNOLOGY IN COMPUTER SCIENCE",
                LocalDate.of(2025, 6, 20), 7.85, "REVOKED",
                "Originally issued; later revoked for administrative review.");

        if (adminUser.getId() == null) {
            userRepository.save(adminUser);
        }

        // Synchronize any registered STUDENT users into students table
        userRepository.findAll().stream()
                .filter(u -> "STUDENT".equalsIgnoreCase(u.getRole()))
                .forEach(u -> {
                    if (studentRepository.findByEmailIgnoreCase(u.getEmail()).isEmpty()) {
                        long count = studentRepository.count() + 1;
                        String roll = String.format("22AG1A%04d", 500 + count);
                        int offset = 1;
                        while (studentRepository.existsByRollNumberIgnoreCase(roll)) {
                            roll = String.format("22AG1A%04d", 500 + count + offset);
                            offset++;
                        }
                        student(roll, u.getName(), u.getEmail(), "+91 9876543299", cse,
                                "B.Tech", "4th Year", 2022, 2026, 8.50, u);
                    }
                });
    }

    private Department department(String code, String name) {
        return departmentRepository.findByDepartmentCode(code).orElseGet(() -> {
            Department d = new Department();
            d.setDepartmentCode(code);
            d.setDepartmentName(name);
            return departmentRepository.save(d);
        });
    }

    private CertificateType type(String name) {
        return certificateTypeRepository.findByTypeName(name).orElseGet(() -> {
            CertificateType t = new CertificateType();
            t.setTypeName(name);
            return certificateTypeRepository.save(t);
        });
    }

    private User user(String name, String email, String password, String role) {
        return userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            User u = new User();
            u.setName(name);
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode(password));
            u.setRole(role);
            return userRepository.save(u);
        });
    }

    private Student student(String roll, String name, String email, String phone, Department dept,
                            String course, String year, int admission, int graduation, double cgpa, User user) {
        return studentRepository.findByRollNumberIgnoreCase(roll).orElseGet(() -> {
            Student s = new Student();
            s.setRollNumber(roll);
            s.setName(name);
            s.setEmail(email);
            s.setPhone(phone);
            s.setDepartment(dept);
            s.setCourse(course);
            s.setYear(year);
            s.setAdmissionYear(admission);
            s.setGraduationYear(graduation);
            s.setCgpa(cgpa);
            s.setStatus("ACTIVE");
            s.setUser(user);
            return studentRepository.save(s);
        });
    }

    private void certificate(String code, Student student, CertificateType type, String title,
                             LocalDate issueDate, double cgpa, String status, String remarks) {
        if (certificateRepository.findByUniqueCertificateCodeIgnoreCase(code).isPresent()) {
            return;
        }
        Certificate cert = new Certificate();
        cert.setUniqueCertificateCode(code);
        cert.setStudent(student);
        cert.setCertificateType(type);
        cert.setTitle(title);
        cert.setIssueDate(issueDate);
        cert.setCgpa(cgpa);
        cert.setStatus(status);
        cert.setAcademicRemarks(remarks);
        cert.setQrCodeBase64(qrCodeService.pngBase64("http://localhost:5173/verify/" + code));
        certificateRepository.save(cert);
    }
}
