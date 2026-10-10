package com.eduproof.controller;

import com.eduproof.dto.CertificateResponse;
import com.eduproof.repository.CertificateRepository;
import com.eduproof.repository.StudentRepository;
import com.eduproof.repository.VerificationLogRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CertificateRepository certificateRepository;
    private final StudentRepository studentRepository;
    private final VerificationLogRepository verificationLogRepository;

    public DashboardController(CertificateRepository certificateRepository,
                               StudentRepository studentRepository,
                               VerificationLogRepository verificationLogRepository) {
        this.certificateRepository = certificateRepository;
        this.studentRepository = studentRepository;
        this.verificationLogRepository = verificationLogRepository;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCertificates", certificateRepository.count());
        stats.put("validCertificates", certificateRepository.countByStatus("VALID"));
        stats.put("revokedCertificates", certificateRepository.countByStatus("REVOKED"));
        stats.put("totalStudents", studentRepository.count());
        stats.put("totalVerifications", verificationLogRepository.count());
        stats.put("recentCertificates", certificateRepository.findAllByOrderByCertificateIdDesc().stream()
                .limit(8)
                .map(CertificateResponse::from)
                .toList());
        stats.put("recentVerifications", verificationLogRepository.findAllByOrderByVerifiedAtDesc().stream()
                .limit(10)
                .map(log -> {
                    Map<String, Object> view = new HashMap<>();
                    view.put("logId", log.getLogId());
                    view.put("certificateCode", log.getCertificateCode());
                    view.put("verifierName", log.getVerifierName());
                    view.put("ipAddress", log.getIpAddress());
                    view.put("verificationStatus", log.getVerificationStatus());
                    view.put("verifiedAt", log.getVerifiedAt());
                    return view;
                })
                .toList());
        return stats;
    }
}
