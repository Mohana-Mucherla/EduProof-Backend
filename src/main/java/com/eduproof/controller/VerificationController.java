package com.eduproof.controller;

import com.eduproof.model.Certificate;
import com.eduproof.model.Student;
import com.eduproof.model.VerificationLog;
import com.eduproof.repository.CertificateRepository;
import com.eduproof.repository.VerificationLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VerificationController {

    private final CertificateRepository certificateRepository;
    private final VerificationLogRepository verificationLogRepository;

    public VerificationController(CertificateRepository certificateRepository,
                                  VerificationLogRepository verificationLogRepository) {
        this.certificateRepository = certificateRepository;
        this.verificationLogRepository = verificationLogRepository;
    }

    @GetMapping("/verify/{code}")
    public Map<String, Object> verify(@PathVariable String code,
                                      @RequestParam(value = "verifier", defaultValue = "Public Recruiter / Verifier") String verifier,
                                      HttpServletRequest request) {
        Certificate cert = certificateRepository.findByUniqueCertificateCodeIgnoreCase(code.trim()).orElse(null);
        Map<String, Object> result = new HashMap<>();
        String status = cert == null ? "NOT_FOUND" : cert.getStatus();

        VerificationLog log = new VerificationLog();
        log.setCertificate(cert);
        log.setCertificateCode(code.trim());
        log.setVerifierName(verifier);
        log.setIpAddress(request.getRemoteAddr());
        log.setVerificationStatus(status);
        log.setVerifiedAt(Instant.now());
        verificationLogRepository.save(log);

        result.put("status", status);
        result.put("verificationStatus", status);
        result.put("certificateCode", code.trim());
        result.put("uniqueCertificateCode", code.trim());
        result.put("verificationTimestamp", log.getVerifiedAt());
        result.put("verifiedAt", log.getVerifiedAt());
        result.put("collegeName", "Anurag University, Hyderabad");

        if (cert != null) {
            Student student = cert.getStudent();
            result.put("certificateTitle", cert.getTitle());
            result.put("certificateType", cert.getCertificateType() != null ? cert.getCertificateType().getTypeName() : null);
            result.put("issueDate", cert.getIssueDate());
            result.put("cgpa", cert.getCgpa());
            result.put("qrCodeBase64", cert.getQrCodeBase64());
            if (student != null) {
                result.put("studentName", student.getName());
                result.put("rollNumber", student.getRollNumber());
                result.put("department", student.getDepartment() != null ? student.getDepartment().getDepartmentName() : null);
            }
        }
        return result;
    }

    @GetMapping("/verification-logs")
    public List<Map<String, Object>> logs() {
        return verificationLogRepository.findAllByOrderByVerifiedAtDesc().stream()
                .map(this::toLogView)
                .toList();
    }

    private Map<String, Object> toLogView(VerificationLog log) {
        Map<String, Object> view = new HashMap<>();
        view.put("logId", log.getLogId());
        view.put("certificateCode", log.getCertificateCode());
        view.put("verifierName", log.getVerifierName());
        view.put("ipAddress", log.getIpAddress());
        view.put("verificationStatus", log.getVerificationStatus());
        view.put("verifiedAt", log.getVerifiedAt());
        return view;
    }
}
